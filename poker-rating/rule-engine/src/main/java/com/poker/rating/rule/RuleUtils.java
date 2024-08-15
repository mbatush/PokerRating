package com.poker.rating.rule;

import static com.poker.util.FluentUtils.not;

import com.andrebreves.tuple.Tuple2;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.poker.model.game.Bet;
import com.poker.model.game.BetType;
import com.poker.model.game.BettingRoundType;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.util.CollectionUtils;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public final class RuleUtils {

  private RuleUtils() {
    throw new UnsupportedOperationException("No constructor");
  }

  public static boolean leadsToWinnerOrWinningHand(GameState gameState) {
    if (isTheWinner(gameState, gameState.userId())) {
      return true;
    }
    Tuple2<BettingRoundType, List<Bet>> lastRoundBet =
        gameState.roundBets().get(gameState.roundBets().size() - 1);
    BettingRoundType lastBetRoundType = lastRoundBet.v1();
    List<Bet> lastRoundBets = lastRoundBet.v2();
    if (lastBetRoundType != BettingRoundType.RIVER) {
      return false;
    }
    if (lastRoundBets.isEmpty()) {
      return false;
    }
    var lastPlayerTurnIndex = lastRoundBets.size() - 1;
    if (lastRoundBets.get(lastPlayerTurnIndex).getType() == BetType.FOLD) {
      lastPlayerTurnIndex += 1;
    }
    GameStateIndex lastGameStateIndex = new GameStateIndex(lastBetRoundType, lastPlayerTurnIndex);
    Map<String, PlayerPercentage> lasPercentagesMap =
        getPlayerPercentageMap(gameState.gameStateIndexPlayerPercentage(), lastGameStateIndex);
    PlayerPercentage currentPlayerPercentage = lasPercentagesMap.get(gameState.userId());
    if (currentPlayerPercentage == null) {
      return false;
    }

    return currentPlayerPercentage.getWinPercentage() >= 100.0d;
  }

  public static boolean isTheWinner(GameState gameState, String userId) {
    return gameState.players().stream().anyMatch(p -> userId.equals(p.getUserId()) && p.isWinner());
  }

  public static boolean isNotTheWinner(GameState gameState, String userId) {
    return not(isTheWinner(gameState, userId));
  }

  public static Tuple2<Boolean, PlayerPercentage> hasTheBestHandOrEquals(
      String userId,
      GameStateIndex gameStateIndex,
      Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage) {
    PlayerPercentage playerPercentage =
        getPlayerPercentage(gameStateIndexPlayerPercentage, gameStateIndex, userId);
    PlayerPercentage maxWinPercentage =
        maxWinPercentage(gameStateIndexPlayerPercentage, gameStateIndex);
    boolean hasBestHand =
        playerPercentage.getWinPercentage() >= maxWinPercentage.getWinPercentage();
    return Tuple2.of(hasBestHand, playerPercentage);
  }

  public static PlayerPercentage maxWinPercentage(
      Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage,
      GameStateIndex gameStateIndex) {
    return getPlayerPercentageMap(gameStateIndexPlayerPercentage, gameStateIndex).values().stream()
        .max(Comparator.comparing(PlayerPercentage::getWinPercentage))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Game state index player percentage map is missing max win percentage for: "
                        + gameStateIndex));
  }

  public static PlayerPercentage getPlayerPercentage(
      Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage,
      GameStateIndex gameStateIndex,
      String userId) {
    return Objects.requireNonNull(
        getPlayerPercentageMap(gameStateIndexPlayerPercentage, gameStateIndex).get(userId),
        () ->
            "Game state index player percentage map is missing value for: "
                + gameStateIndex
                + "and user ID:"
                + userId);
  }

  public static Map<String, PlayerPercentage> getPlayerPercentageMap(
      Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage,
      GameStateIndex gameStateIndex) {
    return Objects.requireNonNull(
        gameStateIndexPlayerPercentage.get(gameStateIndex),
        () -> "Game state index player percentage map is missing value for: " + gameStateIndex);
  }

  public static List<PlayerPercentage> getCurrentStatePlayerPercentagesOrderByWinDesc(
      GameState gameState) {
    var playerPercentageComparator =
        Comparator.comparingDouble(PlayerPercentage::getWinPercentage).reversed();
    return getCurrentStatePlayerPercentagesOrderBy(gameState, playerPercentageComparator);
  }

  public static List<PlayerPercentage> getCurrentStatePlayerPercentagesOrderByShowdownDesc(
      GameState gameState) {
    var playerPercentageComparator =
        Comparator.comparingDouble(PlayerPercentage::getShowdownPercentage).reversed();
    return getCurrentStatePlayerPercentagesOrderBy(gameState, playerPercentageComparator);
  }

  public static List<PlayerPercentage> getCurrentStatePlayerPercentagesOrderBy(
      GameState gameState, Comparator<PlayerPercentage> playerPercentageComparator) {
    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameState.gameStateIndexPlayerPercentage();
    GameStateIndex gameStateIndex = gameState.gameStateIndex();
    return getPlayerPercentageMap(gameStateIndexPlayerPercentage, gameStateIndex).values().stream()
        .sorted(playerPercentageComparator)
        .toList();
  }

  public static PlayerPercentage getCurrentOpponentTopShowdownPlayerPercentage(
      GameState gameState) {
    var currentPlayerPercentage =
        RuleUtils.getPlayerPercentage(
            gameState.gameStateIndexPlayerPercentage(),
            gameState.gameStateIndex(),
            gameState.userId());
    return getCurrentStatePlayerPercentagesOrderByShowdownDesc(gameState).stream()
        .filter(
            Predicate.not(
                playerPercentage ->
                    playerPercentage.getUserId().equals(currentPlayerPercentage.getUserId())))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Top showdown player percentages are empty on: " + gameState.gameStateIndex()));
  }

  public static List<Bet> getRoundBets(GameState gameState, BettingRoundType bettingRoundType) {
    return gameState.roundBets().stream()
        .filter(t -> t.v1() == bettingRoundType)
        .map(Tuple2::v2)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Game state is missing round bets for: " + bettingRoundType));
  }

  public static List<Bet> getPreviousRoundBets(GameState gameState) {
    GameStateIndex gameStateIndex = gameState.gameStateIndex();
    int currentBetIndex = gameStateIndex.playerTurnIndex();
    if (currentBetIndex == 0) {
      return List.of();
    }

    List<Bet> roundBets = getRoundBets(gameState, gameStateIndex.bettingRound());
    if (roundBets.isEmpty()) {
      return List.of();
    }

    if (currentBetIndex > roundBets.size()) {
      return List.of();
    }

    return roundBets.subList(0, currentBetIndex);
  }

  public static List<Bet> getAfterRoundBets(GameState gameState) {
    GameStateIndex gameStateIndex = gameState.gameStateIndex();
    List<Bet> roundBets = getRoundBets(gameState, gameStateIndex.bettingRound());
    int currentBetIndex = gameStateIndex.playerTurnIndex();
    return CollectionUtils.safeSubList(roundBets, currentBetIndex + 1, roundBets.size());
  }

  public static List<Bet> getPreviousRoundBetsReverse(GameState gameState) {
    return Lists.reverse(getPreviousRoundBets(gameState));
  }

  public static double getBigBlindAmount(GameState gameState) {
    List<Bet> preFlopBets = getRoundBets(gameState, BettingRoundType.PRE_FLOP);
    if (preFlopBets.size() < 2) {
      throw new IllegalStateException(
          "Could not find big blind amount. Pre flop round bets must be greater >= 2.");
    }

    Bet bigBlindBet = preFlopBets.get(1);
    if (isNotBigBlind(bigBlindBet)) {
      throw new IllegalStateException(
          "Could not find big blind amount. Second bet is not type of big-blind.");
    }

    double bigBlindAmount = bigBlindBet.getAmount();
    if (bigBlindAmount <= 0) {
      throw new IllegalStateException("Big blind amount must be greater then 0.");
    }
    return bigBlindAmount;
  }

  /**
   * Returns non-empty optional with all folded hands if all hands folded after current game bet,
   * otherwise {@link Optional#empty()}.
   */
  public static Optional<List<Bet>> allHandsFoldAfterBet(GameState gameState) {
    if (isNotAnyBet(gameState.currentBet())) {
      throw new IllegalArgumentException(
          "'isAllHandsFoldAfterBet' method requires a bet, got: "
              + gameState.currentBet().getType());
    }
    List<Bet> roundBets = getRoundBets(gameState, gameState.gameStateIndex().bettingRound());
    int currentBetIndex = gameState.gameStateIndex().playerTurnIndex();
    if (currentBetIndex >= roundBets.size() - 1) {
      return Optional.empty();
    }
    var afterBets = roundBets.subList(currentBetIndex + 1, roundBets.size());
    boolean allFolded = afterBets.stream().allMatch(RuleUtils::isFold);
    return allFolded ? Optional.of(afterBets) : Optional.empty();
  }

  /**
   * Returns {@code true} if all player folded after current game bet in some point of time,
   * otherwise {@code false}.
   */
  public static boolean allOtherPlayersGettingFoldAtTheEndRoundAfterBet(GameState gameState) {
    if (isNotAnyBet(gameState.currentBet())) {
      throw new IllegalArgumentException(
          "'allPlayerGettingFoldAtTheEndAfterBet' method requires a bet, got: "
              + gameState.currentBet().getType());
    }
    var bettingRoundType = gameState.gameStateIndex().bettingRound();
    List<Bet> roundBets = getRoundBets(gameState, bettingRoundType);
    var lastGameStateIndex = roundBets.size() - 1;
    if (roundBets.get(lastGameStateIndex).getType() == BetType.FOLD) {
      lastGameStateIndex += 1;
    }

    GameStateIndex lastGameState = new GameStateIndex(bettingRoundType, lastGameStateIndex);
    Map<String, PlayerPercentage> lasPercentagesMap =
        getPlayerPercentageMap(gameState.gameStateIndexPlayerPercentage(), lastGameState);
    return lasPercentagesMap.size() == 1
        && lasPercentagesMap.containsKey(gameState.currentBet().getUserId());
  }

  public static boolean isAnyBet(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.RAISE
        || betType == BetType.ALL_IN
        || betType == BetType.SMALL_BLIND
        || betType == BetType.BIG_BLIND;
  }

  public static boolean isNotAnyBet(Bet bet) {
    return not(isAnyBet(bet));
  }

  public static boolean isCall(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.CALL;
  }

  public static boolean isNotCall(Bet bet) {
    return not(isCall(bet));
  }

  public static boolean isFold(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.FOLD;
  }

  public static boolean isNotFold(Bet bet) {
    return not(isFold(bet));
  }

  public static boolean isCheck(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.CHECK;
  }

  public static boolean isNotCheck(Bet bet) {
    return not(isCheck(bet));
  }

  public static boolean isAllIn(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.ALL_IN;
  }

  public static boolean isNotAllIn(Bet bet) {
    return not(isAllIn(bet));
  }

  public static boolean isBigBlind(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.BIG_BLIND;
  }

  public static boolean isNotBigBlind(Bet bet) {
    return not(isBigBlind(bet));
  }

  public static boolean isAnyBlind(Bet bet) {
    var betType = bet.getType();
    return betType == BetType.BIG_BLIND || betType == BetType.SMALL_BLIND;
  }

  public static boolean isNotAnyBlind(Bet bet) {
    return not(isAnyBlind(bet));
  }

  public static Optional<Tuple2<GameStateIndex, PlayerPercentage>>
      findFirstWinPercentageIncreaseToAbove(
          Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage,
          String userId,
          BettingRoundType bettingRound,
          int afterTurn,
          double aboveInclusive) {
    return gameStateIndexPlayerPercentage.entrySet().stream()
        .filter(e -> e.getKey().bettingRound() == bettingRound)
        .filter(e -> e.getKey().playerTurnIndex() > afterTurn)
        .flatMap(e -> e.getValue().values().stream().map(p -> Tuple2.of(e.getKey(), p)))
        .filter(t2 -> userId.equals(t2.v2().getUserId()))
        .sorted(Comparator.comparingInt(t2 -> t2.v1().playerTurnIndex()))
        .filter(t2 -> t2.v2().getWinPercentage() >= aboveInclusive)
        .findFirst();
  }

  public static double calcBetEquityPercentage(Bet bet) {
    if (isNotAnyBet(bet)) {
      throw new UnsupportedOperationException(
          "'calcBetEquityPercentage' method does not support bet type: " + bet.getType().text());
    }
    if (bet.getAmount() <= 0) {
      throw new IllegalArgumentException(
          "Bet amount must be greater then 0 on 'calcBetEquityPercentage' method");
    }
    double equity = (bet.getAmount() / (bet.getPot() + (bet.getAmount() * 2))) * 100;
    return DoubleMath.roundToLong(equity, RoundingMode.HALF_UP);
  }

  public static double calcCallEquityPercentage(Bet bet) {
    if (isNotCall(bet)) {
      throw new UnsupportedOperationException(
          "'calcBetEquityPercentage' method does not support bet type: " + bet.getType().text());
    }
    if (bet.getAmount() <= 0) {
      throw new IllegalArgumentException(
          "Bet amount must be greater then 0 on 'calcBetEquityPercentage' method");
    }

    return calcEquityPercentage(bet.getAmount(), bet.getPot());
  }

  public static double calcEquityPercentage(double callAmount, double potAmount) {
    double equity = (callAmount / (potAmount + callAmount)) * 100;
    return DoubleMath.roundToLong(equity, RoundingMode.HALF_UP);
  }

  public static double calcPOF(double equityPercentage, double winPercentage) {
    double pof = ((equityPercentage - winPercentage) / equityPercentage) * 100;
    return DoubleMath.roundToLong(pof, RoundingMode.HALF_UP);
  }
}
