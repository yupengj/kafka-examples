package org.jiangyp.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息消费者
 */
public class Consumer {
	private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.26:9092");// kafka 集群
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "jiangyp_group1"); // 消费组id
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "jiangyp_client1"); // 消费客户端id
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 从消息开始的位置读
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 不自动管理偏移量
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Collections.singletonList(Producer.TOPIC));
		long start = System.currentTimeMillis();
		int count = 0, num = 10;// 有10次拉取的数据记录为 0 时 结束轮询
		while (true) {
			ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
			for (ConsumerRecord<String, String> record : records) {
				LOG.info("Received message topic {} : ({}, {}) at partition {} offset {}", record.topic(), record.key(), record.value(), record.partition(),
						record.offset());
			}
			count += records.count(); // 记录累加
			if (records.count() == 0) {
				num--;
				if (num < 0) {
					break;
				}
			}
		}
		LOG.info("poll topic {} size {} time {} ms", Producer.TOPIC, count, System.currentTimeMillis() - start);
	}
}
