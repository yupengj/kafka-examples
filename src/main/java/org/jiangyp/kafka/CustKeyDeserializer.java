package org.jiangyp.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class CustKeyDeserializer implements Deserializer<JsonNode> {

    private ObjectMapper objectMapper = new ObjectMapper();

    public CustKeyDeserializer() {
    }

    public JsonNode deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            try {
                JsonNode keyNode = this.objectMapper.readTree(bytes);
                log.info("oldKeyNode: {}", keyNode);
                final ObjectNode objectNode = (ObjectNode) keyNode.get("schema");
                if (objectNode == null || objectNode.isNull()) {
                    return keyNode;
                } else {
                    final ObjectNode keyField = (ObjectNode) keyNode.get("schema").get("fields").get(0);
                    final ObjectNode payloadNode = (ObjectNode) keyNode.get("payload");

                    final ObjectNode newKeyNode = JsonNodeFactory.instance.objectNode();
                    newKeyNode.set("id", payloadNode.get(keyField.get("field").textValue()));
                    log.info("newKeyNode: {}", newKeyNode);
                    return newKeyNode;
                }
            } catch (Exception var5) {
                throw new SerializationException(var5);
            }
        }
    }

}
