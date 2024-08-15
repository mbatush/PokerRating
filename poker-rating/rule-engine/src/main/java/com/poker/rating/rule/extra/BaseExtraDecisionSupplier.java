package com.poker.rating.rule.extra;

import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleDecision;
import com.poker.model.rating.ShowdownType;
import com.poker.rating.rule.RuleUtils;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseExtraDecisionSupplier {

  private final ShowdownTypeMapper showdownTypeMapper;

  protected BaseExtraDecisionSupplier(ShowdownTypeMapper showdownTypeMapper) {
    this.showdownTypeMapper = showdownTypeMapper;
  }

  protected ShowdownType showdownType(double showdownPercentage) {
    return showdownTypeMapper.showdownType(showdownPercentage);
  }

  protected RuleDecision ruleDecision(
      @Nonnull String ruleName,
      @Nonnull GameState gameState,
      @Nonnull String name,
      @Nonnull Long ratingChange,
      @Nonnull PlayerPercentage playerPercentage) {
    return ruleDecision(ruleName, gameState, name, ratingChange, playerPercentage, null, null);
  }

  protected RuleDecision ruleDecision(
      @Nonnull String ruleName,
      @Nonnull GameState gameState,
      @Nonnull String name,
      @Nonnull Long ratingChange,
      @Nonnull PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof,
      String... messages) {
    var gameStateIndex = gameState.gameStateIndex();
    var listMessages = Stream.of(messages).filter(Predicate.not(Objects::isNull)).toList();
    return new RuleDecision(
        gameState.currentBet(),
        ruleName,
        gameStateIndex,
        name,
        ratingChange,
        playerPercentage.getWinPercentage(),
        playerPercentage.getShowdownPercentage(),
        RuleUtils.getCurrentStatePlayerPercentagesOrderByWinDesc(gameState),
        equity,
        pof,
        listMessages);
  }
}
