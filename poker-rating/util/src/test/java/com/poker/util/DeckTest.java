package com.poker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DeckTest {

  @Test
  void test_holdem_cards() {
    var deckCards = Deck.holdemCards();
    assertNotNull(deckCards);
    assertEquals(52, deckCards.size());
  }

  @Test
  void test_holdem_cards_randomness() {
    var deckCardsOne = Deck.holdemCards();
    var deckCardsTwo = Deck.holdemCards();
    assertNotEquals(deckCardsOne, deckCardsTwo);
  }
}
