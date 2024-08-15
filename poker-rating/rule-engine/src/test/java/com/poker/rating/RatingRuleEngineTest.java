package com.poker.rating;

import com.poker.model.game.GameHand;
import com.poker.model.rating.RatingRuleExecResult;
import com.poker.test.util.FileTestUtils;
import com.poker.test.util.JacksonTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RatingRuleEngineTest {

  @Test
  void test_execute_rules_sample_1() {
    RatingRuleEngine ratingRuleEngine =
        new RatingRuleEngine(new RatingRuleRegister(), new RandomPokerPercentageCalculator());

    GameHand gameHandSample1 =
        JacksonTestUtils.fromJson(
            FileTestUtils.readUtf8Content("games/sample-1/game-hand.json"), GameHand.class);
    RatingRuleExecResult ruleExecResult = ratingRuleEngine.executeRules(gameHandSample1);

    Assertions.assertNotNull(ruleExecResult);
    System.out.println(JacksonTestUtils.toJson(gameHandSample1));
  }
}
