# docker kafka 集群环境搭建(3个 zookeeper 3个kafka)

## 单个物理机集群

与单节点 kafka 模式唯一不同的是 docker-compose 文件不同。多节点 kafka [docker-compose.yml](https://raw.githubusercontent.com/yupengj/kafka-examples/master/docker-compose.yml)
执行方式与单节点的一样，在文件所在位置执行 `docker-compose up -d` 命令即可。使用 `docker-compose ps` 命令可以看到有6个容器，3个 zookeeper 3个kafka。

- 进入 zookeeper 查看 kafka 集群信息，与进入 kafka 容器命令相同
```sbtshell
docker exec -it kafka_zookeeper1_1 /bin/bash
```

- 启动 zk 客户端,先使用 `cd /opt/zookeeper-3.4.13/bin/` 命令进入 bin 目录下,然后执行 `./zkCli.sh` 命令启动客户端

- 




## 多个物理机集群