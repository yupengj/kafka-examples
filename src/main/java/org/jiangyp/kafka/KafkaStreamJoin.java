package org.jiangyp.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class KafkaStreamJoin {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);// kafka 集群
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka_stream_test_ext_10"); // 流应用车型id，全局唯一
//        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);// 设置每 100 毫秒提交一次偏移量

        props.put(StreamsConfig.CONSUMER_PREFIX + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");// 从消息开始的位置读
        props.put(StreamsConfig.CONSUMER_PREFIX + ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 不自动管理偏移量,即不记录消费者偏移量，可以重复读取数据方便测试

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // key 序列化
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // value 序列化

        final Serializer<JsonNode> custKeySerializer = new CustKeySerializer();
        final Deserializer<JsonNode> custKeyDeserializer = new CustKeyDeserializer();
        final Serde<JsonNode> keySerde = Serdes.serdeFrom(custKeySerializer, custKeyDeserializer);

        final Serializer<JsonNode> jsonNodeSerializer = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        final Serde<JsonNode> valueSerde = Serdes.serdeFrom(jsonNodeSerializer, jsonDeserializer);

        final Consumed<JsonNode, JsonNode> consumed = Consumed.with(keySerde, valueSerde);

        final String leftTopic = "ibom.mstdata.md_color";
        final String rightTopic = "ibom.mstdata.md_color_ext";
        final String toTopic = "ibomExtTest.mstdata.md_color";

        final StreamsBuilder builder = new StreamsBuilder();
        final KTable<JsonNode, JsonNode> leftTable = builder.table(leftTopic, consumed);
        final KTable<JsonNode, JsonNode> rightTable = builder.table(rightTopic, consumed);

        final KStream<JsonNode, String> join = leftTable.join(rightTable, (leftValue, rightValue) -> {
            return "left=" + leftValue.toString() + ", right=" + rightValue.toString();
        }).toStream();
        join.print(Printed.toSysOut());
        join.to(toTopic);
        final KafkaStreams kStreams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                kStreams.close();
                latch.countDown();
            }
        });
        try {
            kStreams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
