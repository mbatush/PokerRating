package com.poker.rating.rule.point;

import static java.util.Map.entry;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.BetPosition;
import com.poker.model.game.BetSizeCategory;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.ShowdownType;
import com.poker.rating.rule.FoldRule.FoldCategory;
import java.util.Map;

public final class FoldPointSupplier {
  private final Map<Tuple2<BettingRoundType, FoldCategory>, Long> pointsMatrix;
  private final Map<Tuple2<FoldCategory, ShowdownType>, Long> extraYourCardsMatrix;
  private final Map<Tuple2<FoldCategory, ShowdownType>, Long> extraOpponentCardsMatrix;
  private final Map<Tuple2<FoldCategory, BetPosition>, Long> extraPositionMatrix;
  private final Map<Tuple2<FoldCategory, BetSizeCategory>, Long> extraBetSizeMatrix;

  public FoldPointSupplier() {
    this.pointsMatrix =
        Map.ofEntries(
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, FoldCategory.NON_PENALIZED_FOLD), 0L),
            entry(Tuple2.of(BettingRoundType.FLOP, FoldCategory.NON_PENALIZED_FOLD), 0L),
            entry(Tuple2.of(BettingRoundType.TURN, FoldCategory.NON_PENALIZED_FOLD), 0L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, FoldCategory.CORRECT_FOLD), 10L),
            entry(Tuple2.of(BettingRoundType.FLOP, FoldCategory.CORRECT_FOLD), 20L),
            entry(Tuple2.of(BettingRoundType.TURN, FoldCategory.CORRECT_FOLD), 30L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, FoldCategory.INCORRECT_FOLD), -10L),
            entry(Tuple2.of(BettingRoundType.FLOP, FoldCategory.INCORRECT_FOLD), -20L),
            entry(Tuple2.of(BettingRoundType.TURN, FoldCategory.INCORRECT_FOLD), -30L));
    this.extraYourCardsMatrix =
        Map.ofEntries(
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.HORRIBLE_HAND), 5L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.TERRIBLE_HAND), 5L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.BAD_HAND), 10L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.OK_HAND), 20L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.GOOD_HAND), 25L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.GREAT_HAND), 30L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, ShowdownType.AMAZING_HAND), 45L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.HORRIBLE_HAND), 0L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.TERRIBLE_HAND), -10L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.BAD_HAND), -15L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.OK_HAND), -20L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.GOOD_HAND), -25L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.GREAT_HAND), -30L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, ShowdownType.AMAZING_HAND), -40L));
    this.extraOpponentCardsMatrix = Map.of();
    this.extraPositionMatrix =
        Map.ofEntries(entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, BetPosition.LAST_TO_ACT), -10L));
    this.extraBetSizeMatrix =
        Map.ofEntries(
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, BetSizeCategory.LESS_THEN_HALF_POT), 30L),
            entry(
                Tuple2.of(
                    FoldCategory.CORRECT_FOLD, BetSizeCategory.BETWEEN_HALF_POT_AND_POT_EXCLUSIVE),
                20L),
            entry(Tuple2.of(FoldCategory.CORRECT_FOLD, BetSizeCategory.MORE_THEN_POT), 10L),
            entry(Tuple2.of(FoldCategory.INCORRECT_FOLD, BetSizeCategory.LESS_THEN_HALF_POT), -20L),
            entry(
                Tuple2.of(
                    FoldCategory.INCORRECT_FOLD,
                    BetSizeCategory.BETWEEN_HALF_POT_AND_POT_EXCLUSIVE),
                -10L));
  }

  public Map<Tuple2<BettingRoundType, FoldCategory>, Long> pointsMatrix() {
    return pointsMatrix;
  }

  public Map<Tuple2<FoldCategory, ShowdownType>, Long> extraYourCardsMatrix() {
    return extraYourCardsMatrix;
  }

  public Map<Tuple2<FoldCategory, ShowdownType>, Long> extraOpponentCardsMatrix() {
    return extraOpponentCardsMatrix;
  }

  public Map<Tuple2<FoldCategory, BetPosition>, Long> extraPositionMatrix() {
    return extraPositionMatrix;
  }

  public Map<Tuple2<FoldCategory, BetSizeCategory>, Long> extraBetSizeMatrix() {
    return extraBetSizeMatrix;
  }
}
