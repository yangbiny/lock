package com.github.impassive.zookeeper;

import com.github.impassive.core.AbstractDistributeLock;
import java.io.IOException;
import java.util.List;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author impassive
 */
public class ZookeeperLock extends AbstractDistributeLock {

  private final ZooKeeper zooKeeper;

  private final String zkUrl;

  private final ZookeeperClientWatcher watcher;

  public ZookeeperLock(String zkUrl) {
    if (zkUrl == null || zkUrl.isBlank()) {
      throw new RuntimeException("zkUrl is null");
    }
    this.zkUrl = zkUrl;
    this.watcher = new ZookeeperClientWatcher();
    this.zooKeeper = initZookeeper();
  }

  private ZooKeeper initZookeeper() {
    try {
      return new ZooKeeper(zkUrl, 1000 * 3600, watcher);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  protected boolean doLock(List<String> keys) {
    return false;
  }

  @Override
  protected boolean doUnLock(List<String> keys) {
    return false;
  }


  static class ZookeeperClientWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
    }
  }
}
