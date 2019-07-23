package org.jiangyp.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * 消息生产者
 */
public class Producer {

	public static final String TOPIC = "jiangyp_test_1";

	public static void main(String[] args) {
		Properties props = new Properties();
		//		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.4:9092"); // kafka 单节点
		//		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.4:9091,192.168.1.4:9092,192.168.1.4:9093"); // 本地 docker kafka 集群
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.97.57.83:9092"); // kubernetes  kafka 集群
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "clicet1");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(props);

		boolean isAsync = true;
		int messageNo = 1;
		while (true) {
			String messageStr = "Message_" + messageNo;
			long startTime = System.currentTimeMillis();
			if (isAsync) {
				kafkaProducer.send(new ProducerRecord<>(TOPIC, messageNo, messageStr), new DemoCallBack(startTime, messageNo, messageStr));
			} else {
				try {
					kafkaProducer.send(new ProducerRecord<>(TOPIC, messageStr)).get();
					System.out.println("Sent message: (" + messageNo + ", " + messageStr + ")");
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			++messageNo;
			//			if (messageNo == 10) {
			//				break;
			//			}
		}
	}
}

class DemoCallBack implements Callback {

	private final long startTime;
	private final int key;
	private final String message;

	public DemoCallBack(long startTime, int key, String message) {
		this.startTime = startTime;
		this.key = key;
		this.message = message;
	}

	/**
	 * 回调
	 *
	 * @param metadata  metadata
	 * @param exception exception
	 */
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		if (metadata != null) {
			System.out.println(
					"message(" + key + ", " + message + ") sent to partition(" + metadata.partition() + "), " + "offset(" + metadata.offset() + ") in "
							+ elapsedTime + " ms");
		} else {
			exception.printStackTrace();
		}
	}
}