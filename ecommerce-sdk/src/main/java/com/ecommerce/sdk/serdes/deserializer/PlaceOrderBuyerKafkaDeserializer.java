package com.ecommerce.sdk.serdes.deserializer;

import com.ecommerce.sdk.request.PlaceOrderBuyerRequestDTO;
import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class PlaceOrderBuyerKafkaDeserializer implements Deserializer<PlaceOrderBuyerRequestDTO> {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PlaceOrderBuyerRequestDTO deserialize(String topic, byte[] data) {
        PlaceOrderBuyerRequestDTO returnObject = null;
        if (data == null) {
            return returnObject;
        }
        try {
            returnObject = objectMapper.readValue(data, PlaceOrderBuyerRequestDTO.class);
        } catch (IOException e) {

        }
        return returnObject;
    }
}