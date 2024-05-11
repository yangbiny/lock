package com.impassive.zookeeper.distribute;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author impassivey
 */
public abstract class AbstractZookeeperLock implements Lock {

  protected final ZooKeeper zooKeeperClient;

  private static final String DEFAULT_LOCK_PARENT = "/lock";

  protected static final String DEFAULT_LOCK_WRITE_PATH = "write";

  private final String lockParentPath;

  private final Map<Thread, LockPath> threadLockPath = new ConcurrentHashMap<>();

  private final Sync sync;

  public AbstractZookeeperLock(String zkUrl, String lockParentPath) {
    if (lockParentPath == null || lockParentPath.isBlank()) {
      throw new RuntimeException("lockParentPath is null");
    }
    this.lockParentPath = lockParentPath;
    this.zooKeeperClient = initZooKeeperClient(zkUrl);
    this.sync = new Sync(this);
  }

  public AbstractZookeeperLock(String zkUrl) {
    this(zkUrl, DEFAULT_LOCK_PARENT);
  }

  protected ZooKeeper initZooKeeperClient(String zkUrl) {
    try {
      return new ZooKeeper(zkUrl, 1000 * 3600, new ZookeeperClientWatcher(this));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void lock(String path) {
    LockPath lockPath = threadLockPath.get(Thread.currentThread());
    if (lockPath != null){
      // 直接去 检验分布式锁 是否还是 正常的，正常直接返回
    }
    // 需要申请 分布式锁的逻辑
    // 第一步：先去 申请锁，申请 不成功就会加入到等待队列
    sync.acquire(1);
    // 第二步：申请成功，就去创建节点
    int version = acquireDistribute(path, lockPath);

    threadLockPath.put(Thread.currentThread(), new LockPath(path, version));
  }

  private int acquireDistribute(String path, LockPath lockPath) {
    return 0;
  }


  protected boolean tryLock(String path) {
    if (path == null || path.isBlank()) {
      throw new RuntimeException("path is null");
    }

    return false;
  }


  private boolean isHeldDistributedLock() {
    LockPath lockPath = threadLockPath.get(Thread.currentThread());
    if (lockPath == null) {
      return false;
    }
    return false;
  }


  static class Sync extends AbstractQueuedSynchronizer {

    private final AbstractZookeeperLock abstractZookeeperLock;

    Sync(AbstractZookeeperLock abstractZookeeperLock) {
      this.abstractZookeeperLock = abstractZookeeperLock;
    }


    @Override
    protected boolean tryRelease(int arg) {
      Thread thread = Thread.currentThread();
      if (getExclusiveOwnerThread() != thread) {
        throw new IllegalMonitorStateException();
      }
      int c = getState() - arg;
      boolean free = (c == 0);
      if (free) {
        setExclusiveOwnerThread(null);
      }
      setState(c);
      return free;
    }

    @Override
    protected boolean tryAcquire(int arg) {
      Thread thread = Thread.currentThread();
      int c;
      if ((c = getState()) > 0 && getExclusiveOwnerThread() == thread) {
        setState(c + arg);
        return true;
      }
      if (c == 0 && compareAndSetState(0, arg)) {
        setExclusiveOwnerThread(thread);
        return true;
      }
      return false;
    }

    @Override
    protected boolean isHeldExclusively() {
      return getExclusiveOwnerThread() == Thread.currentThread()
          && getState() > 0
          && abstractZookeeperLock.isHeldDistributedLock();
    }

  }

  static class LockPath {

    String path;
    Integer version;

    public LockPath(String path, Integer version) {
      this.path = path;
      this.version = version;
    }
  }

  static class ZookeeperClientWatcher implements Watcher {

    private final AbstractZookeeperLock abstractZookeeperLock;

    public ZookeeperClientWatcher(AbstractZookeeperLock abstractZookeeperLock) {
      this.abstractZookeeperLock = abstractZookeeperLock;
    }

    @Override
    public void process(WatchedEvent event) {

    }
  }

}

