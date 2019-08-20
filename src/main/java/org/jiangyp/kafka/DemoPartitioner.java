package org.jiangyp.kafka;

import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义分区器示例
 */
public class DemoPartitioner extends DefaultPartitioner {
	private static final Logger log = LoggerFactory.getLogger(DemoPartitioner.class);

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		final int partition = super.partition(topic, key, keyBytes, value, valueBytes, cluster);

		log.info("topic {} key {} partition {}", topic, key, partition);
		return partition;
	}
}
