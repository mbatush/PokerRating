package com.poker.rating.rule.point;

import static java.util.Map.entry;

import com.andrebreves.tuple.Tuple;
import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.BetPosition;
import com.poker.model.game.BetSizeCategory;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.ShowdownType;
import com.poker.rating.rule.CallRule.CallCategory;
import java.util.Map;

public final class CallPointSupplier {

  private final Map<Tuple2<BettingRoundType, CallCategory>, Long> pointsMatrix;
  private final Map<Tuple2<CallCategory, ShowdownType>, Long> extraYourCardsMatrix;
  private final Map<Tuple2<CallCategory, ShowdownType>, Long> extraOpponentCardsMatrix;
  private final Map<Tuple2<CallCategory, BetPosition>, Long> extraPositionMatrix;
  private final Map<Tuple2<CallCategory, BetSizeCategory>, Long> extraBetSizeMatrix;

  public CallPointSupplier() {
    this.pointsMatrix =
        Map.ofEntries(
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, CallCategory.GOOD_CALL), 30L),
            entry(Tuple2.of(BettingRoundType.FLOP, CallCategory.GOOD_CALL), 40L),
            entry(Tuple2.of(BettingRoundType.TURN, CallCategory.GOOD_CALL), 40L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, CallCategory.NON_PENALIZED_CALL), 0L),
            entry(Tuple2.of(BettingRoundType.FLOP, CallCategory.NON_PENALIZED_CALL), 0L),
            entry(Tuple2.of(BettingRoundType.TURN, CallCategory.NON_PENALIZED_CALL), 0L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, CallCategory.BAD_CALL), -10L),
            entry(Tuple2.of(BettingRoundType.FLOP, CallCategory.BAD_CALL), -20L),
            entry(Tuple2.of(BettingRoundType.TURN, CallCategory.BAD_CALL), -30L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, CallCategory.TERRIBLE_CALL), -10L),
            entry(Tuple2.of(BettingRoundType.FLOP, CallCategory.TERRIBLE_CALL), -30L),
            entry(Tuple2.of(BettingRoundType.TURN, CallCategory.TERRIBLE_CALL), -40L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, CallCategory.HORRIBLE_CALL), -30L),
            entry(Tuple2.of(BettingRoundType.FLOP, CallCategory.HORRIBLE_CALL), -40L),
            entry(Tuple2.of(BettingRoundType.TURN, CallCategory.HORRIBLE_CALL), -50L));
    this.extraYourCardsMatrix =
        Map.ofEntries(
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.HORRIBLE_HAND), 42L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.TERRIBLE_HAND), 35L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.BAD_HAND), 30L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.OK_HAND), 30L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.GOOD_HAND), 30L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.GREAT_HAND), 20L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.AMAZING_HAND), 15L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.HORRIBLE_HAND), -25L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.TERRIBLE_HAND), -20L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.BAD_HAND), -15L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.OK_HAND), -12L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.GOOD_HAND), -10L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.GREAT_HAND), -8L),
            entry(Tuple.of(CallCategory.BAD_CALL, ShowdownType.AMAZING_HAND), -5L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.HORRIBLE_HAND), -25L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.TERRIBLE_HAND), -20L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.BAD_HAND), -15L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.OK_HAND), -12L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.GOOD_HAND), -10L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.GREAT_HAND), -8L),
            entry(Tuple.of(CallCategory.TERRIBLE_CALL, ShowdownType.AMAZING_HAND), -5L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.HORRIBLE_HAND), -25L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.TERRIBLE_HAND), -20L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.BAD_HAND), -15L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.OK_HAND), -12L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.GOOD_HAND), -10L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.GREAT_HAND), -8L),
            entry(Tuple.of(CallCategory.HORRIBLE_CALL, ShowdownType.AMAZING_HAND), -5L));
    this.extraOpponentCardsMatrix =
        Map.ofEntries(
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.OK_HAND), 10L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.GOOD_HAND), 20L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.GREAT_HAND), 20L),
            entry(Tuple.of(CallCategory.GOOD_CALL, ShowdownType.AMAZING_HAND), 30L));
    this.extraPositionMatrix =
        Map.ofEntries(
            entry(Tuple2.of(CallCategory.GOOD_CALL, BetPosition.FIRST_TO_ACT), 20L),
            entry(Tuple2.of(CallCategory.GOOD_CALL, BetPosition.NEITHER), 10L),
            entry(Tuple2.of(CallCategory.BAD_CALL, BetPosition.FIRST_TO_ACT), -20L),
            entry(Tuple2.of(CallCategory.BAD_CALL, BetPosition.NEITHER), -10L),
            entry(Tuple2.of(CallCategory.TERRIBLE_CALL, BetPosition.FIRST_TO_ACT), -20L),
            entry(Tuple2.of(CallCategory.TERRIBLE_CALL, BetPosition.NEITHER), -10L),
            entry(Tuple2.of(CallCategory.HORRIBLE_CALL, BetPosition.FIRST_TO_ACT), -20L),
            entry(Tuple2.of(CallCategory.HORRIBLE_CALL, BetPosition.NEITHER), -10L));
    this.extraBetSizeMatrix =
        Map.ofEntries(
            entry(
                Tuple2.of(
                    CallCategory.GOOD_CALL, BetSizeCategory.BETWEEN_HALF_POT_AND_POT_EXCLUSIVE),
                10L),
            entry(Tuple2.of(CallCategory.GOOD_CALL, BetSizeCategory.MORE_THEN_POT), 20L),
            entry(Tuple2.of(CallCategory.BAD_CALL, BetSizeCategory.MORE_THEN_POT), -10L),
            entry(Tuple2.of(CallCategory.TERRIBLE_CALL, BetSizeCategory.MORE_THEN_POT), -10L),
            entry(Tuple2.of(CallCategory.HORRIBLE_CALL, BetSizeCategory.MORE_THEN_POT), -10L));
  }

  public Map<Tuple2<BettingRoundType, CallCategory>, Long> pointsMatrix() {
    return pointsMatrix;
  }

  public Map<Tuple2<CallCategory, ShowdownType>, Long> extraYourCardsMatrix() {
    return extraYourCardsMatrix;
  }

  public Map<Tuple2<CallCategory, ShowdownType>, Long> extraOpponentCardsMatrix() {
    return extraOpponentCardsMatrix;
  }

  public Map<Tuple2<CallCategory, BetPosition>, Long> extraPositionMatrix() {
    return extraPositionMatrix;
  }

  public Map<Tuple2<CallCategory, BetSizeCategory>, Long> extraBetSizeMatrix() {
    return extraBetSizeMatrix;
  }
}
