package net.ameizi.distributed.lock.zookeeper.example;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 非阻塞式锁
 */
public class NonBlockingLockTest {

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

        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("In NonBlockingLockTest");
                AtomicBoolean acquired = null;
                for (int i = 1; i <= 2; i++) {
                    try {
                        System.out.println("Process " + Thread.currentThread().getName() + " TRYING lock at "+ new Date());
                        acquired = new AtomicBoolean(false);
                        if (lock.acquire(2, TimeUnit.SECONDS)) {
                            acquired.set(true);
                            System.out.println("Process " + Thread.currentThread().getName() + " ACQUIRED lock. Iteration " + i + " at "+ new Date());
                            System.out.println("Process " + Thread.currentThread().getName() + " WORK-IN-PROGRESS");
                            Thread.sleep(3000); //simulating some work
                        }

                    } catch (Exception ex) {
                        Logger.getLogger(NonBlockingLockTest.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            if (acquired.get()) {
                                lock.release();
                                System.out.println("Process " + Thread.currentThread().getName() + " RELEASED lock at "+ new Date());
                            }

                        } catch (Exception ex) {
                            Logger.getLogger(NonBlockingLockTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        };

        new Thread(task, UUID.randomUUID().toString()).start();

        TimeUnit.SECONDS.sleep(5);
    }

}