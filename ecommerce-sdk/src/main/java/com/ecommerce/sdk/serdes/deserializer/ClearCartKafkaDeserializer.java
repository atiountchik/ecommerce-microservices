package com.ecommerce.sdk.serdes.deserializer;

import com.ecommerce.sdk.request.ClearCartRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class ClearCartKafkaDeserializer implements Deserializer<ClearCartRequestDTO> {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ClearCartRequestDTO deserialize(String topic, byte[] data) {
        ClearCartRequestDTO returnObject = null;
        if (data == null) {
            return returnObject;
        }
        try {
            returnObject = objectMapper.readValue(data, ClearCartRequestDTO.class);
        } catch (IOException e) {

        }
        return returnObject;
    }
}
