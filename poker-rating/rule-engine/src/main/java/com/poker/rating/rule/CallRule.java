package com.poker.rating.rule;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.extra.CallExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.CallPointSupplier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CallRule extends BaseRatingRule {

  private final CallPointSupplier callPointSupplier;
  private final CallExtraDecisionSupplier callExtraDecisionSupplier;

  public CallRule(
      CallPointSupplier callPointSupplier,
      ShowdownTypeMapper showdownTypeMapper,
      CallExtraDecisionSupplier callExtraDecisionSupplier) {
    super("Call Rule", showdownTypeMapper);
    this.callPointSupplier = callPointSupplier;
    this.callExtraDecisionSupplier = callExtraDecisionSupplier;
  }

  @Override
  public List<RuleDecision> baseExecute(RuleContext ruleContext) {
    var gameState = ruleContext.gameState();
    var userId = gameState.userId();
    var gameStateIndex = gameState.gameStateIndex();
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();
    var currentBet = gameState.currentBet();

    if (RuleUtils.isNotCall(currentBet)
        || gameStateIndex.bettingRound() == BettingRoundType.RIVER) {
      return List.of();
    }

    PlayerPercentage playerPercentage =
        RuleUtils.getPlayerPercentage(gameStateIndexPlayerPercentage, gameStateIndex, userId);
    double winPercentage = playerPercentage.getWinPercentage();
    double equityPercentage = RuleUtils.calcCallEquityPercentage(currentBet);
    double pof = RuleUtils.calcPOF(equityPercentage, playerPercentage.getWinPercentage());
    log.debug(
        "Player '{}' win '{}', equity '{}' and POF '{}'",
        playerPercentage.getUserId(),
        winPercentage,
        equityPercentage,
        pof);
    if (pof < 0) {
      return goodCall(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 0 && pof < 20) {
      return nonPenalizedCall(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 20 && pof < 75) {
      return badCall(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 75 && pof < 150) {
      return terribleCall(gameState, playerPercentage, equityPercentage, pof);
    } else {
      return horribleCall(gameState, playerPercentage, equityPercentage, pof);
    }
  }

  private List<RuleDecision> goodCall(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return callDecisions(gameState, CallCategory.GOOD_CALL, playerPercentage, equity, pof);
  }

  private List<RuleDecision> nonPenalizedCall(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return callDecisions(gameState, CallCategory.NON_PENALIZED_CALL, playerPercentage, equity, pof);
  }

  private List<RuleDecision> badCall(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return callDecisions(gameState, CallCategory.BAD_CALL, playerPercentage, equity, pof);
  }

  private List<RuleDecision> terribleCall(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return callDecisions(gameState, CallCategory.TERRIBLE_CALL, playerPercentage, equity, pof);
  }

  private List<RuleDecision> horribleCall(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return callDecisions(gameState, CallCategory.HORRIBLE_CALL, playerPercentage, equity, pof);
  }

  private List<RuleDecision> callDecisions(
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      double equity,
      double pof) {
    var gameStateIndex = gameState.gameStateIndex();
    var points = getPoints(gameStateIndex.bettingRound(), callCategory);
    var callDecision =
        ruleDecision(gameState, callCategory.toString(), points, playerPercentage, equity, pof);
    List<RuleDecision> extraDecisions =
        callExtraDecisions(gameState, callCategory, playerPercentage, equity, pof);
    if (extraDecisions.isEmpty()) {
      return List.of(callDecision);
    } else {
      return Stream.concat(Stream.of(callDecision), extraDecisions.stream()).toList();
    }
  }

  private List<RuleDecision> callExtraDecisions(
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      double equity,
      double pof) {
    return callExtraDecisionSupplier.extraDecisions(
        name(), gameState, callCategory, playerPercentage, equity, pof);
  }

  private Long getPoints(BettingRoundType bettingRound, CallCategory callCategory) {
    var key = Tuple2.of(bettingRound, callCategory);
    return Objects.requireNonNull(
        callPointSupplier.pointsMatrix().get(key),
        () -> "Points missing for: " + key.v1() + ", " + key.v2());
  }

  public enum CallCategory {
    GOOD_CALL("Good Call"),
    NON_PENALIZED_CALL("Non Penalized Call"),
    BAD_CALL("Bad Call"),
    TERRIBLE_CALL("Terrible Call"),
    HORRIBLE_CALL("Horrible Call");

    private final String name;

    CallCategory(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
