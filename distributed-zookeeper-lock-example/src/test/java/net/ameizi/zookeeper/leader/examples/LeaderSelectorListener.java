package net.ameizi.zookeeper.leader.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LeaderSelector方式Zookeeper选主
 */
public class LeaderSelectorListener extends LeaderSelectorListenerAdapter implements Closeable {

    private final String name;
    private final LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger();

    public LeaderSelectorListener(CuratorFramework client, String path, String name) {
        this.name = name;
        this.leaderSelector = new LeaderSelector(client, path, this);
        leaderSelector.autoRequeue();
    }

    public void start() {
        leaderSelector.start();
    }

    @Override
    public void close() {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        final int waitSeconds = (int) (3 * Math.random()) + 1;
        System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
        System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
        TimeUnit.SECONDS.sleep(waitSeconds);
        System.out.println(name + " relinquishing leadership.\n");
    }
}
