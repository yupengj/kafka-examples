package org.jiangyp.kafka;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
        final Set<String> allTopics = kafkaUtils.findAllTopics();
    }

    @Test
    public void deleteTopics() throws ExecutionException, InterruptedException {
        final Set<String> allTopics = kafkaUtils.findAllTopics();
        List<String> deleteTops = new ArrayList<>();
        for (String allTopic : allTopics) {
            if (allTopic.startsWith("ibom.")) {
                deleteTops.add(allTopic);
            }
        }
        kafkaUtils.deleteTopics(deleteTops);
    }

    @Test
    public void deleteAll() throws ExecutionException, InterruptedException {
        final Set<String> allTopics = kafkaUtils.findAllTopics();
        kafkaUtils.deleteTopics(allTopics);
    }
}