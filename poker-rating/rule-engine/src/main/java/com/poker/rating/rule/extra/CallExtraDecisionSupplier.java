package com.poker.rating.rule.extra;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.Bet;
import com.poker.model.game.BetSizeCategory;
import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.CallRule.CallCategory;
import com.poker.rating.rule.RuleUtils;
import com.poker.rating.rule.point.CallPointSupplier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class CallExtraDecisionSupplier extends BaseExtraDecisionSupplier {

  private final CallPointSupplier callPointSupplier;

  public CallExtraDecisionSupplier(
      CallPointSupplier callPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    super(showdownTypeMapper);
    this.callPointSupplier = callPointSupplier;
  }

  public List<RuleDecision> extraDecisions(
      String ruleName,
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    return Stream.of(
            extraYourCardsDecision(
                ruleName, gameState, callCategory, playerPercentage, equity, pof),
            extraOpponentCardsDecision(
                ruleName, gameState, callCategory, playerPercentage, equity, pof),
            extraPositionDecision(ruleName, gameState, callCategory, playerPercentage, equity, pof),
            extraBetSizeDecision(ruleName, gameState, callCategory, playerPercentage, equity, pof))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Optional<RuleDecision> extraYourCardsDecision(
      String ruleName,
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var showdownType = showdownType(playerPercentage.getShowdownPercentage());
    var extraYourCardsPoints =
        callPointSupplier.extraYourCardsMatrix().get(Tuple2.of(callCategory, showdownType));
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            callCategory + " and your cards are " + showdownType,
            extraYourCardsPoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraOpponentCardsDecision(
      String ruleName,
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var opponentTopShowdownPlayer =
        RuleUtils.getCurrentOpponentTopShowdownPlayerPercentage(gameState);
    var opponentShowdownType = showdownType(opponentTopShowdownPlayer.getShowdownPercentage());
    var extraYourCardsPoints =
        callPointSupplier
            .extraOpponentCardsMatrix()
            .get(Tuple2.of(callCategory, opponentShowdownType));
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            callCategory + " and opponent cards are " + opponentShowdownType,
            extraYourCardsPoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraPositionDecision(
      String ruleName,
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var betPosition = gameState.betPosition();
    var extraBetSizePoints =
        callPointSupplier.extraPositionMatrix().get(Tuple2.of(callCategory, betPosition));
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            callCategory + " and position is " + betPosition.title(),
            extraBetSizePoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraBetSizeDecision(
      String ruleName,
      GameState gameState,
      CallCategory callCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    Bet bet = gameState.currentBet();
    BetSizeCategory betSizeCategory = BetSizeCategory.from(bet);
    var extraBetSizePoints =
        callPointSupplier.extraBetSizeMatrix().get(Tuple2.of(callCategory, betSizeCategory));
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            callCategory + " and bet size is " + betSizeCategory.title(),
            extraBetSizePoints,
            playerPercentage,
            equity,
            pof));
  }
}
