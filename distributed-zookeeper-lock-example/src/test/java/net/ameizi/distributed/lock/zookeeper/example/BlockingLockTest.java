package net.ameizi.distributed.lock.zookeeper.example;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

/**
 * 阻塞式锁
 */
public class BlockingLockTest {

    public static void main(String[] args) throws Exception {
        // 创建一个测试 Zookeeper 服务器
        TestingServer testingServer = new TestingServer();
        // 获取该服务器的链接地址
        String connectString = testingServer.getConnectString();
        final String lockPath = "/lock";

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorClient.start();
        // 全局可重入锁
        final InterProcessMutex lock = new InterProcessMutex(curatorClient, lockPath);

        Runnable task = () -> {
            System.out.println("In BlockingLockTest");
            AtomicBoolean acquired = null;
            for (int i = 1; i <= 2; i++) {
                try {
                    System.out.println("Process " + Thread.currentThread().getName() + " TRYING lock at "+ new Date());
                    acquired = new AtomicBoolean(false);
                    lock.acquire();
                    acquired.set(true);
                    System.out.println("Process " + Thread.currentThread().getName() + " ACQUIRED lock. Iteration " + i + " at "+ new Date());
                    System.out.println("Process " + Thread.currentThread().getName() + " WORK-IN-PROGRESS");
                    Thread.sleep(1000);

                } catch (Exception ex) {
                    Logger.getLogger(BlockingLockTest.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if (acquired.get()) {
                            lock.release();
                            System.out.println("Process " + Thread.currentThread().getName() + " RELEASED lock at "+ new Date());
                        }

                    } catch (Exception ex) {
                        Logger.getLogger(BlockingLockTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        new Thread(task, UUID.randomUUID().toString()).start();

        TimeUnit.SECONDS.sleep(3);

        CloseableUtils.closeQuietly(curatorClient);
        CloseableUtils.closeQuietly(testingServer);

    }

}
