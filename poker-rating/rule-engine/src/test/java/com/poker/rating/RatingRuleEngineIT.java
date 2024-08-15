package com.poker.rating;

import com.poker.model.game.GameHand;
import com.poker.model.rating.RatingRuleExecResult;
import com.poker.test.IntegrationTag;
import com.poker.test.util.FileTestUtils;
import com.poker.test.util.JacksonTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RatingRuleEngineIT {

  @Autowired private RatingRuleEngine ratingRuleEngine;

  @Test
  @IntegrationTag
  void test_integration() {
    GameHand gameHandSample1 =
        JacksonTestUtils.fromJson(
            FileTestUtils.readUtf8Content("games/sample-1/game-hand.json"), GameHand.class);
    RatingRuleExecResult ruleExecResult = ratingRuleEngine.executeRules(gameHandSample1);
    System.out.println(JacksonTestUtils.toJson(ruleExecResult));
  }
}
