package org.jiangyp.kafka;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * 消息生产者
 */
public class Producer {

	//	private final static Logger log = LoggerFactory.getLogger(Consumer.class);

	public static String TOPIC = "test_1";//定义主题

	public static void main(String[] args) throws InterruptedException {
		Properties p = new Properties();
		p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.5:9092");//kafka地址，多个地址用逗号分割
		p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(p);

		try {
			while (true) {
				String msg = "Hello," + new Random().nextInt(100);
				ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, msg);
				kafkaProducer.send(record);
				//				log.info("消息发送成功: {} ", msg);
				System.out.println("消息发送成功: {} " + msg);
				Thread.sleep(500);
			}
		} finally {
			kafkaProducer.close();
		}

	}
}
