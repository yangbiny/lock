package com.impassive.zookeeper.distribute;

import com.impassive.Lock;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
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

  private static final String LOCK_READ_PATH = "/lock/read";

  private static final String LOCK_WRITE_PATH = "/lock/write";

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
  public static class ReadLock implements Lock {

    private final ZooKeeper zooKeeperClient;

    public ReadLock(ZooKeeper zooKeeperClient) {
      this.zooKeeperClient = zooKeeperClient;
    }

    @Override
    public void lock() {
      try {
        final String path =
            zooKeeperClient.create(
                LOCK_READ_PATH,
                LOCK_READ_PATH.getBytes(StandardCharsets.UTF_8),
                Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

        final Stat exists = zooKeeperClient.exists(path, false);
        if (exists != null) {
          log.info("exits : {}", exists);
        }
      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("create path error", e);
      }
    }

    @Override
    public void unLock() {}
  }

  @Slf4j
  public static class WriteLock implements Lock {

    private final ZooKeeper zooKeeperClient;

    public WriteLock(ZooKeeper zooKeeperClient) {
      this.zooKeeperClient = zooKeeperClient;
    }

    @Override
    public void lock() {}

    @Override
    public void unLock() {}
  }
}
