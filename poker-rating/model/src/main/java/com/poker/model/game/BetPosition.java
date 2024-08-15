package com.poker.model.game;

public enum BetPosition {
  FIRST_TO_ACT("First to act"),
  NEITHER("Neither"),
  LAST_TO_ACT("Last to act");

  private final String title;

  BetPosition(String title) {
    this.title = title;
  }

  public String title() {
    return title;
  }
}
