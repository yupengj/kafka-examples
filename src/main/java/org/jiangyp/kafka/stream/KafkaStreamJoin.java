package org.jiangyp.kafka.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.jiangyp.kafka.KafkaConfig;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class KafkaStreamJoin {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);// kafka 集群
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-stream-merge-ext-table-test-7"); // 全局唯一
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);// 设置每 100 毫秒提交一次偏移量
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // key 序列化
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // value 序列化

        final Serde<JsonNode> valueSerde = Serdes.serdeFrom(new CustJsonSerializer(), new CustJsonDeserializer());
        final Serde<JsonNode> mergedValueSerde = Serdes.serdeFrom(new JsonSerializer(), new JsonDeserializer());
        final Consumed<String, JsonNode> consumed = Consumed.with(Serdes.String(), valueSerde);

        final String leftTopic = "ibom-raw.mstdata.md_material";
        final String rightTopic = "ibom-raw.mstdata.md_material_ext";
        final String toTopic = "ibom-test-main.mstdata.md_material";

        final StreamsBuilder builder = new StreamsBuilder();
        final KTable<String, JsonNode> leftTable = builder.table(leftTopic, consumed);
        final KTable<String, JsonNode> rightTable = builder.table(rightTopic, consumed);

        final KStream<String, JsonNode> join = leftTable.join(rightTable, (leftValue, rightValue) -> {
            if (leftValue == null || leftValue.isNull() || rightValue == null || rightValue.isNull()) {
                return null;
            } else {
                final ObjectNode leftObjValue = (ObjectNode) leftValue;
                final ObjectNode rightObjValue = (ObjectNode) rightValue;
                leftObjValue.setAll(rightObjValue);
                return leftValue;
            }
        }).toStream();// Materialized.with(Serdes.String(), mergedValueSerde)
        join.print(Printed.toSysOut());
        join.to(toTopic, Produced.with(Serdes.String(), mergedValueSerde, null));

        final KafkaStreams kStreams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                kStreams.close();
                kStreams.cleanUp();
                latch.countDown();
            }
        });
        try {
            kStreams.cleanUp();
            kStreams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
