package org.jiangyp.kafka.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.kafka.connect.json.JsonSerializer;

public class CustJsonSerializer extends JsonSerializer {
    @Override
    public byte[] serialize(String topic, JsonNode data) {
        final byte[] serialize = super.serialize(topic, data);
        if (serialize == null) {
            return NullNode.getInstance().asText().getBytes();
        }
        return serialize;
    }
}
