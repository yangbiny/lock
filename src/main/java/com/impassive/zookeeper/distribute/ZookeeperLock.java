package com.impassive.zookeeper.distribute;

import javax.annotation.Resource;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;

/** @author impassivey */
@Component
public class ZookeeperLock {

  @Resource private ZooKeeper zooKeeperClient;

  private int cnt = 0;

  public void test() {

    for (int i = 0; i < 10; i++) {

    }
  }
}
