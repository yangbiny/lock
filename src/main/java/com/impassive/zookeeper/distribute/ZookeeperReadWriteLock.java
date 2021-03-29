package com.impassive.zookeeper.distribute;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;

/**
 * 基于Zookeeper实现的读写锁
 *
 * @author impassivey
 */
@Slf4j
public class ZookeeperReadWriteLock {

  private final ReadLock readLock;

  private final WriteLock writeLock;

  public ZookeeperReadWriteLock(ZooKeeper zooKeeper) {
    this.readLock = new ReadLock(zooKeeper);
    this.writeLock = new WriteLock(zooKeeper);
  }

  public ReadLock readLock() {
    return this.readLock;
  }

  public WriteLock writeLock() {
    return this.writeLock;
  }

  @Slf4j
  public static class ReadLock extends AbstractZookeeperLock {

    public ReadLock(ZooKeeper zooKeeperClient) {
      super.init(zooKeeperClient);
    }

    @Override
    public void lock() {
      try {
        addLock(false);
      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("create path error", e);
      }
    }

    @Override
    public void process(WatchedEvent event) {
      if (event.getType() == EventType.NodeDeleted) {
        wakeUp();
      }
    }
  }

  @Slf4j
  public static class WriteLock extends AbstractZookeeperLock {

    public WriteLock(ZooKeeper zooKeeperClient) {
      super.init(zooKeeperClient);
    }

    @Override
    public void lock() {
      try {
        addLock(true);
      } catch (KeeperException e) {
        log.error("keeper error", e);
      } catch (InterruptedException e) {
        log.error("unlock write lock error : ", e);
      }
    }

    @Override
    public void process(WatchedEvent event) {}
  }
}
