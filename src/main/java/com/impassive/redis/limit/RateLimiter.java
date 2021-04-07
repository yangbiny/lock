package com.impassive.redis.limit;

import com.google.common.collect.Lists;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/** @author impassivey */
public class RateLimiter {

  private static final String SCRIPT = "";

  private final StringRedisTemplate redisTemplate;

  private DefaultRedisScript<Integer> redisScript;

  public RateLimiter(StringRedisTemplate stringRedisTemplate) {
    redisTemplate = stringRedisTemplate;
    init();
  }

  private void init() {
    redisScript = new DefaultRedisScript<>();
    redisScript.setScriptSource(
        new ResourceScriptSource(new ClassPathResource("RedisLimiter.lua")));
    redisScript.setResultType(Integer.class);
    redisTemplate.execute(redisScript,Lists.newArrayList("init"),1000,10);
  }

  public void acquire() {
    redisTemplate.execute(redisScript, Lists.newArrayList());
  }
}
