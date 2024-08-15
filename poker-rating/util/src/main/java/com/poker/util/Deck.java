package com.poker.util;

import com.poker.model.game.Card;
import com.poker.model.game.CardSuit;
import com.poker.model.game.CardValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Deck {

  private static final Set<Card> HOLDEM_DECK =
      CardValue.all().stream()
          .flatMap(
              cardValue -> CardSuit.all().stream().map(cardSuit -> Card.of(cardValue, cardSuit)))
          .collect(Collectors.toUnmodifiableSet());

  private Deck() {
    throw new UnsupportedOperationException("Utility class");
  }

  /** Generate random holdem poker deck cards. */
  public static List<Card> holdemCards() {
    var holdemDeck = new ArrayList<>(HOLDEM_DECK);
    Collections.shuffle(holdemDeck);
    return holdemDeck;
  }
}
