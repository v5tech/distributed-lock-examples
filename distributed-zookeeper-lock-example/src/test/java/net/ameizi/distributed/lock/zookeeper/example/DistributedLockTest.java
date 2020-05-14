package net.ameizi.distributed.lock.zookeeper.example;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DistributedLockTest {

    // Zookeeper 锁节点路径,分布式锁的相关操作都是在这个节点上进行
    private final String lockPath = "/distributed-lock";
    private String connectString;
    // Curator 客户端重试策略
    private RetryPolicy retryPolicy;
    // Curator 客户端对象
    private CuratorFramework client1;
    // Curator 客户端对象 用户模拟其他客户端
    private CuratorFramework client2;

    private TestingServer testingServer;
    private TestingCluster testingCluster;

    /**
     * 可重入锁
     * 全局可重入的锁。 Shared意味着锁是全局可见的， 客户端都可以请求锁。 Reentrant和JDK的ReentrantLock类似， 意味着同一个客户端在拥有锁的同时，可以多次获取，不会被阻塞。
     * 它是由类InterProcessMutex来实现,该实例可重用
     */
    @Test
    public void sharedReentrantLock() throws Exception {
        // 创建可重入锁
        InterProcessLock lock1 = new InterProcessMutex(client1, lockPath);
        // 模拟另一个客户端
        InterProcessLock lock2 = new InterProcessMutex(client2, lockPath);
        // lock1 获取锁
        lock1.acquire();
        try {
            // lock1 第二次获取锁
            lock1.acquire();
            try {
                // lock2 超时获取锁,因为锁已经被 lock1 客户端占用,所以获取失败,需要等 lock1 释放
                Assert.assertFalse(lock2.acquire(2, TimeUnit.SECONDS));
            } finally {
                lock1.release();
            }
        } finally {
            // 重入锁获取与释放需要一一对应,如果获取2次,释放1次,那么该锁依然是被占用,如果将下面这行代码注释,那么会发现下面的 lock2 获取锁失败
            lock1.release();
        }
        // 在 lock1 释放后,lock2 能够获取锁
        Assert.assertTrue(lock2.acquire(2, TimeUnit.SECONDS));
        lock2.release();
    }

    /**
     * 不可重入锁Shared Lock
     * 这个锁和上面的相比，就是少了Reentrant的功能，也就意味着它不能在同一个线程中重入。
     * 这个类是InterProcessSemaphoreMutex。
     *
     * 注意需要调用release两次。这和JDK的ReentrantLock用法一致。如果少调用一次release，则此线程依然拥有锁。
     * @throws Exception
     */
    @Test
    public void sharedLock() throws Exception {
        // 创建共享锁
        InterProcessLock lock1 = new InterProcessSemaphoreMutex(client1, lockPath);
        // 模拟另一个客户端
        InterProcessLock lock2 = new InterProcessSemaphoreMutex(client2, lockPath);
        // 获取锁对象
        lock1.acquire();
        // 打开此方法会发现线程被阻塞在该方法上，也就是说该锁不是可重入的
        // lock1.acquire();

        // 测试是否可以重入
        // 超时获取锁对象(第一个参数为时间,第二个参数为时间单位),因为锁已经被获取,所以返回 false
        Assert.assertFalse(lock1.acquire(2, TimeUnit.SECONDS));
        Assert.assertFalse(lock2.acquire(2, TimeUnit.SECONDS));
        // 释放锁
        lock1.release();
        // lock2 尝试获取锁成功,因为锁已经被释放
        lock2.acquire();
        Assert.assertFalse(lock2.acquire(2, TimeUnit.SECONDS));
        Assert.assertFalse(lock1.acquire(2, TimeUnit.SECONDS));
        lock2.release();
    }

    /**
     * 可重入读写锁
     *
     * 类似JDK的ReentrantReadWriteLock.
     * 一个读写锁管理一对相关的锁。 一个负责读操作，另外一个负责写操作。 读操作在写锁没被使用时可同时由多个进程使用，而写锁使用时不允许读 (阻塞)。
     * 此锁是可重入的。一个拥有写锁的线程可重入读锁，但是读锁却不能进入写锁。
     * 这也意味着写锁可以降级成读锁， 比如请求写锁 --->读锁 ---->释放写锁。 从读锁升级成写锁是不成的。
     *
     * 主要由两个类实现：
     *
     * InterProcessReadWriteLock
     * InterProcessLock
     *
     * @throws Exception
     */
    @Test
    public void sharedReentrantReadWriteLock() throws Exception {
        // 创建读写锁对象,因 curator 的实现原理,该锁是公平的
        InterProcessReadWriteLock lock1 = new InterProcessReadWriteLock(client1, lockPath);
        // lock2 用于模拟其他客户端
        InterProcessReadWriteLock lock2 = new InterProcessReadWriteLock(client2, lockPath);
        // 使用 lock1 模拟读操作
        // 使用 lock2 模拟写操作
        // 获取读锁(使用InterProcessMutex实现,所以是可以重入的)
        InterProcessLock readLock = lock1.readLock();
        // 获取写锁(使用InterProcessMutex实现,所以是可以重入的)
        InterProcessLock writeLock = lock2.writeLock();

        /**
         * 读写锁测试对象
         */
        class ReadWriteLockTest {
            // 测试数据变更字段
            private Integer testData = 0;
            private Set<Thread> threadSet = new HashSet<>();

            // 写入数据
            private void write() throws Exception {
                writeLock.acquire();
                try {
                    Thread.sleep(10);
                    testData++;
                    System.out.println("写入数据\t" + testData);
                } finally {
                    writeLock.release();
                }
            }

            // 读取数据
            private void read() throws Exception {
                readLock.acquire();
                try {
                    Thread.sleep(10);
                    System.out.println("读取数据\t" + testData);
                } finally {
                    readLock.release();
                }
            }

            // 等待线程结束,防止test方法调用完成后,当前线程直接退出,导致控制台无法输出信息
            public void waitThread() throws InterruptedException {
                for (Thread thread : threadSet) {
                    thread.join();
                }
            }

            // 创建线程方法
            private void createThread(int type) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (type == 1) {
                                write();
                            } else {
                                read();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                threadSet.add(thread);
                thread.start();
            }

            // 测试方法
            public void test() {
                for (int i = 0; i < 5; i++) {
                    createThread(1);
                }
                for (int i = 0; i < 5; i++) {
                    createThread(2);
                }
            }
        }

        ReadWriteLockTest readWriteLockTest = new ReadWriteLockTest();
        readWriteLockTest.test();
        readWriteLockTest.waitThread();
    }


    /**
     * 信号量
     *
     * 一个计数的信号量类似JDK的Semaphore。 JDK中Semaphore维护的一组许可(permits)，而Cubator中称之为租约(Lease)。
     * 有两种方式可以决定semaphore的最大租约数。第一种方式是有用户给定的path决定。第二种方式使用SharedCountReader类。
     * 如果不使用SharedCountReader, 没有内部代码检查进程是否假定有10个租约而进程B假定有20个租约。 所以所有的实例必须使用相同的numberOfLeases值.
     *
     * 这次调用acquire会返回一个租约对象。 客户端必须在finally中close这些租约对象，否则这些租约会丢失掉。 但是，如果客户端session由于某种原因比如crash丢掉，那么这些客户端持有的租约会自动close，这样其它客户端可以继续使用这些租约。
     *
     * 注意一次你可以请求多个租约，如果Semaphore当前的租约不够，则请求线程会被阻塞。 同时还提供了超时的重载方法。
     *
     * @throws Exception
     */
    @Test
    public void semaphore() throws Exception {
        // 创建一个信号量
        InterProcessSemaphoreV2 semaphore1 = new InterProcessSemaphoreV2(client1, lockPath, 6);
        // 模拟其他客户端
        InterProcessSemaphoreV2 semaphore2 = new InterProcessSemaphoreV2(client2, lockPath, 6);

        // 获取一个许可
        Lease lease1 = semaphore1.acquire();
        Assert.assertNotNull(lease1);
        // semaphore.getParticipantNodes() 会返回当前参与信号量的节点列表,俩个客户端所获取的信息相同
        Assert.assertEquals(semaphore1.getParticipantNodes(), semaphore2.getParticipantNodes());

        // 超时获取一个许可
        Lease lease2 = semaphore2.acquire(2, TimeUnit.SECONDS);
        Assert.assertNotNull(lease2);
        Assert.assertEquals(semaphore1.getParticipantNodes(), semaphore2.getParticipantNodes());

        // 获取多个许可,参数为许可数量
        Collection<Lease> leases1 = semaphore1.acquire(2);
        Assert.assertEquals(2, leases1.size());
        Assert.assertEquals(semaphore1.getParticipantNodes(), semaphore2.getParticipantNodes());

        // 超时获取多个许可,第一个参数为许可数量
        Collection<Lease> leases2 = semaphore2.acquire(2, 2, TimeUnit.SECONDS);
        Assert.assertEquals(2, leases2.size());
        Assert.assertEquals(semaphore1.getParticipantNodes(), semaphore2.getParticipantNodes());

        // 目前 semaphore1 已经获取 3 个许可,semaphore2 也获取 3 个许可,加起来为 6 个,所以他们无法在进行许可获取
        Assert.assertNull(semaphore1.acquire(2, TimeUnit.SECONDS));
        Assert.assertNull(semaphore2.acquire(2, TimeUnit.SECONDS));

        semaphore1.returnLease(lease1);
        semaphore2.returnLease(lease2);
        semaphore1.returnAll(leases1);
        semaphore2.returnAll(leases2);
    }

    /**
     * 多重锁
     *
     * Multi Shared Lock是一个锁的容器。 当调用acquire， 所有的锁都会被acquire，如果请求失败，所有的锁都会被release。 同样调用release时所有的锁都被release(失败被忽略)。
     * 基本上，它就是组锁的代表，在它上面的请求释放操作都会传递给它包含的所有的锁。
     *
     * 主要涉及两个类：
     *
     * InterProcessMultiLock
     * InterProcessLock
     *
     * 新建一个InterProcessMultiLock， 包含一个重入锁和一个非重入锁。
     * 调用acquire后可以看到线程同时拥有了这两个锁。
     * 调用release看到这两个锁都被释放了。
     *
     * @throws Exception
     */
    @Test
    public void multiLock() throws Exception {
        // 可重入锁
        InterProcessLock interProcessLock1 = new InterProcessMutex(client1, lockPath);
        // 不可重入锁
        InterProcessLock interProcessLock2 = new InterProcessSemaphoreMutex(client2, lockPath);
        // 创建多重锁对象
        InterProcessLock lock = new InterProcessMultiLock(Arrays.asList(interProcessLock1, interProcessLock2));
        // 获取参数集合中的所有锁
        lock.acquire();

        // 因为存在一个不可重入锁,所以整个 InterProcessMultiLock 不可重入
        Assert.assertFalse(lock.acquire(2, TimeUnit.SECONDS));
        // interProcessLock1 是可重入锁,所以可以继续获取锁
        Assert.assertTrue(interProcessLock1.acquire(2, TimeUnit.SECONDS));
        // interProcessLock2 是不可重入锁,所以获取锁失败
        Assert.assertFalse(interProcessLock2.acquire(2, TimeUnit.SECONDS));

        // 释放参数集合中的所有锁
        lock.release();

        // interProcessLock2 中的所已经释放,所以可以获取
        Assert.assertTrue(interProcessLock2.acquire(2, TimeUnit.SECONDS));

    }

    @Before
    public void init() throws Exception {

        // 创建一个测试 Zookeeper 服务器
        testingServer = new TestingServer();
        // 获取该服务器的链接地址
        connectString = testingServer.getConnectString();

        // 创建一个集群测试 zookeeper 服务器
        // testingCluster = new TestingCluster(3);
        // 获取该服务器的链接地址
        // connectString = testingCluster.getConnectString();

        // 重试策略
        // 初始休眠时间为 1000ms,最大重试次数为3
        retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client1 = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client2 = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        // 创建会话
        client1.start();
        client2.start();
    }

    // 释放资源
    @After
    public void close() {
        CloseableUtils.closeQuietly(client1);
        CloseableUtils.closeQuietly(client2);
        CloseableUtils.closeQuietly(testingServer);
        CloseableUtils.closeQuietly(testingCluster);
    }
}
