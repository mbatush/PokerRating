package com.poker.rating.service;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.poker.model.game.GameHand;
import com.poker.model.game.Player;
import com.poker.model.rating.PlayerRating;
import com.poker.model.rating.RatingCalcResult;
import com.poker.model.rating.RatingRuleExecResult;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.RatingRuleEngine;
import com.poker.rating.service.player.PlayerRatingService;
import com.poker.rating.service.player.model.PlayerStatisticDoc;
import com.poker.rating.service.player.model.PlayerStatisticDoc.PlayerStatisticDocBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor
@Slf4j
public class PokerRatingCalculator {
  @SuppressWarnings("UnstableApiUsage")
  private final RangeMap<Long, RatingPenality> coefficientRatingChange =
      ImmutableRangeMap.<Long, RatingPenality>builder()
          .put(Range.lessThan(4000L), RatingPenality.of(0.8, 1.2))
          .put(Range.closedOpen(4000L, 8000L), RatingPenality.of(0.6, 1.4))
          .put(Range.closedOpen(8000L, 12000L), RatingPenality.of(0.5, 1.5))
          .put(Range.closedOpen(12000L, 16000L), RatingPenality.of(0.4, 1.6))
          .put(Range.closedOpen(16000L, 20000L), RatingPenality.of(0.3, 1.7))
          .put(Range.closedOpen(20000L, 24000L), RatingPenality.of(0.2, 2.0))
          .put(Range.closedOpen(24000L, 28000L), RatingPenality.of(0.15, 2.0))
          .put(Range.closedOpen(28000L, 32000L), RatingPenality.of(0.12, 2.0))
          .put(Range.atLeast(32000L), RatingPenality.of(0.1, 2.0))
          .build();

  @Nonnull private final RatingRuleEngine ratingRuleEngine;
  @Nonnull private final PlayerRatingService playerRatingService;

  public RatingCalcResult calculate(GameHand gameHand) {
    RatingRuleExecResult ruleExecResult = ratingRuleEngine.executeRules(gameHand);
    String applicationId = gameHand.getApplicationId();
    var userIds =
        gameHand.getPlayers().stream()
            .map(Player::getUserId)
            .collect(Collectors.toUnmodifiableSet());
    Set<PlayerRating> playerRatings = playerRatingService.getPlayerRatings(applicationId, userIds);
    Map<String, Long> sumRuleRatingChangeByUserId =
        ruleExecResult.decisions().stream()
            .collect(
                Collectors.groupingBy(
                    r -> r.bet().getUserId(), Collectors.summingLong(RuleDecision::ratingChange)));
    Map<String, Long> finalRatingChangeByUserId =
        sumRuleRatingChangeByUserId.entrySet().stream()
            .map(
                e ->
                    Map.entry(
                        e.getKey(),
                        penalize(e.getValue(), playerRatings, applicationId, e.getKey())))
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    playerRatingService.addPlayerRatings(
        applicationId, gameHand.getSessionId(), finalRatingChangeByUserId);
    appendPlayerStatistics(gameHand, ruleExecResult, finalRatingChangeByUserId);
    Set<PlayerRating> newPlayerRatings =
        playerRatingService.getPlayerRatings(applicationId, userIds);
    return new RatingCalcResult(
        newPlayerRatings, playerRatings, ruleExecResult, sumRuleRatingChangeByUserId);
  }

  void appendPlayerStatistics(
      GameHand gameHand,
      RatingRuleExecResult ruleExecResult,
      Map<String, Long> finalRatingChangeByUserId) {
    try {
      Map<String, List<RuleDecision>> decisionsByUserId =
          ruleExecResult.decisions().stream()
              .collect(Collectors.groupingBy(r -> r.bet().getUserId()));
      decisionsByUserId.forEach(
          (userId, decisions) ->
              appendPlayerStatistic(
                  userId,
                  gameHand.getApplicationId(),
                  decisions,
                  Objects.requireNonNull(
                      finalRatingChangeByUserId.get(userId),
                      () -> "Missing required final rating change for user ID: " + userId)));
    } catch (Exception e) {
      log.error(
          "Could not append player statistics for game hand session ID '{}' and application '{}'",
          gameHand.getSessionId(),
          gameHand.getApplicationId(),
          e);
    }
  }

  void appendPlayerStatistic(
      String userId,
      String applicationId,
      List<RuleDecision> ruleDecisions,
      Long finalRatingChange) {
    try {
      PlayerStatisticDocBuilder statisticBuilder = PlayerStatisticDoc.builder();
      statisticBuilder
          .id(PlayerStatisticDoc.toDocId(applicationId, userId))
          .applicationId(applicationId)
          .userId(userId);
      if (finalRatingChange > 0) {
        statisticBuilder.maxPositiveSumFinalDecisions(finalRatingChange);
        statisticBuilder.totalPositiveFinalSumDecisions(1L);
      } else if (finalRatingChange < 0) {
        statisticBuilder.maxNegativeSumFinalDecisions(finalRatingChange);
        statisticBuilder.totalNegativeFinalSumDecisions(1L);
      }
      statisticBuilder.totalPositiveDecisions(
          ruleDecisions.stream().filter(ruleDecision -> ruleDecision.ratingChange() > 0).count());
      statisticBuilder.totalNegativeDecisions(
          ruleDecisions.stream().filter(ruleDecision -> ruleDecision.ratingChange() < 0).count());
      statisticBuilder.totalPositiveDecisionsPerRound(
          ruleDecisions.stream()
              .filter(ruleDecision -> ruleDecision.ratingChange() > 0)
              .collect(
                  Collectors.groupingBy(
                      t -> t.gameStateIndex().bettingRound().text(), Collectors.counting())));
      statisticBuilder.totalNegativeDecisionsPerRound(
          ruleDecisions.stream()
              .filter(ruleDecision -> ruleDecision.ratingChange() < 0)
              .collect(
                  Collectors.groupingBy(
                      t -> t.gameStateIndex().bettingRound().text(), Collectors.counting())));
      Map<String, Map<String, Long>> totalRuleCounts =
          ruleDecisions.stream()
              .collect(
                  Collectors.groupingBy(
                      RuleDecision::ruleName,
                      Collectors.groupingBy(RuleDecision::name, Collectors.counting())));
      statisticBuilder.totalRulesCount(totalRuleCounts);
      playerRatingService.appendPlayerStatistic(statisticBuilder.build());
    } catch (Exception e) {
      log.error(
          "Could not append player statistic for user ID '{}' and application '{}'",
          userId,
          applicationId,
          e);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  long penalize(
      Long newRatingChange,
      Set<PlayerRating> existingPlayerRatings,
      String applicationId,
      String userId) {

    long currentRating =
        existingPlayerRatings.stream()
            .filter(p -> p.equals(new PlayerRating(applicationId, userId, 0)))
            .mapToLong(PlayerRating::getRating)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Could not find existing rating for application '"
                            + applicationId
                            + "' and user '"
                            + userId
                            + "'"));

    RatingPenality ratingPenality =
        Objects.requireNonNull(
            coefficientRatingChange.get(currentRating),
            () -> "Missing penalization coefficient for rating: " + currentRating);

    return ratingPenality.penalize(newRatingChange);
  }

  private static final class RatingPenality {
    private final double positivePenality;
    private final double negativePenality;

    private RatingPenality(double positivePenality, double negativePenality) {
      this.positivePenality = positivePenality;
      this.negativePenality = negativePenality;
    }

    static RatingPenality of(double positivePenality, double negativePenality) {
      return new RatingPenality(positivePenality, negativePenality);
    }

    long penalize(long rating) {
      if (rating < 0) {
        return Math.round(rating * negativePenality);
      } else if (rating == 0) {
        return rating;
      } else {
        return Math.round(rating * positivePenality);
      }
    }
  }
}
