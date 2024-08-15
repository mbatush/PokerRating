package com.poker.rating.rule.point;

import static java.util.Map.entry;

import com.poker.model.game.BetPosition;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.ShowdownType;
import java.util.Map;

public final class CrackedPointSupplier {

  private final Map<BettingRoundType, Long> pointsMatrix;
  private final Map<ShowdownType, Long> extraYourCardsMatrix;
  private final Map<BetPosition, Long> extraPositionMatrix;

  public CrackedPointSupplier() {
    this.pointsMatrix =
        Map.ofEntries(
            entry(BettingRoundType.PRE_FLOP, -20L),
            entry(BettingRoundType.FLOP, -30L),
            entry(BettingRoundType.TURN, -40L));
    this.extraYourCardsMatrix =
        Map.ofEntries(
            entry(ShowdownType.GOOD_HAND, -10L),
            entry(ShowdownType.GREAT_HAND, -20L),
            entry(ShowdownType.AMAZING_HAND, -30L));
    this.extraPositionMatrix = Map.ofEntries(entry(BetPosition.LAST_TO_ACT, -10L));
  }

  public Map<BettingRoundType, Long> pointsMatrix() {
    return pointsMatrix;
  }

  public Map<ShowdownType, Long> extraYourCardsMatrix() {
    return extraYourCardsMatrix;
  }

  public Map<BetPosition, Long> extraPositionMatrix() {
    return extraPositionMatrix;
  }
}
