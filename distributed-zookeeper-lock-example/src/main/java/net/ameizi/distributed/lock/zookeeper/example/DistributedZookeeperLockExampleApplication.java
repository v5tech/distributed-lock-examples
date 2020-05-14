package net.ameizi.distributed.lock.zookeeper.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
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
public class DistributedZookeeperLockExampleApplication {

	@Autowired
	private CuratorFramework curatorFramework;

	/** 线程池 */
	private ExecutorService executor = Executors.newFixedThreadPool(5);

	public static void main(String[] args) {
		SpringApplication.run(DistributedZookeeperLockExampleApplication.class, args);
	}

	@GetMapping("/lock")
	public void lock() throws Exception{
		// for (int i = 0; i < 10; i++) {
		// 	executor.submit(() -> {
		// 		// 创建锁对象
		// 		InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/lock");
		// 		try{
		// 			// 获取锁
		// 			if (lock.acquire(3, TimeUnit.SECONDS)) {
		// 				// 如果获取锁成功，则执行对应逻辑
		// 				log.info("获取分布式锁，执行逻辑");
		// 			}
		// 		}catch (Exception e){
		// 			e.printStackTrace();
		// 		}finally {
		// 			try {
		// 				lock.release();
		// 			} catch (Exception e) {
		// 				e.printStackTrace();
		// 			}
		// 		}
		// 	});
		// }

		Thread thread1 = new Thread(() -> {
			InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/lock");
			try{
				lock.acquire();
				log.info("{}获取分布式锁，执行逻辑",Thread.currentThread().getName());
				TimeUnit.SECONDS.sleep(3);
			}catch (Exception e){
				e.printStackTrace();
			}finally {
				try {
					lock.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread1.start();
		Thread thread2 = new Thread(() -> {
			InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/lock");
			try{
				lock.acquire();
				log.info("{}获取分布式锁，执行逻辑",Thread.currentThread().getName());
			}catch (Exception e){
				e.printStackTrace();
			}finally {
				try {
					lock.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread2.start();
	}

}
