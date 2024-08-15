package com.poker.rating.rule;

import static com.poker.util.FluentUtils.not;

import com.poker.model.game.Bet;
import com.poker.model.game.BetPosition;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.model.rating.ShowdownType;
import com.poker.rating.rule.BetRule.BetCategory;
import com.poker.rating.rule.CallRule.CallCategory;
import com.poker.rating.rule.FoldRule.FoldCategory;
import com.poker.rating.rule.extra.BetExtraDecisionSupplier;
import com.poker.rating.rule.extra.CallExtraDecisionSupplier;
import com.poker.rating.rule.extra.CrackedExtraDecisionSupplier;
import com.poker.rating.rule.extra.FoldExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.RiverPointSupplier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RiverRule extends BaseRatingRule {

  private static final long BLUFF_RE_RAISE_EXTRA_POINTS = 20L;
  private final RiverPointSupplier riverPointSupplier;
  private final BetExtraDecisionSupplier betExtraDecisionSupplier;
  private final CallExtraDecisionSupplier callExtraDecisionSupplier;
  private final FoldExtraDecisionSupplier foldExtraDecisionSupplier;
  private final CrackedExtraDecisionSupplier crackedExtraDecisionSupplier;

  public RiverRule(
      RiverPointSupplier riverPointSupplier,
      ShowdownTypeMapper showdownTypeMapper,
      BetExtraDecisionSupplier betExtraDecisionSupplier,
      CallExtraDecisionSupplier callExtraDecisionSupplier,
      FoldExtraDecisionSupplier foldExtraDecisionSupplier,
      CrackedExtraDecisionSupplier crackedExtraDecisionSupplier) {
    super("River Rule", showdownTypeMapper);
    this.riverPointSupplier = riverPointSupplier;
    this.betExtraDecisionSupplier = betExtraDecisionSupplier;
    this.callExtraDecisionSupplier = callExtraDecisionSupplier;
    this.foldExtraDecisionSupplier = foldExtraDecisionSupplier;
    this.crackedExtraDecisionSupplier = crackedExtraDecisionSupplier;
  }

  private static Integer requiredRankOrThrow(PlayerPercentage playerPercentage) {
    if (playerPercentage.getRank() == null) {
      throw new IllegalStateException("Player rank value is required on river");
    }

    return playerPercentage.getRank();
  }

  @Override
  public List<RuleDecision> baseExecute(RuleContext ruleContext) {
    var gameState = ruleContext.gameState();
    var gameStateIndex = gameState.gameStateIndex();

    if (gameStateIndex.bettingRound() != BettingRoundType.RIVER) {
      return List.of();
    }

    var currentUserId = gameState.userId();
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();
    List<PlayerPercentage> winPercentagesDesc =
        RuleUtils.getCurrentStatePlayerPercentagesOrderByWinDesc(gameState);
    PlayerPercentage topFirstWinPercentage = requiredFirstPlayerPercentage(winPercentagesDesc);
    var isCurrentUserWinner =
        currentUserId.equals(topFirstWinPercentage.getUserId())
            && topFirstWinPercentage.getWinPercentage() == 100.0;

    PlayerPercentage playerPercentage =
        RuleUtils.getPlayerPercentage(
            gameStateIndexPlayerPercentage, gameStateIndex, currentUserId);
    if (isCurrentUserWinner) {
      return winnerRules(gameState, playerPercentage);
    } else {
      return nonWinnerRules(gameState, playerPercentage);
    }
  }

  private List<RuleDecision> winnerRules(GameState gameState, PlayerPercentage playerPercentage) {
    var currentBet = gameState.currentBet();
    ShowdownType showdownType = showdownType(playerPercentage.getShowdownPercentage());
    List<Bet> prevRoundBetsReverse = RuleUtils.getPreviousRoundBetsReverse(gameState);

    boolean anyCheckBefore = anyCheckBefore(prevRoundBetsReverse);
    if (RuleUtils.isCheck(currentBet) && anyCheckBefore) {
      if (gameState.betPosition() != BetPosition.LAST_TO_ACT) {
        long points =
            Objects.requireNonNull(
                riverPointSupplier.failedTrapPoints().get(showdownType),
                "Failed trap points missing for showdown: " + showdownType);
        return Stream.concat(
                Stream.of(
                    ruleDecision(
                        gameState,
                        "Winning hand and he checks and you check",
                        points,
                        playerPercentage)),
                crackedExtraDecisionSupplier
                    .extraDecisions(name(), gameState, playerPercentage)
                    .stream())
            .toList();
      } else {
        long points =
            Objects.requireNonNull(
                riverPointSupplier.winningHandOnLastCheckPoints().get(showdownType),
                "Winning hand on last check points missing for showdown: " + showdownType);
        return List.of(
            ruleDecision(
                gameState, "Winning hand and he checks and you check", points, playerPercentage));
      }
    }

    boolean anyBetBefore = anyBetBefore(prevRoundBetsReverse);
    if (RuleUtils.isFold(currentBet)) { //  && anyBetBefore ??
      long points =
          Objects.requireNonNull(
              riverPointSupplier.foldOnWinningHandPoints().get(showdownType),
              "Fold on winning hand points missing for showdown: " + showdownType);
      return Stream.concat(
              Stream.of(
                  ruleDecision(
                      gameState,
                      "Winning hand and he bets and you fold",
                      points,
                      playerPercentage)),
              foldExtraDecisionSupplier
                  .extraDecisions(
                      name(), gameState, FoldCategory.INCORRECT_FOLD, playerPercentage, null, null)
                  .stream())
          .toList();
    }

    if (anyBetBefore && RuleUtils.isNotAnyBet(currentBet)) {
      long points =
          Objects.requireNonNull(
              riverPointSupplier.winningHandNoReRaisePoints().get(showdownType),
              "Winning hand no re-raise points missing for showdown: " + showdownType);
      return List.of(
          ruleDecision(
              gameState,
              "Winning hand and he bets and you do not re-raise",
              points,
              playerPercentage));
    }

    if (RuleUtils.isAnyBet(currentBet)) {
      if (not(anyBetBefore)) {
        long points =
            Objects.requireNonNull(
                riverPointSupplier.winningHandYouBetPoints().get(showdownType),
                "Winning hand and you bet points missing for showdown: " + showdownType);
        return List.of(
            ruleDecision(gameState, "You have winning hand and you bet", points, playerPercentage));
      } else {
        return raiseBestHandToGetMaxValue(gameState, playerPercentage);
      }
    }

    return List.of();
  }

  private List<RuleDecision> raiseBestHandToGetMaxValue(
      GameState gameState, PlayerPercentage playerPercentage) {
    boolean youBetFirst =
        RuleUtils.getPreviousRoundBets(gameState).stream()
            .filter(RuleUtils::isAnyBet)
            .findFirst()
            .filter(bet -> bet.getUserId().equals(gameState.userId()))
            .isPresent();
    ShowdownType showdownType = showdownType(playerPercentage.getShowdownPercentage());
    if (youBetFirst) {
      long points =
          Objects.requireNonNull(
              riverPointSupplier.youBetAndYourRaiseToGetMaxValue().get(showdownType),
              "You bet, he raises, you re-raise with best hand to try to get max value points missing for showdown: "
                  + showdownType);
      return List.of(
          ruleDecision(
              gameState,
              "You bet, he raises, you re-raise with best hand to try to get max value",
              points,
              playerPercentage));
    } else {
      long points =
          Objects.requireNonNull(
              riverPointSupplier.opponentBetsAndYourRaiseToGetMaxValue().get(showdownType),
              "Opponent bets and You raise with the best hand in order to try to get max value points missing for showdown: "
                  + showdownType);
      return Stream.concat(
              Stream.of(
                  ruleDecision(
                      gameState,
                      "Opponent bets and You raise with the best hand in order to try to get max value",
                      points,
                      playerPercentage)),
              betExtraDecisionSupplier
                  .extraDecisions(
                      name(), gameState, BetCategory.GOOD_BET, playerPercentage, null, null)
                  .stream())
          .toList();
    }
  }

  private boolean anyCheckBefore(List<Bet> prevRoundBetsReverse) {
    return prevRoundBetsReverse.stream().anyMatch(RuleUtils::isCheck);
  }

  private boolean anyBetBefore(List<Bet> prevRoundBetsReverse) {
    return prevRoundBetsReverse.stream().anyMatch(RuleUtils::isAnyBet);
  }

  private boolean anyYourBetBefore(List<Bet> prevRoundBetsReverse, String userId) {
    return prevRoundBetsReverse.stream()
        .filter(bet -> userId.equals(bet.getUserId()))
        .anyMatch(RuleUtils::isAnyBet);
  }

  private List<RuleDecision> nonWinnerRules(
      GameState gameState, PlayerPercentage playerPercentage) {
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();

    List<PlayerPercentage> topRankHandPlayers =
        RuleUtils.getPlayerPercentageMap(gameStateIndexPlayerPercentage, gameState.gameStateIndex())
            .values()
            .stream()
            .sorted(Comparator.comparing(RiverRule::requiredRankOrThrow).reversed())
            .toList();
    PlayerPercentage topFirstRankHandPlayer = requiredFirstPlayerPercentage(topRankHandPlayers);

    boolean bestRankHand =
        Objects.equals(topFirstRankHandPlayer.getRank(), playerPercentage.getRank());
    if (bestRankHand) {
      return nonWinnerBestRankHandRules(gameState, playerPercentage);
    } else {
      return nonWinnerNonBestRankHandRules(gameState, playerPercentage);
    }
  }

  private List<RuleDecision> nonWinnerBestRankHandRules(
      GameState gameState, PlayerPercentage playerPercentage) {
    var currentBet = gameState.currentBet();
    List<Bet> prevRoundBetsReverse = RuleUtils.getPreviousRoundBetsReverse(gameState);
    boolean anyBetBefore = anyBetBefore(prevRoundBetsReverse);
    if (RuleUtils.isAnyBet(currentBet) && anyBetBefore) {
      return raiseBestHandToGetMaxValue(gameState, playerPercentage);
    }

    return List.of();
  }

  private List<RuleDecision> nonWinnerNonBestRankHandRules(
      GameState gameState, PlayerPercentage playerPercentage) {
    var currentBet = gameState.currentBet();
    ShowdownType showdownType = showdownType(playerPercentage.getShowdownPercentage());
    List<Bet> prevRoundBetsReverse = RuleUtils.getPreviousRoundBetsReverse(gameState);

    if (RuleUtils.isAnyBet(currentBet)
        && not(anyYourBetBefore(prevRoundBetsReverse, gameState.userId()))) {
      boolean allPlayersGettingFoldAtTheEndRound =
          RuleUtils.allOtherPlayersGettingFoldAtTheEndRoundAfterBet(gameState);
      final long points;
      final long extraPoints;
      final String nameDecision;
      final BetCategory betCategory;
      double equityPercentage = RuleUtils.calcBetEquityPercentage(currentBet);
      double pof = RuleUtils.calcPOF(equityPercentage, playerPercentage.getWinPercentage());
      if (allPlayersGettingFoldAtTheEndRound) {
        points =
            Objects.requireNonNull(
                riverPointSupplier.successfulBluffPoints().get(showdownType),
                "Successful bluff points missing for showdown: " + showdownType);
        extraPoints = BLUFF_RE_RAISE_EXTRA_POINTS;
        nameDecision = "Successful Bluff on " + showdownType;
        betCategory = BetCategory.SUCCESSFUL_BLUFF;
      } else {
        points =
            Objects.requireNonNull(
                riverPointSupplier.failedBluffPoints().get(showdownType),
                "Failed bluff points missing for showdown: " + showdownType);
        extraPoints = -BLUFF_RE_RAISE_EXTRA_POINTS;
        nameDecision = "Failed Bluff on " + showdownType;
        betCategory = pofToBluffBetCategory(pof);
      }

      final List<RuleDecision> extraDecision =
          betExtraDecisionSupplier.extraDecisions(
              name(), gameState, betCategory, playerPercentage, equityPercentage, pof);

      List<Bet> myRaisesAfter =
          RuleUtils.getAfterRoundBets(gameState).stream()
              .filter(bet -> RuleUtils.isAnyBet(bet) && bet.getUserId().equals(gameState.userId()))
              .toList();

      return Stream.of(
              Stream.of(ruleDecision(gameState, nameDecision, points, playerPercentage)),
              extraDecision.stream(),
              myRaisesAfter.stream()
                  .flatMap(
                      bet -> {
                        RuleDecision betRuleDecision =
                            ruleDecision(
                                gameState,
                                "You are beat and you bet, he re-raises, you re-raise and everyone folds",
                                extraPoints,
                                playerPercentage);
                        return Stream.concat(
                            Stream.of(betRuleDecision),
                            betExtraDecisionSupplier
                                .extraDecisions(
                                    name(),
                                    gameState,
                                    betCategory,
                                    playerPercentage,
                                    equityPercentage,
                                    pof)
                                .stream());
                      }))
          .flatMap(Function.identity())
          .toList();
    }

    if (RuleUtils.isCall(currentBet)) {
      long points =
          Objects.requireNonNull(
              riverPointSupplier.callOnNonBestHandPoints().get(showdownType),
              "Call on non best hand points missing for showdown: " + showdownType);
      return Stream.concat(
              Stream.of(
                  ruleDecision(
                      gameState,
                      "You are beat and he bets and you call",
                      points,
                      playerPercentage)),
              callExtraDecisionSupplier
                  .extraDecisions(
                      name(), gameState, CallCategory.BAD_CALL, playerPercentage, null, null)
                  .stream())
          .toList();
    }

    if (RuleUtils.isFold(currentBet)) {
      long points =
          Objects.requireNonNull(
              riverPointSupplier.foldOnNonBestHandPoints().get(showdownType),
              "Fold on non best hand points missing for showdown: " + showdownType);
      return Stream.concat(
              Stream.of(
                  ruleDecision(
                      gameState,
                      "You are beat and he bets and you fold",
                      points,
                      playerPercentage)),
              foldExtraDecisionSupplier
                  .extraDecisions(
                      name(), gameState, FoldCategory.CORRECT_FOLD, playerPercentage, null, null)
                  .stream())
          .toList();
    }

    return List.of();
  }

  private BetCategory pofToBluffBetCategory(double pof) {
    if (pof < 0) {
      return BetCategory.GOOD_BET;
    } else if (pof >= 0 && pof < 20) {
      return BetCategory.NON_PENALIZED_BET;
    } else if (pof >= 20 && pof < 50) {
      return BetCategory.BAD_BLUFF;
    } else if (pof >= 50 && pof < 75) {
      return BetCategory.TERRIBLE_BLUFF;
    } else {
      return BetCategory.HORRIBLE_BLUFF;
    }
  }

  private PlayerPercentage requiredFirstPlayerPercentage(
      List<PlayerPercentage> winPercentagesDesc) {
    return Optional.ofNullable(winPercentagesDesc)
        .filter(Predicate.not(List::isEmpty))
        .map(l -> l.get(0))
        .orElseThrow(
            () -> new IllegalStateException("At least one player percentage must exist on river"));
  }
}
