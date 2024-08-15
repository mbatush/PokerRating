package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BetType {
  CALL("call"),
  CHECK("check"),
  FOLD("fold"),
  RAISE("raise"),
  SMALL_BLIND("small-blind"),
  BIG_BLIND("big-blind"),
  ALL_IN("all-in");

  private static final Set<BetType> ALL =
      EnumSet.allOf(BetType.class).stream().collect(Collectors.toUnmodifiableSet());
  private static final Map<String, BetType> MAPPINGS =
      ALL.stream().collect(Collectors.toUnmodifiableMap(en -> en.text, Function.identity()));
  private final String text;

  BetType(String text) {
    this.text = text;
  }

  @JsonCreator
  public static BetType of(String betTypeText) {
    var betTypeEnum = MAPPINGS.get(betTypeText);
    if (betTypeEnum == null) {
      throw new IllegalArgumentException("Invalid bet type : " + betTypeText);
    }
    return betTypeEnum;
  }

  @JsonValue
  public String text() {
    return text;
  }
}
