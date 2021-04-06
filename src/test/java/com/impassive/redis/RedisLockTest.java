package com.impassive.redis;

import com.impassive.redis.distrtbute.RedisLock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisLockTest {
  private int testNum = 0;

  private RedisLock redisLock;

  @Before
  public void setUp() throws Exception {
    RedisLockConfig redisLockConfig = new RedisLockConfig();
    redisLockConfig.setRedisUrl("impassive.com:6379");
    LettuceConnectionFactory lettuceConnectionFactory = redisLockConfig.redisConnectionFactory();
    lettuceConnectionFactory.afterPropertiesSet();
    RedisTemplate<String, Object> template =
        redisLockConfig.redisTemplate(lettuceConnectionFactory);
    template.afterPropertiesSet();
    redisLock = new RedisLock(template);
  }

  @Test
  public void test() throws InterruptedException {
    int threadNum = 1000;
    final ExecutorService threadPool = Executors.newFixedThreadPool(100);
    CountDownLatch count = new CountDownLatch(threadNum);
    for (int i = 0; i < threadNum; i++) {
      threadPool.submit(
          () -> {
            redisLock.lock();
            testNum++;
            redisLock.unLock();
            count.countDown();
          });
    }
    count.await();
    System.out.println(testNum);
  }
}
