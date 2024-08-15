package com.poker.rating.service;

import com.poker.model.game.Card;
import com.poker.model.game.Player;
import com.poker.model.rating.PlayerPercentage;
import com.poker.rating.client.calc.HttpJdkPokerHoldemCalculatorClient;
import com.poker.rating.client.calc.PokerHoldemCalculatorClientConfig;
import com.poker.test.IntegrationTag;
import com.poker.util.task.TaskExecutorFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DefaultPokerPercentageCalculatorIT {

  @Test
  @IntegrationTag
  void test_integration() {
    HttpJdkPokerHoldemCalculatorClient client =
        new HttpJdkPokerHoldemCalculatorClient(
            new PokerHoldemCalculatorClientConfig("http://127.0.0.1:8081", null, null));
    PokerPercentageCalculator calculator =
        new DefaultPokerPercentageCalculator(
            client, new PreFlopShowdownPercentageCalc(), new TaskExecutorFactory().create(10));
    var players =
        List.of(
            new Player("u1", List.of(Card.of("Ks"), Card.of("Td"))),
            new Player("u2", List.of(Card.of("Jc"), Card.of("Th"))),
            new Player("u3", List.of(Card.of("9s"), Card.of("3s"))),
            new Player("u4", List.of(Card.of("6d"), Card.of("9d"))));
    List<Card> board = null;
    Set<Card> deadCards = null;

    System.out.println("PRE FLOP");
    PokerPercentageResult result =
        calculator.calculate(new PercentageCalculatorContext(players, board, deadCards));
    printResult(result, players);

    System.out.println("FLOP");
    board = List.of(Card.of("Qd"), Card.of("8s"), Card.of("Jh"));
    result = calculator.calculate(new PercentageCalculatorContext(players, board, deadCards));
    printResult(result, players);

    System.out.println("TURN");
    board = List.of(Card.of("Qd"), Card.of("8s"), Card.of("Jh"), Card.of("2h"));
    result = calculator.calculate(new PercentageCalculatorContext(players, board, deadCards));
    printResult(result, players);

    System.out.println("RIVER");
    board = List.of(Card.of("Qd"), Card.of("8s"), Card.of("Jh"), Card.of("2h"), Card.of("Qh"));
    result = calculator.calculate(new PercentageCalculatorContext(players, board, deadCards));
    printResult(result, players);
  }

  private void printResult(PokerPercentageResult result, List<Player> players) {
    Map<String, PlayerPercentage> userIdToPlayerPercentage = result.getPlayerPercentages();
    players.forEach(p -> System.out.println(userIdToPlayerPercentage.get(p.getUserId())));
  }
}
