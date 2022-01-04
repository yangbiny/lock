package com.impassive.zookeeper.distribute;

import com.impassive.zookeeper.ZookeeperLockConfig;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperLockTest {

  private ZooKeeper zooKeeper;

  private ZookeeperLock zookeeperLock;

  private ExecutorService executorService;

  private int cnt = 0;

  @Before
  public void setUp() throws Exception {
    ZookeeperLockConfig zookeeperLockConfig = new ZookeeperLockConfig();
    executorService = zookeeperLockConfig.executorService();
    zookeeperLockConfig.setConnectionString("impassive.com:2181");
    zooKeeper = zookeeperLockConfig.zooKeeperClient();
    zookeeperLock =
        new ZookeeperLock(new ArrayBlockingQueue<>(1000), zooKeeper);
  }

  @Test
  public void test1() throws InterruptedException {
    CountDownLatch count = new CountDownLatch(10000);
    for (int i = 0; i < 10000; i++) {
      executorService.submit(
          () -> {
            zookeeperLock.lock();
            cnt++;
            count.countDown();
            zookeeperLock.unLock();
          });
    }
    count.await();
    System.out.println(cnt);
  }

  @Test
  public void testZookeeper() throws InterruptedException, KeeperException {
    String path = "/test/test-mul/[consumer,consumer1]";
    Stat exists = zooKeeper.exists(path, false);
    if (exists == null) {
      zooKeeper.create(path, "".getBytes(StandardCharsets.UTF_8), Ids.OPEN_ACL_UNSAFE,
          CreateMode.PERSISTENT);
    }
    zooKeeper.setData(path, "consumer".getBytes(StandardCharsets.UTF_8), -1);
  }
}
