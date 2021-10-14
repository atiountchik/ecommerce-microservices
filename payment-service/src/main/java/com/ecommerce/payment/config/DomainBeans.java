package com.ecommerce.payment.config;

import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import com.ecommerce.sdk.serdes.serializer.EcommerceKafkaSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class DomainBeans {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBrokerAddr;
    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaGroupId;

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