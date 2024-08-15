package com.poker.model.game;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.experimental.Accessors;

@SuppressWarnings("SameNameButDifferent")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
public class NCard {

  @Include private final Set<Card> nCards;

  private NCard(Card... nCards) {
    this.nCards = Set.of(nCards);
  }

  public static NCard of(Card... nCards) {
    return new NCard(nCards);
  }

  @Override
  public String toString() {
    return nCards.toString();
  }
}
