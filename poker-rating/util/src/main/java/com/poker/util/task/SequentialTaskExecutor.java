package com.poker.util.task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SequentialTaskExecutor implements TaskExecutor {

  @Nonnull
  @Override
  public <T> List<T> execute(@Nonnull List<Supplier<T>> tasks) {
    List<T> result = new ArrayList<>();
    for (Supplier<T> task : tasks) {
      result.add(task.get());
    }
    return result;
  }

  @Override
  public void close() throws Exception {
    // NO-OP
  }
}
