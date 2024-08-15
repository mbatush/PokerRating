package com.poker.model.rating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@SuppressWarnings("SameNameButDifferent")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class PlayerRating {

  @EqualsAndHashCode.Include private final String applicationId;
  @EqualsAndHashCode.Include private final String userId;
  private final long rating;

  @JsonCreator(mode = Mode.PROPERTIES)
  public PlayerRating(
      @JsonProperty("applicationId") String applicationId,
      @JsonProperty("userId") String userId,
      @JsonProperty("rating") long rating) {
    this.applicationId = applicationId;
    this.userId = userId;
    this.rating = rating;
  }
}
