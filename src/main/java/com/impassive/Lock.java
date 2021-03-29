package com.impassive;

/** @author impassivey */
public interface Lock {

  /** 加锁 */
  void lock();

  /** 释放锁 */
  void unLock();
}
