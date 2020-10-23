package org.jiangyp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 消息消费者
 */
@Slf4j
public class Consumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);// kafka 集群
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_1"); // 消费组id
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "test_1"); // 消费客户端id
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 从消息开始的位置读
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 从消息最新的位置
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 不自动管理偏移量,即不记录消费者偏移量，可以重复读取数据方便测试
        props.put(ConsumerConfig.METRICS_RECORDING_LEVEL_CONFIG, "INFO");// 日志等级
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);

//        kafkaConsumer.subscribe(Arrays.asList("ibom-raw.mstdata.md_color"));

        kafkaConsumer.subscribe(Pattern.compile("ibom-main.*"));

        Set<String> keySet = new HashSet<>();
        int count = 0;
        long start = System.currentTimeMillis();
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                log.info("Received message topic {} partition {} offset {} key {} value {}", record.topic(), record.partition(), record.offset(), record.key(), record.value());
                if (record.value() == null || record.value().equals("null")) {
                    keySet.remove(record.key());
                } else {
                    keySet.add(record.key());
                }
            }
            count += records.count();
            keySet.remove(null);
            keySet.remove("null");
            log.info("poll record size {}, table size {}, time {} ms", count, keySet.size(), System.currentTimeMillis() - start);
        }
    }
}
