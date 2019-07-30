# kafka-examples


## [单机 kafka 环境搭建](doc/docker%20kafka%20本地环境搭建(1个%20zookeeper%201个kafka).md)


## [集群 kafka 环境搭建](doc/docker%20kafka%20集群环境搭建(3个%20zookeeper%203个kafka).md)


## 后续修改

- [ ] 单机版和集群版连接器的使用
- [ ] 把 kafka-manager 加到公司集群


查询所有连接器
curl http://localhost:8083/connector-plugins

启动连接器
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @connect-plugins/postgres.json
