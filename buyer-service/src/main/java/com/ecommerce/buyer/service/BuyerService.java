package com.ecommerce.buyer.service;

import com.ecommerce.buyer.repository.BuyerRepository;
import com.ecommerce.buyer.repository.dbo.BuyerDBO;
import com.ecommerce.sdk.enums.CountryEnum;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.request.*;
import com.ecommerce.sdk.response.LoginResponseDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class BuyerService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    private final Keycloak keycloak;

    private final BuyerRepository buyerRepository;

    private final KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic;
    private final KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    @Autowired
    public BuyerService(BuyerRepository buyerRepository, Keycloak keycloak,
                        KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic,
                        @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC) KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus) {
        this.buyerRepository = buyerRepository;
        this.keycloak = keycloak;
        this.kafkaProducerPlaceOrderTopic = kafkaProducerPlaceOrderTopic;
        this.kafkaProducerSwitchStatus = kafkaProducerSwitchStatus;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();
        String token = AuthzClient.create().authorization(email, password).authorize().getToken();
        return new LoginResponseDTO(token);
    }

    @Transactional
    public void register(RegisterBuyerRequestDTO inputData) throws InvalidInputException, UserConflictException, DeleteUserException {
        if (inputData == null) {
            throw new InvalidInputException("buyer.api.register.error.nullInput");
        }

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(inputData.getEmail());
        userRepresentation.setEmail(inputData.getEmail());
        userRepresentation.setEnabled(true);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(inputData.getPassword());
        userRepresentation.setCredentials(Arrays.asList(credentialRepresentation));

        Response response = this.keycloak.realm(this.realm).users().create(userRepresentation);
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                throw new UserConflictException("buyer.api.register.error.buyerAlreadyExists");
            }
        }
        userRepresentation = this.keycloak.realm(this.realm).users().search(inputData.getEmail()).get(0);
        UUID userId = UUID.fromString(userRepresentation.getId());
        if (this.buyerRepository.findByAuthId(userId) != null) {
            response = this.keycloak.realm(this.realm).users().delete(inputData.getEmail());
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new DeleteUserException("buyer.api.register.error.failedToRevertUserCreation");
            }
            throw new UserConflictException("buyer.api.register.error.buyerInformationAlreadyPresent");
        }
        ZonedDateTime now = ZonedDateTime.now();
        BuyerDBO entity = new BuyerDBO(0l, now, now, inputData.getName(), inputData.getLatitude(), inputData.getLongitude(), inputData.getCountry(), userId);

        this.buyerRepository.save(entity);
    }

    public BuyerDBO getProfile(Authentication authentication) {
        String userId = authentication.getName();
        return this.buyerRepository.findByAuthId(UUID.fromString(userId));
    }

    private boolean isAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.BUYER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }

    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_BUYER_TOPIC)
    @Transactional
    public void placeOrderBuyer(@Valid PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO) throws  IllegalArgumentException {
        if (placeOrderBuyerRequestDTO == null) {
            throw new IllegalArgumentException("buyer.api.placeOrder.error.nullInput");
        }
        BuyerDBO buyerDBO = this.buyerRepository.findByAuthId(placeOrderBuyerRequestDTO.getBuyerId());
        if (buyerDBO == null) {
            this.kafkaProducerSwitchStatus.send(new ProducerRecord<>(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, new SwitchOrderStatusDTO(OrderStatusEnum.ERROR_UNKOWN_BUYER, placeOrderBuyerRequestDTO.getStatusUuid())));
            // throw new BuyerDoesNotExistException("buyer.api.placeOrder.error.buyerDoesNotExist");
        }
        PlaceOrderRequestDTO placeOrderRequestDTO = new PlaceOrderRequestDTO();
        placeOrderRequestDTO.setPlaceOrderBuyerRequestDTO(placeOrderBuyerRequestDTO);
        placeOrderRequestDTO.setCountry(buyerDBO.getCountry());
        this.kafkaProducerPlaceOrderTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_TOPIC, placeOrderRequestDTO));
    }
}
