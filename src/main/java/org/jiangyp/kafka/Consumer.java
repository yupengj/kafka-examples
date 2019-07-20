package org.jiangyp.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * 消息消费者
 */
public class Consumer {
	public static void main(String[] args) {
		Properties props = new Properties();
		//		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.4:9092"); // kafka 单节点
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.4:9091,192.168.1.4:9092,192.168.1.4:9093");  // kafka 多节点
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Collections.singletonList(Producer.TOPIC));
		while (true) {
			ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
			for (ConsumerRecord<Integer, String> record : records) {
				System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
			}
		}
	}
}
