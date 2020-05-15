package net.ameizi.zookeeper.leader.examples;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.queue.BlockingQueueConsumer;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.test.Timing;
import org.apache.curator.utils.CloseableUtils;
import org.junit.Assert;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * LeaderLatch方式Zookeeper选主
 */
public class LeaderLatchTest {

    private static final int CLIENT_QTY = 10;
    private static final String PATH = "/leader";

    List<CuratorFramework> clients;
    List<LeaderLatch> latches;
    TestingServer server;

    public static void main(String[] args) throws Exception{
        LeaderLatchTest test = new LeaderLatchTest();
        test.setup();
        test.testSelect();
        test.close();
    }

    public void setup() throws Exception {
        clients = Lists.newArrayList();
        latches = Lists.newArrayList();
        server = new TestingServer();
        // 模拟 10 个客户端
        for (int i = 0; i < CLIENT_QTY; i++) {
            CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            clients.add(client);
            LeaderLatch leaderLatch = new LeaderLatch(client, PATH, "Client #" + i);
            latches.add(leaderLatch);
            client.start();
            // 启动该实例，并参与选举
            leaderLatch.start();
        }
    }

    public void testSelect() throws Exception {
        // 延迟执行，确保zk已顺利选主
        TimeUnit.SECONDS.sleep(3);
        // 获取Leader
        LeaderLatch currentLeader = latches.stream().filter(LeaderLatch::hasLeadership).findFirst().get();
        System.out.println("current leader is " + currentLeader.getId());
        System.out.println("release the leader " + currentLeader.getId());
        // 释放Leader
        currentLeader.close();
        testSelect();
    }

    public void close() {
        for (LeaderLatch leaderLatch : latches) {
            if (!LeaderLatch.State.CLOSED.equals(leaderLatch.getState())) {
                CloseableUtils.closeQuietly(leaderLatch);
            }
        }
        for (CuratorFramework client : clients) {
            CloseableUtils.closeQuietly(client);
        }
        CloseableUtils.closeQuietly(server);
    }


    public void queue() throws Exception {
        final int QTY = 10;
        Random random = new Random();
        Timing timing = new Timing();
        TestingServer server = new TestingServer();
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
        client.start();
        BlockingQueueConsumer<Integer> blockingQueueConsumer = new BlockingQueueConsumer<Integer>(new ConnectionStateListener(){
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
            }
        });
        DistributedDelayQueue<Integer> delayQueue = QueueBuilder.builder(client, blockingQueueConsumer,new QueueSerializer<Integer>(){
            @Override
            public byte[] serialize(Integer item) {
                return Integer.toString(item).getBytes();
            }
            @Override
            public Integer deserialize(byte[] bytes) {
                return Integer.parseInt(new String(bytes));
            }
        },"/delay_queue").buildDelayQueue();
        delayQueue.start();
        try{
            for (int i = 0; i < QTY; i++) {
                long delay = System.currentTimeMillis() + random.nextInt(100);
                delayQueue.put(i,delay);
            }
            long lastValue = -1;
            for (int i = 0; i < QTY; i++) {
                Integer value = blockingQueueConsumer.take(timing.forWaiting().seconds(), TimeUnit.SECONDS);
                Assert.assertNotNull(value);
                Assert.assertTrue(value >= lastValue);
                lastValue = value;
                System.out.println(value);
            }

        }finally {
            CloseableUtils.closeQuietly(delayQueue);
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(server);
        }
    }

}
