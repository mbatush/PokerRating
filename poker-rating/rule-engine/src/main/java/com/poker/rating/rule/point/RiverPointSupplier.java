package com.poker.rating.rule.point;

import static java.util.Map.entry;

import com.poker.model.rating.ShowdownType;
import java.util.Map;

public final class RiverPointSupplier {
  private final Map<ShowdownType, Long> foldOnWinningHandPoints;
  private final Map<ShowdownType, Long> successfulBluffPoints;
  private final Map<ShowdownType, Long> failedBluffPoints;
  private final Map<ShowdownType, Long> callOnNonBestHandPoints;
  private final Map<ShowdownType, Long> foldOnNonBestHandPoints;
  private final Map<ShowdownType, Long> failedTrapPoints;
  private final Map<ShowdownType, Long> winningHandNoReRaisePoints;
  private final Map<ShowdownType, Long> winningHandOnLastCheckPoints;
  private final Map<ShowdownType, Long> winningHandYouBetPoints;
  private final Map<ShowdownType, Long> opponentBetsAndYourRaiseToGetMaxValue;
  private final Map<ShowdownType, Long> youBetAndYourRaiseToGetMaxValue;

  public RiverPointSupplier() {
    // 4. You have winning hand and he bets and you fold
    this.foldOnWinningHandPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, -10L),
            entry(ShowdownType.TERRIBLE_HAND, -15L),
            entry(ShowdownType.BAD_HAND, -20L),
            entry(ShowdownType.OK_HAND, -30L),
            entry(ShowdownType.GOOD_HAND, -40L),
            entry(ShowdownType.GREAT_HAND, -50L),
            entry(ShowdownType.AMAZING_HAND, -60L));
    // 1. On the river you are beat but you bet. Scenarios with you betting first or Re-raise
    this.successfulBluffPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 60L),
            entry(ShowdownType.TERRIBLE_HAND, 55L),
            entry(ShowdownType.BAD_HAND, 50L),
            entry(ShowdownType.OK_HAND, 50L),
            entry(ShowdownType.GOOD_HAND, 45L),
            entry(ShowdownType.GREAT_HAND, 40L),
            entry(ShowdownType.AMAZING_HAND, 30L));
    // 1. On the river you are beat but you bet. Scenarios with you betting first or Re-raise
    this.failedBluffPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, -60L),
            entry(ShowdownType.TERRIBLE_HAND, -55L),
            entry(ShowdownType.BAD_HAND, -50L),
            entry(ShowdownType.OK_HAND, -45L),
            entry(ShowdownType.GOOD_HAND, -40L),
            entry(ShowdownType.GREAT_HAND, -35L),
            entry(ShowdownType.AMAZING_HAND, -30L));
    // 2. On the river you are beat, and he bets, and you call
    this.callOnNonBestHandPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, -65L),
            entry(ShowdownType.TERRIBLE_HAND, -60L),
            entry(ShowdownType.BAD_HAND, -55L),
            entry(ShowdownType.OK_HAND, -45L),
            entry(ShowdownType.GOOD_HAND, -40L),
            entry(ShowdownType.GREAT_HAND, -35L),
            entry(ShowdownType.AMAZING_HAND, -25L));
    // 3. You are beat and he bets and you fold
    this.foldOnNonBestHandPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 10L),
            entry(ShowdownType.TERRIBLE_HAND, 15L),
            entry(ShowdownType.BAD_HAND, 20L),
            entry(ShowdownType.OK_HAND, 30L),
            entry(ShowdownType.GOOD_HAND, 40L),
            entry(ShowdownType.GREAT_HAND, 50L),
            entry(ShowdownType.AMAZING_HAND, 60L));
    // 5. You have winning hand, and you check, and he checks (failed trap, you are any position
    // except last here)
    this.failedTrapPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 0L),
            entry(ShowdownType.TERRIBLE_HAND, 0L),
            entry(ShowdownType.BAD_HAND, -5L),
            entry(ShowdownType.OK_HAND, -10L),
            entry(ShowdownType.GOOD_HAND, -15L),
            entry(ShowdownType.GREAT_HAND, -20L),
            entry(ShowdownType.AMAZING_HAND, -22L));
    // 6.You have a winning hand, and he bets, and you do not re-raise only call
    this.winningHandNoReRaisePoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 0L),
            entry(ShowdownType.TERRIBLE_HAND, 0L),
            entry(ShowdownType.BAD_HAND, 0L),
            entry(ShowdownType.OK_HAND, 0L),
            entry(ShowdownType.GOOD_HAND, 0L),
            entry(ShowdownType.GREAT_HAND, -10L),
            entry(ShowdownType.AMAZING_HAND, -15L));
    // 7. You have winning hand, and he checks, and you check (you are last position here)
    this.winningHandOnLastCheckPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 0L),
            entry(ShowdownType.TERRIBLE_HAND, 0L),
            entry(ShowdownType.BAD_HAND, -5L),
            entry(ShowdownType.OK_HAND, -10L),
            entry(ShowdownType.GOOD_HAND, -15L),
            entry(ShowdownType.GREAT_HAND, -20L),
            entry(ShowdownType.AMAZING_HAND, -30L));
    // 9.You have winning hand and you bet
    this.winningHandYouBetPoints =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 50L),
            entry(ShowdownType.TERRIBLE_HAND, 45L),
            entry(ShowdownType.BAD_HAND, 40L),
            entry(ShowdownType.OK_HAND, 35L),
            entry(ShowdownType.GOOD_HAND, 35L),
            entry(ShowdownType.GREAT_HAND, 30L),
            entry(ShowdownType.AMAZING_HAND, 25L));
    // 8. Opponent bets and You raise on the river with the best hand in order to try to get max
    // value=
    // +60, +55, +50, +40, +40, +30
    this.opponentBetsAndYourRaiseToGetMaxValue =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 60L),
            entry(ShowdownType.TERRIBLE_HAND, 55L),
            entry(ShowdownType.BAD_HAND, 50L),
            entry(ShowdownType.OK_HAND, 45L),
            entry(ShowdownType.GOOD_HAND, 40L),
            entry(ShowdownType.GREAT_HAND, 40L),
            entry(ShowdownType.AMAZING_HAND, 30L));

    // 11. You bet, he raises, you re-raise with best hand to try to get max value so each re-raise
    this.youBetAndYourRaiseToGetMaxValue =
        Map.ofEntries(
            entry(ShowdownType.HORRIBLE_HAND, 50L),
            entry(ShowdownType.TERRIBLE_HAND, 40L),
            entry(ShowdownType.BAD_HAND, 38L),
            entry(ShowdownType.OK_HAND, 35L),
            entry(ShowdownType.GOOD_HAND, 30L),
            entry(ShowdownType.GREAT_HAND, 30L),
            entry(ShowdownType.AMAZING_HAND, 20L));
  }

  public Map<ShowdownType, Long> foldOnWinningHandPoints() {
    return foldOnWinningHandPoints;
  }

  public Map<ShowdownType, Long> successfulBluffPoints() {
    return successfulBluffPoints;
  }

  public Map<ShowdownType, Long> failedBluffPoints() {
    return failedBluffPoints;
  }

  public Map<ShowdownType, Long> callOnNonBestHandPoints() {
    return callOnNonBestHandPoints;
  }

  public Map<ShowdownType, Long> foldOnNonBestHandPoints() {
    return foldOnNonBestHandPoints;
  }

  public Map<ShowdownType, Long> failedTrapPoints() {
    return failedTrapPoints;
  }

  public Map<ShowdownType, Long> winningHandNoReRaisePoints() {
    return winningHandNoReRaisePoints;
  }

  public Map<ShowdownType, Long> winningHandOnLastCheckPoints() {
    return winningHandOnLastCheckPoints;
  }

  public Map<ShowdownType, Long> winningHandYouBetPoints() {
    return winningHandYouBetPoints;
  }

  public Map<ShowdownType, Long> opponentBetsAndYourRaiseToGetMaxValue() {
    return opponentBetsAndYourRaiseToGetMaxValue;
  }

  public Map<ShowdownType, Long> youBetAndYourRaiseToGetMaxValue() {
    return youBetAndYourRaiseToGetMaxValue;
  }
}
