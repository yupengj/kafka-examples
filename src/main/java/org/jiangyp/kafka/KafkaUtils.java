package org.jiangyp.kafka;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class KafkaUtils {

	private static AdminClient adminClient;

	static {
		adminClient = getAdminClient();
	}

	private static AdminClient getAdminClient() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.4.109:9092");
		return AdminClient.create(props);
	}

	private static void close() {
		if (adminClient != null) {
			adminClient.close();
		}
	}

	private static Set<String> findAllTopics() throws ExecutionException, InterruptedException {
		ListTopicsResult listTopicsResult = adminClient.listTopics();
		Collection<TopicListing> topicListings = listTopicsResult.listings().get();
		return topicListings.stream().map(TopicListing::name).collect(Collectors.toSet());
	}

	private static void deleteTopics(Collection<String> topics) {
		adminClient.deleteTopics(topics);
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		Set<String> allTopics = findAllTopics();

		Set<String> ibomTopics = allTopics.stream().filter(it -> it.startsWith("ibom") || it.startsWith("BI_CHANGE_ANALYSIS")).collect(Collectors.toSet());
		deleteTopics(ibomTopics);

		close();
	}
}
