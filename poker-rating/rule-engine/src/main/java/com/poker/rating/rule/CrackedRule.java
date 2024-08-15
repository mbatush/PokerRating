package com.poker.rating.rule;

import static com.poker.util.FluentUtils.not;

import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.extra.CrackedExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.CrackedPointSupplier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CrackedRule extends BaseRatingRule {

  private final CrackedPointSupplier crackedPointSupplier;
  private final CrackedExtraDecisionSupplier crackedExtraDecisionSupplier;

  public CrackedRule(
      CrackedPointSupplier crackedPointSupplier,
      ShowdownTypeMapper showdownTypeMapper,
      CrackedExtraDecisionSupplier crackedExtraDecisionSupplier) {
    super("Cracked Rule", showdownTypeMapper);
    this.crackedPointSupplier = crackedPointSupplier;
    this.crackedExtraDecisionSupplier = crackedExtraDecisionSupplier;
  }

  @Override
  public List<RuleDecision> baseExecute(RuleContext ruleContext) {
    var gameState = ruleContext.gameState();
    var userId = gameState.userId();
    var gameStateIndex = gameState.gameStateIndex();
    var currentBet = gameState.currentBet();

    if (RuleUtils.isAnyBet(currentBet)
        || RuleUtils.isFold(currentBet)
        || gameStateIndex.bettingRound() == BettingRoundType.RIVER) {
      return List.of();
    }

    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();

    List<PlayerPercentage> topWinPlayerPercentages =
        RuleUtils.getCurrentStatePlayerPercentagesOrderByWinDesc(gameState);
    if (topWinPlayerPercentages.isEmpty()) {
      throw new IllegalStateException(
          "Top winner player percentages are empty on: " + gameStateIndex);
    }
    if (topWinPlayerPercentages.size() == 1) {
      // invalid data, we should always have at least 2 players on same state bet decision
      return List.of();
    }

    PlayerPercentage playerPercentage =
        RuleUtils.getPlayerPercentage(gameStateIndexPlayerPercentage, gameStateIndex, userId);
    var topFirstPercentage = topWinPlayerPercentages.get(0);
    if (not(playerPercentage.equals(topFirstPercentage))) {
      return List.of();
    }

    var topSecondPercentage = topWinPlayerPercentages.get(1);
    if (firstWinPercentageMoreOrEqThenFifty(topFirstPercentage, topSecondPercentage)
        && RuleUtils.isNotTheWinner(gameState, topFirstPercentage.getUserId())
        && RuleUtils.isNotAnyBet(currentBet)) {
      return crackedDecisions(gameState, playerPercentage);
    } else {
      return List.of();
    }
  }

  private List<RuleDecision> crackedDecisions(
      GameState gameState, PlayerPercentage playerPercentage) {
    var crackedDecision =
        ruleDecision(
            gameState,
            "Cracked",
            getPoints(gameState.gameStateIndex().bettingRound()),
            playerPercentage);
    List<RuleDecision> extraDecisions = crackedExtraDecisions(gameState, playerPercentage);
    if (extraDecisions.isEmpty()) {
      return List.of(crackedDecision);
    } else {
      return Stream.concat(Stream.of(crackedDecision), extraDecisions.stream()).toList();
    }
  }

  private boolean firstWinPercentageMoreOrEqThenFifty(
      PlayerPercentage topFirstPercentage, PlayerPercentage topSecondPercentage) {
    return topFirstPercentage.getWinPercentage() - topSecondPercentage.getWinPercentage() >= 50;
  }

  private Long getPoints(BettingRoundType bettingRound) {
    return Objects.requireNonNull(
        crackedPointSupplier.pointsMatrix().get(bettingRound),
        () -> "Points missing for: " + bettingRound);
  }

  private List<RuleDecision> crackedExtraDecisions(
      GameState gameState, PlayerPercentage playerPercentage) {
    return crackedExtraDecisionSupplier.extraDecisions(name(), gameState, playerPercentage);
  }
}
