# kafka 技术分享

## 目录

- kafka 是什么 `不细讲`
    - Kafka 诞生背景
    - Kafka 的主要设计目标
    - 为什么选择 Kafka
- kafka 安装以及配置 `不细讲`
- kafka 核心概念
- Kafka Connector(连接器)
    - 作用
    - 原理
    - 安装及配置 `不细讲`
    - 演示 postgres 连接器数据同步
    - 演示数据从 postgres 到 dgraph 同步
- 目前使用 kafka 做了什么


## kafka 是什么

**Kafka 是一个高吞吐量、分布式的发布一订阅消息系统**

### Kafka 诞生背景
Kafka 是在 Linkedln 内部诞生的，Linkedln 使用的数据系统包括：
- 全文搜索
- Social Graph （社会图谱）
- Voldemort （键值存储）
- Espresso （文档存储）
- 推荐引擎
- OLAP （查询引擎）
- Hadoop
- Teradata （数据仓库）
- Ingraphs （监控图表和指标服务）

上述专用的分布式系统都需要经过数据源来获取数据，同时有些系统还会产生数据，作为其他系统的数据源。如果为每个数据源和目标构建自定义的数据加载。情况就会变成下图的样子：

![kafka_bj1](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/kafka_bj1.png)

我们需要尽量将每个生产者、消费者与数据源隔离。理想情况下，生产者或消费者应该只与一个数据源单独集成，这样就能访问到所有数据。根据这个思路增加一个新的数据系统：
- 作为数据来源或者数据目的地 。
- 集成工作只需要连接这个新系统到一个单独的管道，而无须连接到每个数据的生产者和消费者。如下图：

![kafka_bj2](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/kafka_bj2.png)

### Kafka 的主要设计目标
Kafka 作为一种分布式的 、基于发布／订阅的消息系统，其主要设计目标如下：
- 以时间复杂度为 0 ( I ）的方式提供消息持久化能力，即使对 TB 级以上的数据也能保证常数时间的访问性能 。
- 高吞吐率 ，即使在非常廉价的商用机器上也能做到单机支持每秒 lOOK 条消息的传输 。
- 支持 Kafka Server 间的消息分区，及分布式消费，同时保证每个分区内的消息顺序传输 。
- 支持离线数据处理和实时数据处理 。
- 支持在线水平扩展 。

### 为什么选择 Kafka
基于发布与订阅的消息系统那么多，为什么 Kafka 会是一个更好的选择呢？

#### 多个生产者
Kafka 可以无缝地支持多个生产者，不管客户端在使用单个主题还是多个主题。所以它很适合用来从多个前端系统收集数据，并以统一的格式对外提供数据。例如， 一个包含了多个微服务的网站，可以为页面视图创建一个单独的主题，所有服务都以相同的消息格式向该主题写入数据。消费者应用程序会获得统一的页面视图，而无需协调来自不同生产者的数据流。

#### 多个消费者
除了支持多个生产者外， Kafka 也支持多个消费者从一个单独的消息流上读取数据，而且消费者之间直不影响。这与其他队列系统不同，其他队列系统的消息一旦被一个客户端读取，其他客户端就无法再读取它。另外，多个消费者可以组成一个群组，它们共享一个消息流，并保证整个群组对每个给定的消息只处理一次。

#### 基于磁盘的数据存储
Kafka 不仅支持多个消费者，还允许消费者非实时地读取消息，这要归功于 Kafka 的数据保留特性。消息被提交到磁盘，根据设置的保留规则进行保存。每个主题可以设置单独的保留规则，以便满足不同消费者的需求，各个主题可以保留不同数量的消息。消费者可能会因为处理速度慢或突发的流量高峰导致无陆及时读取消息，而持久化数据可以保证数据不会丢失。消费者可以在进行应用程序维护时离线一小段时间，而无需担心消息丢失或堵塞在生产者端。 消费者可以被关闭，但消息会继续保留在 Kafka 里。消费者可以从上次中断的地方继续处理消息。

#### 伸缩性
为了能够轻松处理大量数据， Kafka 从一开始就被设计成一个具有灵活伸缩性的系统。用户在开发阶段可以先使用单个 broker，再扩展到包含 3 个 broker 的小型开发集群，然后随着数据盐不断增长，部署到生产环境的集群可能包含上百个 broker。对在线集群进行扩展丝毫不影响整体系统的可用性。也就是说，一个包含多个 broker 的集群，即使个别 broker失效，仍然可以持续地为客户提供服务。要提高集群的容错能力，需要配置较高的复制系数。

#### 高性能
上面提到的所有特性，让 Kafka 成为了一个高性能的发布与订阅消息系统。通过横向扩展生产者、消费者和 broker, Kafka 可以轻松处理巨大的消息流。在处理大量数据的同时，它还能保证亚秒级的消息延迟。


## kafka 安装以及配置

- 这里不详细说明安装和配置，请参考ppt
- kafka docker 环境安装和配置参考[这里](https://github.com/yupengj/kafka-examples/blob/master/README.md)


## kafka 核心概念

### Broker(Kafka 服务器)

一台 Kafka 服务器就是一个 Broker，一个集群由多个 Broker 组成， 一个 Broker可以容纳多个 Topic, Broker 和 Broker 之间没有 Master 和 Standby 的概念 ， 它们之间的地位基本是平等的。

### Topic和Partition(主题和分区)

kafka 的消息通过主题进行分类。主题就好比数据库的表，或者文件系统里的文件夹。主题可以被分为若干个分区,一个分区就是一个提交日志,如下所示：

![log_anatomy](http://kafka.apache.org/23/images/log_anatomy.png)

消息以追加的方式写入分区，然后以先入先出的顺序读取。分区可以分布在不同的 broker 上，也就是说， 一个主题可以横跨多个服务器，以此来提供比
单个服务器更强大的性能。

分区中的记录每个都被分配一个称为**偏移(Offset)** 的顺序ID号，它唯一地标识分区中的每个记录。
Kafka集群持久地保留所有已发布的记录无论它们是否已被消耗（可以配置保留时间）

### Replica (副本)

Topic 的 Partition 含有 N 个 Replica, N 为副本因子。其中一个 Replica 为 Leader，其他都为 Follower, Leader 处理 Partition 的所有读写请求，与此同时，Follower 会定期地去同步 Leader 上的数据 。

### Message(数据)

消息，是通信的基本单位。每个 Producer（生产者） 可以向一个或多个 Topic （主题）发布一些消息 。

### Producer(生产者)

消息生产者，即将消息发布到指定的 Topic 中，同时 Producer 也能决定此消息所属的 Partition ：比如基于 Round-Robin （轮询）方式或者 Hash （哈希）方式等一些算法 。
也可以自定义分区器，继承 `org.apache.kafka.clients.producer.internals.DefaultPartitioner` 类或者实现 `org.apache.kafka.clients.producer.Partitioner` 接口

**生产者发送消息主要步骤：**
![producer_send](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/producer_send.png)

**生成者代码示例：**
```java
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

		boolean isAsync = false;
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
```

**异步发送消息回调代码示例：**

```java
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
```

### Consumer(消费者)

消费者（Consumer）以拉取（pull）方式拉取数据，它是消费的客户端。在 Kafka 中每一个消费者都属于一个特定消费组（ConsumerGroup），我们可以为每个消费者指定一个消费组，以 groupld 代表消费组名称，通过 group.id 配置设置 。如果不指定消费组，则该消费者属于默认消费组 
test-consumer-group 。同时，每个消费者也有一个全局唯一的 id ， 通过配置项 client.id 指定，如果客户端没有指定消费者的 id, Kafka 会自动为该消费者生成一个全局唯一的 id，格式为$ {groupld}-${hostName}-${timestamp}-$ {UUID 前
 8 位字符｝。同一个主题的一条消息只能被同一个消费组下某一个消费者消费，但不同消费组的消费者可同时消费该消息。 消费组是 Kafka用来实现对一个主题消息进行广播和单播的手段，实现消息广播只需指定各消费者均属于不同的消费组，消息单播则只需让各消费者属于同一个消费组。

**消费者代码示例：**
```java
public class Consumer {
	private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.26:9092");// kafka 集群
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "jiangyp_group1"); // 消费组id
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "jiangyp_client1"); // 消费客户端id
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 从消息开始的位置读
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 不自动管理偏移量
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Collections.singletonList(Producer.TOPIC));
		long start = System.currentTimeMillis();
		int count = 0, num = 10;// 有10次拉取的数据记录为 0 时 结束轮询
		while (true) {
			ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
			for (ConsumerRecord<String, String> record : records) {
				LOG.info("Received message topic {} : ({}, {}) at partition {} offset {}", record.topic(), record.key(), record.value(), record.partition(),
						record.offset());
			}
			count += records.count(); // 记录累加
			if (records.count() == 0) {
				num--;
				if (num < 0) {
					break;
				}
			}
		}
		LOG.info("poll topic {} size {} time {} ms", Producer.TOPIC, count, System.currentTimeMillis() - start);
	}
}
```

### kafka 基本结构图：
![zk_kafka_producer_consumer](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/zk_kafka_producer_consumer.png)

### kafka 4个核心API
![kafka-apis](http://kafka.apache.org/23/images/kafka-apis.png)


## Kafka Connector(连接器)

### 作用
### 原理
### 安装及配置
不细讲， ppt 中有连接器的安装与配置。kafka docker 环境安装文档中也有docker版的连接器安装与配置

### 演示 postgres 连接器数据同步
### 如何自定义连接器
### 演示 postgres 数据库到 dgraph 数据同步

## 目前使用 kafka 做了什么

画一个目前各个系统直接的关系和数据的流向。

------
## 参考


------
问题
1. kafka 的分区信息存储在哪里？


```json
1.简介Kafka
kafka是什么？
一些核心概念，如生产者、消费者、Broker、主题、分区
一个简单的生产者和消费者（演示）
安装、配置、关键参数（不讲，自己看PPT）
提供一个docker运行环境（不详细讲）
2.简介
kafka connector的作用
如何从头配置一个kafka connector
对exactly_once的支持
演示实际的数据同步与增删改
3.自定义Connector
如何实现自己的connector（简介思想）
演示到图数据库的同步
4.变更影响分析
如何利用图节点的边做变更的影响分析？
构建边程序的设计思路
演示边的创建
一些关键的业务场景探讨
```