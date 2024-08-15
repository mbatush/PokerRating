package com.poker.rating.service;

import com.poker.model.game.Card;
import com.poker.model.game.Player;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;

@SuppressWarnings("SameNameButDifferent")
@Getter
public final class PercentageCalculatorContext {
  @Nonnull private final List<Player> playingPlayers;
  @Nullable private final List<Card> boardCards;
  @Nullable private final Set<Card> deadCards;
  @Nullable private final Map<String, Double> userIdToShowdownPercentage;

  public PercentageCalculatorContext(
      @Nonnull List<Player> playingPlayers,
      @Nullable List<Card> boardCards,
      @Nullable Set<Card> deadCards) {
    this(playingPlayers, boardCards, deadCards, null);
  }

  public PercentageCalculatorContext(
      @Nonnull List<Player> playingPlayers,
      @Nullable List<Card> boardCards,
      @Nullable Set<Card> deadCards,
      @Nullable Map<String, Double> userIdToShowdownPercentage) {
    this.playingPlayers = Collections.unmodifiableList(playingPlayers);
    this.boardCards = boardCards != null ? Collections.unmodifiableList(boardCards) : null;
    this.deadCards = deadCards != null ? Collections.unmodifiableSet(deadCards) : null;
    this.userIdToShowdownPercentage =
        userIdToShowdownPercentage != null
            ? Collections.unmodifiableMap(userIdToShowdownPercentage)
            : null;
  }
}
