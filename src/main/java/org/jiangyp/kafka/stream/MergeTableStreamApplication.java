/*
 * Copyright (c) 2015—2030 GantSoftware.Co.Ltd. All rights reserved.
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * is not allowed to be distributed or copied without the license from
 * GantSoftware.Co.Ltd. Please contact the company for more information.
 */

package org.jiangyp.kafka.stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class MergeTableStreamApplication {

    /**
     * kafka stream 对象
     */
    private KafkaStreams kStreams;
    /**
     * kafka stream 构建对象
     */
    private final StreamsBuilder builder = new StreamsBuilder();
    /**
     * 日期格式换工具
     */
    private static final ThreadLocal<DateFormat> format = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    /**
     * 自定义序列化和反序列化工具，主要是在读取原始主题使把删除记录的把 null 转换成 "null"，因为如果不转换 join 操作将捕获不到数据变化。
     */
    private final Consumed<String, JsonNode> consumed = Consumed.with(Serdes.String(), Serdes.serdeFrom(new CustJsonSerializer(), new CustJsonDeserializer()));
    /**
     * 写到合并后主题的序列化和反序列化工具，就是原生的序列化和反序列化
     */
    private final Serde<JsonNode> mergedValueSerde = Serdes.serdeFrom(new JsonSerializer(), new JsonDeserializer());
    /**
     * key 是表名，value 是对应的 kTable
     */
    private final Map<String, KTable<String, JsonNode>> tableAndKTableMap = new HashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String SYNC_TABLES_CONFIG_PATH = "full-text-search-sync-tables.json";
    /**
     * kafka 集群地址
     */
    private String bootstrapServer;

    public void setBootstrapServer(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    /**
     * 启动同步主题流
     */
    public void startStreamApplication() {
        final List<SyncTable> syncTables = loadSyncTable();
        if (syncTables != null) {
            builderKafkaStreams(syncTables);
            createKafkaStreams();
        }
    }


    /**
     * 从 json 文件中加载同步表数据
     *
     * @return 同步表数据
     */
    private List<SyncTable> loadSyncTable() {
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource("full-text-search-sync-tables2.json");
            final ArrayList<SyncTable> syncTables = objectMapper.readValue(resource, new TypeReference<ArrayList<SyncTable>>() {
            });
            for (SyncTable syncTable : syncTables) {
                if (syncTable.getMetadataTableName() == null) {
                    syncTable.setMetadataTableName(syncTable.getMainTableName());
                }
                // 如果 Oracle 和 Pg 产生的主题不同可以在这里兼容
                syncTable.setMainTableRawTopicName("ibom-raw." + syncTable.getMainTableName());
                syncTable.setExtTableRawTopicName("ibom-raw." + syncTable.getExtTableName());
                syncTable.setEsIndexName("ibom-test-main." + syncTable.getMainTableName());
                syncTable.setDateFields(new HashSet<>());
                syncTable.setFieldAndCodeList(new HashMap<>());
            }
            return syncTables;
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("读取同步表数据失败", e);
        }
        return null;
    }

    /**
     * 启动 kafka stream
     *
     * @param syncTables 所有同步的表
     * @see #mergeTable(SyncTable)
     * @see #convertNotHasExtTable(SyncTable)
     */
    private void builderKafkaStreams(final List<SyncTable> syncTables) {
        for (SyncTable syncTable : syncTables) {
            if (syncTable.getExtTableName() != null && !syncTable.getExtTableName().isEmpty()) {
                // 合并主表和扩展表
                mergeTable(syncTable);
            } else {
                // 没有扩展表不需要合并直接转换
                convertNotHasExtTable(syncTable);
            }
        }
    }

    /**
     * 合并主表和扩展表的数据到同一个主题
     * <p>
     * 合并到同一个主题后进行日期和codeList的转换
     *
     * @param syncTable 所有有扩展表的表
     * @see #convertDateAndCodeList(SyncTable)
     */
    private void mergeTable(SyncTable syncTable) {
        // 构建主表
        tableAndKTableMap.computeIfAbsent(syncTable.getMainTableRawTopicName(), it -> builder.table(syncTable.getMainTableRawTopicName(), consumed));
        final KTable<String, JsonNode> leftTable = tableAndKTableMap.get(syncTable.getMainTableRawTopicName());
        // 构建扩展表
        tableAndKTableMap.computeIfAbsent(syncTable.getExtTableRawTopicName(), it -> builder.table(syncTable.getExtTableRawTopicName(), consumed));
        final KTable<String, JsonNode> rightTable = tableAndKTableMap.get(syncTable.getExtTableRawTopicName());
        // 构建连接表
        final KStream<String, JsonNode> join = leftTable.join(rightTable, (leftValue, rightValue) -> {
            if (leftValue == null || leftValue.isNull() || rightValue == null || rightValue.isNull()) {
                return null;
            } else {
                final ObjectNode leftObjValue = (ObjectNode) leftValue;
                final ObjectNode rightObjValue = (ObjectNode) rightValue;
                leftObjValue.setAll(rightObjValue);
                return leftObjValue;
            }
        }).toStream().map(convertDateAndCodeList(syncTable));
        join.print(Printed.toSysOut());// todo jyp 这行输出测试时使用
        join.to(syncTable.getEsIndexName(), Produced.with(Serdes.String(), mergedValueSerde, null));
    }

    /**
     * 转换没有扩展表的主表，需要转换的有日期和codeList的转换
     *
     * @param syncTable 所有没有扩展表的表
     * @see #convertDateAndCodeList(SyncTable)
     */
    private void convertNotHasExtTable(SyncTable syncTable) {
        final KStream<String, JsonNode> stream = builder.stream(syncTable.getMainTableRawTopicName(), Consumed.with(Serdes.String(), mergedValueSerde)).map(convertDateAndCodeList(syncTable));
        stream.print(Printed.toSysOut());// todo jyp 这行输出测试时使用
        stream.to(syncTable.getEsIndexName(), Produced.with(Serdes.String(), mergedValueSerde, null));
    }

    /**
     * 转换日期 和 codeList
     * <p>
     * 日期，制转换 Long 类型的时间戳，转换成没有只有日期格式的时间
     * <p>
     * codeList，如果编码在 codeList 中不存在则保留原值
     *
     * @param syncTable 当前转换的表
     * @return stream map 函数
     */
    private KeyValueMapper<String, JsonNode, KeyValue<? extends String, ? extends JsonNode>> convertDateAndCodeList(SyncTable syncTable) {
        return (key, value) -> {
            final ObjectNode objectNode = (ObjectNode) value;
            convertTimestamp(objectNode, syncTable.getDateFields());
            convertCodeList(objectNode, syncTable.getFieldAndCodeList());
            return new KeyValue<>(key, objectNode);
        };
    }

    /**
     * 转换日期
     *
     * @param objectNode 一条 Json 记录
     * @param dateFields 日期属性
     */
    private static void convertTimestamp(ObjectNode objectNode, Set<String> dateFields) {
        if (objectNode == null || objectNode.isNull() || dateFields == null || dateFields.isEmpty()) {
            return;
        }
        for (String dateField : dateFields) {
            final JsonNode jsonNode = objectNode.get(dateField);
            if (jsonNode != null && !jsonNode.isNull() && jsonNode.isLong()) {
                objectNode.replace(dateField, TextNode.valueOf(format.get().format(new Date(jsonNode.asLong()))));
            }
        }
    }

    /**
     * 转换 codeList
     *
     * @param objectNode       一条 Json 记录
     * @param fieldAndCodeList codeList 字段和对应值和名称的map
     */
    private static void convertCodeList(ObjectNode objectNode, Map<String, Map<String, String>> fieldAndCodeList) {
        if (objectNode == null || objectNode.isNull() || fieldAndCodeList == null || fieldAndCodeList.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : fieldAndCodeList.entrySet()) {
            final JsonNode jsonNode = objectNode.get(entry.getKey());
            if (jsonNode != null && !jsonNode.isNull() && entry.getValue() != null) {
                final String codeName = entry.getValue().get(jsonNode.asText());
                if (codeName != null && !codeName.isEmpty()) {
                    objectNode.replace(entry.getKey(), TextNode.valueOf(codeName));
                }
            }
        }
    }

    /**
     * 创建 kafka stream
     */
    private void createKafkaStreams() {
        this.kStreams = new KafkaStreams(builder.build(), kafkaStreamConfig());
        try {
            kStreams.start();
            log.info("启动流程序成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("启动流程序失败", e);
        }
    }

    /**
     * kafka stream 配置参数
     *
     * @return Properties
     */
    private Properties kafkaStreamConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);// kafka 集群
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-stream-merge-ext-table-test-3"); // 全局唯一
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);// 设置每 100 毫秒提交一次偏移量
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // key 序列化
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // value 序列化
        return props;
    }

    /**
     * 关闭流
     */
    public void closeStreamApplication() {
        if (kStreams != null) {
            kStreams.cleanUp();
            kStreams.close();
        }
    }

    /**
     * 获取流运行状态和元数据等信息
     */
    public void monitor() {
        if (kStreams == null) {
            log.info("流对象是 null");
        } else {
            log.info("流状态 {}", kStreams.state());
            log.info("流元数据 {}", kStreams.allMetadata());
            log.info("流指标数据 {}", kStreams.metrics());
        }
    }
}
