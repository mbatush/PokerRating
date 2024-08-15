package com.poker.rating.rule.interceptor;

import com.poker.model.game.Bet;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.RuleUtils;
import java.util.List;

public class PreFlopNormalizerAfterRuleInterceptor implements AfterRatingRuleInterceptor {

  @Override
  public List<RuleDecision> afterRuleExecute(
      List<RuleDecision> ruleDecisions, RuleContext ruleContext) {
    var gameState = ruleContext.gameState();
    var gameStateIndex = gameState.gameStateIndex();
    if (gameStateIndex.bettingRound() != BettingRoundType.PRE_FLOP || ruleDecisions.isEmpty()) {
      return List.of();
    }
    List<Bet> previousPreFlopBets = RuleUtils.getPreviousRoundBets(gameState);
    int decisionNumber = decisionNumberForUser(previousPreFlopBets, gameState.userId());
    Decision decision = toDecision(decisionNumber);

    return List.of(normalizeRuleDecision(gameState, ruleDecisions, decision));
  }

  private RuleDecision normalizeRuleDecision(
      GameState gameState, List<RuleDecision> ruleDecisions, Decision decision) {
    long sumDecisions = ruleDecisions.stream().mapToLong(RuleDecision::ratingChange).sum();
    double normalizeCoefficient = decisionToPercentage(decision);
    long normalizeRatingChange = -(sumDecisions - Math.round(sumDecisions * normalizeCoefficient));
    PlayerPercentage playerPercentage =
        RuleUtils.getPlayerPercentage(
            gameState.gameStateIndexPlayerPercentage(),
            gameState.gameStateIndex(),
            gameState.userId());
    String normalizeMessage =
        String.format(
            "Normalize Pre-Flop total rule decisions '%d' to '%.1f' coefficient on '%s' decision",
            sumDecisions, normalizeCoefficient, decision);
    return new RuleDecision(
        gameState.currentBet(),
        "Normalize Pre-Flop",
        gameState.gameStateIndex(),
        "Normalize Pre-Flop gain/lost points",
        normalizeRatingChange,
        playerPercentage.getWinPercentage(),
        playerPercentage.getShowdownPercentage(),
        RuleUtils.getCurrentStatePlayerPercentagesOrderByWinDesc(gameState),
        null,
        null,
        List.of(normalizeMessage));
  }

  double decisionToPercentage(Decision decision) {
    return switch (decision) {
      case FIRST -> 0.1d;
      case SECOND -> 0.2d;
      case THIRD_N -> 0.3d;
    };
  }

  int decisionNumberForUser(List<Bet> previousPreFlopBets, String userId) {
    return (int)
            previousPreFlopBets.stream()
                .filter(bet -> userId.equals(bet.getUserId()))
                .filter(RuleUtils::isNotAnyBlind)
                .count()
        + 1;
  }

  Decision toDecision(int decisionNumber) {
    if (decisionNumber < 1) {
      throw new IllegalStateException("decisionNumber cannot be less then 1");
    } else if (decisionNumber == 1) {
      return Decision.FIRST;
    } else if (decisionNumber == 2) {
      return Decision.SECOND;
    } else {
      return Decision.THIRD_N;
    }
  }

  enum Decision {
    FIRST,
    SECOND,
    THIRD_N
  }
}
