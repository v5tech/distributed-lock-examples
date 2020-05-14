package spring.integration.distributed.lock.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@RestController
@SpringBootApplication
public class DistributedLockRegistryApplication {

    @Autowired
    private RedisLockRegistry redisLockRegistry;

    @Autowired
    private ZookeeperLockRegistry zookeeperLockRegistry;

    @Autowired
    private JdbcLockRegistry jdbcLockRegistry;

    public static void main(String[] args) {
        SpringApplication.run(DistributedLockRegistryApplication.class, args);
    }

    @GetMapping("/lock")
    public void lock() throws InterruptedException {
        // Lock lock = redisLockRegistry.obtain("lock");
        // Lock lock = zookeeperLockRegistry.obtain("lock");
        Lock lock = jdbcLockRegistry.obtain("lock");
        try{
            boolean b1 = lock.tryLock(3, TimeUnit.SECONDS);
            log.info("b1 is : {}", b1);
            // TimeUnit.SECONDS.sleep(5);
            boolean b2 = lock.tryLock(3, TimeUnit.SECONDS);
            log.info("b2 is : {}", b2);
        }finally {
            // 切记解锁，获取了几次锁，就要释放几次
            lock.unlock();
            lock.unlock();
        }
    }
}
