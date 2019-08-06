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

public class TestStream {

    private static KafkaStreams createStreamsInstance(String bootstrapServers) {
        StreamsBuilder builder = new StreamsBuilder();
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test17");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        //添加一个状态存储，名称可以是数据表名，key为主键，value为图数据库中的id
//        String storeName = Material.class.getName();
//        KeyValueBytesStoreSupplier storeSupplier = Stores.inMemoryKeyValueStore(storeName);
//        StoreBuilder<KeyValueStore<Long, String>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.Long(), Serdes.String());

        //设置变更日志的属性为永久保存且大小无上限
//        Map<String, String> changeLogConfigs = new HashMap<>();
//        changeLogConfigs.put("retention.ms", String.valueOf(Integer.MAX_VALUE));
//        changeLogConfigs.put("retention.bytes", String.valueOf(Integer.MAX_VALUE));
//        storeBuilder.withLoggingEnabled(changeLogConfigs);
//        builder.addStateStore(storeBuilder);

        //从连接器读入最原始的JSON数据


        KStream<String, String> rawStream =builder.stream("ibom.mstdata.md_feature", Consumed.with(Serdes.String(),Serdes.String()));


        rawStream.print(Printed.toSysOut());
//        //用主键ID生成新的数据流，并把值转换为Material对象
//        KeyValueMapper<KeyModel, ValueModel, Long> idAsKey = (key, value) -> Long.parseLong(key.getPayload().values().iterator().next());
//        KStream<Long, Material> idStream = rawStream.selectKey(idAsKey).mapValues(v -> Material.builder(v.getValue(), v.getPayload().getOp()).build());
//
//        //为每一个值执行转换操作，这里可以植入任何业务代码，并且启用了缓存
//        KStream<Long, String> statefulStream = idStream.transformValues(() -> new MaterialTransformer(), storeName);
        return new KafkaStreams(builder.build(), props);
    }

    public static void main(String... args) {
        String bootstrapServers = "192.168.2.26:9092";
        KafkaStreams kstreams = createStreamsInstance(bootstrapServers);
        kstreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                kstreams.close();
            }
        });
    }
}
