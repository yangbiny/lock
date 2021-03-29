package com.impassive.zookeeper.distribute;

import com.impassive.zookeeper.ZookeeperLockConfig;
import com.impassive.zookeeper.distribute.ZookeeperReadWriteLock.ReadLock;
import com.impassive.zookeeper.distribute.ZookeeperReadWriteLock.WriteLock;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperReadWriteLockTest {

  private ZookeeperReadWriteLock zookeeperLock;

  private ExecutorService executorService;

  private Long cnt = 0L;

  @Before
  public void setUp() throws Exception {
    ZookeeperLockConfig zookeeperLockConfig = new ZookeeperLockConfig();
    executorService = zookeeperLockConfig.executorService();
    zookeeperLockConfig.setConnectionString("impassive.com:2181");
    zookeeperLock = new ZookeeperReadWriteLock(zookeeperLockConfig.zooKeeperClient());
  }

  @Test
  public void test1() throws InterruptedException {
    while (true) {
      cnt = 0L;
      final WriteLock writeLock = zookeeperLock.writeLock();
      CountDownLatch count = new CountDownLatch(100);
      for (int i = 0; i < 100; i++) {
        executorService.submit(
            () -> {
              writeLock.lock();
              cnt++;
              writeLock.unLock();
              count.countDown();
            });
      }
      count.await();
      System.out.println(cnt);
      if (cnt < 100) {
        System.out.println(cnt);
      }
    }
  }
}
