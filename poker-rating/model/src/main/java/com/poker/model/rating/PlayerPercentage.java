package com.poker.model.rating;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("SameNameButDifferent")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PlayerPercentage {

  @Include private final String userId;
  private final double winPercentage;
  private final double showdownPercentage;
  @Nullable private final Integer rank;
  @Nullable private final String rankName;

  public PlayerPercentage(@Nonnull String userId, double winPercentage, double showdownPercentage) {
    this(userId, winPercentage, showdownPercentage, null, null);
  }

  public PlayerPercentage(
      @Nonnull String userId,
      double winPercentage,
      double showdownPercentage,
      @Nullable Integer rank,
      @Nullable String rankName) {
    this.userId = userId;
    this.winPercentage = winPercentage;
    this.showdownPercentage = showdownPercentage;
    this.rank = rank;
    this.rankName = rankName;
  }
}
