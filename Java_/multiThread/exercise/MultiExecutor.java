package Java_.multiThread.exercise;
/*
 * 코딩 연습 1: 스레드 생성 - MultiExecutor
 * 이번 연습에서는 MultiExecutor를 구현해 보겠습니다.
 * 
 * 이 클래스의 클라이언트는 Runnable 작업의 목록을 생성해서 해당 목록을 MultiExecutor의 생성자에게 제공할 것입니다.
 * 
 * 클라이언트가 executeAll()을 실행하면, MultiExecutor가 주어진 모든 작업을 실행하게 됩니다.
 * 
 * 멀티코어 CPU를 최대한 활용하기 위해, 우리는 각 작업을 서로 다른 스레드로 전달해서 MultiExecutor가 모든 작업을 동시에
 * 진행하게 하려고 합니다.
 */

import java.util.*;

public class MultiExecutor {

  // Add any necessary member variables here
  private final List<Thread> threads = new ArrayList<>();

  /*
   * @param tasks to executed concurrently
   */
  public MultiExecutor(List<Runnable> tasks) {
    // Complete your code here
    for (Runnable task : tasks) {
      threads.add(new Thread(task));
    }

  }

  /**
   * Starts and executes all the tasks concurrently
   */
  public void executeAll() {
    // complete your code here
    for (Thread thread : threads) {
      if (thread.getState() == Thread.State.NEW) { // Only start threads that haven't been started yet
        thread.start();
      }
    }
  }
}