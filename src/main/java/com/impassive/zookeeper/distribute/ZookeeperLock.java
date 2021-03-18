package com.impassive.zookeeper.distribute;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import javax.annotation.Resource;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;

/** @author impassivey */
@Component
public class ZookeeperLock {

  @Resource private ExecutorService executorService;

  @Resource private ZooKeeper zooKeeperClient;

  private int cnt = 0;

  public void test() throws InterruptedException {
    CountDownLatch count = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      executorService.submit(
          () -> {
            cnt++;
          });
      count.countDown();
    }
    count.await();
  }
}
