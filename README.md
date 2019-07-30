# kafka 本地开发环境搭建

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

1. 下载 [docker-compose.yml](docker-compose.yml) 文件
2. 修改 `KAFKA_ADVERTISED_LISTENERS` 属性的ip，改为物理主机的ip(若果在虚拟机内就是虚拟机的ip, 本地直接写localhost)
3. 在 docker-compose.yml 所在位置新建文件夹 connect-plugins， 并下载 postgres 连接器插件 [debezium-connector-postgres](https://github.com/yupengj/kafka-examples/raw/master/connect-plugins/debezium-connector-postgres-0.9.5.Final-plugin.tar.gz) 把解压后的所有jar复制到 connect-plugins 文件夹中。把 [postgres
.json](/connect-plugins/postgres.json)
文件也一并复制到 connect-plugins 文件夹下。后面启动连接器时使用
4. 在 `docker-compose.yml.yml` 文件所在的位置执行命令 `docker-compose up -d` 会自动下载镜像
5. 继续执行 `docker-compose ps` 查看所有容器是否为 Up(代表已启动) 状态
6. 启动 postgres 连接器执行命令 `curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @connect-plugins/postgres.json`

## 测试消息通道

- 控制台测试消息通道,网上找了一个只要修改 zookeeper 和 kafka 名称即可。 https://blog.csdn.net/qq_41665356/article/details/80376075
- kafka client API 测试消息通道,启动 [Producer类](/src/main/java/org/jiangyp/kafka/Producer.java) 和 [Consumer类](/src/main/java/org/jiangyp/kafka/Consumer.java)

## 常用命令

### docker 常用命令

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

### kafka connect 常用命令
```sbtshell
# 启动连接器
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @connect-plugins/postgres.json

# 重启连接器 restart
curl -X POST localhost:8083/connectors/ibom-connector/restart

# 删除连接器
curl -X DELETE localhost:8083/connectors/ibom-connector

# 查询所有连接器
curl localhost/connector-plugins

# 查看现在活跃的连接器
curl localhost/connectors

# 查看连接器状态
curl localhost/connectors/ibom-connector/status

# 查看连接器配置
curl localhost/connectors/ibom-connector/config
```

## 修改记录
- [x] 修改 kafka , zookeeper 镜像，统一使用 confluentin 和公司 kafka 集群使用相同的镜像
- [x] 增加带有 wal2json 插件的 postgresql 数据库容器
- [x] 增加连接器可以读取 postgresql 数据到 kafka 中
- [ ] 把 kafka-manager 加到公司集群


## 参考地址
- docker-compose 命令文档: https://docs.docker.com/glossary/?term=Compose
- kafka-connect 文档: http://kafka.apache.org/documentation/#connect
- kafka-connect-postgres 连接器: https://www.confluent.io/hub/debezium/debezium-connector-postgresql
- confluentinc/cp-kafka-connect 增加连接器说明文档: https://docs.confluent.io/current/installation/docker/development.html#adding-connectors-to-images


恢复数据库
pg_restore -h 192.168.1.6 -p 5432 -d postgres -U postgres D:\software\PostgreSQL\11\backup\20190730.backup




----
## [集群 kafka 环境搭建](doc/docker%20kafka%20集群环境搭建(3个%20zookeeper%203个kafka).md)
