package com.poker.rating.rule;

import com.andrebreves.tuple.Tuple;
import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.Bet;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.extra.BetExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.BetPointSupplier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class BetRule extends BaseRatingRule {

  private static final Set<BetCategory> FAILED_BLUFFS =
      Set.of(BetCategory.BAD_BLUFF, BetCategory.TERRIBLE_BLUFF, BetCategory.HORRIBLE_BLUFF);

  private final BetPointSupplier betPointSupplier;
  private final BetExtraDecisionSupplier betExtraDecisionSupplier;

  public BetRule(
      BetPointSupplier betPointSupplier,
      ShowdownTypeMapper showdownTypeMapper,
      BetExtraDecisionSupplier betExtraDecisionSupplier) {
    super("Bet Rule", showdownTypeMapper);
    this.betPointSupplier = betPointSupplier;
    this.betExtraDecisionSupplier = betExtraDecisionSupplier;
  }

  @Override
  public List<RuleDecision> baseExecute(RuleContext ruleContext) {
    var gameState = ruleContext.gameState();
    var userId = gameState.userId();
    var gameStateIndex = gameState.gameStateIndex();
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();
    var currentBet = gameState.currentBet();

    if (RuleUtils.isNotAnyBet(currentBet)
        || gameStateIndex.bettingRound() == BettingRoundType.RIVER) {
      return List.of();
    }

    Tuple2<Boolean, PlayerPercentage> playerHasTheBestHand =
        RuleUtils.hasTheBestHandOrEquals(userId, gameStateIndex, gameStateIndexPlayerPercentage);
    if (Boolean.TRUE.equals(playerHasTheBestHand.v1())) {
      return currentlyHasTheBestHand(playerHasTheBestHand.v2(), ruleContext);
    } else {
      return currentlyDoNotHaveTheBestHand(playerHasTheBestHand.v2(), ruleContext);
    }
  }

  private List<RuleDecision> currentlyHasTheBestHand(
      PlayerPercentage playerPercentage, RuleContext ruleContext) {
    log.debug(
        "PLayer '{}' has the best hand '{}'",
        playerPercentage.getUserId(),
        playerPercentage.getWinPercentage());
    var gameState = ruleContext.gameState();
    var userId = gameState.userId();
    var gameStateIndex = gameState.gameStateIndex();

    if (playerPercentage.getWinPercentage() >= 50) {
      return goodBet(gameState, playerPercentage);
    }

    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();
    Optional<Tuple2<GameStateIndex, PlayerPercentage>> aboveFiftyIncreaseOpt =
        RuleUtils.findFirstWinPercentageIncreaseToAbove(
            gameStateIndexPlayerPercentage,
            userId,
            gameStateIndex.bettingRound(),
            gameStateIndex.playerTurnIndex(),
            50);
    if (aboveFiftyIncreaseOpt.isPresent()) {
      Tuple2<GameStateIndex, PlayerPercentage> aboveFiftyIncrease = aboveFiftyIncreaseOpt.get();
      log.debug(
          "Above 50 increase found for player '{}' on turn {}, Good Bet",
          userId,
          aboveFiftyIncrease.v1());
      return goodBet(gameState, playerPercentage);
    } else {
      return pofDecisionOnBestHand(gameState, playerPercentage);
    }
  }

  private List<RuleDecision> pofDecisionOnBestHand(
      GameState gameState, PlayerPercentage playerPercentage) {
    var currentBet = gameState.currentBet();
    double equityPercentage = RuleUtils.calcBetEquityPercentage(currentBet);
    double pof = RuleUtils.calcPOF(equityPercentage, playerPercentage.getWinPercentage());
    log.debug(
        "Player '{}' equity is '{}' and POF '{}' on best hand",
        playerPercentage.getUserId(),
        equityPercentage,
        pof);
    if (pof < 0) {
      return goodBet(gameState, playerPercentage);
    } else if (pof >= 0 && pof < 20) {
      return nonPenalizedBet(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 20 && pof < 50) {
      return badBet(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 50 && pof < 75) {
      return terribleBet(gameState, playerPercentage, equityPercentage, pof);
    } else {
      return horribleBet(gameState, playerPercentage, equityPercentage, pof);
    }
  }

  private List<RuleDecision> goodBet(GameState gameState, PlayerPercentage playerPercentage) {
    return betDecisions(gameState, BetCategory.GOOD_BET, playerPercentage, null, null);
  }

  private List<RuleDecision> successfulBluff(
      GameState gameState, PlayerPercentage playerPercentage) {
    return betDecisions(gameState, BetCategory.SUCCESSFUL_BLUFF, playerPercentage, null, null);
  }

  private List<RuleDecision> badBet(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.BAD_BET, playerPercentage, equity, pof);
  }

  private List<RuleDecision> nonPenalizedBet(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.NON_PENALIZED_BET, playerPercentage, equity, pof);
  }

  private List<RuleDecision> badBluff(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.BAD_BLUFF, playerPercentage, equity, pof);
  }

  private List<RuleDecision> terribleBluff(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.TERRIBLE_BLUFF, playerPercentage, equity, pof);
  }

  private List<RuleDecision> horribleBluff(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.HORRIBLE_BLUFF, playerPercentage, equity, pof);
  }

  private List<RuleDecision> terribleBet(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.TERRIBLE_BET, playerPercentage, equity, pof);
  }

  private List<RuleDecision> horribleBet(
      GameState gameState, PlayerPercentage playerPercentage, double equity, double pof) {
    return betDecisions(gameState, BetCategory.HORRIBLE_BET, playerPercentage, equity, pof);
  }

  private List<RuleDecision> betDecisions(
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    var gameStateIndex = gameState.gameStateIndex();

    if (FAILED_BLUFFS.contains(betCategory) && RuleUtils.leadsToWinnerOrWinningHand(gameState)) {
      Tuple2<Long, String[]> points =
          Tuple.of(
              10L,
              new String[] {"Unsuccessful bluff leads to you winning hand. Turn points into +10."});
      return List.of(
          ruleDecision(
              gameState,
              betCategory.toString(),
              points.v1(),
              playerPercentage,
              equity,
              pof,
              points.v2()));
    }

    Tuple2<Long, String[]> points =
        Tuple.of(getPoints(gameStateIndex.bettingRound(), betCategory), new String[0]);
    RuleDecision betDecision =
        ruleDecision(
            gameState,
            betCategory.toString(),
            points.v1(),
            playerPercentage,
            equity,
            pof,
            points.v2());
    List<RuleDecision> extraDecisions =
        betExtraDecisions(gameState, betCategory, playerPercentage, equity, pof);
    if (extraDecisions.isEmpty()) {
      return List.of(betDecision);
    } else {
      return Stream.concat(Stream.of(betDecision), extraDecisions.stream()).toList();
    }
  }

  private List<RuleDecision> betExtraDecisions(
      GameState gameState,
      BetCategory betCategory,
      PlayerPercentage playerPercentage,
      @Nullable Double equity,
      @Nullable Double pof) {
    return betExtraDecisionSupplier.extraDecisions(
        name(), gameState, betCategory, playerPercentage, equity, pof);
  }

  private Long getPoints(BettingRoundType bettingRound, BetCategory betCategory) {
    var key = Tuple2.of(bettingRound, betCategory);
    return Objects.requireNonNull(
        betPointSupplier.pointsMatrix().get(key),
        () -> "Points missing for: " + key.v1() + ", " + key.v2());
  }

  private List<RuleDecision> currentlyDoNotHaveTheBestHand(
      PlayerPercentage playerPercentage, RuleContext ruleContext) {
    log.debug(
        "PLayer '{}' do not have the best hand '{}'",
        playerPercentage.getUserId(),
        playerPercentage.getWinPercentage());

    var gameState = ruleContext.gameState();
    var userId = gameState.userId();
    var gameStateIndex = gameState.gameStateIndex();
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();

    Optional<Tuple2<GameStateIndex, PlayerPercentage>> aboveFiftyIncreaseOpt =
        RuleUtils.findFirstWinPercentageIncreaseToAbove(
            gameStateIndexPlayerPercentage,
            userId,
            gameStateIndex.bettingRound(),
            gameStateIndex.playerTurnIndex(),
            50);
    if (aboveFiftyIncreaseOpt.isPresent()) {
      Tuple2<GameStateIndex, PlayerPercentage> aboveFiftyIncrease = aboveFiftyIncreaseOpt.get();
      log.debug(
          "Above 50 increase found for player '{}' on turn {}, Good Bet and Successful Bluff",
          userId,
          aboveFiftyIncrease.v1());
      Optional<List<Bet>> allHandsFoldAfterBetOpt = RuleUtils.allHandsFoldAfterBet(gameState);
      if (allHandsFoldAfterBetOpt.isPresent()) {
        return successfulBluff(gameState, playerPercentage);
      } else {
        return goodBet(gameState, playerPercentage);
      }
    } else {
      return pofDecisionOnNonBestHand(gameState, playerPercentage);
    }
  }

  private List<RuleDecision> pofDecisionOnNonBestHand(
      GameState gameState, PlayerPercentage playerPercentage) {
    var currentBet = gameState.currentBet();
    double equityPercentage = RuleUtils.calcBetEquityPercentage(currentBet);
    double pof = RuleUtils.calcPOF(equityPercentage, playerPercentage.getWinPercentage());
    log.debug(
        "Player '{}' equity is '{}' and POF '{}' on non best hand",
        playerPercentage.getUserId(),
        equityPercentage,
        pof);
    if (pof < 0) {
      return goodBet(gameState, playerPercentage);
    } else if (pof >= 0 && pof < 20) {
      return nonPenalizedBet(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 20 && pof < 50) {
      return badBluff(gameState, playerPercentage, equityPercentage, pof);
    } else if (pof >= 50 && pof < 75) {
      return terribleBluff(gameState, playerPercentage, equityPercentage, pof);
    } else {
      return horribleBluff(gameState, playerPercentage, equityPercentage, pof);
    }
  }

  public enum BetCategory {
    GOOD_BET("Good Bet"),
    BAD_BET("Bad Bet"),
    TERRIBLE_BET("Terrible Bet"),
    HORRIBLE_BET("Horrible Bet"),
    NON_PENALIZED_BET("Non Penalized Bet"),
    SUCCESSFUL_BLUFF("Successful bluff"),
    BAD_BLUFF("Bad Bluff"),
    TERRIBLE_BLUFF("Terrible Bluff"),
    HORRIBLE_BLUFF("Horrible Bluff");

    private final String name;

    BetCategory(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
