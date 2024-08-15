package com.poker.rating.client.calc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WinPercentageResponse {

  @Nonnull
  @JsonProperty("players")
  private final List<WinPercentagePlayer> players;

  @Nullable
  @JsonProperty("operationTime")
  private final Double operationTime;

  @JsonProperty("tiesPercentage")
  private double tiesPercentage;
}
