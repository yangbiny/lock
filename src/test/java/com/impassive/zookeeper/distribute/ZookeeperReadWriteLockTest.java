package com.impassive.zookeeper.distribute;

import com.impassive.zookeeper.ZookeeperLockConfig;
import com.impassive.zookeeper.distribute.ZookeeperReadWriteLock.ReadLock;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperReadWriteLockTest {

  private ZookeeperReadWriteLock zookeeperLock;

  private ExecutorService executorService;

  private int cnt = 0;

  @Before
  public void setUp() throws Exception {
    ZookeeperLockConfig zookeeperLockConfig = new ZookeeperLockConfig();
    executorService = zookeeperLockConfig.executorService();
    zookeeperLockConfig.setConnectionString("impassive.com:2181");
    zookeeperLock = new ZookeeperReadWriteLock(zookeeperLockConfig.zooKeeperClient());
  }

  @Test
  public void test1() throws InterruptedException {
    CountDownLatch count = new CountDownLatch(100);
    for (int i = 0; i < 100; i++) {
      final ReadLock readLock = zookeeperLock.readLock();
      readLock.lock();
      cnt++;
      count.countDown();
      readLock.unLock();
    }
    count.await();
    System.out.println(cnt);
  }
}
