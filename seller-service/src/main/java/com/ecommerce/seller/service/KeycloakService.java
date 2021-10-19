package com.ecommerce.seller.service;

import com.ecommerce.sdk.exceptions.DeleteUserException;
import com.ecommerce.sdk.exceptions.UserConflictException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.UUID;

@Service
public class KeycloakService {
    @Value("${keycloak.realm}")
    private String realm;

    private final Keycloak keycloak;

    @Autowired
    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public String obtainToken(String email, String password) throws HttpResponseException, AuthorizationDeniedException, UnknownHostException {
        return AuthzClient.create().authorization(email, password).authorize().getToken();
    }

    public UUID register(String email, String password) throws UserConflictException, UnknownHostException {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(email);
        userRepresentation.setEmail(email);
        userRepresentation.setEnabled(true);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        userRepresentation.setCredentials(Arrays.asList(credentialRepresentation));

        Response response = this.keycloak.realm(this.realm).users().create(userRepresentation);
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                throw new UserConflictException("seller.api.register.error.sellerAlreadyExists");
            }
        }
        userRepresentation = this.keycloak.realm(this.realm).users().search(email).get(0);
        return UUID.fromString(userRepresentation.getId());
    }

    public void deleteUser(UUID userId) throws DeleteUserException, UnknownHostException {
        Response response = this.keycloak.realm(this.realm).users().delete(userId.toString());
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new DeleteUserException("seller.api.register.error.failedToRevertUserCreation");
        }
    }
}
