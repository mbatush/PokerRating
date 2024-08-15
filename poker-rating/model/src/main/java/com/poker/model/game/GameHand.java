package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.poker.model.game.validator.GameHandConstraint;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonPropertyOrder({
  "applicationId",
  "sessionId",
  "boardCards",
  "tableCards",
  "players",
  "roundBets"
})
@GameHandConstraint
public final class GameHand {
  @NotBlank private final String applicationId;
  @NotBlank private final String sessionId;

  private final List<Card> boardCards;

  @NotEmpty
  @Size(min = 2, max = 10)
  @JsonInclude(Include.NON_NULL)
  private final List<@Valid Player> players;

  @NotNull @Valid private final RoundBets roundBets;

  @JsonCreator(mode = Mode.PROPERTIES)
  public GameHand(
      @JsonProperty("applicationId") String applicationId,
      @JsonProperty("sessionId") String sessionId,
      @Nullable @JsonProperty("boardCards") List<Card> boardCards,
      @JsonProperty("players") List<Player> players,
      @JsonProperty("roundBets") RoundBets roundBets) {
    this.applicationId = applicationId;
    this.sessionId = sessionId;
    this.boardCards =
        List.copyOf(Objects.requireNonNullElseGet(boardCards, Collections::emptyList));
    this.players = List.copyOf(Objects.requireNonNullElseGet(players, Collections::emptyList));
    this.roundBets =
        Objects.requireNonNullElseGet(
            roundBets, () -> new RoundBets(List.of(), List.of(), List.of(), List.of()));
  }

  public String getApplicationId() {
    return applicationId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public List<Card> getBoardCards() {
    return boardCards;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public RoundBets getRoundBets() {
    return roundBets;
  }
}
