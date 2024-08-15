package com.poker.model.rating;

import com.poker.model.game.BettingRoundType;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;

public record GameStateIndex(BettingRoundType bettingRound, int playerTurnIndex)
    implements Comparable<GameStateIndex> {
  public GameStateIndex {
    Objects.requireNonNull(bettingRound);
  }

  @Override
  public String toString() {
    return "(" + bettingRound + ", " + playerTurnIndex + ')';
  }

  @Override
  public int compareTo(@Nonnull GameStateIndex other) {
    return Comparator.comparing(GameStateIndex::bettingRound)
        .thenComparing(GameStateIndex::playerTurnIndex)
        .compare(this, other);
  }
}
