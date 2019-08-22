package org.jiangyp.kafka;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KafkaUtilsTest {

	private KafkaUtils kafkaUtils;

	@Before
	public void setUp() throws Exception {
		kafkaUtils = new KafkaUtils();
	}

	@After
	public void tearDown() throws Exception {
		if (kafkaUtils != null) {
			kafkaUtils.close();
		}
	}

	@Test
	public void findAllTopics() throws ExecutionException, InterruptedException {
		kafkaUtils.findAllTopics();
	}

//	@Test
	public void deleteTopics() {
		kafkaUtils.deleteTopics(Collections.singletonList("jiangyp_test_topic_1"));
	}
}