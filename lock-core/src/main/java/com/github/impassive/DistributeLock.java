package com.github.impassive;

import java.util.List;

/**
 * @author impassive
 */
public interface DistributeLock {

  String DEFAULT_KEYS = "default_key";

  /**
   * 加锁。 如果加锁失败，则会进入到等待队列中。一直阻塞，直到获取锁成功
   */
  default void lock() {
    lock(DEFAULT_KEYS);
  }

  default void lock(String key) {
    if (key == null || key.isBlank()) {
      throw new RuntimeException("key is null");
    }
    lock(List.of(key));
  }


  void lock(List<String> keys);

  /**
   * 尝试加锁。如果加锁失败，则直接返回，不会阻塞
   *
   * @return 是否加锁成功
   */
  default boolean tryLock() {
    return tryLock(DEFAULT_KEYS);
  }

  default boolean tryLock(String key) {
    if (key == null || key.isBlank()) {
      throw new RuntimeException("key is null");
    }
    return tryLock(List.of(key));
  }

  /**
   * 尝试 申请锁。 如果 所有的锁都申请成功，即为成功，否则失败，立即返回，不会阻塞
   *
   * @param keys 需要申请的锁
   * @return true: 申请成功
   */
  boolean tryLock(List<String> keys);

  /**
   * 解锁
   */
  void unlock(List<String> keys);

}
