package org.jiangyp.kafka.stream;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Printed;
import org.jiangyp.kafka.KafkaConfig;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class KafkaStream {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);// kafka 集群
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka_stream_test_color_6"); // 流应用车型id，全局唯一
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);// 设置每 100 毫秒提交一次偏移量
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");// 从消息开始的位置读
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), "false"); // 不自动管理偏移量,即不记录消费者偏移量，可以重复读取数据方便测试
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // key 序列化
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // value 序列化

        // 序列化
        final Serializer<JsonNode> custJsonNodeSerializer = new CustJsonSerializer();
        final Deserializer<JsonNode> custJsonDeserializer = new CustJsonDeserializer();
        final Serde<JsonNode> valueSerde = Serdes.serdeFrom(custJsonNodeSerializer, custJsonDeserializer);
        final Consumed<String, JsonNode> consumed = Consumed.with(Serdes.String(), valueSerde);

        final String leftTopic = "ibom-raw.mstdata.md_color";
        final StreamsBuilder builder = new StreamsBuilder();
        builder.table(leftTopic, consumed).toStream().groupByKey().count().toStream().print(Printed.toSysOut());
        final KafkaStreams kStreams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                kStreams.close();
                latch.countDown();
            }
        });
        kStreams.setUncaughtExceptionHandler((t, e) -> {
            log.warn("t:{},e:{}", t, e);
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
