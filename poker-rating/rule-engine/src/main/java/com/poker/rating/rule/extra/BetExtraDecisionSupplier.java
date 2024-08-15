package com.poker.rating.rule.extra;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.Bet;
import com.poker.model.game.BetSizeCategory;
import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.BetRule.BetCategory;
import com.poker.rating.rule.RuleUtils;
import com.poker.rating.rule.point.BetPointSupplier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class BetExtraDecisionSupplier extends BaseExtraDecisionSupplier {

  private final BetPointSupplier betPointSupplier;

  public BetExtraDecisionSupplier(
      BetPointSupplier betPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    super(showdownTypeMapper);
    this.betPointSupplier = betPointSupplier;
  }

  public List<RuleDecision> extraDecisions(
      String ruleName,
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    return Stream.of(
            extraYourCardsDecision(ruleName, gameState, betCategory, playerPercentage, equity, pof),
            extraOpponentCardsDecision(
                ruleName, gameState, betCategory, playerPercentage, equity, pof),
            extraPositionDecision(ruleName, gameState, betCategory, playerPercentage, equity, pof),
            extraBetSizeDecision(ruleName, gameState, betCategory, playerPercentage, equity, pof))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Optional<RuleDecision> extraYourCardsDecision(
      String ruleName,
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var showdownType = showdownType(playerPercentage.getShowdownPercentage());
    var extraYourCardsPoints =
        betPointSupplier.extraYourCardsMatrix().get(Tuple2.of(betCategory, showdownType));
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            betCategory + " and your cards are " + showdownType,
            extraYourCardsPoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraOpponentCardsDecision(
      String ruleName,
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var opponentTopShowdownPlayer =
        RuleUtils.getCurrentOpponentTopShowdownPlayerPercentage(gameState);
    var opponentShowdownType = showdownType(opponentTopShowdownPlayer.getShowdownPercentage());
    var extraYourCardsPoints =
        betPointSupplier
            .extraOpponentCardsMatrix()
            .get(Tuple2.of(betCategory, opponentShowdownType));
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            betCategory + " and opponent cards are " + opponentShowdownType,
            extraYourCardsPoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraPositionDecision(
      String ruleName,
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var betPosition = gameState.betPosition();
    var extraBetSizePoints =
        betPointSupplier.extraPositionMatrix().get(Tuple2.of(betCategory, betPosition));
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            betCategory + " and position is " + betPosition.title(),
            extraBetSizePoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraBetSizeDecision(
      String ruleName,
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    Bet bet = gameState.currentBet();
    BetSizeCategory betSizeCategory = BetSizeCategory.from(bet);
    var extraBetSizePoints =
        betPointSupplier.extraBetSizeMatrix().get(Tuple2.of(betCategory, betSizeCategory));
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            betCategory + " and bet size is " + betSizeCategory.title(),
            extraBetSizePoints,
            playerPercentage,
            equity,
            pof));
  }
}
