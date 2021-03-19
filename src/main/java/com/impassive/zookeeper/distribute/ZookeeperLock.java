package com.impassive.zookeeper.distribute;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.LockSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/** @author impassivey */
@Slf4j
public class ZookeeperLock implements Watcher {

  private static final String LOCK_PATH = "/lock";

  private final BlockingQueue<Thread> queue;

  private final ZooKeeper zooKeeperClient;

  public ZookeeperLock(BlockingQueue<Thread> queue, ZooKeeper zooKeeper) {
    this.queue = queue;
    this.zooKeeperClient = zooKeeper;
  }

  public int lock() {
    String path;
    try {
      path =
          zooKeeperClient.create(
              LOCK_PATH,
              LOCK_PATH.getBytes(StandardCharsets.UTF_8),
              Ids.OPEN_ACL_UNSAFE,
              CreateMode.EPHEMERAL);
      zooKeeperClient.addWatch(path, this, AddWatchMode.PERSISTENT);
      final Stat exists = zooKeeperClient.exists(LOCK_PATH, this);
      return exists.getAversion();
    } catch (KeeperException e) {
      if (e.code() == Code.NODEEXISTS) {
        final Thread thread = Thread.currentThread();
        queue.offer(thread);
        LockSupport.park();
      }
    } catch (InterruptedException e) {
      log.error("lock error ", e);
    }
    return -1;
  }

  public void unLock(int version) {
    try {
      zooKeeperClient.delete(LOCK_PATH, version);
    } catch (InterruptedException e) {
      log.error("unlock error ", e);
    } catch (KeeperException e) {
      log.error("unlock keeper error", e);
    }
  }

  @Override
  public void process(WatchedEvent event) {
    if (!StringUtils.equalsIgnoreCase(event.getPath(), LOCK_PATH)) {
      return;
    }
    if (event.getType() != EventType.NodeDeleted) {
      return;
    }
    try {
      final Thread take = queue.take();
      LockSupport.unpark(take);
    } catch (InterruptedException e) {
      log.error("get thread error");
    }
  }
}
