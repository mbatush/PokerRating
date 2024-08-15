package com.poker.rating.service.player;

import com.poker.model.rating.PlayerRating;
import com.poker.rating.service.player.model.PlayerRatingDoc;
import com.poker.rating.service.player.model.PlayerRatingHistory;
import com.poker.rating.service.player.repo.PlayerRatingRepo;
import com.poker.rating.service.player.repo.PlayerStatisticRepo;
import com.poker.util.task.TaskExecutor;
import com.poker.util.task.TaskExecutorFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("SameNameButDifferent")
@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ResourceLock("MongodbPlayerRatingServiceTest")
class MongodbPlayerRatingServiceTest {

  @SpringBootApplication
  @EnableRetry
  static class TestBootApplication {
    @Bean
    public PlayerRatingService playerRatingService(
        MongoTemplate mongoTemplate,
        PlayerRatingRepo playerRatingRepo,
        PlayerStatisticRepo playerStatisticRepo) {
      return new MongodbPlayerRatingService(mongoTemplate, playerRatingRepo, playerStatisticRepo);
    }
  }

  @Container static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5");

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private PlayerRatingService playerRatingService;

  @BeforeEach
  void setUp() {
    mongoTemplate.dropCollection(PlayerRatingDoc.COLLECTION_NAME);
  }

  @Test
  void test_add_get_player_ratings() {
    var applicationId = "app1";
    var user = "user1";
    playerRatingService.addPlayerRatings(applicationId, "session1", Map.of(user, 20L));
    playerRatingService.addPlayerRatings(applicationId, "session2", Map.of(user, 30L));
    playerRatingService.addPlayerRatings(applicationId, "session3", Map.of(user, 40L));

    Set<PlayerRating> playerRatings =
        playerRatingService.getPlayerRatings(applicationId, Set.of(user));
    long expectedRating = 10090L;
    Assertions.assertThat(playerRatings)
        .singleElement()
        .usingRecursiveComparison()
        .isEqualTo(new PlayerRating(applicationId, user, expectedRating));
    Optional<PlayerRatingDoc> playerRatingDocOpt =
        playerRatingService.getPlayerRatingDoc(applicationId, user);
    Assertions.assertThat(playerRatingDocOpt).isPresent();

    PlayerRatingDoc playerRatingDoc = playerRatingDocOpt.get();
    NavigableSet<PlayerRatingHistory> expectedRatingHistories =
        new TreeSet<>(
            Set.of(
                new PlayerRatingHistory(2L, Instant.now(), 10050L, "session2"),
                new PlayerRatingHistory(1L, Instant.now(), 10020L, "session1"),
                new PlayerRatingHistory(0L, Instant.now(), 10000L, "session1")));
    var expectedPlayerRatingDoc =
        new PlayerRatingDoc(
            "urn:applicationId:app1:userId:user1",
            applicationId,
            user,
            expectedRating,
            Instant.now(),
            "session3",
            expectedRatingHistories,
            3L);
    Assertions.assertThat(playerRatingDoc)
        .usingRecursiveComparison()
        .ignoringFields("updatedAt", "ratingHistories.updatedAt")
        .isEqualTo(expectedPlayerRatingDoc);
  }

  @Test
  void test_add_player_ratings_parallel() throws Exception {
    int parallelSize = 25;
    var applicationId = "app1";
    var user = "user1";
    List<Supplier<Void>> tasks =
        IntStream.range(0, parallelSize)
            .mapToObj(
                i ->
                    addPlayerRatingsSupplier(
                        applicationId, "sessionId" + (i + 1), Map.of(user, 10 + i * 10L)))
            .collect(Collectors.toList());
    long expectedRating = IntStream.range(0, parallelSize).map(i -> 10 + i * 10).sum() + 10000L;
    try (TaskExecutor parallelExecutor = new TaskExecutorFactory().create(25)) {
      parallelExecutor.execute(tasks);
    }

    PlayerRatingDoc playerRatingDoc =
        playerRatingService.getPlayerRatingDoc(applicationId, user).orElseThrow();
    Assertions.assertThat(playerRatingDoc.getRating()).isEqualTo(expectedRating);
    Assertions.assertThat(playerRatingDoc.getVersion()).isEqualTo(parallelSize);
    Assertions.assertThat(playerRatingDoc.getRatingHistories()).hasSize(parallelSize);
  }

  Supplier<Void> addPlayerRatingsSupplier(
      String applicationId, String sessionId, Map<String, Long> addRatingChangeByUserId) {
    return () -> {
      playerRatingService.addPlayerRatings(applicationId, sessionId, addRatingChangeByUserId);
      return null;
    };
  }
}
