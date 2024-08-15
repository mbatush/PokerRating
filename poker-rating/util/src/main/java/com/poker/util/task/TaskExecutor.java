package com.poker.util.task;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public interface TaskExecutor extends AutoCloseable {
  @Nonnull
  <T> List<T> execute(@Nonnull List<Supplier<T>> tasks);
}
