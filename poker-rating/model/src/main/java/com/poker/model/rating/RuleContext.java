package com.poker.model.rating;

public final class RuleContext {
  private final GameState gameState;

  public RuleContext(GameState gameState) {
    this.gameState = gameState;
  }

  public GameState gameState() {
    return gameState;
  }
}
