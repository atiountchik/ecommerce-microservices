package com.ecommerce.sdk.serdes.deserializer;

import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class PlaceOrderKafkaDeserializer implements Deserializer<PlaceOrderRequestDTO> {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PlaceOrderRequestDTO deserialize(String topic, byte[] data) {
        PlaceOrderRequestDTO returnObject = null;
        if (data == null) {
            return returnObject;
        }
        try {
            returnObject = objectMapper.readValue(data, PlaceOrderRequestDTO.class);
        } catch (IOException e) {

        }
        return returnObject;
    }
}