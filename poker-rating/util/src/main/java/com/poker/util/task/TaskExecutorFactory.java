package com.poker.util.task;

import java.util.concurrent.Executors;

public class TaskExecutorFactory {

  public TaskExecutor create(int parallelism) {
    if (parallelism <= 1) {
      return new SequentialTaskExecutor();
    } else {
      return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(parallelism));
    }
  }
}
