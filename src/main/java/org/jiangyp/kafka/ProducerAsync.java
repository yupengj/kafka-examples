package org.jiangyp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerAsync {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "test_1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DemoPartitioner.class);// 自定义分区器
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

        String topic = "test";
        int count = 0;
        while (true) {
            String messageKey = "key_" + (++count) + "你好";
            String messageValue = "value_" + count + "你好";
            long startTime = System.currentTimeMillis();
            kafkaProducer.send(new ProducerRecord<>(topic, messageKey, messageValue), new DemoCallBack(topic, messageKey, messageValue, startTime));
            if (count > 10) {//只发送10条消息
                break;
            }
        }
    }
}

@Slf4j
class DemoCallBack implements Callback {
    private final String topic;
    private final String key;
    private final String message;
    private final long startTime;

    DemoCallBack(String topic, String key, String message, long startTime) {
        this.topic = topic;
        this.key = key;
        this.message = message;
        this.startTime = startTime;
    }

    /**
     * 回调
     *
     * @param metadata  元数据
     * @param exception exception
     */
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata == null) {
            exception.printStackTrace();
        } else {
            log.info("send message topic{}({}, {})  partition({}), offset({}) in {} ms", topic, key, message, metadata.partition(), metadata.offset(), elapsedTime);
        }
    }
}
