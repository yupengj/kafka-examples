# kafka 技术分享与变更影响分析实现

## 目录

- kafka 是什么 `不细讲`
    - Kafka 诞生背景
    - Kafka 主要设计目标
    - 为什么选择 Kafka
- kafka 安装以及配置 `不细讲`
- kafka 核心概念
- Kafka Connector(连接器)
    - 作用
    - 安装及配置 `不细讲`
    - 演示 postgres 连接器数据同步
    - 如何自定义连接器
    - 自定义 dgraph 连接器(实现向 dgraph 数据库创建图节点)
    - 演示数据从 postgres 到 dgraph 同步
- 节点间的关系(边)
    - 边的建立和边的元数据
    - 用边的元数据指导边的生成
- 图的展现形式示例
- 一阶段规划的关系图
- 总结-变更影响架构图

## kafka 是什么

**Kafka 是一个高吞吐量、分布式的发布一订阅消息系统**

### Kafka 诞生背景
Kafka 是在 Linkedln 内部诞生的，在 Linkedln 内部使用的数据系统包括：
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

### Kafka 主要设计目标
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
除了支持多个生产者外， Kafka 也支持多个消费者从一个单独的消息流上读取数据，而且消费者之间互不影响。这与其他队列系统不同，其他队列系统的消息一旦被一个客户端读取，其他客户端就无法再读取它。另外，多个消费者可以组成一个群组，它们共享一个消息流，并保证整个群组对每个给定的消息只处理一次。

#### 基于磁盘的数据存储
Kafka 不仅支持多个消费者，还允许消费者非实时地读取消息，这要归功于 Kafka 的数据保留特性。消息被提交到磁盘，根据设置的保留规则进行保存。每个主题可以设置单独的保留规则，以便满足不同消费者的需求，各个主题可以保留不同数量的消息。消费者可能会因为处理速度慢或突发的流量高峰导致无陆及时读取消息，而持久化数据可以保证数据不会丢失。消费者可以在进行应用程序维护时离线一小段时间，而无需担心消息丢失或堵塞在生产者端。 消费者可以被关闭，但消息会继续保留在 Kafka 里。消费者可以从上次中断的地方继续处理消息。

#### 伸缩性
为了能够轻松处理大量数据， Kafka 从一开始就被设计成一个具有灵活伸缩性的系统。用户在开发阶段可以先使用单个 broker，再扩展到包含 3 个 broker 的小型开发集群，然后随着数据量不断增长，部署到生产环境的集群可能包含上百个 broker。对在线集群进行扩展丝毫不影响整体系统的可用性。也就是说，一个包含多个 broker 的集群，即使个别 broker失效，仍然可以持续地为客户提供服务。要提高集群的容错能力，需要配置较高的复制系数。

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

**生产者代码示例：**
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

消费者（Consumer）以拉取（pull）方式拉取数据，它是消费的客户端。在 Kafka 中每一个消费者都属于一个特定消费组（ConsumerGroup），我们可以为每个消费者指定一个消费组，以 groupld 代表消费组名称，通过 `group.id` 配置设置 。如果不指定消费组，则该消费者属于默认消费组 
test-consumer-group 。同时，每个消费者也有一个全局唯一的 id ， 通过配置项 `client.id` 指定，如果客户端没有指定消费者的 id, Kafka 会自动为该消费者生成一个全局唯一的 id，格式为$ {groupld}-${hostName}-${timestamp}-$ {UUID 前
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

### 演示简单的生产者和消费者程序

看代码,跑一下

## Kafka Connector(连接器)

### 作用

为了解决不同系统之间的数据同步，Kafka连接器用一个标准框架来解决这些问题。它把连接器需要解决的故障容错、分区扩展、偏移量管理、发送语义、管理和监控等问题抽象出来，这样开发和使用连接器就变得非常简单。用户只需要在配置文件中定义连接器，就可以将数据导人或导出 Kafka。

Kafka连接器只是一个组件，它需要和具体的数据源结合起来使用 。数据源可以分成两种：源数据源（ Data Source，也叫作“源系统”）和目标数据源（ Data Sink，也叫作“目标系统”）。Kafka连接器和源系统一起使用时，它会将源系统的数据导人到Kafka集群。 Kafka连接器和目标系统一起使用时，它会将Kafka集群的数据导人到目标系统

如下图所示中间部分是kafka连接器的内部组件，连接器使用源连接器（SourceConnector）从源系统导人数据到Kafka集群，使用目标连接器（Sink Connector）从Kafka集群导出数据到目标系统。源连接器会写数据到Kafka集群，内部会有生产者对象。目标连接器会消费Kafka集群的数据，内部会有消费者对象。

![kafka_connect1](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/kafka_connect1.png)

> 箭头代表数据流向

### 安装及配置
不细讲， ppt 中有连接器的安装与配置。kafka docker 环境安装文档中也有docker版的连接器安装与配置

### 演示 postgres 连接器数据同步

> 演示方案：使用公司 kafka, kafka-connect, postgres 环境(因为 topic-ui 可以看到 topic 中的数据 )
1. 查看所有的连接器 `curl localhost:8083/connector-plugins`
2. 查看活跃的连接器 `curl localhost:8083/connectors`
3. 看一下启动连接器的参数 .json 文件
4. 启动连接器 `curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @postgres.json` 
5. 在 postgres 数据库 mstdata schema 中新建一个 test 表。并执行新增、修改、删除操作
```sql
CREATE TABLE mstdata.md_test
(
    id   serial primary key,
    name text,
    age  int
);
insert into mstdata.md_test(name, age) VALUES ('zhangsan', 22);
insert into mstdata.md_test(name, age) VALUES ('lisi', 11);
update mstdata.md_test set age = age + 10 where name = 'zhangsan';
delete from mstdata.md_test where name = 'lisi';
delete from mstdata.md_test mt;
select * from mstdata.md_test mt;
```

6. 使用消费者客户端消费 test 表在kafka中产生的主题, 看数据变化, 看数据结构。

### 如何自定义连接器

在编写自定义连接器前，我们先看一下连接器主要类结构。如下图

![kafka_connect2](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/kafka_connect2.png)

> 1. Worker 由于每个任务都有一个专用线程，因此这个类主要只是一个容器。
> 2. WorkerConnector 是连接器的容器，主要负责管理连接器的生命周期(启动、停止等)
> 3. WorkerTask 主要处理单个任务的线程类
> 4. Kafka 连接器的主要组件是 Worker、Connector 和 Task, 这3 个类分别是Java进程、抽象类和接口。WorkerTask 及其实现类（WorkerSourceTask 和 WorkerSinkTask）是线程类。虽然 Task 接口不是一个线程类，但框架内置的 WorkerTask 则是一个线程类。内置的线程类会循环调用自定义实现的任务类，这样用户的任务实现类就不需要以线程的角度来处理数据同步。


- **Connector**
```java
public class DgraphSinkConnector extends SinkConnector {
	private Map<String, String> settings;
	
	@Override
	public void start(Map<String, String> props) {
		settings = props;
	}
	
	@Override
	public Class<? extends Task> taskClass() {
		return DgraphSinkTask.class;
	}
	
	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		return settings;
	}
	
	@Override
	public void stop() { }
	
	@Override
	public ConfigDef config() {
		return DgraphSinkConfig.CONFIG_DEF;
	}
	
	@Override
	public String version() {
		return Version.getVersion();
	}
}
```

- **task**
```java
public class DgraphSinkTask extends SinkTask {
	@Override
	public String version() {
		return Version.getVersion();
	}
	
	@Override
	public void start(Map<String, String> props) {}
	
	@Override
	public void put(Collection<SinkRecord> records) {
		dgraphSinkWriter.write(records);
	}

	@Override
	public void stop() {}
}
```


### 自定义 dgraph 连接器(实现向 dgraph 数据库创建图节点)

dgraph 连接器工作流程图

![kafka_connect_dgraph](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/kafka_connect_dgraph.png)
 
> - DgraphSinkTask 执行任务的类，其中 put 方法是连接器向 DgraphSinkTask 类put数据的入口
> - DgraphSinkWriter 接收 DgraphSinkTask 类传递的数据，进行序列化、写入缓冲类
> - BufferedRecords 数据缓存类，按批次向数据库保存
> - DgraphSinkMapping 负责修改或删除的数据映射 uid(图数据库唯一标记)
> - DgraphSinkClient 封装了与图数据库交互的方法
> - DgraphSinkConverter 复制序列化连接器 put 过来的数据

### 演示 postgres 数据库到 dgraph 数据同步

> 演示方案：使用公司 kafka, kafka-connect, postgres, dgraph 环境
1. 演示在 bom 测试系统新增，修改，删除变更单信息（可以是任何对象）, 查看数据是否同步到图数据库
2. 查看图数据中节点的 object_type 和 object_id
3. 查看产生的 uid 主题数据格式

## 节点间的关系(边)

### 边的建立和边的元数据
图数据库中的边基本可以由关系型数据库中的两种模式来表达
1. 主键与外键的参照引用
2. 字段与业务上唯一编码的弱引用

![dgraph_b1](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/dgraph_b1.png)

对上图中的连接线用自然语言描述如下：
```text
1. mstdata.md_product_node 通过 parent_id 与 mstdata.md_product_node 中 md_product_node_id 的参照构成了父子关系；
2. mstdata.md_product_node 通过 part_id 与 mstdata.md_material 中 md_material_id 的参照构成了实例化关系；
3. bommgmt.bm_part_assembly 通过 master_part_id 与 mstdata.md_material 中 md_material_id 的参照构成了构成关系；
4. bommgmt.bm_part_assembly 通过 sub_part_id 与 mstdata.md_material 中 md_material_id 的参照构成了使用关系；
5. bommgmt.bm_part_assembly 通过 usage_value 与 mstdata.md_feature 中 feature_code 的弱引用构成了使用关系；
6. ... ... ... ...
```

构建边的元数据，边的元数据是可配置的。数据格式如下图：
![dgraph_b2](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/dgraph_b2.png)

### 用边的元数据指导边的生成

1. 生成边的主题

![dgraph_b3](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/dgraph_b3.png)

> 自定义连接器中对每一条记录均生成一个节点，同时设置object_type与object_id属性，object_type对应数据库表名，object_id为主键ID，因此上述生成的主题已经表达了节点之间的关系。
  弱引用字段不从图数据库本身查询是因为相关联节点有可能在图数据库中并不存在，而只有作为源头的关系型数据库才是最完整的。

2. 根据边的主题创建 dgraph 中节点的边

![dgraph_b4](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/dgraph_b4.png)

> 1. 自定义连接器中会向一个中间主题记录object_type、object_id与数据库内部UID的映射关系，应用程序从该主题消费后会保存到内存中。
> 2. 优先从内存中获取源节点与目标节点的UID，获取不到时会尝试从图数据库直接获取，并更新内存。
> 3. 经过上述两步，如果源节点与目标节点有任意一个不存在UID，表示此节点还未创建，此时边不能创建。
> 4. 若边不能创建，需要把记录写回到主题末端，以便消费者程序可以再次读取。

## 图的展现形式示例

![dgraph_t1](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/dgraph_t1.png)

## 一阶段规划的关系图

![dgraph_t2](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/dgraph_t2.png)


## 总结-变更影响架构图

![bgyxfx](https://raw.githubusercontent.com/yupengj/kafka-examples/master/doc/images/bgyxfx.png)


------
## 参考
- Kafka docker 环境安装和配置：https://github.com/yupengj/kafka-examples/blob/master/README.md
- Kafka 示例代码仓库： https://github.com/yupengj/kafka-examples
- Kafka 官方文档：http://kafka.apache.org/documentation
- Dgraph 连接器代码仓库：https://git.gantcloud.com/ibom/kafka-connect-dgraph
- Kafka 连接器 hub ： https://www.confluent.io/hub/
- 《Kafka权威指南》
- 《Kafka技术内幕-图文详解Kafka源码设计与实现》
- 《Kafka源码解析与实战》
