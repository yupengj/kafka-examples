package org.jiangyp.kafka.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.kafka.connect.json.JsonDeserializer;

public class CustJsonDeserializer extends JsonDeserializer {

    @Override
    public JsonNode deserialize(String topic, byte[] bytes) {
        final JsonNode jsonNode = super.deserialize(topic, bytes);
        if (jsonNode == null) {
            return NullNode.getInstance();
        }
        return jsonNode;
    }
}
