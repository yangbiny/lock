package com.impassive.zookeeper.distribute;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @author impassivey
 */
public class ZookeeperLock extends AbstractZookeeperLock {

  public ZookeeperLock(String zkUrl) {
    super(zkUrl);
  }

  public ZookeeperLock(String zkUrl, String lockParentPath) {
    super(zkUrl, lockParentPath);
  }


  @Override
  public void lock() {
    lock(DEFAULT_LOCK_WRITE_PATH);
  }


  public void lock(String path) {
    if (path == null || path.isBlank()) {
      throw new RuntimeException("path is null");
    }
    super.lock(path);
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {

  }

  @Override
  public boolean tryLock() {
    return tryLock(DEFAULT_LOCK_WRITE_PATH);
  }

  public boolean tryLock(String path) {
    return super.tryLock(path);
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public void unlock() {

  }

  @Override
  public Condition newCondition() {
    return null;
  }
}
