package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class InvalidCard extends Card {

  private final String invalidCard;

  @JsonCreator
  public InvalidCard(String value) {
    super(CardValue.ACE, CardSuit.CLUB);
    this.invalidCard = value;
  }

  @Override
  public String toString() {
    return String.valueOf(invalidCard);
  }

  @JsonValue
  public String getInvalidCard() {
    return invalidCard;
  }
}
