package net.ameizi.distributed.hazelcast.example;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@Slf4j
@RestController
public class LockController {

    public static final String ID_GENERATOR = "idGenerator";

    @Resource(name="hazelcastInstance1")
    private HazelcastInstance hazelcastInstance;

    public static void main(String[] args) {
        SpringApplication.run(DistributedHazelcastLockExampleApplication.class, args);
    }

    /**
     * 分布式锁
     */
    @GetMapping("/loc")
    public void lock() {
        FencedLock loc = hazelcastInstance.getCPSubsystem().getLock("loc");
        boolean b = loc.tryLock();
        Assert.assertTrue(b);
        loc.unlock();
    }

    /**
     * 分布式 id
     * @return
     */
    @GetMapping("/getid")
    public long getid(){
        FlakeIdGenerator flakeIdGenerator = hazelcastInstance.getFlakeIdGenerator(ID_GENERATOR);
        return flakeIdGenerator.newId();
    }

}
