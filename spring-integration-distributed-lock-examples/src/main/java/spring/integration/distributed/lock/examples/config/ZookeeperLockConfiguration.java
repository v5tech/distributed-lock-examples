package spring.integration.distributed.lock.examples.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

/**
 * 嵌入式Zookeeper
 */
@Configuration
public class ZookeeperLockConfiguration {

    @Bean
    public CuratorFramework curatorFramework() {
        CuratorFramework curatorFramework = null;
        try {
            TestingServer testingServer = new TestingServer();
            // 重试策略，重试时间1s，重试3次
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            // 通过工厂创建 Curator
            curatorFramework = CuratorFrameworkFactory.newClient(testingServer.getConnectString(), retryPolicy);
            curatorFramework.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        return curatorFramework;
    }

    @Bean
    public ZookeeperLockRegistry zookeeperLockRegistry(CuratorFramework curatorFramework) {
        return new ZookeeperLockRegistry(curatorFramework);
    }

}
