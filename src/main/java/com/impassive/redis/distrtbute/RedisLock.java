package com.impassive.redis.distrtbute;

import com.impassive.Lock;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.LockSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** @author impassivey */
@Slf4j
public class RedisLock implements Lock {

  private static final String LOCK_KEY = "lock";

  private static final Integer LOCK_VALUE = 0;

  private final ValueOperations<String, Object> ops;

  private final RedisTemplate<String, Object> redisTemplate;

  private final BlockingQueue<Thread> blockingQueue = new ArrayBlockingQueue<>(1000);

  public RedisLock(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.ops = this.redisTemplate.opsForValue();
  }

  @Override
  public void lock() {
    while (true) {
      Boolean hasKey = false;
      try {
        hasKey = redisTemplate.hasKey(LOCK_KEY);
      } catch (Exception e) {
        log.error("has error : ", e);
      }
      if (hasKey == null || hasKey) {
        return;
      }
      final Boolean result = ops.setIfAbsent(LOCK_KEY, LOCK_VALUE);
      if (result != null && result) {
        break;
      }
      final Thread thread = Thread.currentThread();
      blockingQueue.add(thread);
      LockSupport.park();
    }
  }

  @Override
  public void unLock() {
    final Boolean delete = redisTemplate.delete(LOCK_KEY);
    if (delete == null || !delete) {
      // TODO 删除key失败时的处理方式
      return;
    }
    final Thread thread = blockingQueue.poll();
    if (thread == null) {
      return;
    }
    LockSupport.unpark(thread);
  }
}
