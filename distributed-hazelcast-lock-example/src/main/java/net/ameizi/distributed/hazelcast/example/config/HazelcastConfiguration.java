package net.ameizi.distributed.hazelcast.example.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientFlakeIdGeneratorConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FlakeIdGeneratorConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.cp.FencedLockConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * hazelcast集群 server 注册，程序启动后启动三个hazelcast节点并自动发现注册为集群
 */
@Configuration
public class HazelcastConfiguration {

    public static final String ID_GENERATOR = "idGenerator";

    @Bean
    public FlakeIdGeneratorConfig flakeIdGeneratorConfig(){
        FlakeIdGeneratorConfig idGeneratorConfig = new FlakeIdGeneratorConfig(ID_GENERATOR);
        idGeneratorConfig.setPrefetchCount(10)
                .setPrefetchValidityMillis(MINUTES.toMillis(10));
        return idGeneratorConfig;
    }

    @Bean
    public ClientFlakeIdGeneratorConfig clientFlakeIdGeneratorConfig(){
        ClientFlakeIdGeneratorConfig idGeneratorConfig = new ClientFlakeIdGeneratorConfig(ID_GENERATOR);
        idGeneratorConfig.setPrefetchCount(10)
                .setPrefetchValidityMillis(MINUTES.toMillis(10));
        return idGeneratorConfig;
    }

    /**
     * 本地启动嵌入式hazelcast集群配置，会在本地启动hazelcast服务器并组好集群
     * @return
     */
    @Bean
    public Config hazelCastConfig() {
        // 设置集群管理中心
        ManagementCenterConfig centerConfig = new ManagementCenterConfig();
        centerConfig.setUrl("http://localhost:8080/hazelcast-mancenter");
        centerConfig.setEnabled(true);

        FencedLockConfig fencedLockConfig = new FencedLockConfig();
        // 不可重入
        fencedLockConfig.disableReentrancy();

        Config config = new Config();
        config.getCPSubsystemConfig()
                .setCPMemberCount(3);
                // 设置为不可重入锁
                // .addLockConfig(fencedLockConfig);

        config.setManagementCenterConfig(centerConfig);
        config.addFlakeIdGeneratorConfig(flakeIdGeneratorConfig());

        return config;
    }

    /**
     * 客户端配置，连接远程hazelcast服务器集群
     * @return
     */
    @Bean
    public ClientConfig clientConfig(){
        ClientConfig clientConfig = new ClientConfig();
        //集群组名称
        clientConfig.getGroupConfig().setName("dev");
        //节点地址
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701", "127.0.0.1:5702", "127.0.0.1:5703");
        clientConfig.addFlakeIdGeneratorConfig(clientFlakeIdGeneratorConfig());
        return clientConfig;
    }


    @Bean
    public HazelcastInstance hazelcastInstance1(){
        // return Hazelcast.newHazelcastInstance(hazelCastConfig()); // 本地启动hazelcast服务器
        return HazelcastClient.newHazelcastClient(clientConfig()); // 连接远程hazelcast服务器
    }

    @Bean
    public HazelcastInstance hazelcastInstance2(){
        // return Hazelcast.newHazelcastInstance(hazelCastConfig()); // 本地启动hazelcast服务器
        return HazelcastClient.newHazelcastClient(clientConfig()); // 连接远程hazelcast服务器
    }

    @Bean
    public HazelcastInstance hazelcastInstance3(){
        // return Hazelcast.newHazelcastInstance(hazelCastConfig()); // 本地启动hazelcast服务器
        return HazelcastClient.newHazelcastClient(clientConfig()); // 连接远程hazelcast服务器
    }

}
