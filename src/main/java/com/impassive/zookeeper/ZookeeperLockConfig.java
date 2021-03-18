package com.impassive.zookeeper;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** @author impassivey */
@Slf4j
@Configuration
public class ZookeeperLockConfig {

  private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

  @Value("${zk.url}")
  private String connectionString;

  @Bean
  public ZooKeeper zooKeeperClient() {
    ZooKeeper zooKeeper = null;
    try {
      zooKeeper =
          new ZooKeeper(
              connectionString,
              1000 * 3600,
              watchedEvent -> {
                if (watchedEvent.getState() == KeeperState.SyncConnected) {
                  COUNT_DOWN_LATCH.countDown();
                }
              });
      COUNT_DOWN_LATCH.await();
    } catch (IOException | InterruptedException e) {
      log.error("create zk client error", e);
    }
    return zooKeeper;
  }

  @Bean
  public ExecutorService executorService() {
    return new ThreadPoolExecutor(
        10,
        20,
        3600,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(1000),
        r -> {
          Thread thread = new Thread(r);
          thread.setName("zookeeper");
          return thread;
        },
        new AbortPolicy());
  }
}
