package org.jiangyp.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息生产者
 */
public class Producer {
	public static final String TOPIC = "jiangyp_test_topic_1";
	private static final Logger LOG = LoggerFactory.getLogger(Producer.class);

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.26:9092");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "jiangyp_client1");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DemoPartitioner.class);// 自定义分区器
		KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

		boolean isAsync = true;
		int count = 0;
		while (true) {
			String messageKey = "key_" + (++count);
			String messageValue = "value_" + count;
			if (isAsync) {// 异步
				long startTime = System.currentTimeMillis();
				kafkaProducer.send(new ProducerRecord<>(TOPIC, messageKey, messageValue), new DemoCallBack(startTime, messageKey, messageValue));
			} else {// 同步
				kafkaProducer.send(new ProducerRecord<>(TOPIC, messageKey, messageValue)).get();
				LOG.info("send message: ( {}, {})", messageKey, messageValue);
			}
			if (count > 10) {//只发送10条消息
				break;
			}
		}
	}
}

class DemoCallBack implements Callback {
	private static final Logger log = LoggerFactory.getLogger(DemoCallBack.class);
	private final long startTime;
	private final String key;
	private final String message;

	public DemoCallBack(long startTime, String key, String message) {
		this.startTime = startTime;
		this.key = key;
		this.message = message;
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
			log.info("message({}, {}) sent to partition({}), offset({}) in {} ms", key, message, metadata.partition(), metadata.offset(), elapsedTime);
		}
	}
}