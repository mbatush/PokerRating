package com.poker.rating.rule;

import com.poker.model.rating.GameState;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.model.rating.ShowdownType;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.interceptor.AfterRatingRuleInterceptor;
import com.poker.rating.rule.interceptor.PreFlopNormalizerAfterRuleInterceptor;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRatingRule implements RatingRule {

  public static final List<AfterRatingRuleInterceptor> DEFAULT_AFTER_INTERCEPTORS =
      List.of(new PreFlopNormalizerAfterRuleInterceptor());
  protected final Logger log = LoggerFactory.getLogger(getClass());
  private final ShowdownTypeMapper showdownTypeMapper;
  private final String name;

  private final List<AfterRatingRuleInterceptor> afterRatingRuleInterceptors;

  protected BaseRatingRule(String name, ShowdownTypeMapper showdownTypeMapper) {
    this(name, showdownTypeMapper, DEFAULT_AFTER_INTERCEPTORS);
  }

  protected BaseRatingRule(
      String name,
      ShowdownTypeMapper showdownTypeMapper,
      List<AfterRatingRuleInterceptor> afterRatingRuleInterceptors) {
    this.name = Objects.requireNonNull(name);
    this.showdownTypeMapper = showdownTypeMapper;
    this.afterRatingRuleInterceptors = afterRatingRuleInterceptors;
  }

  @Override
  public String name() {
    return name;
  }

  protected ShowdownType showdownType(double showdownPercentage) {
    return showdownTypeMapper.showdownType(showdownPercentage);
  }

  @Override
  public final List<RuleDecision> execute(RuleContext ruleContext) {
    List<RuleDecision> ruleDecisions = baseExecute(ruleContext);

    List<List<RuleDecision>> afterRuleInterceptorDecisions =
        afterRatingRuleInterceptors.stream()
            .map(interceptor -> interceptor.afterRuleExecute(ruleDecisions, ruleContext))
            .filter(Predicate.not(List::isEmpty))
            .toList();
    if (afterRuleInterceptorDecisions.isEmpty()) {
      return ruleDecisions;
    } else {
      return Stream.concat(
              ruleDecisions.stream(),
              afterRuleInterceptorDecisions.stream().flatMap(Collection::stream))
          .toList();
    }
  }

  protected abstract List<RuleDecision> baseExecute(RuleContext ruleContext);

  protected RuleDecision ruleDecision(
      @Nonnull GameState gameState,
      @Nonnull String name,
      @Nonnull Long ratingChange,
      @Nonnull PlayerPercentage playerPercentage) {
    return ruleDecision(gameState, name, ratingChange, playerPercentage, null, null);
  }

  protected RuleDecision ruleDecision(
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
        name(),
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
