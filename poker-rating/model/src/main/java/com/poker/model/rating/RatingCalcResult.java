package com.poker.model.rating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@SuppressWarnings("SameNameButDifferent")
@Accessors(fluent = true)
@Getter
@AllArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@JsonInclude(Include.NON_NULL)
public class RatingCalcResult {
  @Nonnull
  @JsonProperty("newRatings")
  private final Set<PlayerRating> newRatings;

  @Nonnull
  @JsonProperty("prevRatings")
  private final Set<PlayerRating> prevRatings;

  @Nonnull @JsonIgnore private final RatingRuleExecResult ruleExecResult;
  @Nonnull @JsonIgnore private final Map<String, Long> sumRuleRatingChangeByUserId;
}
