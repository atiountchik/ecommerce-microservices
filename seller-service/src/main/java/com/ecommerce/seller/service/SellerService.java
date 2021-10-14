package com.ecommerce.seller.service;

import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.exceptions.DeleteUserException;
import com.ecommerce.sdk.exceptions.InvalidInputException;
import com.ecommerce.sdk.exceptions.UserConflictException;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.RegisterSellerRequestDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.seller.repository.dbo.SellerDBO;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SellerService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    private Keycloak keycloak;

    private SellerRepository sellerRepository;

    @Autowired
    public SellerService(SellerRepository sellerRepository, Keycloak keycloak) {
        this.sellerRepository = sellerRepository;
        this.keycloak = keycloak;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();
        String token = AuthzClient.create().authorization(email, password).authorize().getToken();
        return new LoginResponseDTO(token);
    }

    @Transactional
    public void register(RegisterSellerRequestDTO inputData) throws InvalidInputException, UserConflictException, DeleteUserException {
        if (inputData == null) {
            throw new InvalidInputException("seller.api.register.error.nullInput");
        }
        OffsetTime opensAt = OffsetTime.parse(inputData.getOpensAt());
        OffsetTime closesAt = OffsetTime.parse(inputData.getClosesAt());
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
                throw new UserConflictException("seller.api.register.error.buyerAlreadyExists");
            }
        }
        userRepresentation = this.keycloak.realm(this.realm).users().search(inputData.getEmail()).get(0);
        UUID userId = UUID.fromString(userRepresentation.getId());
        if (this.sellerRepository.findByAuthId(userId) != null) {
            response = this.keycloak.realm(this.realm).users().delete(inputData.getEmail());
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new DeleteUserException("seller.api.register.error.failedToRevertUserCreation");
            }
            throw new UserConflictException("seller.api.register.error.buyerInformationAlreadyPresent");
        }
        ZonedDateTime now = ZonedDateTime.now();
        SellerDBO entity = new SellerDBO(0l, now, now, inputData.getName(), inputData.getLatitude(), inputData.getLongitude(), inputData.getCountry(), inputData.getCountryVatNumber(), opensAt, closesAt, userId);

        this.sellerRepository.save(entity);
    }

    public SellerDBO getProfile(Authentication authentication) {
        String userId = authentication.getName();
        return this.sellerRepository.findByAuthId(UUID.fromString(userId));
    }

    private boolean isAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.BUYER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }
}
