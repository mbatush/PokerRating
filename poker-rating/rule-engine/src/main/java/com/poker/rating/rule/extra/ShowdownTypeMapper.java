package com.poker.rating.rule.extra;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.poker.model.rating.ShowdownType;
import java.util.Objects;

public class ShowdownTypeMapper {

  private final RangeMap<Double, ShowdownType> showdownValueToType;

  public ShowdownTypeMapper() {
    this.showdownValueToType =
        ImmutableRangeMap.<Double, ShowdownType>builder()
            .put(Range.lessThan(5.0), ShowdownType.HORRIBLE_HAND)
            .put(Range.closedOpen(5.0, 15.0), ShowdownType.TERRIBLE_HAND)
            .put(Range.closedOpen(15.0, 30.0), ShowdownType.BAD_HAND)
            .put(Range.closedOpen(30.0, 50.0), ShowdownType.OK_HAND)
            .put(Range.closedOpen(50.0, 70.0), ShowdownType.GOOD_HAND)
            .put(Range.closedOpen(70.0, 95.0), ShowdownType.GREAT_HAND)
            .put(Range.atLeast(95.0), ShowdownType.AMAZING_HAND)
            .build();
  }

  public ShowdownType showdownType(double showdownPercentage) {
    return Objects.requireNonNull(
        showdownValueToType.get(showdownPercentage),
        "Showdown mapping to type is missing for: " + showdownPercentage);
  }
}
