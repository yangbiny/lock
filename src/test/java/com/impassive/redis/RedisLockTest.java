package com.impassive.redis;

import com.google.common.collect.Lists;
import com.impassive.redis.distrtbute.RedisLock;
import com.impassive.redis.limit.RateLimiter;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisLockTest {

  private int testNum = 0;

  private RedisLock redisLock;

  private RateLimiter rateLimiter;

  @Before
  public void setUp() throws Exception {
    RedisLockConfig redisLockConfig = new RedisLockConfig();
    redisLockConfig.setRedisUrl("impassive.com:6379");
    LettuceConnectionFactory lettuceConnectionFactory = redisLockConfig.redisConnectionFactory();
    lettuceConnectionFactory.afterPropertiesSet();
    RedisTemplate<String, Object> template =
        redisLockConfig.redisTemplate(lettuceConnectionFactory);
    template.afterPropertiesSet();
    final StringRedisTemplate stringRedisTemplate =
        redisLockConfig.stringRedisTemplate(lettuceConnectionFactory);
    stringRedisTemplate.afterPropertiesSet();
    redisLock = new RedisLock(template);
    rateLimiter = new RateLimiter(stringRedisTemplate);
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

  @Test
  public void testRateLimiter() {
    rateLimiter.acquire(10);
  }

  @Test
  public void testLettuce() {
    RedisClient redisClient = RedisClient.create();
    StatefulRedisMasterReplicaConnection<String, String> connect = MasterReplica.connect(
        redisClient,
        StringCodec.UTF8,
        Lists.newArrayList(
            RedisURI.builder()
                .withHost("impassive.com")
                .withPort(6379)
                .withPassword("test")
                .withVerifyPeer(false)
                .build()
        )
    );

    connect.setReadFrom(ReadFrom.REPLICA_PREFERRED);
  }

  @Test
  public void testReadWriteLock() {
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    Lock read = readWriteLock.readLock();
    Lock write = readWriteLock.writeLock();
    write.lock();
    read.lock();
    read.unlock();
    write.unlock();
  }

}
