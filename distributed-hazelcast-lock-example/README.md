# Hazelcast

Docker部署Hazelcast

```bash
$ docker run -e JAVA_OPTS="-Xms512M -Xmx1024M" -p 5701:5701 hazelcast/hazelcast:3.12.7
$ docker run -e JAVA_OPTS="-Xms512M -Xmx1024M" -p 5702:5701 hazelcast/hazelcast:3.12.7
$ docker run -e JAVA_OPTS="-Xms512M -Xmx1024M" -p 5703:5701 hazelcast/hazelcast:3.12.7
```

正确姿势应该是这样的

```bash
$ docker run -d -e JAVA_OPTS="-Dhazelcast.local.publicAddress=172.24.202.128:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx256M" -e MANCENTER_URL="http://172.24.202.128:8080/hazelcast-mancenter" -p 5701:5701 hazelcast/hazelcast:3.12.7
$ docker run -d -e JAVA_OPTS="-Dhazelcast.local.publicAddress=172.24.202.128:5702 -Dhazelcast.rest.enabled=true -Xms128M -Xmx256M" -e MANCENTER_URL="http://172.24.202.128:8080/hazelcast-mancenter" -p 5702:5701 hazelcast/hazelcast:3.12.7
$ docker run -d -e JAVA_OPTS="-Dhazelcast.local.publicAddress=172.24.202.128:5703 -Dhazelcast.rest.enabled=true -Xms128M -Xmx256M" -e MANCENTER_URL="http://172.24.202.128:8080/hazelcast-mancenter" -p 5703:5701 hazelcast/hazelcast:3.12.7
```

Hazelcast management-center

安装配置管理节点，监控和实时查看缓存情况

```bash
$ docker run 
    -m 512m \
    -p 8080:8080 \
    --rm \
    hazelcast/management-center:3.12.9
```

### Hazelcast镜像单节点部署

```bash
docker run -d -e JAVA_OPTS="-Dhazelcast.local.publicAddress=172.24.202.128:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx256M" -p 5701:5701 hazelcast/hazelcast:3.12.7
```

注意:

* hazelcast.rest.enabled=true，需要开启，不然管理节点连不上
* docker需要后台启动服务-d，内部端口为5701
* 最好指定publicAddress且需要设置JVM大小

### Hazelcast镜像多节点multicast集群部署

节点1：

```bash
docker run -d -e JAVA_OPTS="-Dhazelcast.local.publicAddress=172.24.202.128:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx256M" -e MANCENTER_URL="http://127.0.0.1:8080/hazelcast-mancenter" -p 5701:5701 hazelcast/hazelcast:3.12.7
```

节点2：
```bash
docker run -d -e JAVA_OPTS="-Dhazelcast.local.publicAddress=172.24.202.129:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx256M" -e MANCENTER_URL="http://127.0.0.1:8080/hazelcast-mancenter" -p 5701:5701 hazelcast/hazelcast:3.12.7
```

注意：

* 指定MANCENTER_URL管理节点地址
* multicast广播必须为同一台集群，因为docker下广播必须本机容器才能连接

### Hazelcast镜像多节点TCP-IP集群部署

hazelcast.xml配置：

```xml
<management-center enabled="true">http://127.0.0.1:8080/hazelcast-mancenter</management-center>
<port auto-increment="true" port-count="10">5701</port>
<multicast enabled="false">
    <multicast-group>224.2.2.3</multicast-group>
    <multicast-port>54327</multicast-port>
</multicast>
<tcp-ip enabled="true">
    <interface>172.24.202.120-129</interface>
    <member-list>
        <member>172.24.202.128</member>
        <member>172.24.202.129</member>
    </member-list>
</tcp-ip>
```

节点1

```bash
docker run -d -e JAVA_OPTS="-Dhazelcast.config=/opt/hazelcast/config_ext/hazelcast.xml -Dhazelcast.local.publicAddress=172.24.202.128:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx512M" -v ./config:/opt/hazelcast/config_ext -p 5701:5701 hazelcast/hazelcast:3.12.7
```

节点2

```bash
docker run -d -e JAVA_OPTS="-Dhazelcast.config=/opt/hazelcast/config_ext/hazelcast.xml -Dhazelcast.local.publicAddress=172.24.202.129:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx512M" -v ./config:/opt/hazelcast/config_ext -p 5701:5701 hazelcast/hazelcast:3.12.7
```

输出

```
Members {size:2, ver:2} [
	Member [172.24.202.128]:5701 - cd40d155-d993-46a5-b07c-19f001c71f3c
	Member [172.24.202.129]:5701 - 34d0798c-37d0-42e8-88f0-1268eab9a90a this
]
```

注意：

* 端口自增限制为10，即每个机器端口限制为5701-5711，以提高发现效率
* multicast关闭，tcp-ip开启
* 限制interface，指定ip范围
* 指定member-list，指定集群成员
* 指定宿主机配置文件地址


快速在本地启动一个三个节点的hazelcast集群

```bash
docker run \
-d \
-e \
JAVA_OPTS="-Dhazelcast.config=/opt/hazelcast/config_ext/hazelcast.xml -Dhazelcast.local.publicAddress=172.24.202.128:5701 -Dhazelcast.rest.enabled=true -Xms128M -Xmx512M" \
-v $(pwd)/hazelcast.xml:/opt/hazelcast/config_ext/hazelcast.xml \
-p 5701:5701 \
hazelcast/hazelcast:3.12.7
```


```bash
docker run \
-d \
-e \
JAVA_OPTS="-Dhazelcast.config=/opt/hazelcast/config_ext/hazelcast.xml -Dhazelcast.local.publicAddress=172.24.202.128:5702 -Dhazelcast.rest.enabled=true -Xms128M -Xmx512M" \
-v $(pwd)/hazelcast.xml:/opt/hazelcast/config_ext/hazelcast.xml \
-p 5702:5701 \
hazelcast/hazelcast:3.12.7
```


```bash
docker run \
-d \
-e \
JAVA_OPTS="-Dhazelcast.config=/opt/hazelcast/config_ext/hazelcast.xml -Dhazelcast.local.publicAddress=172.24.202.128:5703 -Dhazelcast.rest.enabled=true -Xms128M -Xmx512M" \
-v $(pwd)/hazelcast.xml:/opt/hazelcast/config_ext/hazelcast.xml \
-p 5703:5701 \
hazelcast/hazelcast:3.12.7
```


配置

登录management-center

打开地址：http://127.0.0.1:8080/hazelcast-mancenter

第一次设置初始化账户和密码，密码有格式要求

添加成员节点，即Change URL

输入Cluster Name and Password，即集群名和集群密码

输入Server UR，为Management Center URL，即管理系统地址；例如http://127.0.0.1:8080/hazelcast-mancenter

测试连接

引入包pom.xml

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
    <version>3.12.7</version>
</dependency>
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-spring</artifactId>
    <version>3.12.7</version>
</dependency>
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-client</artifactId>
    <version>3.12.7</version>
</dependency>
```

client代码

```java
@Slf4j
public class HazelcastTest {
    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        //集群组名称
        clientConfig.getGroupConfig().setName("dev");
        //节点地址
        clientConfig.getNetworkConfig().addAddress("172.24.202.128:5701", "172.24.202.129:5701");
        //客户端
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
    }
}
```