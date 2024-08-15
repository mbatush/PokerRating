package com.poker.model.game;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CardValue {
  ACE('A', 14),
  KING('K', 13),
  QUEEN('Q', 12),
  JACK('J', 11),
  TEN('T', 10),
  NINE('9', 9),
  EIGHT('8', 8),
  SEVEN('7', 7),
  SIX('6', 6),
  FIVE('5', 5),
  FOUR('4', 4),
  THREE('3', 3),
  TWO('2', 2);

  private static final Set<CardValue> ALL =
      EnumSet.allOf(CardValue.class).stream().collect(Collectors.toUnmodifiableSet());

  private static final Map<Character, CardValue> MAPPINGS =
      ALL.stream().collect(Collectors.toUnmodifiableMap(en -> en.type, Function.identity()));

  private final char type;
  private final int value;

  CardValue(char type, int value) {
    this.type = type;
    this.value = value;
  }

  public static CardValue of(char cardValue) {
    var cardValueEnum = MAPPINGS.get(cardValue);
    if (cardValueEnum == null) {
      throw new IllegalArgumentException("Invalid card value: " + cardValue);
    }
    return cardValueEnum;
  }

  public static boolean valid(char cardValue) {
    return MAPPINGS.containsKey(cardValue);
  }

  public static Set<CardValue> all() {
    return ALL;
  }

  public int value() {
    return value;
  }

  public char type() {
    return type;
  }
}
