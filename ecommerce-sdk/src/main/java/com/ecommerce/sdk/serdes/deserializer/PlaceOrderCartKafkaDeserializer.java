package com.ecommerce.sdk.serdes.deserializer;

import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class PlaceOrderCartKafkaDeserializer implements Deserializer<ValidateCartRequestDTO> {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ValidateCartRequestDTO deserialize(String topic, byte[] data) {
        ValidateCartRequestDTO returnObject = null;
        if (data == null) {
            return returnObject;
        }
        try {
            returnObject = objectMapper.readValue(data, ValidateCartRequestDTO.class);
        } catch (IOException e) {

        }
        return returnObject;
    }
}