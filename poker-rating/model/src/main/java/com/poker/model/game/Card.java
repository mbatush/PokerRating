package com.poker.model.game;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.poker.model.game.deser.CardDeserializer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.experimental.Accessors;

@SuppressWarnings("SameNameButDifferent")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
@JsonDeserialize(using = CardDeserializer.class)
public class Card {
  private static final ConcurrentHashMap<Map.Entry<CardValue, CardSuit>, Card> CARDS_CACHE =
      new ConcurrentHashMap<>();

  @Include private final CardValue cardValue;
  @Include private final CardSuit cardSuit;

  Card(CardValue cardValue, CardSuit cardSuit) {
    this.cardValue = cardValue;
    this.cardSuit = cardSuit;
  }

  public static Card of(@Nullable String value) {
    if (value == null || value.length() < 2) {
      return new InvalidCard(String.valueOf(value));
    }
    char cardValue = value.charAt(0);
    char cardSuit = value.charAt(1);
    if (CardValue.valid(cardValue) && CardSuit.valid(cardSuit)) {
      return of(CardValue.of(cardValue), CardSuit.of(cardSuit));
    } else {
      return new InvalidCard(value);
    }
  }

  public static Card of(CardValue cardValue, CardSuit cardSuit) {
    return CARDS_CACHE.computeIfAbsent(
        Map.entry(cardValue, cardSuit), k -> new Card(k.getKey(), k.getValue()));
  }

  @JsonValue
  @Override
  public String toString() {
    return "" + cardValue.type() + cardSuit.suit();
  }
}
