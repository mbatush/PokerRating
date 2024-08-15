package com.poker.rating.service;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.google.common.base.Stopwatch;
import com.poker.model.game.GameHand;
import com.poker.model.rating.PlayerRating;
import com.poker.model.rating.RatingCalcResult;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rest.internal.model.RatingGameCalcResponse;
import com.poker.test.IntegrationTag;
import com.poker.test.util.FileTestUtils;
import com.poker.test.util.JacksonTestUtils;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("SameNameButDifferent")
@SpringBootTest
@Testcontainers
@Slf4j
class PokerRatingCalculatorIT {

  @Container static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5");

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private PokerRatingCalculator pokerRatingCalculator;

  private static RatingGameCalcResponse toRatingGameCalcResponse(
      RatingCalcResult ratingCalcResult, Stopwatch stopwatch) {
    List<PlayerRating> newRatings =
        ratingCalcResult.newRatings().stream()
            .sorted(Comparator.comparing(PlayerRating::getUserId))
            .toList();
    List<PlayerRating> prevRatings =
        ratingCalcResult.prevRatings().stream()
            .sorted(Comparator.comparing(PlayerRating::getUserId))
            .toList();
    List<RuleDecision> decisions =
        ratingCalcResult.ruleExecResult().decisions().stream()
            .sorted(Comparator.comparing(RuleDecision::gameStateIndex))
            .toList();
    return new RatingGameCalcResponse(
        newRatings,
        prevRatings,
        ratingCalcResult.sumRuleRatingChangeByUserId(),
        decisions,
        stopwatch.stop().elapsed().toMillis());
  }

  @Test
  @IntegrationTag
  void test_sample_1() {
    GameHand gameHandSample1 =
        JacksonTestUtils.fromJson(
            FileTestUtils.readUtf8Content("games/sample-1/game-hand.json"), GameHand.class);
    Stopwatch stopwatch = Stopwatch.createStarted();
    RatingCalcResult result = pokerRatingCalculator.calculate(gameHandSample1);
    assertRatingCalcResult(result, "games/sample-1/expected.json", stopwatch);
  }

  @Test
  @IntegrationTag
  void test_sample_2() {
    GameHand gameHandSample1 =
        JacksonTestUtils.fromJson(
            FileTestUtils.readUtf8Content("games/sample-2/game-hand.json"), GameHand.class);
    Stopwatch stopwatch = Stopwatch.createStarted();
    RatingCalcResult result = pokerRatingCalculator.calculate(gameHandSample1);
    assertRatingCalcResult(result, "games/sample-2/expected.json", stopwatch);
  }

  @Test
  @IntegrationTag
  void test_integration_successful_bluff() {
    GameHand gameHandSample1 =
        JacksonTestUtils.fromJson(
            FileTestUtils.readUtf8Content("games/successful-bluff/game-hand.json"), GameHand.class);
    Stopwatch stopwatch = Stopwatch.createStarted();
    RatingCalcResult result = pokerRatingCalculator.calculate(gameHandSample1);
    assertRatingCalcResult(result, "games/successful-bluff/expected.json", stopwatch);
  }

  private void assertRatingCalcResult(
      RatingCalcResult result, String expectedJsonPath, Stopwatch stopwatch) {
    var response = toRatingGameCalcResponse(result, stopwatch);
    log.info("Executed in: {}", stopwatch);
    log.info("Result: {}", JacksonTestUtils.toJson(response));
    assertThatJson(response)
        .withTolerance(1)
        .whenIgnoringPaths(
            "operationTimeMillis",
            "prevRatings",
            "newRatings",
            "decisions[*].playerPercentages[*]",
            "decisions[*].pof")
        .isEqualTo(FileTestUtils.readUtf8Content(expectedJsonPath));
  }
}
