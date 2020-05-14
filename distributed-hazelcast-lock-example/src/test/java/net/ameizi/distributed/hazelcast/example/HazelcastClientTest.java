package net.ameizi.distributed.hazelcast.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class HazelcastClientTest {

    public static void main(String[] args) {

        ClientConfig clientConfig = new ClientConfig();
        //集群组名称
        clientConfig.getGroupConfig().setName("dev");
        //节点地址
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701", "127.0.0.1:5702", "127.0.0.1:5703");
        //客户端
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Object, Object> instruments = client.getMap("instruments");

        //并发测试
        Runnable runnable = () -> {
            long total = 10000;
            for (int i = 0; i < total; i++) {
                //插入缓存
                instruments.put(i, "user"+i);
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int threadNum = 10;
        for (int i = 0; i < threadNum; i++) {
            executorService.submit(runnable);
        }

        // client.getLifecycleService().shutdown();
        // executorService.shutdown();
    }
}
