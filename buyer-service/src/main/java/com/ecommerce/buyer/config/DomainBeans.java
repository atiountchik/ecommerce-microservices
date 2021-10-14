package com.ecommerce.buyer.config;

import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import com.ecommerce.sdk.serdes.serializer.EcommerceKafkaSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;

@Configuration
public class DomainBeans {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBrokerAddr;
    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaGroupId;

    @Value("${keycloak.credentials.secret}")
    private String secretKey;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.auth-server-url}")
    private String authUrl;
    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .grantType(CLIENT_CREDENTIALS)
                .serverUrl(authUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(secretKey)
                .build();
    }

    @Bean
    public KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaBrokerAddr);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EcommerceKafkaSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    @Bean
    @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC)
    public KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaBrokerAddr);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EcommerceKafkaSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }
}
