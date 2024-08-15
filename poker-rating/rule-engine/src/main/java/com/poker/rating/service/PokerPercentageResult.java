package com.poker.rating.service;

import com.poker.model.rating.PlayerPercentage;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("SameNameButDifferent")
@Getter
@ToString
public final class PokerPercentageResult {

  /** User ID to player percentage map */
  @Nonnull private final Map<String, PlayerPercentage> playerPercentages;

  public PokerPercentageResult(@Nonnull Map<String, PlayerPercentage> playerPercentages) {
    this.playerPercentages = playerPercentages;
  }
}
