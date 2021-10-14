package com.ecommerce.sdk.serdes.deserializer;

import com.ecommerce.sdk.request.PaymentRequestDTO;
import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class PlaceOrderPaymentKafkaDeserializer implements Deserializer<PaymentRequestDTO> {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentRequestDTO deserialize(String topic, byte[] data) {
        PaymentRequestDTO returnObject = null;
        if (data == null) {
            return returnObject;
        }
        try {
            returnObject = objectMapper.readValue(data, PaymentRequestDTO.class);
        } catch (IOException e) {

        }
        return returnObject;
    }
}