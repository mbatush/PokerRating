package com.poker.model.rating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.poker.model.game.Bet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@SuppressWarnings("SameNameButDifferent")
@Accessors(fluent = true)
@Getter
@AllArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
  "name",
  "ruleName",
  "bet",
  "ratingChange",
  "win",
  "showdown",
  "equity",
  "pof",
  "messages",
  "playerPercentages",
  "gameStateIndex"
})
public class RuleDecision {

  @Nonnull
  @JsonProperty("bet")
  private final Bet bet;

  @Nonnull
  @JsonProperty("ruleName")
  private final String ruleName;

  @Nonnull
  @JsonProperty("gameStateIndex")
  private final GameStateIndex gameStateIndex;

  @Nonnull
  @JsonProperty("name")
  private final String name;

  @Nonnull
  @JsonProperty("ratingChange")
  private final Long ratingChange;

  @Nonnull
  @JsonProperty("win")
  private final Double win;

  @Nonnull
  @JsonProperty("showdown")
  private final Double showdown;

  @Nonnull
  @JsonProperty("playerPercentages")
  private final List<PlayerPercentage> playerPercentages;

  @Nullable
  @JsonProperty("equity")
  private final Double equity;

  @Nullable
  @JsonProperty("pof")
  private final Double pof;

  @Nullable
  @JsonProperty("messages")
  @JsonInclude(Include.NON_EMPTY)
  private final List<String> messages;
}
