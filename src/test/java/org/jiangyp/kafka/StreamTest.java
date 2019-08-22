package org.jiangyp.kafka;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StreamTest {

	private KafkaStreams kStreams;
	private StreamsBuilder builder;
	private Properties props;

	@Before
	public void setUp() throws Exception {
		builder = new StreamsBuilder();
		props = new Properties();
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.26:9092");
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "jiangyp_app_id_1");
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
		props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
	}

	@After
	public void tearDown() throws Exception {
		if (kStreams != null) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					kStreams.close();
				}
			});
		}
	}

	@Test
	public void testPostgresTopic() {
		KStream<String, String> rawStream = builder.stream("ibom.mstdata.md_test", Consumed.with(Serdes.String(), Serdes.String()));
		rawStream.print(Printed.toSysOut());
		kStreams = new KafkaStreams(builder.build(), props);
		kStreams.start();
	}
}
