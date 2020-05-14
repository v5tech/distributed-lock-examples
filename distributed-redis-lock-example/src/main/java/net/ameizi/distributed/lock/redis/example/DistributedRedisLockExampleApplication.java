package net.ameizi.distributed.lock.redis.example;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@SpringBootApplication
public class DistributedRedisLockExampleApplication {

	private static final String LOCK = "lock";

	@Autowired
	private RedissonClient redissonClient;

	private ExecutorService executor = Executors.newFixedThreadPool(10);

	public static void main(String[] args) {
		SpringApplication.run(DistributedRedisLockExampleApplication.class, args);
	}

	@GetMapping("/lock")
	public void lock(){
		for (int i = 0; i <100 ; i++) {
			executor.submit(()->{
				// 获取公平锁
				// RLock lock = redissonClient.getFairLock(LOCK);
				// 获取非公平锁
				RLock lock = redissonClient.getLock(LOCK);
				try{
					// 尝试加锁，最多等待 100 秒，10 秒后释放
					boolean b = lock.tryLock(100, 10, TimeUnit.SECONDS);
					if(b){
						// 获取到锁，执行业务逻辑
						log.info("获取到分布式锁，执行业务逻辑");
					}
				}catch (Exception e){
					e.printStackTrace();
				}finally {
					lock.unlock();
				}
			});
		}
	}

}
