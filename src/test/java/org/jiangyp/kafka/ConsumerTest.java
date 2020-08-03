package org.jiangyp.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerTest {
    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

    private Properties props;
    private KafkaConsumer kafkaConsumer;

    @Before
    public void setUp() throws Exception {
        props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.26:9092");// kafka 集群
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "jiangyp_group1"); // 消费组id
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "jiangyp_client1"); // 消费客户端id
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 从消息开始的位置读
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 从消息最新的位置读
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 不自动管理偏移量
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    }

    @After
    public void tearDown() throws Exception {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
    }

    private void createKafkaConsumer() {
        kafkaConsumer = new KafkaConsumer<>(props);
    }

    @Test
    public void testPostgresTopic() {
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        createKafkaConsumer();

        kafkaConsumer.subscribe(Collections.singletonList("ibom.mstdata.md_change"));

        long count = 0;
        while (true) {
            ConsumerRecords<JsonNode, JsonNode> records = kafkaConsumer.poll(Duration.ofSeconds(1));
            count += records.count();

            for (ConsumerRecord<JsonNode, JsonNode> record : records) {
                System.out.println("key: " + record.key() + " -- value" + record.value());
            }
        }
    }
}
