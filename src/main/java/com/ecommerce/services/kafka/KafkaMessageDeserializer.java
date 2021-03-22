package com.ecommerce.services.kafka;

import java.nio.charset.Charset;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.ecommerce.domain.Order;
import com.google.gson.Gson;

public class KafkaMessageDeserializer implements Deserializer {
    private static final Charset CHARSET = Charset.forName("UTF-8");
    static private Gson gson;

    static {
        gson = new Gson();
    }

    @Override
    public void configure(Map map, boolean b) {

    }

    @Override
    public Object deserialize(String s, byte[] bytes) {
        try {
            // Transform the bytes to String
            String message = new String(bytes, CHARSET);
            // Return the Person object created from the String 'person'
            return gson.fromJson(message, Order.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading bytes!", e);
        }
    }

    @Override
    public void close() {

    }
}