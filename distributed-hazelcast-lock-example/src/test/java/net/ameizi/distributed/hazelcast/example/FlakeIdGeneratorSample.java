package net.ameizi.distributed.hazelcast.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientFlakeIdGeneratorConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

import static java.util.concurrent.TimeUnit.MINUTES;

public class FlakeIdGeneratorSample {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig()
                .addFlakeIdGeneratorConfig(new ClientFlakeIdGeneratorConfig("idGenerator")
                        .setPrefetchCount(10)
                        .setPrefetchValidityMillis(MINUTES.toMillis(10)));
        //集群组名称
        clientConfig.getGroupConfig().setName("dev");
        //节点地址
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701", "127.0.0.1:5702", "127.0.0.1:5703");
        //客户端
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        FlakeIdGenerator idGenerator = client.getFlakeIdGenerator("idGenerator");
        for (int i = 0; i < 10000; i++) {
            System.out.printf("Id: %s\n", idGenerator.newId());
        }

        client.getLifecycleService().shutdown();
    }
}