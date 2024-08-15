package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BettingRoundType {
  PRE_FLOP("pre-flop", 0),
  FLOP("flop", 1),
  TURN("turn", 2),
  RIVER("river", 3);

  private static final Set<BettingRoundType> ALL =
      EnumSet.allOf(BettingRoundType.class).stream().collect(Collectors.toUnmodifiableSet());
  private static final Map<String, BettingRoundType> MAPPINGS =
      ALL.stream().collect(Collectors.toUnmodifiableMap(en -> en.text, Function.identity()));

  private final String text;
  private final int order;

  BettingRoundType(String text, int order) {
    this.text = text;
    this.order = order;
  }

  @JsonCreator
  public static BettingRoundType of(String bettingRound) {
    var bettingRoundEnum = MAPPINGS.get(bettingRound);
    if (bettingRoundEnum == null) {
      throw new IllegalArgumentException("Invalid betting round : " + bettingRound);
    }
    return bettingRoundEnum;
  }

  @JsonValue
  public String text() {
    return text;
  }

  public int order() {
    return order;
  }
}
