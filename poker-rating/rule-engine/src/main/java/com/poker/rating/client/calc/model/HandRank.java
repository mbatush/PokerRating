package com.poker.rating.client.calc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HandRank {

  @Nonnull
  @JsonProperty("name")
  private final String name;

  @JsonProperty("rank")
  private final int rank;
}
