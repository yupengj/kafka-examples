# docker kafka 本地环境搭建(1个 zookeeper 1个kafka)

## docker 安装
> 需要安装 docker(Docker version 18.09.6) 和 docker-compose(docker-compose version 1.8.0)

- docker 是安装在 windown 系统的虚拟机中，虚拟机的系统是Ubuntu。参考地址 https://docs.docker.com/install/linux/docker-ce/ubuntu/ 在 nbuntu 中安装 docker
- docker-compose 安装。在终端执行下面命令。输出 docker-compose 版本标识安装成功

```shell
sudo curl -L https://github.com/docker/compose/releases/download/1.8.0/run.sh > /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

## kafka 环境搭建

### 需要资源
- docker 镜像 wurstmeister/zookeeper 最新版。使用的是 `zookeeper_version 3.4.13`
- docker 镜像 wurstmeister/kafka:2.12-2.3.0 。使用的是 `scala_version=2.12 kafka_version=2.3.0`
- docker-compose-single-broker.yml 容器编排文件

### kafka 开发环境搭建 （1个 zookeeper， 1个 kafka）

1. 下载 zookeeper 镜像：
```
docker pull wurstmeister/zookeeper
```
2. 下载 kafka 镜像
```
docker pull wurstmeister/kafka:2.12-2.3.0
```
3. 新建文件 docker-compose-single-broker.yml 复制[内容](https://raw.githubusercontent.com/yupengj/kafka-examples/master/docker-compose-single-broker.yml)到新建的文件中。修改 
`KAFKA_ADVERTISED_LISTENERS` 属性的ip，改为物理主机的ip(若果在虚拟机内就是虚拟机的ip)
4. 启动 zookeeper 和 kafka 容器。 注意要在 `docker-compose-single-broker.yml` 文件所在的位置在终端执行下面命令
```
docker-compose -f docker-compose-single-broker.yml up -d
```
> 注: 如果文件名称是 docker-compose.yml 则不需要 -f 参数。这里为了区分另一个多 kafka 节点的 docker-compose 文件(多节点下一篇介绍).

5. 查看是否启动成功,执行下面的命令会看到有两个容器已启动。一个是名称是 kafka_zookeeper_1 另一个名称是 kafka_kafka_1
```
docker-compose -f docker-compose-single-broker.yml ps
```

**到这里单机版的 kafka 已搭建完毕**

## 测试消息通道

### 控制台测试消息通道

1. 进入 docker 容器。在终端执行下面命令。 其中 `kafka_kafka_1` 是容器名称，可以通过 `docker ps` 命令查看到
```
docker exec -it kafka_kafka_1 /bin/bash
```
2. 进入容器中输入下面命令创建主题, 其中 `kafka_zookeeper_1:2181` 是 zookeeper 容器的名称和端口
```
kafka-topics.sh --create --topic test --zookeeper kafka_zookeeper_1:2181 --replication-factor 1 --partitions 1
```
3. 查看创建出来的主题
```
kafka-topics.sh --zookeeper kafka_zookeeper_1:2181 --describe --topic test
```
4. 创建消息生产者，执行命令后会切换到消息生产者的行控制台。
```
kafka-console-producer.sh --topic=test --broker-list kafka_kafka_1:9092
```
5. 在宿主机器中再次执行第一步的命令进入 kafka_kafka_1 docker 容器，然后执行下面命令创建消息消费者控制台。
```
kafka-console-consumer.sh --bootstrap-server kafka_kafka_1:9092 --from-beginning --topic test
```

在第4步和第5五步执行完会有两个控制台，第4步产生的控制台是生产者控制台，第5不产生的控制台是消费者控制台。现在就可以测试消息了，在生产者控制台随便输入内容然后回车，然后马上就会在消费者控制台接收到相同的消息

6. `ctrl+c` 关闭控制台

### kafka client API 测试消息通道

`192.168.1.4:9092` 是 docker-compose 文件中 `KAFKA_ADVERTISED_LISTENERS` 参数对应的ip和端口

- Producer
```
public class Producer {
	public static final String TOPIC = "test_1";
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.4:9092");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "clicet1");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(props);

		int messageNo = 1;
		while (true) {
			String messageStr = "Message_" + messageNo;
			try {
				kafkaProducer.send(new ProducerRecord<>(TOPIC, messageNo, messageStr)).get();
				System.out.println("Sent message: (" + messageNo + ", " + messageStr + ")");
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			++messageNo;
		}
	}
}
```

- Consumer
```
public class Consumer {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.4:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Collections.singletonList(Producer.TOPIC));
		while (true) {
			ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
			for (ConsumerRecord<Integer, String> record : records) {
				System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
			}
		}
	}
}
```

启动这两个类，Producer 发送消息， Consumer 接收消息

> 完整代码：https://github.com/yupengj/kafka-examples

## 最后 docker 常用命令

> 这个环境搭建只是把 kafka 环境启动成功，用 java 程序向 kafka 发送消息和接收消息成功。对于 kafka 的一配置都是采用默认的配置。
 
> docker-compose 命令只会操作 -f 参数指定文件中的容器

```sbtshell
# 启动容器，容器会进入 up 状态（如果容器已启动则停止容器后重新启动，重启可以重新加载配置）
# -d 是后台启动，如果去掉这个参数将看到每个容器的输出,执行 exit 命令时退出，所有容器都将停止
docker-compose -f docker-compose-single-broker.yml up -d 

# 停止容器，容器会进入 Exit 状态
docker-compose -f docker-compose-single-broker.yml stop 

# 启动容器，只能对 Exit 状态的容器进行启动
docker-compose -f docker-compose-single-broker.yml start 

# 启动容器，只能对Exit 状态的容器进行重启。不会重新加载配置
docker-compose -f docker-compose-single-broker.yml restart

# 删除容器，只能删除状态是 Exit 的容器
docker-compose -f docker-compose-single-broker.yml rm 

# 查看容器详细信息，包括环境变量的配置，容器的ip 等 
docker inspect <容器名称/容器id>

```
其他命令参考： https://docs.docker.com/glossary/?term=Compose