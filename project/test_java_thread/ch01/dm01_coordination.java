package ch01;

import java.util.LinkedList;

public class dm01_coordination {
  LinkedList<String> queue = new LinkedList<String>();
  private final int MAX_QUEUE_SIZE = 20;
  private final int MAX_THREAD_COUNT = 10;

  public void start() {
    Runnable task = new Runnable() {
      @Override
      public void run() {
        while (true) {
          String message = pullMessage();
          System.out.println(Thread.currentThread().getName() + message);
        }
      }
    };

    for (int i = 0; i < MAX_THREAD_COUNT; i++) {
      new Thread(task).start();
    }

    try {
      Thread.sleep(8000);
    } catch (InterruptedException e) {
      System.out.println("Thread.sleep(8000) InterruptedException => " + e);
    }

    for (int i = 0; i < MAX_QUEUE_SIZE; i++) {
      pushMessage("message: #" + i);
    }
  }

  private synchronized String pullMessage() {
    while(queue.isEmpty()) {
      try {
        System.out.println(Thread.currentThread().getName() + ": i am waiting for message");
        wait();
      } catch (InterruptedException e) {
        System.out.println("wait() InterruptedException => " + e);
      }
    }

    return queue.pop();
  }

  private synchronized void pushMessage(String message) {
    if (queue.size() < MAX_QUEUE_SIZE) {
      queue.push(message);
      notifyAll();
    }
  }

  public static void main(String args[]) {
    System.out.println("Hello Java!!");
    new dm01_coordination().start();
  }

}
