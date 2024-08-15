package com.poker.rating.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.poker.model.game.Card;
import com.poker.model.game.NCard;
import com.poker.rating.client.calc.model.ShowdownPercentageResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/** Cached in memory implementation */
public class PreFlopShowdownPercentageCalc {

  private static final Map<NCard, Double> PRE_FLOP_PERCENTAGES = initPreFlopPercentages();

  @SuppressWarnings("java:S112")
  private static Map<NCard, Double> initPreFlopPercentages() {
    try {
      var resourceName = "preflop_showdown.txt";
      URL resource = PreFlopShowdownPercentageCalc.class.getClassLoader().getResource(resourceName);
      Preconditions.checkNotNull(
          resource, "Pre flop showdown file is missing in classpath: " + resourceName);
      Map<NCard, Double> preFlopPercentages =
          Files.readAllLines(Path.of(resource.toURI())).stream()
              .map(
                  l -> {
                    List<String> lineData = Splitter.on(' ').splitToList(l);
                    BigDecimal percentage = new BigDecimal(lineData.get(2), MathContext.UNLIMITED);
                    percentage = percentage.setScale(0, RoundingMode.HALF_UP);
                    return Map.entry(
                        NCard.of(Card.of(lineData.get(0)), Card.of(lineData.get(1))),
                        percentage.doubleValue());
                  })
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      Preconditions.checkArgument(
          preFlopPercentages.size() == 1326, "Wrong pre flop percentages data");
      return preFlopPercentages;
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public @Nonnull ShowdownPercentageResponse calculate(@Nonnull List<Card> playerCards) {
    Preconditions.checkArgument(playerCards.size() == 2, "Invalid size of player cards");
    NCard key = NCard.of(playerCards.toArray(new Card[] {}));
    Double preFlopPercentage = PRE_FLOP_PERCENTAGES.get(key);
    Preconditions.checkNotNull(preFlopPercentage, "Pre flop percentage is missing for: " + key);
    return new ShowdownPercentageResponse(preFlopPercentage, null);
  }
}
