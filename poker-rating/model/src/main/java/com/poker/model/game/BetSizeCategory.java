package com.poker.model.game;

public enum BetSizeCategory {
  LESS_THEN_HALF_POT("Less than half pot"),
  BETWEEN_HALF_POT_AND_POT_EXCLUSIVE("Between half pot and pot exclusive"),
  MORE_THEN_POT("More then pot");

  private final String title;

  BetSizeCategory(String title) {
    this.title = title;
  }

  public String title() {
    return title;
  }

  public static BetSizeCategory from(Bet bet) {
    double betAmount = bet.getAmount();
    double pot = bet.getPot();
    double halfPot = pot / 2;
    if (betAmount < halfPot) {
      return LESS_THEN_HALF_POT;
    } else if (betAmount < pot) {
      return BETWEEN_HALF_POT_AND_POT_EXCLUSIVE;
    } else {
      return MORE_THEN_POT;
    }
  }
}
