package com.impassive.zookeeper.distribute;

import com.impassive.zookeeper.ZookeeperLockConfig;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperLockTest {

  private ZookeeperLock zookeeperLock;

  private ExecutorService executorService;

  private int cnt = 0;

  @Before
  public void setUp() throws Exception {
    ZookeeperLockConfig zookeeperLockConfig = new ZookeeperLockConfig();
    executorService = zookeeperLockConfig.executorService();
    zookeeperLockConfig.setConnectionString("impassive.com:2181");
    zookeeperLock =
        new ZookeeperLock(new ArrayBlockingQueue<>(1000), zookeeperLockConfig.zooKeeperClient());
  }

  @Test
  public void test1() throws InterruptedException {
    CountDownLatch count = new CountDownLatch(100);
    for (int i = 0; i < 100; i++) {
      executorService.submit(
          () -> {
            int version;
            while ((version = zookeeperLock.lock()) == -1);
            cnt++;
            count.countDown();
            zookeeperLock.unLock(version);
          });
    }
    count.await();
    System.out.println(cnt);
  }
}
