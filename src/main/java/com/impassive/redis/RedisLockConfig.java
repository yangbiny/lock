package com.impassive.redis;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceSentinelConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/** @author impassivey */
@Configuration
public class RedisLockConfig {

  @Setter
  @Value("${redis.url}")
  private String redisUrl;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    final String[] split = redisUrl.split(":");
    RedisStandaloneConfiguration configuration =
        new RedisStandaloneConfiguration(split[0], Integer.parseInt(split[1]));
    configuration.setPassword("test");
    configuration.setDatabase(0);
    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(RedisSerializer.string());
    return redisTemplate;
  }
}
