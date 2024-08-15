package com.poker.rating.client.calc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowdownPercentageResponse {

  @JsonProperty("showdownPercentage")
  private final double showdownPercentage;

  @Nullable
  @JsonProperty("operationTime")
  private final Double operationTime;
}
