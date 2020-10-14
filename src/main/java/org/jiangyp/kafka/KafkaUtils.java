package org.jiangyp.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class KafkaUtils implements AutoCloseable {

    private static AdminClient adminClient;

    static {
        adminClient = getAdminClient();
    }

    private static AdminClient getAdminClient() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);
        return AdminClient.create(props);
    }

    @Override
    public void close() throws Exception {
        if (adminClient != null) {
            adminClient.close();
        }
    }

    public Set<String> findAllTopics() throws ExecutionException, InterruptedException {
        ListTopicsResult listTopicsResult = adminClient.listTopics();
        Collection<TopicListing> topicListings = listTopicsResult.listings().get();
        return topicListings.stream().map(TopicListing::name).collect(Collectors.toSet());
    }

    public void deleteTopics(Collection<String> topics) {
        adminClient.deleteTopics(topics);
    }

    public AdminClient adminClient() {
        return this.adminClient;
    }

}
