package org.jiangyp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 消息生产者
 */
@Slf4j
public class Producer {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "test_1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);
        String topic = "test";
        int count = 0;
        while (true) {
            String messageKey = "key_" + (++count) + "你好";
            String messageValue = "value_" + count + "你好";
            kafkaProducer.send(new ProducerRecord<>(topic, messageKey, messageValue)).get();
            log.info("send message topic {} ( {}, {})", topic, messageKey, messageValue);
            if (count > 10) {//只发送10条消息
                break;
            }
        }
    }
}
