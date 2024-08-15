package com.poker.rating.rule.extra;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.Bet;
import com.poker.model.game.BetSizeCategory;
import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.FoldRule.FoldCategory;
import com.poker.rating.rule.RuleUtils;
import com.poker.rating.rule.point.FoldPointSupplier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class FoldExtraDecisionSupplier extends BaseExtraDecisionSupplier {

  private final FoldPointSupplier foldPointSupplier;

  public FoldExtraDecisionSupplier(
      FoldPointSupplier foldPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    super(showdownTypeMapper);
    this.foldPointSupplier = foldPointSupplier;
  }

  public List<RuleDecision> extraDecisions(
      String ruleName,
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    return Stream.of(
            extraYourCardsDecision(
                ruleName, gameState, foldCategory, playerPercentage, equity, pof),
            extraOpponentCardsDecision(
                ruleName, gameState, foldCategory, playerPercentage, equity, pof),
            extraPositionDecision(ruleName, gameState, foldCategory, playerPercentage, equity, pof),
            extraBetSizeDecision(ruleName, gameState, foldCategory, playerPercentage, equity, pof))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Optional<RuleDecision> extraYourCardsDecision(
      String ruleName,
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var showdownType = showdownType(playerPercentage.getShowdownPercentage());
    var extraYourCardsPoints =
        foldPointSupplier.extraYourCardsMatrix().get(Tuple2.of(foldCategory, showdownType));
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            foldCategory + " and your cards are " + showdownType,
            extraYourCardsPoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraOpponentCardsDecision(
      String ruleName,
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var opponentTopShowdownPlayer =
        RuleUtils.getCurrentOpponentTopShowdownPlayerPercentage(gameState);
    var opponentShowdownType = showdownType(opponentTopShowdownPlayer.getShowdownPercentage());
    var extraYourCardsPoints =
        foldPointSupplier
            .extraOpponentCardsMatrix()
            .get(Tuple2.of(foldCategory, opponentShowdownType));
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            foldCategory + " and opponent cards are " + opponentShowdownType,
            extraYourCardsPoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraPositionDecision(
      String ruleName,
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var betPosition = gameState.betPosition();
    var extraBetSizePoints =
        foldPointSupplier.extraPositionMatrix().get(Tuple2.of(foldCategory, betPosition));
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            foldCategory + " and position is " + betPosition.title(),
            extraBetSizePoints,
            playerPercentage,
            equity,
            pof));
  }

  private Optional<RuleDecision> extraBetSizeDecision(
      String ruleName,
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    Bet bet = gameState.currentBet();
    BetSizeCategory betSizeCategory = BetSizeCategory.from(bet);
    var extraBetSizePoints =
        foldPointSupplier.extraBetSizeMatrix().get(Tuple2.of(foldCategory, betSizeCategory));
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            foldCategory + " and bet size is " + betSizeCategory.title(),
            extraBetSizePoints,
            playerPercentage,
            equity,
            pof));
  }
}
