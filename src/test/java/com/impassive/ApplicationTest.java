package com.impassive;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Before;
import org.junit.Test;

/** Unit test for simple App. */
public class ApplicationTest {

  private ReentrantLock reentrantLock;

  @Before
  public void setUp() throws Exception {
    reentrantLock = new ReentrantLock();
  }


  @Test
  public void shouldAnswerWithTrue() throws InterruptedException {
    final Condition condition = reentrantLock.newCondition();
    reentrantLock.lock();
    condition.await();
    reentrantLock.unlock();
  }
}
