package com.poker.model.game;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CardSuit {
  CLUB('c'),
  DIAMOND('d'),
  HEART('h'),
  SPADE('s');

  private static final Set<CardSuit> ALL =
      EnumSet.allOf(CardSuit.class).stream().collect(Collectors.toUnmodifiableSet());
  private static final Map<Character, CardSuit> MAPPINGS =
      ALL.stream().collect(Collectors.toUnmodifiableMap(en -> en.suit, Function.identity()));
  private final char suit;

  CardSuit(char suit) {
    this.suit = suit;
  }

  public static CardSuit of(char cardSuit) {
    var cardSuitEnum = MAPPINGS.get(cardSuit);
    if (cardSuitEnum == null) {
      throw new IllegalArgumentException("Invalid card suit: " + cardSuit);
    }
    return cardSuitEnum;
  }

  public static boolean valid(char cardSuit) {
    return MAPPINGS.containsKey(cardSuit);
  }

  public static Set<CardSuit> all() {
    return ALL;
  }

  public char suit() {
    return suit;
  }
}
