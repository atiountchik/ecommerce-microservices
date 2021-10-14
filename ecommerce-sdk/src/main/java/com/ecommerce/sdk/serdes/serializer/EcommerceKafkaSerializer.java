package com.ecommerce.sdk.serdes.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class EcommerceKafkaSerializer<T> implements Serializer<T> {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, T data) {
        byte[] returnBytes = null;
        if (data == null){
            return returnBytes;
        }
        try {
            returnBytes = objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {

        }
        return returnBytes;
    }
}
