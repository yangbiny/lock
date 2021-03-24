package com.impassive.zookeeper.distribute;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 基于Zookeeper实现的读写锁
 *
 * @author impassivey
 */
@Slf4j
public class ZookeeperReadWriteLock {

  private final ReadLock readLock;

  private final WriteLock writeLock;

  public ZookeeperReadWriteLock(ZooKeeper zooKeeper) {
    this.readLock = new ReadLock(zooKeeper);
    this.writeLock = new WriteLock(zooKeeper);
  }

  public ReadLock readLock() {
    return this.readLock;
  }

  public WriteLock writeLock() {
    return this.writeLock;
  }

  @Slf4j
  public static class ReadLock extends AbstractZookeeperLock {

    private final ZooKeeper zooKeeperClient;

    private final Map<Thread, Node> lockPath = new ConcurrentHashMap<>();

    public ReadLock(ZooKeeper zooKeeperClient) {
      this.zooKeeperClient = zooKeeperClient;
    }

    @Override
    public void lock() {
      try {
        final List<String> children = zooKeeperClient.getChildren(LOCK_PARENT, false);
        if (CollectionUtils.isNotEmpty(children)) {
          checkAndParkThread(children, false);
        }
        addLock();
      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("create path error", e);
      }
    }

    private void addLock() throws KeeperException, InterruptedException {
      final String path =
          zooKeeperClient.create(
              LOCK_READ_PATH,
              LOCK_READ_PATH.getBytes(StandardCharsets.UTF_8),
              Ids.OPEN_ACL_UNSAFE,
              CreateMode.EPHEMERAL_SEQUENTIAL);
      final Stat exists = zooKeeperClient.exists(path, this);
      if (exists == null) {
        // TODO 获取锁失败的操作
        return;
      }
      lockPath.put(Thread.currentThread(), new Node(path, exists.getVersion()));
    }

    @Override
    public void unLock() {
      final Thread thread = Thread.currentThread();
      final Node node = lockPath.get(thread);
      if (node == null || StringUtils.isEmpty(node.path)) {
        // TODO 说明没有获取到锁，却在释放锁，需要抛出异常
        return;
      }
      try {
        zooKeeperClient.delete(node.path, node.version);
        wakeUp();
      } catch (InterruptedException e) {
        log.error("unlock error : {},{}", thread, node, e);
      } catch (KeeperException e) {
        log.error("keeper error ", e);
      }
    }

    @Override
    public void process(WatchedEvent event) {
      if (event.getType() == EventType.NodeDeleted) {
        // 释放了一个读锁
      }
    }
  }

  @Slf4j
  public static class WriteLock extends AbstractZookeeperLock {

    private final ZooKeeper zooKeeperClient;

    public WriteLock(ZooKeeper zooKeeperClient) {
      this.zooKeeperClient = zooKeeperClient;
    }

    @Override
    public void lock() {
      try {
        final List<String> children = zooKeeperClient.getChildren(LOCK_WRITE_PATH, false);
        checkAndParkThread(children, true);
      } catch (KeeperException e) {
        log.error("keeper error", e);
      } catch (InterruptedException e) {
        log.error("unlock write lock error : ", e);
      }
    }

    @Override
    public void unLock() {

    }

    @Override
    public void process(WatchedEvent event) {}
  }

  @Getter
  static class Node {

    private final String path;

    private final Integer version;

    public Node(String path, Integer version) {
      this.path = path;
      this.version = version;
    }
  }
}
