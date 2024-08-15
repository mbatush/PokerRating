package com.poker.model.rating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RatingRuleExecResult {
  @Nonnull
  @JsonProperty("decisions")
  private final List<RuleDecision> decisions;

  @Nonnull
  @JsonProperty("percentages")
  private final Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage;
}
