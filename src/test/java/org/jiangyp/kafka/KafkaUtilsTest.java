package org.jiangyp.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.acl.AclOperation;
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
        allTopics.forEach(System.out::println);
    }

    @Test
    public void deleteTopics() throws ExecutionException, InterruptedException {
        final Set<String> allTopics = kafkaUtils.findAllTopics();
        List<String> deleteTops = new ArrayList<>();
        for (String allTopic : allTopics) {
            if (allTopic.equals("ibom.core.ts_acct_user")) {
                deleteTops.add(allTopic);
            }
        }
        deleteTops.forEach(System.out::println);
        kafkaUtils.deleteTopics(deleteTops);
    }

    @Test
    public void deleteAll() throws ExecutionException, InterruptedException {
        final Set<String> allTopics = kafkaUtils.findAllTopics();
        kafkaUtils.deleteTopics(allTopics);
    }

    @Test
    public void c() throws ExecutionException, InterruptedException {
        final AdminClient adminClient = kafkaUtils.adminClient();
    }
}