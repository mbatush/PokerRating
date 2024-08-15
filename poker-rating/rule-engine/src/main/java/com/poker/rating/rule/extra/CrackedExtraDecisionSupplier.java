package com.poker.rating.rule.extra;

import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.point.CrackedPointSupplier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CrackedExtraDecisionSupplier extends BaseExtraDecisionSupplier {

  private final CrackedPointSupplier crackedPointSupplier;

  public CrackedExtraDecisionSupplier(
      CrackedPointSupplier crackedPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    super(showdownTypeMapper);
    this.crackedPointSupplier = crackedPointSupplier;
  }

  public List<RuleDecision> extraDecisions(
      String ruleName, GameState gameState, PlayerPercentage playerPercentage) {
    return Stream.of(
            extraYourCardsDecision(ruleName, gameState, playerPercentage),
            extraPositionDecision(ruleName, gameState, playerPercentage))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Optional<RuleDecision> extraYourCardsDecision(
      String ruleName, GameState gameState, PlayerPercentage playerPercentage) {
    var showdownType = showdownType(playerPercentage.getShowdownPercentage());
    var extraYourCardsPoints = crackedPointSupplier.extraYourCardsMatrix().get(showdownType);
    if (extraYourCardsPoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            "Cracked and your cards are " + showdownType,
            extraYourCardsPoints,
            playerPercentage));
  }

  private Optional<RuleDecision> extraPositionDecision(
      String ruleName, GameState gameState, PlayerPercentage playerPercentage) {
    var betPosition = gameState.betPosition();
    var extraBetSizePoints = crackedPointSupplier.extraPositionMatrix().get(betPosition);
    if (extraBetSizePoints == null) {
      return Optional.empty();
    }

    return Optional.of(
        ruleDecision(
            ruleName,
            gameState,
            "Cracked and position is " + betPosition.title(),
            extraBetSizePoints,
            playerPercentage));
  }
}
