package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Size;

@JsonPropertyOrder({"pre-flop", "flop", "turn", "river"})
@Valid
public final class RoundBets {
  @Nonnull
  @Size(min = 2)
  @JsonProperty("pre-flop")
  private final List<Bet> preFlop;

  @Nonnull private final List<Bet> flop;
  @Nonnull private final List<Bet> turn;
  @Nonnull private final List<Bet> river;

  @JsonCreator(mode = Mode.PROPERTIES)
  public RoundBets(
      @Nonnull @JsonProperty("pre-flop") List<Bet> preFlop,
      @Nullable @JsonProperty("flop") List<Bet> flop,
      @Nullable @JsonProperty("turn") List<Bet> turn,
      @Nullable @JsonProperty("river") List<Bet> river) {
    this.preFlop = List.copyOf(Objects.requireNonNullElseGet(preFlop, Collections::emptyList));
    this.flop = List.copyOf(Objects.requireNonNullElseGet(flop, Collections::emptyList));
    this.turn = List.copyOf(Objects.requireNonNullElseGet(turn, Collections::emptyList));
    this.river = List.copyOf(Objects.requireNonNullElseGet(river, Collections::emptyList));
  }

  public List<Bet> getPreFlop() {
    return preFlop;
  }

  public List<Bet> getFlop() {
    return flop;
  }

  public List<Bet> getTurn() {
    return turn;
  }

  public List<Bet> getRiver() {
    return river;
  }
}
