package com.poker.rating;

import static java.util.Objects.requireNonNull;

import com.poker.rating.rule.BetRule;
import com.poker.rating.rule.CallRule;
import com.poker.rating.rule.CrackedRule;
import com.poker.rating.rule.FoldRule;
import com.poker.rating.rule.RatingRule;
import com.poker.rating.rule.RiverRule;
import com.poker.rating.rule.extra.BetExtraDecisionSupplier;
import com.poker.rating.rule.extra.CallExtraDecisionSupplier;
import com.poker.rating.rule.extra.CrackedExtraDecisionSupplier;
import com.poker.rating.rule.extra.FoldExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.BetPointSupplier;
import com.poker.rating.rule.point.CallPointSupplier;
import com.poker.rating.rule.point.CrackedPointSupplier;
import com.poker.rating.rule.point.FoldPointSupplier;
import com.poker.rating.rule.point.RiverPointSupplier;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RatingRuleRegister {

  private final List<RatingRule> rules;

  public RatingRuleRegister(
      ShowdownTypeMapper showdownTypeMapper,
      BetPointSupplier betPointSupplier,
      BetExtraDecisionSupplier betExtraDecisionSupplier,
      CallPointSupplier callPointSupplier,
      CallExtraDecisionSupplier callExtraDecisionSupplier,
      FoldPointSupplier foldPointSupplier,
      FoldExtraDecisionSupplier foldExtraDecisionSupplier,
      RiverPointSupplier riverPointSupplier,
      CrackedPointSupplier crackedPointSupplier,
      CrackedExtraDecisionSupplier crackedExtraDecisionSupplier) {
    this(
        uniqueRules(
            List.of(
                new BetRule(betPointSupplier, showdownTypeMapper, betExtraDecisionSupplier),
                new CallRule(callPointSupplier, showdownTypeMapper, callExtraDecisionSupplier),
                new FoldRule(foldPointSupplier, showdownTypeMapper, foldExtraDecisionSupplier),
                new CrackedRule(
                    crackedPointSupplier, showdownTypeMapper, crackedExtraDecisionSupplier),
                new RiverRule(
                    riverPointSupplier,
                    showdownTypeMapper,
                    betExtraDecisionSupplier,
                    callExtraDecisionSupplier,
                    foldExtraDecisionSupplier,
                    crackedExtraDecisionSupplier))));
  }

  RatingRuleRegister() {
    this(
        new ShowdownTypeMapper(),
        new BetPointSupplier(),
        new BetExtraDecisionSupplier(new BetPointSupplier(), new ShowdownTypeMapper()),
        new CallPointSupplier(),
        new CallExtraDecisionSupplier(new CallPointSupplier(), new ShowdownTypeMapper()),
        new FoldPointSupplier(),
        new FoldExtraDecisionSupplier(new FoldPointSupplier(), new ShowdownTypeMapper()),
        new RiverPointSupplier(),
        new CrackedPointSupplier(),
        new CrackedExtraDecisionSupplier(new CrackedPointSupplier(), new ShowdownTypeMapper()));
  }

  public RatingRuleRegister(List<RatingRule> rules) {
    this.rules = List.copyOf(requireNonNull(rules));
  }

  @SuppressWarnings({"java:S2201", "ReturnValueIgnored"})
  private static List<RatingRule> uniqueRules(List<RatingRule> rules) {
    // unique name precondition check for rules
    rules.stream()
        .collect(
            Collectors.toMap(
                RatingRule::name,
                Function.identity(),
                (r1, r2) -> {
                  throw new IllegalArgumentException(
                      "The rule name is declared twice: " + r1.name());
                }));
    return rules;
  }

  public List<RatingRule> getRules() {
    return rules;
  }
}
