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
		Properties p = new Properties();
		p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
		p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
		p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		p.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer1");

		KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(p);
		kafkaConsumer.subscribe(Collections.singletonList(Producer.TOPIC));
		while (true) {
			ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
			for (ConsumerRecord<Integer, String> record : records) {
				System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
			}
		}
	}
}
