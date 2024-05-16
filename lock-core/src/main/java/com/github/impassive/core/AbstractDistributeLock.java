package com.github.impassive.core;

import com.github.impassive.DistributeLock;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * @author impassive
 */
public abstract class AbstractDistributeLock implements DistributeLock {

  private Thread currentThread = null;

  private final Queue<Thread> waitQueue = new ConcurrentLinkedQueue<>();

  @Override
  public void lock(List<String> keys) {
    if (keys == null || keys.isEmpty() || keys.stream().anyMatch(String::isBlank)) {
      throw new RuntimeException("keys is null or empty");
    }
    Thread thread = Thread.currentThread();
    while (true) {
      if ((this.currentThread == null || Thread.currentThread() == this.currentThread) &&
          doLock(keys)) {
        this.currentThread = thread;
        return;
      }
      waitQueue.add(thread);
      LockSupport.park(this);
    }
  }

  @Override
  public boolean tryLock(List<String> keys) {
    if (keys == null || keys.isEmpty() || keys.stream().anyMatch(String::isBlank)) {
      throw new RuntimeException("keys is null or empty");
    }
    return doLock(keys);
  }

  @Override
  public void unlock(List<String> keys) {
    if (this.currentThread == null || Thread.currentThread() != this.currentThread) {
      throw new IllegalMonitorStateException("current thread is not the lock thread");
    }
    if (doUnLock(keys)) {
      this.currentThread = null;
      Thread poll = waitQueue.poll();
      if (poll != null){
        LockSupport.unpark(poll);
      }
      return;
    }
    throw new IllegalMonitorStateException("unlock failed");
  }

  protected abstract boolean doLock(List<String> keys);

  protected abstract boolean doUnLock(List<String> keys);
}
