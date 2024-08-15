package com.poker.rating;

import com.poker.model.rating.PlayerPercentage;
import com.poker.rating.service.PercentageCalculatorContext;
import com.poker.rating.service.PokerPercentageCalculator;
import com.poker.rating.service.PokerPercentageResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RandomPokerPercentageCalculator implements PokerPercentageCalculator {

  @Override
  public PokerPercentageResult calculate(PercentageCalculatorContext calculatorContext) {
    Map<String, PlayerPercentage> playerPercentages =
        calculatorContext.getPlayingPlayers().stream()
            .map(
                p ->
                    new PlayerPercentage(
                        p.getUserId(), randomPercentage(), randomPercentage(), 0, "N/A"))
            .collect(
                Collectors.toUnmodifiableMap(PlayerPercentage::getUserId, Function.identity()));
    return new PokerPercentageResult(playerPercentages);
  }

  private double randomPercentage() {
    return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 100))
        .setScale(2, RoundingMode.CEILING)
        .doubleValue();
  }
}
