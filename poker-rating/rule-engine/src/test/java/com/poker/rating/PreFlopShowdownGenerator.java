package com.poker.rating;

import com.andrebreves.tuple.Tuple3;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.poker.model.game.Card;
import com.poker.model.game.CardSuit;
import com.poker.model.game.NCard;
import com.poker.util.Deck;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PreFlopShowdownGenerator {

  private static final List<CardSuit> SUITS =
      List.of(CardSuit.SPADE, CardSuit.CLUB, CardSuit.DIAMOND, CardSuit.HEART);

  public static void main(String[] args) throws Exception {
    URL url =
        PreFlopShowdownGenerator.class
            .getClassLoader()
            .getResource("calculated_preflop_showdown.txt");
    if (url == null) {
      throw new IllegalStateException(
          "calculated_preflop_showdown.txt missing in classpath resource");
    }

    List<Tuple3<Card, Card, Double>> tuples =
        Files.readAllLines(Path.of(url.toURI())).stream()
            .map(
                l -> {
                  List<String> lineData = Splitter.on(' ').splitToList(l);
                  BigDecimal percentage = new BigDecimal(lineData.get(2), MathContext.UNLIMITED);
                  percentage = percentage.setScale(0, RoundingMode.HALF_UP);
                  return Tuple3.of(
                      Card.of(lineData.get(0)), Card.of(lineData.get(1)), percentage.doubleValue());
                })
            .toList();
    Map<NCard, Double> preFlopPercentages = new HashMap<>();
    for (Tuple3<Card, Card, Double> tuple3 : tuples) {
      Card card1 = tuple3.v1();
      Card card2 = tuple3.v2();
      Double percentage = tuple3.v3();

      var preFlopCards = NCard.of(card1, card2);
      putIfAbsent(preFlopPercentages, preFlopCards, percentage);
      if (card1.cardSuit() == card2.cardSuit()) {
        SUITS.forEach(
            suit -> {
              if (suit != card1.cardSuit()) {
                putIfAbsent(
                    preFlopPercentages,
                    NCard.of(Card.of(card1.cardValue(), suit), Card.of(card2.cardValue(), suit)),
                    percentage);
              }
            });
      } else {
        for (int i = 0; i < SUITS.size() - 1; i++) {
          for (int j = i + 1; j < SUITS.size(); j++) {
            CardSuit cardSuit1 = SUITS.get(i);
            CardSuit cardSuit2 = SUITS.get(j);
            if (!(cardSuit1 == card1.cardSuit() && cardSuit2 == card2.cardSuit())) {
              putIfAbsent(
                  preFlopPercentages,
                  NCard.of(
                      Card.of(card1.cardValue(), cardSuit1), Card.of(card2.cardValue(), cardSuit2)),
                  percentage);
            }
          }
        }
      }
    }
    System.out.println(preFlopPercentages.size());
    Set<Set<Card>> twoCardCombinations = Sets.combinations(Set.copyOf(Deck.holdemCards()), 2);
    System.out.println(twoCardCombinations.size());
    twoCardCombinations.forEach(
        twoCard -> {
          var preFlopCards = NCard.of(twoCard.toArray(new Card[] {}));
          if (!preFlopPercentages.containsKey(preFlopCards)) {
            System.out.println("Missing: " + preFlopCards);
          }
        });

    var outPath = Path.of("preflop_showdown.txt");
    Files.writeString(outPath, "", StandardOpenOption.CREATE);
    System.out.println(outPath.toAbsolutePath());

    String preFlopShowdownData =
        preFlopPercentages.entrySet().stream()
            .map(
                e -> {
                  var nCards = List.of(e.getKey().nCards().toArray());
                  return nCards.get(0).toString()
                      + " "
                      + nCards.get(1).toString()
                      + " "
                      + e.getValue();
                })
            .collect(Collectors.joining("\n"));
    Files.writeString(outPath, preFlopShowdownData);
    System.out.println("Done");
  }

  static void putIfAbsent(
      Map<NCard, Double> preFlopPercentages, NCard preFlopCards, Double percentage) {
    Double prevPercentage = preFlopPercentages.putIfAbsent(preFlopCards, percentage);
    if (prevPercentage != null && !prevPercentage.equals(percentage)) {
      Preconditions.checkArgument(preFlopCards.nCards().size() > 1);
      Iterator<Card> it = preFlopCards.nCards().iterator();
      Card preFlopCard1 = it.next();
      Card preFlopCard2 = it.next();
      System.out.printf(
          "Cards '%s %s' percent already present '%f'. Given percentage is '%f'\n",
          preFlopCard1.toString(), preFlopCard2.toString(), prevPercentage, percentage);
    }
  }
}
