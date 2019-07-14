package org.jiangyp.kafka;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * 消息消费者
 */
public class Consumer {
	//	private final static Logger log = LoggerFactory.getLogger(Consumer.class);

	public static void main(String[] args) {
		Properties p = new Properties();
		p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.5:9092");
		p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		p.put(ConsumerConfig.GROUP_ID_CONFIG, Producer.TOPIC);

		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(p);
		kafkaConsumer.subscribe(Collections.singletonList(Producer.TOPIC));// 订阅消息

		while (true) {
			ConsumerRecords<String, String> records = kafkaConsumer.poll(100L);
			for (ConsumerRecord<String, String> record : records) {
				//				log.info("topic:{}, offset:{}, 消息 {}", record.topic(), record.offset(), record.value());
				System.out.println(String.format("topic:%s,offset:%d,消息:%s", record.topic(), record.offset(), record.value()));
			}
		}
	}
}
