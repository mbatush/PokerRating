package com.poker.rating.rule.point;

import static java.util.Map.entry;

import com.andrebreves.tuple.Tuple;
import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.BetPosition;
import com.poker.model.game.BetSizeCategory;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.ShowdownType;
import com.poker.rating.rule.BetRule.BetCategory;
import java.util.Map;

public final class BetPointSupplier {

  private final Map<Tuple2<BettingRoundType, BetCategory>, Long> pointsMatrix;
  private final Map<Tuple2<BetCategory, ShowdownType>, Long> extraYourCardsMatrix;
  private final Map<Tuple2<BetCategory, ShowdownType>, Long> extraOpponentCardsMatrix;
  private final Map<Tuple2<BetCategory, BetPosition>, Long> extraPositionMatrix;
  private final Map<Tuple2<BetCategory, BetSizeCategory>, Long> extraBetSizeMatrix;

  public BetPointSupplier() {
    this.pointsMatrix =
        Map.ofEntries(
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.GOOD_BET), 10L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.GOOD_BET), 20L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.GOOD_BET), 30L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.BAD_BET), -10L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.BAD_BET), -10L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.BAD_BET), -20L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.TERRIBLE_BET), -10L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.TERRIBLE_BET), -20L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.TERRIBLE_BET), -30L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.HORRIBLE_BET), -30L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.HORRIBLE_BET), -40L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.HORRIBLE_BET), -50L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.NON_PENALIZED_BET), 0L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.NON_PENALIZED_BET), 0L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.NON_PENALIZED_BET), 0L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.SUCCESSFUL_BLUFF), 40L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.SUCCESSFUL_BLUFF), 50L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.SUCCESSFUL_BLUFF), 60L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.BAD_BLUFF), -20L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.BAD_BLUFF), -30L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.BAD_BLUFF), -40L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.TERRIBLE_BLUFF), -20L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.TERRIBLE_BLUFF), -30L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.TERRIBLE_BLUFF), -30L),
            entry(Tuple2.of(BettingRoundType.PRE_FLOP, BetCategory.HORRIBLE_BLUFF), -30L),
            entry(Tuple2.of(BettingRoundType.FLOP, BetCategory.HORRIBLE_BLUFF), -40L),
            entry(Tuple2.of(BettingRoundType.TURN, BetCategory.HORRIBLE_BLUFF), -40L));
    this.extraYourCardsMatrix =
        Map.ofEntries(
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.HORRIBLE_HAND), 40L),
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.TERRIBLE_HAND), 35L),
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.BAD_HAND), 32L),
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.OK_HAND), 30L),
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.GOOD_HAND), 30L),
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.GREAT_HAND), 20L),
            entry(Tuple.of(BetCategory.GOOD_BET, ShowdownType.AMAZING_HAND), 10L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.HORRIBLE_HAND), -20L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.TERRIBLE_HAND), -15L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.BAD_HAND), -12L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.OK_HAND), -10L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.GOOD_HAND), -10L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.GREAT_HAND), -10L),
            entry(Tuple.of(BetCategory.BAD_BET, ShowdownType.AMAZING_HAND), -10L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.HORRIBLE_HAND), -20L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.TERRIBLE_HAND), -15L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.BAD_HAND), -12L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.OK_HAND), -10L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.GOOD_HAND), -10L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.GREAT_HAND), -10L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, ShowdownType.AMAZING_HAND), -10L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.HORRIBLE_HAND), -20L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.TERRIBLE_HAND), -15L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.BAD_HAND), -12L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.OK_HAND), -10L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.GOOD_HAND), -10L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.GREAT_HAND), -10L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, ShowdownType.AMAZING_HAND), -10L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.HORRIBLE_HAND), -35L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.TERRIBLE_HAND), -30L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.BAD_HAND), -25L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.OK_HAND), -20L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.GOOD_HAND), -17L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.GREAT_HAND), -15L),
            entry(Tuple.of(BetCategory.BAD_BLUFF, ShowdownType.AMAZING_HAND), -10L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.HORRIBLE_HAND), -35L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.TERRIBLE_HAND), -30L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.BAD_HAND), -25L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.OK_HAND), -20L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.GOOD_HAND), -17L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.GREAT_HAND), -15L),
            entry(Tuple.of(BetCategory.TERRIBLE_BLUFF, ShowdownType.AMAZING_HAND), -10L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.HORRIBLE_HAND), -35L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.TERRIBLE_HAND), -30L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.BAD_HAND), -25L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.OK_HAND), -20L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.GOOD_HAND), -17L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.GREAT_HAND), -15L),
            entry(Tuple.of(BetCategory.HORRIBLE_BLUFF, ShowdownType.AMAZING_HAND), -10L));

    this.extraOpponentCardsMatrix = Map.of();
    this.extraPositionMatrix = Map.of();
    this.extraBetSizeMatrix =
        Map.ofEntries(
            entry(Tuple.of(BetCategory.BAD_BET, BetSizeCategory.MORE_THEN_POT), -10L),
            entry(Tuple.of(BetCategory.TERRIBLE_BET, BetSizeCategory.MORE_THEN_POT), -10L),
            entry(Tuple.of(BetCategory.HORRIBLE_BET, BetSizeCategory.MORE_THEN_POT), -10L));
  }

  public Map<Tuple2<BettingRoundType, BetCategory>, Long> pointsMatrix() {
    return pointsMatrix;
  }

  public Map<Tuple2<BetCategory, ShowdownType>, Long> extraYourCardsMatrix() {
    return extraYourCardsMatrix;
  }

  public Map<Tuple2<BetCategory, ShowdownType>, Long> extraOpponentCardsMatrix() {
    return extraOpponentCardsMatrix;
  }

  public Map<Tuple2<BetCategory, BetPosition>, Long> extraPositionMatrix() {
    return extraPositionMatrix;
  }

  public Map<Tuple2<BetCategory, BetSizeCategory>, Long> extraBetSizeMatrix() {
    return extraBetSizeMatrix;
  }
}
