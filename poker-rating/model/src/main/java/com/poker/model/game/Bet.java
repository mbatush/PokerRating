package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@SuppressWarnings("SameNameButDifferent")
@Getter
@Valid
public final class Bet {

  @Nonnull @NotBlank private final String userId;
  @Nonnull @NotNull private final BetType type;
  private final double amount;
  private final double pot;

  @JsonCreator(mode = Mode.PROPERTIES)
  public Bet(
      @Nonnull @JsonProperty("userId") String userId,
      @Nullable @JsonProperty("amount") Double amount,
      @Nonnull @JsonProperty("type") BetType type,
      @JsonProperty("pot") double pot) {
    this.userId = userId;
    this.amount = Objects.requireNonNullElse(amount, (double) 0);
    this.type = type;
    this.pot = pot;
  }
}
