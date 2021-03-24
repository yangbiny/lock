package com.impassive.zookeeper.distribute;

import com.google.common.collect.Lists;
import com.impassive.Lock;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.LockSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher;

/** @author impassivey */
@Slf4j
public abstract class AbstractZookeeperLock implements Lock, Watcher {

  protected static final String LOCK_PARENT = "/lock";

  protected static final String LOCK_READ_PATH = "/lock/read";

  protected static final String LOCK_WRITE_PATH = "/lock/write";

  protected final List<String> READ_LOCK = Lists.newArrayList(LOCK_WRITE_PATH);

  protected final List<String> WRITE_LOCK = Lists.newArrayList(LOCK_READ_PATH, LOCK_WRITE_PATH);

  private final BlockingQueue<Thread> blockingQueue = new LinkedBlockingDeque<>(100);

  protected void wakeUp() {
    try {
      final Thread take = blockingQueue.take();
      LockSupport.unpark(take);
    } catch (InterruptedException e) {
      log.error("wake thread error ", e);
    }
  }

  protected void checkAndParkThread(List<String> childrenLock, boolean write) {
    if (CollectionUtils.isEmpty(childrenLock)) {
      return;
    }
    List<String> path = write ? WRITE_LOCK : READ_LOCK;
    boolean hasPath = false;
    for (String s : childrenLock) {
      for (String s1 : path) {
        if (!StringUtils.startsWithIgnoreCase(s, s1)) {
          continue;
        }
        hasPath = true;
        break;
      }
    }
    if (hasPath) {
      final Thread thread = Thread.currentThread();
      blockingQueue.add(thread);
      // 阻塞
      LockSupport.park();
    }
  }
}
