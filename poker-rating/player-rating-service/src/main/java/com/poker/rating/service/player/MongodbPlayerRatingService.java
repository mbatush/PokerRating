package com.poker.rating.service.player;

import com.poker.model.rating.PlayerRating;
import com.poker.rating.service.player.model.PlayerRatingDoc;
import com.poker.rating.service.player.model.PlayerStatisticDoc;
import com.poker.rating.service.player.repo.PlayerRatingRepo;
import com.poker.rating.service.player.repo.PlayerStatisticRepo;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public class MongodbPlayerRatingService implements PlayerRatingService {

  private static final Logger LOG = LoggerFactory.getLogger(MongodbPlayerRatingService.class);
  private static final FailsafeExecutor<PlayerRatingDoc> OPTIMISTIC_LOCKING_RETRY =
      optimisticLockingRetry();
  private static final FailsafeExecutor<PlayerStatisticDoc> OPTIMISTIC_LOCKING_RETRY_STATS =
      optimisticLockingRetry();

  private static <T> FailsafeExecutor<T> optimisticLockingRetry() {
    return Failsafe.with(
        RetryPolicy.<T>builder()
            .onRetriesExceeded(
                e ->
                    LOG.warn(
                        "Failed to add player rating. Max retries exceeded.", e.getException()))
            .onFailedAttempt(
                e ->
                    LOG.error(
                        "Retry add player rating attempt failed #{}",
                        e.getAttemptCount(),
                        e.getLastException()))
            .onRetry(e -> LOG.warn("Failure #{}. Retrying.", e.getAttemptCount()))
            .handle(OptimisticLockingFailureException.class)
            .withMaxAttempts(50)
            .withMaxDuration(Duration.ofMinutes(5))
            .withDelay(Duration.ofMillis(200), Duration.ofMillis(500))
            .build());
  }

  public static final long DEFAULT_RATING = 10000L;

  private final MongoTemplate mongoTemplate;
  private final PlayerRatingRepo playerRatingRepo;
  private final PlayerStatisticRepo playerStatisticRepo;

  public MongodbPlayerRatingService(
      MongoTemplate mongoTemplate,
      PlayerRatingRepo playerRatingRepo,
      PlayerStatisticRepo playerStatisticRepo) {
    this.mongoTemplate = mongoTemplate;
    this.playerRatingRepo = playerRatingRepo;
    this.playerStatisticRepo = playerStatisticRepo;
  }

  static String toDocId(String applicationId, String userId) {
    return "urn:applicationId:" + applicationId + ":userId:" + userId;
  }

  @Retryable(
      include = {DataAccessResourceFailureException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, maxDelay = 1000))
  @Override
  public Set<PlayerRating> getPlayerRatings(String applicationId, Set<String> userIds) {
    Set<PlayerRating> result = new HashSet<>();
    for (String userId : userIds) {
      long playerRating =
          playerRatingRepo
              .findById(toDocId(applicationId, userId))
              .map(PlayerRatingDoc::getRating)
              .orElse(DEFAULT_RATING);
      result.add(new PlayerRating(applicationId, userId, playerRating));
    }
    return Collections.unmodifiableSet(result);
  }

  @Retryable(
      include = {DataAccessResourceFailureException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, maxDelay = 1000))
  @Override
  public void addPlayerRatings(
      String applicationId, String sessionId, Map<String, Long> addRatingChangeByUserId) {
    for (Map.Entry<String, Long> ratingUserEntry : addRatingChangeByUserId.entrySet()) {
      String userId = ratingUserEntry.getKey();
      long addRating = Objects.requireNonNullElse(ratingUserEntry.getValue(), 0L);
      PlayerRatingDoc newPlayerRatingDoc =
          addPlayerRatingWithOptimisticLockingRetry(applicationId, sessionId, userId, addRating);
      LOG.debug(
          "Player '{}' (applicationId '{}') rating bumped. Current rating is: {}",
          userId,
          applicationId,
          newPlayerRatingDoc.getRating());
    }
  }

  @Retryable(
      include = {DataAccessResourceFailureException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, maxDelay = 1000))
  @Override
  public Optional<PlayerRatingDoc> getPlayerRatingDoc(String applicationId, String userId) {
    return playerRatingRepo.findById(toDocId(applicationId, userId));
  }

  @Retryable(
      include = {DataAccessResourceFailureException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, maxDelay = 1000))
  @Override
  public PlayerRatingDoc resetPlayerRating(String applicationId, String userId, long rating) {
    playerRatingRepo.deleteById(toDocId(applicationId, userId));
    playerStatisticRepo.deleteById(toDocId(applicationId, userId));
    return findOrCreate(applicationId, userId, "initial-on-reset", rating);
  }

  @Retryable(
      include = {DataAccessResourceFailureException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, maxDelay = 1000))
  @Override
  public PlayerStatisticDoc appendPlayerStatistic(PlayerStatisticDoc playerStatistic) {
    return addPlayerStatisticWithOptimisticLockingRetry(playerStatistic);
  }

  @Override
  public Optional<PlayerStatisticDoc> getPlayerStatisticDoc(String applicationId, String userId) {
    return playerStatisticRepo.findById(toDocId(applicationId, userId));
  }

  PlayerStatisticDoc addPlayerStatisticWithOptimisticLockingRetry(
      PlayerStatisticDoc playerStatistic) {
    return OPTIMISTIC_LOCKING_RETRY_STATS.get(() -> appendPlayerStatisticInternal(playerStatistic));
  }

  PlayerStatisticDoc appendPlayerStatisticInternal(PlayerStatisticDoc playerStatistic) {
    PlayerStatisticDoc existing = findOrCreatePlayerStats(playerStatistic);
    PlayerStatisticDoc newStats = existing.append(playerStatistic);
    return mongoTemplate.save(newStats);
  }

  PlayerStatisticDoc findOrCreatePlayerStats(PlayerStatisticDoc playerStatistic) {
    String docId = playerStatistic.getId();
    try {
      Optional<PlayerStatisticDoc> existing = playerStatisticRepo.findById(docId);
      if (existing.isEmpty()) {
        return mongoTemplate.save(
            PlayerStatisticDoc.builder()
                .id(docId)
                .applicationId(playerStatistic.getApplicationId())
                .userId(playerStatistic.getUserId())
                .build());
      }
      return existing.get();
    } catch (DuplicateKeyException duplicateKeyException) {
      LOG.warn("PlayerStatisticDoc with id '{}' already inserted", docId);
      return playerStatisticRepo
          .findById(docId)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "PlayerStatisticDoc is not returned on findById and DuplicateKeyException, id is: "
                          + docId));
    }
  }

  PlayerRatingDoc addPlayerRatingWithOptimisticLockingRetry(
      String applicationId, String sessionId, String userId, long addRating) {
    return OPTIMISTIC_LOCKING_RETRY.get(
        () -> addPlayerRating(applicationId, sessionId, userId, addRating));
  }

  PlayerRatingDoc addPlayerRating(
      String applicationId, String sessionId, String userId, long addRating) {
    PlayerRatingDoc playerRatingDoc =
        findOrCreateWithDefaultRating(applicationId, userId, sessionId);
    PlayerRatingDoc incrementedPlayerRatingDoc =
        playerRatingDoc.incrementRating(addRating, sessionId);
    return mongoTemplate.save(incrementedPlayerRatingDoc);
  }

  PlayerRatingDoc findOrCreateWithDefaultRating(
      String applicationId, String userId, String sessionId) {
    return findOrCreate(applicationId, userId, sessionId, DEFAULT_RATING);
  }

  PlayerRatingDoc findOrCreate(String applicationId, String userId, String sessionId, long rating) {
    String docId = toDocId(applicationId, userId);
    try {
      Optional<PlayerRatingDoc> existing = playerRatingRepo.findById(docId);
      if (existing.isEmpty()) {
        var playerRatingDoc =
            new PlayerRatingDoc(
                docId,
                applicationId,
                userId,
                rating,
                Instant.now(),
                sessionId,
                new TreeSet<>(),
                null);
        return mongoTemplate.save(playerRatingDoc);
      }
      return existing.get();
    } catch (DuplicateKeyException duplicateKeyException) {
      LOG.warn("PlayerRatingDoc with id '{}' already inserted", docId);
      return playerRatingRepo
          .findById(docId)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "PlayerRatingDoc is not returned on findById on DuplicateKeyException, id is: "
                          + docId));
    }
  }
}
