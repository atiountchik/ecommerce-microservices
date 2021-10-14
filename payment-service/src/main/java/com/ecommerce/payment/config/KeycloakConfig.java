package com.ecommerce.payment.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Based on:
 * https://czetsuya.medium.com/how-to-implement-multitenancy-with-spring-boot-and-keycloak-36be8570005a
 * https://www.keycloak.org/docs/latest/securing_apps/#_multi_tenancy
 */
@Configuration
public class KeycloakConfig {

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new PathBasedKeycloakConfigResolver();
    }

    public class PathBasedKeycloakConfigResolver implements KeycloakConfigResolver {
        private static final String SELLER_REALM = "seller-realm";
        private static final String BUYER_REALM = "buyer-realm";

        private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

        @Override
        public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
            if (request.getRelativePath().toLowerCase().contains("seller")) {
                return this.cache.get(SELLER_REALM);
            } else {
                return this.cache.get(BUYER_REALM);
            }
        }

        @PostConstruct
        private void init() {
            KeycloakDeployment deployment;
            deployment = cache.get(SELLER_REALM);
            if (null == deployment) {
                InputStream is = getClass().getResourceAsStream("/seller-keycloak.json");
                deployment = KeycloakDeploymentBuilder.build(is);
                cache.put(SELLER_REALM, deployment);
            }
            deployment = cache.get(BUYER_REALM);
            if (null == deployment) {
                InputStream is = getClass().getResourceAsStream("/buyer-keycloak.json");
                deployment = KeycloakDeploymentBuilder.build(is);
                cache.put(BUYER_REALM, deployment);
            }
        }
    }


}
