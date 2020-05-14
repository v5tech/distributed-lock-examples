package net.ameizi.distributed.lock.zookeeper.example.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化 Zookeeper Curator 客户端
 */
@Configuration
public class ZookeeperConfig {

    /**
     * 创建 CuratorFramework 对象并连接 Zookeeper
     *
     * @param zookeeperProperties 从 Spring 容器载入 ZookeeperProperties Bean 对象，读取连接 ZK 的参数
     * @return CuratorFramework
     */
    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
        // return CuratorFrameworkFactory.newClient(
        //         zookeeperProperties.getAddress(),
        //         zookeeperProperties.getSessionTimeoutMs(),
        //         zookeeperProperties.getConnectionTimeoutMs(),
        //         new RetryNTimes(zookeeperProperties.getRetryCount(),
        //                 zookeeperProperties.getElapsedTimeMs()));
        String connectString = null;
        try {
            connectString = new TestingServer().getConnectString();
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            return CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
