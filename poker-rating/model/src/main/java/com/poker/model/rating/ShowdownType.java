package com.poker.model.rating;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ShowdownType {
  HORRIBLE_HAND("Horrible Hand"),
  TERRIBLE_HAND("Terrible Hand"),
  BAD_HAND("Bad Hand"),
  OK_HAND("Ok Hand"),
  GOOD_HAND("Good Hand"),
  GREAT_HAND("Great Hand"),
  AMAZING_HAND("Amazing Hand");

  private final String name;

  ShowdownType(String name) {
    this.name = name;
  }

  @JsonValue
  @Override
  public String toString() {
    return name;
  }
}
