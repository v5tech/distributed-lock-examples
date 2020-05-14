# distributed-lock-examples

史上最全的分布式锁合辑(我们不造轮子，只需用好轮子！)

```
distributed-lock-examples
├── README.md
├── Screenshots
│   ├── hazelcast_management_center.png
│   ├── hazelcast_management_center_idgenerator.png
│   └── hazelcast_management_center_map.png
├── distributed-consul-lock-example
│   ├── distributed-consul-lock-example.iml
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── net
│           │       └── ameizi
│           │           └── distributed
│           │               └── consul
│           │                   └── example
│           │                       └── DistributedConsulLockExampleApplication.java
│           └── resources
│               └── application.properties
├── distributed-hazelcast-lock-example
│   ├── README.md
│   ├── distributed-hazelcast-lock-example.iml
│   ├── docker-compose.yaml
│   ├── hazelcast.xml
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── net
│       │   │       └── ameizi
│       │   │           └── distributed
│       │   │               └── hazelcast
│       │   │                   └── example
│       │   │                       ├── DistributedHazelcastLockExampleApplication.java
│       │   │                       ├── LockController.java
│       │   │                       └── config
│       │   │                           └── HazelcastConfiguration.java
│       │   └── resources
│       │       └── application.properties
│       └── test
│           ├── java
│           │   └── net
│           │       └── ameizi
│           │           └── distributed
│           │               └── hazelcast
│           │                   └── example
│           │                       ├── FlakeIdGeneratorSample.java
│           │                       └── HazelcastClientTest.java
│           └── resources
├── distributed-redis-lock-example
│   ├── distributed-redis-lock-example.iml
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── net
│           │       └── ameizi
│           │           └── distributed
│           │               └── lock
│           │                   └── redis
│           │                       └── example
│           │                           └── DistributedRedisLockExampleApplication.java
│           └── resources
│               ├── application.properties
│               ├── redisson-cluster.yaml
│               ├── redisson-master-slave.yaml
│               ├── redisson-sentinel.yaml
│               └── redisson-single.yaml
├── distributed-zookeeper-lock-example
│   ├── distributed-zookeeper-lock-example.iml
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── net
│       │   │       └── ameizi
│       │   │           └── distributed
│       │   │               └── lock
│       │   │                   └── zookeeper
│       │   │                       └── example
│       │   │                           ├── DistributedZookeeperLockExampleApplication.java
│       │   │                           └── config
│       │   │                               ├── ZookeeperConfig.java
│       │   │                               └── ZookeeperProperties.java
│       │   └── resources
│       │       ├── META-INF
│       │       └── application.properties
│       └── test
│           ├── java
│           │   └── net
│           │       └── ameizi
│           │           └── distributed
│           │               └── lock
│           │                   └── zookeeper
│           │                       └── example
│           │                           ├── BlockingLockTest.java
│           │                           ├── DistributedLockTest.java
│           │                           └── NonBlockingLockTest.java
│           └── resources
├── pom.xml
└── spring-integration-distributed-lock-examples
    ├── pom.xml
    ├── spring-integration-distributed-lock-examples.iml
    └── src
        └── main
            ├── java
            │   └── spring
            │       └── integration
            │           └── distributed
            │               └── lock
            │                   └── examples
            │                       ├── DistributedLockRegistryApplication.java
            │                       └── config
            │                           ├── JdbcConfiguration.java
            │                           ├── RedisLockConfiguration.java
            │                           └── ZookeeperLockConfiguration.java
            └── resources
                └── application.properties
```

注：为学习实验方便，示例代码中的zookeeper、redis、jdbc、hazelcast均使用本地嵌入式，实际应用应使用独立部署的服务。

* distributed-hazelcast-lock-example(hazelcast实现分布式锁及分布式id)

修改`docker-compose.yaml`和`hazelcast.xml`中宿主机的IP 地址后执行`docker-compose up -d`即可启动一个三个节点的hazelcast集群。

浏览地址栏访问`http://localhost:8080/hazelcast-mancenter`可访问`hazelcast`的控制台

三台子节点信息

![](Screenshots/hazelcast_management_center.png)

分布式 id

![](Screenshots/hazelcast_management_center_idgenerator.png)

map

![](Screenshots/hazelcast_management_center_map.png)

* distributed-redis-lock-example(redis 实现分布式锁)

* distributed-zookeeper-lock-example(zookeeper 实现分布式锁)

* spring-integration-distributed-lock-examples(spring-integration 实现分布式锁) 分别使用ZookeeperLockRegistry、RedisLockRegistry、JdbcLockRegistry实现分布式锁

