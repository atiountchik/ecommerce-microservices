package com.ecommerce.cart.config;

import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.request.ProductAvailabilityRequestDTO;
import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.ecommerce.sdk.response.ProductAvailabilityResponseDTO;
import com.ecommerce.sdk.serdes.serializer.EcommerceKafkaSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DomainBeans {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBrokerAddr;
    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaGroupId;

    @Bean
    public KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCart() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaBrokerAddr);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EcommerceKafkaSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    @Bean
    @Qualifier(KafkaTopics.CHECK_ITEM_AVAILABILITY_REQUEST_TOPIC)
    public KafkaProducer<String, ProductAvailabilityRequestDTO> kafkaProducerAddItem() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaBrokerAddr);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EcommerceKafkaSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    @Bean
    public ConsumerFactory<String, ProductAvailabilityResponseDTO> productAvailabilityResponseCConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaBrokerAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.kafkaGroupId);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(ProductAvailabilityResponseDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductAvailabilityResponseDTO> productAvailabilityResponseContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductAvailabilityResponseDTO> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productAvailabilityResponseCConsumerFactory());
        return factory;
    }

}