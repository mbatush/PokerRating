package com.poker.rating.rest.internal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.poker.model.rating.PlayerRating;
import com.poker.model.rating.RuleDecision;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@SuppressWarnings("SameNameButDifferent")
@Accessors(fluent = true)
@Getter
@AllArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
  "operationTimeMillis",
  "prevRatings",
  "newRatings",
  "sumDecisions",
  "decisions"
})
public class RatingGameCalcResponse {
  @Nonnull
  @JsonProperty("newRatings")
  private final List<PlayerRating> newRatings;

  @Nonnull
  @JsonProperty("prevRatings")
  private final List<PlayerRating> prevRatings;

  @Nonnull
  @JsonProperty("sumDecisions")
  private final Map<String, Long> sumRuleRatingChangeByUserId;

  @Nonnull
  @JsonProperty("decisions")
  private final List<RuleDecision> decisions;

  @JsonProperty("operationTimeMillis")
  private final double operationTimeMillis;
}
