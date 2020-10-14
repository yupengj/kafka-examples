package org.jiangyp.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class CustKeySerializer implements Serializer<JsonNode> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustKeySerializer() {
    }

    public byte[] serialize(String topic, JsonNode data) {
        if (data == null) {
            return null;
        } else {
            try {
                return this.objectMapper.writeValueAsBytes(data);
            } catch (Exception var4) {
                throw new SerializationException("Error serializing JSON message", var4);
            }
        }
    }
}
