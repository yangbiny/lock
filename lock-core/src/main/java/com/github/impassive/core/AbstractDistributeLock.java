package com.github.impassive.core;

import com.github.impassive.DistributeLock;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author impassive
 */
public abstract class AbstractDistributeLock implements DistributeLock {

  private final Lock lock = new ReentrantLock();



  @Override
  public void lock(List<String> keys) {
    if (keys == null || keys.isEmpty() || keys.stream().anyMatch(String::isBlank)) {
      throw new RuntimeException("keys is null or empty");
    }
    if (doLock(keys)) {
      return;
    }

    lock.newCondition();
  }

  @Override
  public boolean tryLock(List<String> keys) {
    if (keys == null || keys.isEmpty() || keys.stream().anyMatch(String::isBlank)) {
      throw new RuntimeException("keys is null or empty");
    }
    return doLock(keys);
  }

  @Override
  public void unlock() {

  }

  private void acquire() {

  }


  protected abstract boolean doLock(List<String> keys);
}
