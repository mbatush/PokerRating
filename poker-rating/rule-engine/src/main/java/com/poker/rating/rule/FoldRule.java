package com.poker.rating.rule;

import com.andrebreves.tuple.Tuple2;
import com.google.common.collect.Lists;
import com.poker.model.game.Bet;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.extra.FoldExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.FoldPointSupplier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FoldRule extends BaseRatingRule {

  private final FoldPointSupplier foldPointSupplier;
  private final FoldExtraDecisionSupplier foldExtraDecisionSupplier;

  public FoldRule(
      FoldPointSupplier foldPointSupplier,
      ShowdownTypeMapper showdownTypeMapper,
      FoldExtraDecisionSupplier foldExtraDecisionSupplier) {
    super("Fold Rule", showdownTypeMapper);
    this.foldPointSupplier = foldPointSupplier;
    this.foldExtraDecisionSupplier = foldExtraDecisionSupplier;
  }

  @Override
  public List<RuleDecision> baseExecute(RuleContext ruleContext) {
    var gameState = ruleContext.gameState();
    var userId = gameState.userId();
    var gameStateIndex = gameState.gameStateIndex();
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();
    var currentBet = gameState.currentBet();

    if (RuleUtils.isNotFold(currentBet)
        || gameStateIndex.bettingRound() == BettingRoundType.RIVER) {
      return List.of();
    }

    PlayerPercentage playerPercentage =
        RuleUtils.getPlayerPercentage(gameStateIndexPlayerPercentage, gameStateIndex, userId);
    double winPercentage = playerPercentage.getWinPercentage();
    double callAmountOnFold = findCallAmountOnFold(gameState);
    double equityPercentage = RuleUtils.calcEquityPercentage(callAmountOnFold, currentBet.getPot());
    double pof = RuleUtils.calcPOF(equityPercentage, playerPercentage.getWinPercentage());
    log.debug(
        "Player '{}' win '{}', showdown '{}', equity '{}' and POF '{}'",
        playerPercentage.getUserId(),
        winPercentage,
        playerPercentage.getShowdownPercentage(),
        equityPercentage,
        pof);
    if (pof >= 0) {
      return correctFold(gameState, playerPercentage, equityPercentage, pof, callAmountOnFold);
    } else if (pof >= -20) {
      return nonPenalizedFold(gameState, playerPercentage, equityPercentage, pof, callAmountOnFold);
    } else {
      return incorrectFold(gameState, playerPercentage, equityPercentage, pof, callAmountOnFold);
    }
  }

  private double findCallAmountOnFold(GameState gameState) {
    GameStateIndex gameStateIndex = gameState.gameStateIndex();
    String currentUserId = gameState.userId();
    if (gameStateIndex.playerTurnIndex() == 0) {
      return RuleUtils.getBigBlindAmount(gameState);
    }

    List<Bet> previousRoundBets = getNonEmptyPreviousRoundBets(gameState);
    List<Bet> reversePrevBets = Lists.reverse(previousRoundBets);
    double currentPrevBetAmount =
        findPreviousBetAmountFor(currentUserId, reversePrevBets).orElse(0.0);
    double prevBetAmount =
        previousBetAmountNotFor(currentUserId, reversePrevBets)
            .orElseGet(() -> RuleUtils.getBigBlindAmount(gameState));
    double callAmountOnFold = prevBetAmount - currentPrevBetAmount;
    if (callAmountOnFold <= 0) {
      throw new IllegalStateException("Calculated call amount on fold must be positive");
    }

    return callAmountOnFold;
  }

  private Optional<Double> findPreviousBetAmountFor(
      String currentUserId, List<Bet> reversePrevBets) {
    return reversePrevBets.stream()
        .filter(RuleUtils::isAnyBet)
        .filter(bet -> currentUserId.equals(bet.getUserId()))
        .map(Bet::getAmount)
        .findFirst();
  }

  private Optional<Double> previousBetAmountNotFor(
      String currentUserId, List<Bet> reversePrevBets) {
    return reversePrevBets.stream()
        .filter(RuleUtils::isAnyBet)
        .filter(Predicate.not(bet -> currentUserId.equals(bet.getUserId())))
        .map(Bet::getAmount)
        .findFirst();
  }

  private List<Bet> getNonEmptyPreviousRoundBets(GameState gameState) {
    List<Bet> prevRoundBets = RuleUtils.getPreviousRoundBets(gameState);
    if (prevRoundBets.isEmpty()) {
      throw new IllegalStateException(
          "Unexpected state on 'findCallAmountOnFold' method, previous round bets are missing for game state: "
              + gameState.gameStateIndex());
    }

    return prevRoundBets;
  }

  private List<RuleDecision> correctFold(
      GameState gameState,
      PlayerPercentage playerPercentage,
      double equity,
      double pof,
      double callAmountCalculated) {
    return foldDecisions(
        gameState, FoldCategory.CORRECT_FOLD, playerPercentage, equity, pof, callAmountCalculated);
  }

  private List<RuleDecision> nonPenalizedFold(
      GameState gameState,
      PlayerPercentage playerPercentage,
      double equity,
      double pof,
      double callAmountCalculated) {
    return foldDecisions(
        gameState,
        FoldCategory.NON_PENALIZED_FOLD,
        playerPercentage,
        equity,
        pof,
        callAmountCalculated);
  }

  private List<RuleDecision> incorrectFold(
      GameState gameState,
      PlayerPercentage playerPercentage,
      double equity,
      double pof,
      double callAmountCalculated) {
    return foldDecisions(
        gameState,
        FoldCategory.INCORRECT_FOLD,
        playerPercentage,
        equity,
        pof,
        callAmountCalculated);
  }

  private List<RuleDecision> foldDecisions(
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      Double equity,
      Double pof,
      double callAmountCalculated) {
    var gameStateIndex = gameState.gameStateIndex();
    var points = getPoints(gameStateIndex.bettingRound(), foldCategory);
    var foldDecision =
        ruleDecision(
            gameState,
            foldCategory.toString(),
            points,
            playerPercentage,
            equity,
            pof,
            "Calculated call amount on fold: " + callAmountCalculated);
    List<RuleDecision> extraDecisions =
        foldExtraDecisions(gameState, foldCategory, playerPercentage, equity, pof);
    if (extraDecisions.isEmpty()) {
      return List.of(foldDecision);
    } else {
      return Stream.concat(Stream.of(foldDecision), extraDecisions.stream()).toList();
    }
  }

  private List<RuleDecision> foldExtraDecisions(
      GameState gameState,
      FoldCategory foldCategory,
      PlayerPercentage playerPercentage,
      double equity,
      double pof) {
    return foldExtraDecisionSupplier.extraDecisions(
        name(), gameState, foldCategory, playerPercentage, equity, pof);
  }

  private Long getPoints(BettingRoundType bettingRound, FoldCategory callCategory) {
    var key = Tuple2.of(bettingRound, callCategory);
    return Objects.requireNonNull(
        foldPointSupplier.pointsMatrix().get(key),
        () -> "Points missing for: " + key.v1() + ", " + key.v2());
  }

  public enum FoldCategory {
    CORRECT_FOLD("Correct Fold"),
    INCORRECT_FOLD("Incorrect Fold"),
    NON_PENALIZED_FOLD("Non Penalized Fold");

    private final String name;

    FoldCategory(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
