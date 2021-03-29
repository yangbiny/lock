package com.impassive.zookeeper.distribute;

import com.google.common.collect.Lists;
import com.impassive.Lock;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/** @author impassivey */
@Slf4j
public abstract class AbstractZookeeperLock implements Lock, Watcher {

  private ZooKeeper zooKeeperClient;

  protected static final String LOCK_PARENT = "/lock";

  protected static final String LOCK_READ_PATH = "/lock/read";

  protected static final String LOCK_WRITE_PATH = "/lock/write";

  protected final List<String> READ_LOCK = Lists.newArrayList("read");

  protected final List<String> WRITE_LOCK = Lists.newArrayList("read", "write");

  private final BlockingQueue<Thread> blockingQueue = new LinkedBlockingDeque<>(100);

  private final Map<Thread, Node> lockPath = new ConcurrentHashMap<>();

  protected void init(ZooKeeper zooKeeper) {
    this.zooKeeperClient = zooKeeper;
  }

  protected void wakeUp() {
    final Thread take = blockingQueue.peek();
    if (take == null) {
      return;
    }
    LockSupport.unpark(take);
  }

  protected void addLock(Boolean write) throws KeeperException, InterruptedException {
    final String path1 = write ? LOCK_WRITE_PATH : LOCK_READ_PATH;
    final List<String> children = zooKeeperClient.getChildren(LOCK_PARENT, false);
    // TODO 这里其实有问题，如果两个都同时检测通过，会出现一把锁被重复申请
    if (CollectionUtils.isNotEmpty(children)) {
      checkAndParkThread(children, write);
    }
    final String path =
        zooKeeperClient.create(
            path1,
            path1.getBytes(StandardCharsets.UTF_8),
            Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
    final Stat exists = zooKeeperClient.exists(path, this);
    if (exists == null) {
      // TODO 获取锁失败的操作
      return;
    }
    lockPath.put(Thread.currentThread(), new Node(path, exists.getVersion()));
  }

  protected void checkAndParkThread(List<String> childrenLock, boolean write) {
    if (CollectionUtils.isEmpty(childrenLock)) {
      return;
    }
    List<String> path = write ? WRITE_LOCK : READ_LOCK;
    boolean hasPath = false;
    for (String s : childrenLock) {
      for (String s1 : path) {
        if (StringUtils.startsWithIgnoreCase(s, s1)) {
          continue;
        }
        hasPath = true;
        break;
      }
    }
    if (hasPath) {
      final Thread thread = Thread.currentThread();
      blockingQueue.add(thread);
      // 阻塞
      LockSupport.park();
    }
  }

  @Override
  public void unLock() {
    try {
      Thread.sleep(30000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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
