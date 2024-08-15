package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Valid
public final class Player {

  @NotBlank private final String userId;
  private final boolean winner;

  @NotEmpty
  @Size(min = 2, max = 2)
  private final List<Card> cards;

  @JsonCreator(mode = Mode.PROPERTIES)
  public Player(
      @JsonProperty("userId") String userId,
      @JsonProperty("winner") boolean winner,
      @JsonProperty("cards") List<Card> cards) {
    this.userId = userId;
    this.winner = winner;
    this.cards = List.copyOf(Objects.requireNonNullElseGet(cards, Collections::emptyList));
  }

  public Player(String userId, List<Card> cards) {
    this(userId, false, cards);
  }

  public String getUserId() {
    return userId;
  }

  public boolean isWinner() {
    return winner;
  }

  public List<Card> getCards() {
    return cards;
  }
}
