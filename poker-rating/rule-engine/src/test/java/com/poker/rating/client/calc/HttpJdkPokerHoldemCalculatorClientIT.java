package com.poker.rating.client.calc;

import com.poker.model.game.Card;
import com.poker.rating.client.calc.model.ShowdownPercentageRequest;
import com.poker.rating.client.calc.model.ShowdownPercentageResponse;
import com.poker.rating.client.calc.model.WinPercentageRequest;
import com.poker.rating.client.calc.model.WinPercentageResponse;
import com.poker.test.IntegrationTag;
import com.poker.test.util.JacksonTestUtils;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HttpJdkPokerHoldemCalculatorClientIT {

  @Test
  @IntegrationTag
  void test_integration() {
    HttpJdkPokerHoldemCalculatorClient client =
        new HttpJdkPokerHoldemCalculatorClient(
            new PokerHoldemCalculatorClientConfig("http://127.0.0.1:8081", null, null));

    WinPercentageResponse winPercentage =
        client.winPercentage(new WinPercentageRequest(List.of("As|Ks", "Td|Jd"), null, null));
    System.out.println(JacksonTestUtils.toJson(winPercentage));

    winPercentage =
        client.winPercentage(
            new WinPercentageRequest(
                List.of("As|Ks", "Td|Jd"),
                List.of(Card.of("Ts"), Card.of("4s"), Card.of("Qd")),
                null));
    System.out.println(JacksonTestUtils.toJson(winPercentage));

    winPercentage =
        client.winPercentage(
            new WinPercentageRequest(
                List.of("As|Ks", "Td|Jd"),
                List.of(Card.of("Ts"), Card.of("4s"), Card.of("Qd")),
                Set.of(Card.of("Ad"))));
    System.out.println(JacksonTestUtils.toJson(winPercentage));

    ShowdownPercentageResponse showdownPercentage =
        client.showdownPercentage(
            new ShowdownPercentageRequest(
                List.of(Card.of("As"), Card.of("Kd"), Card.of("Ts")),
                List.of(Card.of("4d"), Card.of("Jd"))));
    System.out.println(JacksonTestUtils.toJson(showdownPercentage));
  }
}
