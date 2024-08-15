package com.poker.util.task;

import static com.poker.util.FluentUtils.not;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentTaskExecutor implements TaskExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(ConcurrentTaskExecutor.class);

  @Nonnull private final ExecutorService executor;

  public ConcurrentTaskExecutor(@Nonnull ExecutorService executor) {
    this.executor = Objects.requireNonNull(executor);
  }

  @Nonnull
  @Override
  public <T> List<T> execute(@Nonnull List<Supplier<T>> tasks) {
    List<CompletableFuture<T>> taskFeatures =
        tasks.stream().map(t -> CompletableFuture.supplyAsync(t, executor)).toList();
    CompletableFuture.allOf(taskFeatures.toArray(CompletableFuture[]::new)).join();
    return taskFeatures.stream().map(CompletableFuture::join).toList();
  }

  @Override
  public void close() throws Exception {
    executor.shutdown();
    boolean terminated = executor.awaitTermination(30, TimeUnit.SECONDS);
    if (not(terminated)) {
      LOG.warn("Could not terminate executor tasks in 30 sec");
    }
  }
}
