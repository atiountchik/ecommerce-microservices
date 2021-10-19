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
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class BuyerService {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final BuyerRepository buyerRepository;

    private final KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic;
    private final KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    private KeycloakService keycloakService;

    @Autowired
    public BuyerService(BuyerRepository buyerRepository,
                        KeycloakService keycloakService,
                        KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic,
                        @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC) KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus) {
        this.buyerRepository = buyerRepository;
        this.keycloakService = keycloakService;
        this.kafkaProducerPlaceOrderTopic = kafkaProducerPlaceOrderTopic;
        this.kafkaProducerSwitchStatus = kafkaProducerSwitchStatus;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws HttpResponseException, AuthorizationDeniedException, UnknownHostException {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();
        String token = this.keycloakService.obtainToken(email, password);
        return new LoginResponseDTO(token);
    }


    @Transactional
    public void register(RegisterBuyerRequestDTO inputData) throws InvalidInputException, UserConflictException, DeleteUserException, UnknownHostException {
        if (inputData == null) {
            throw new InvalidInputException("buyer.api.register.error.nullInput");
        }

        String email = inputData.getEmail();
        String password = inputData.getPassword();
        UUID userId = this.keycloakService.register(email, password);

        if (this.buyerRepository.findByAuthId(userId) != null) {
            this.keycloakService.deleteUser(userId);
            throw new UserConflictException("buyer.api.register.error.buyerInformationAlreadyPresent");
        }

        ZonedDateTime now = ZonedDateTime.now();
        BuyerDBO entity = new BuyerDBO(0l, now, now, inputData.getName(), inputData.getLatitude(), inputData.getLongitude(), inputData.getCountry(), userId);

        this.buyerRepository.save(entity);
    }

    public BuyerDBO getProfile(Authentication authentication) throws CountryNotFoundException {
        UUID userId = getBuyerId(authentication);
        return this.buyerRepository.findByAuthId(userId);
    }

    private UUID getBuyerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.BUYER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }

    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_BUYER_TOPIC)
    @Transactional
    public void placeOrderBuyer(@Valid PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO) throws IllegalArgumentException, CountryNotFoundException {
        if (placeOrderBuyerRequestDTO == null) {
            throw new IllegalArgumentException("buyer.api.placeOrder.error.nullInput");
        }
        UUID buyerId = placeOrderBuyerRequestDTO.getBuyerId();
        BuyerDBO buyerDBO = this.buyerRepository.findByAuthId(buyerId);
        if (buyerDBO == null) {
            this.kafkaProducerSwitchStatus.send(new ProducerRecord<>(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, new SwitchOrderStatusDTO(OrderStatusEnum.ERROR_UNKOWN_BUYER, placeOrderBuyerRequestDTO.getStatusUuid())));
            logger.error("buyer.api.placeOrder.error.buyerDoesNotExist | " + buyerId);
            return;
        }
        PlaceOrderRequestDTO placeOrderRequestDTO = new PlaceOrderRequestDTO();
        placeOrderRequestDTO.setPlaceOrderBuyerRequestDTO(placeOrderBuyerRequestDTO);
        placeOrderRequestDTO.setCountry(CountryEnum.getCountryEnum(buyerDBO.getCountry()));
        this.kafkaProducerPlaceOrderTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_TOPIC, placeOrderRequestDTO));
    }

    @Transactional
    public BuyerDBO updateProfile(Authentication authentication, BuyerDTO buyerDTO) throws UserDoesNotExistException, AccessDeniedException {
        UUID buyerId = getBuyerId(authentication);
        BuyerDBO fetchedBuyerDBO = this.buyerRepository.findById(buyerDTO.getId()).orElse(null);
        if (fetchedBuyerDBO == null) {
            throw new UserDoesNotExistException("buyer.api.update.error.idNotFound");
        }
        if (!isAdmin(authentication) && !fetchedBuyerDBO.getAuthId().equals(buyerId)) {
            throw new AccessDeniedException("buyer.api.update.error.forbidden");
        }
        fetchedBuyerDBO.setCountry(buyerDTO.getCountry().name());
        fetchedBuyerDBO.setLastUpdateDate(ZonedDateTime.now());
        fetchedBuyerDBO.setLatitude(buyerDTO.getLatitude());
        fetchedBuyerDBO.setLongitude(buyerDTO.getLongitude());
        fetchedBuyerDBO.setName(buyerDTO.getName());
        return this.buyerRepository.save(fetchedBuyerDBO);
    }


    public void deleteProfile(Authentication authentication, UUID buyerIdToDelete) throws UserDoesNotExistException, AccessDeniedException, UnknownHostException, DeleteUserException {
        BuyerDBO fetchedBuyerDBO = checkPermissionsAndFetchExistingUser(authentication, buyerIdToDelete);
        this.keycloakService.deleteUser(buyerIdToDelete);
        this.buyerRepository.delete(fetchedBuyerDBO);
    }

    private BuyerDBO checkPermissionsAndFetchExistingUser(Authentication authentication, UUID buyerIdToDelete) throws UserDoesNotExistException {
        UUID buyerId = getBuyerId(authentication);
        BuyerDBO fetchedBuyerDBO = this.buyerRepository.findByAuthId(buyerIdToDelete);
        if (fetchedBuyerDBO == null) {
            throw new UserDoesNotExistException("buyer.api.delete.error.idNotFound");
        }
        if (!isAdmin(authentication) && !fetchedBuyerDBO.getAuthId().equals(buyerId)) {
            throw new AccessDeniedException("buyer.api.delete.error.forbidden");
        }
        return fetchedBuyerDBO;
    }

    public BuyerDBO getProfile(Authentication authentication, UUID buyerId) throws UserDoesNotExistException {
        return checkPermissionsAndFetchExistingUser(authentication, buyerId);
    }
}
