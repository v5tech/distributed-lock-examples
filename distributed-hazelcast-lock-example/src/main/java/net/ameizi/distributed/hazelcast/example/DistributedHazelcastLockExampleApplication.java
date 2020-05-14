package net.ameizi.distributed.hazelcast.example;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@SpringBootApplication
public class DistributedHazelcastLockExampleApplication {

	@Resource(name="hazelcastInstance1")
	private HazelcastInstance hz1;
	@Resource(name="hazelcastInstance2")
	private HazelcastInstance hz2;
	@Resource(name="hazelcastInstance3")
	private HazelcastInstance hz3;

	public static void main(String[] args) {
		SpringApplication.run(DistributedHazelcastLockExampleApplication.class, args);
	}

	@GetMapping("/reentrant-lock")
	public void reentrantlock() {
		FencedLock hz1Lock = hz1.getCPSubsystem().getLock("LOCK");
		FencedLock hz2Lock = hz2.getCPSubsystem().getLock("LOCK");

		hz1Lock.lock();
		hz1Lock.lock();

		boolean b = hz2Lock.tryLock();
		Assert.assertFalse(b);

		hz1Lock.unlock();
		hz1Lock.unlock();

		b = hz2Lock.tryLock();
		Assert.assertTrue(b);
		hz2Lock.unlock();
	}

	@GetMapping("/lock")
	public void lock() {
		// 默认情况下FencedLock是可重入锁，可以通过FencedLockConfig设置为非重入锁
		FencedLock hz1Lock = hz1.getCPSubsystem().getLock("LOCK");
		FencedLock hz2Lock = hz2.getCPSubsystem().getLock("LOCK");

		hz1Lock.lock();
		// 再次加锁会报错 com.hazelcast.cp.lock.exception.LockAcquireLimitReachedException: Lock[LOCK] reentrant lock limit is already reached!]
		// hz1Lock.lock();

		boolean b = hz2Lock.tryLock();
		Assert.assertFalse(b);

		hz1Lock.unlock();

		b = hz2Lock.tryLock();
		Assert.assertTrue(b);
		hz2Lock.unlock();
	}

}
