package com.poker.rating;

import static java.util.Objects.requireNonNull;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.Bet;
import com.poker.model.game.BetPosition;
import com.poker.model.game.BetType;
import com.poker.model.game.BettingRoundType;
import com.poker.model.game.Card;
import com.poker.model.game.GameHand;
import com.poker.model.game.Player;
import com.poker.model.game.RoundBets;
import com.poker.model.rating.GameState;
import com.poker.model.rating.GameStateIndex;
import com.poker.model.rating.PlayerPercentage;
import com.poker.model.rating.RatingRuleExecResult;
import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rule.RatingRule;
import com.poker.rating.service.PercentageCalculatorContext;
import com.poker.rating.service.PokerPercentageCalculator;
import com.poker.rating.service.PokerPercentageResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.util.CollectionUtils;

public class RatingRuleEngine {

  private final RatingRuleRegister ratingRuleRegister;
  private final PokerPercentageCalculator pokerPercentageCalculator;

  public RatingRuleEngine(
      RatingRuleRegister ratingRuleRegister, PokerPercentageCalculator pokerPercentageCalculator) {
    this.ratingRuleRegister = requireNonNull(ratingRuleRegister);
    this.pokerPercentageCalculator = requireNonNull(pokerPercentageCalculator);
  }

  static List<Tuple2<BettingRoundType, List<Bet>>> roundBetsToTuples(RoundBets roundBets) {
    return List.of(
        Tuple2.of(BettingRoundType.PRE_FLOP, roundBets.getPreFlop()),
        Tuple2.of(BettingRoundType.FLOP, roundBets.getFlop()),
        Tuple2.of(BettingRoundType.TURN, roundBets.getTurn()),
        Tuple2.of(BettingRoundType.RIVER, roundBets.getRiver()));
  }

  private static Map<String, PlayerPercentage> getRequiredPlayerPercentage(
      Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage,
      BettingRoundType bettingRound,
      int turnIndex) {
    return requireNonNull(
        gameStateIndexPlayerPercentage.get(new GameStateIndex(bettingRound, turnIndex)),
        "Missing player percentages for " + bettingRound + " on turn " + turnIndex);
  }

  private static Map<String, Double> mapUserIdToShowdownPercentage(PokerPercentageResult result) {
    return result.getPlayerPercentages().entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                Entry::getKey,
                playerPercentage -> playerPercentage.getValue().getShowdownPercentage()));
  }

  public RatingRuleExecResult executeRules(GameHand gameHand) {
    List<RatingRule> rules = ratingRuleRegister.getRules();
    List<RuleDecision> rulesResults = new ArrayList<>(100);

    // ordered map to be used here
    List<Tuple2<BettingRoundType, List<Bet>>> roundsBets =
        roundBetsToTuples(gameHand.getRoundBets());

    Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        gameStateIndexPlayerPercentage(roundsBets, gameHand);

    for (Tuple2<BettingRoundType, List<Bet>> roundBet : roundsBets) {
      BettingRoundType bettingRound = roundBet.v1();
      List<Bet> bets = roundBet.v2();

      var betsSize = bets.size();
      for (int i = 0; i < betsSize; i++) {
        if (bettingRound == BettingRoundType.PRE_FLOP && i < 2) {
          continue; // skip small and big blind
        }

        int turnIndex = i;
        var bet = bets.get(i);
        String userId = bet.getUserId();
        final BetPosition betPosition;
        if (turnIndex == 0) {
          betPosition = BetPosition.FIRST_TO_ACT;
        } else if (turnIndex == betsSize - 1) {
          betPosition = BetPosition.NEITHER;
        } else {
          betPosition = BetPosition.NEITHER;
        }
        rulesResults.addAll(
            rules.parallelStream()
                .flatMap(
                    rule ->
                        rule
                            .execute(
                                new RuleContext(
                                    new GameState(
                                        userId,
                                        gameHand.getPlayers(),
                                        new GameStateIndex(bettingRound, turnIndex),
                                        bet,
                                        betPosition,
                                        roundsBets,
                                        gameStateIndexPlayerPercentage)))
                            .stream())
                .toList());
      }
    }

    return new RatingRuleExecResult(
        Collections.unmodifiableList(rulesResults), gameStateIndexPlayerPercentage);
  }

  @SuppressWarnings("java:S3776")
  private Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage(
      List<Tuple2<BettingRoundType, List<Bet>>> roundsBets, GameHand gameHand) {
    final Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage =
        CollectionUtils.newHashMap(100);

    final List<Card> gameBoardCards = gameHand.getBoardCards();
    final List<Player> playingPlayers =
        new ArrayList<>(gameHand.getPlayers()); // mutable playing players
    final Set<Card> deadCards = new HashSet<>(); // mutable dead cards
    Map<String, Double> latestUserIdToShowdownPercentage =
        null; // latest calculated on board cards adding
    for (Tuple2<BettingRoundType, List<Bet>> roundBet : roundsBets) {
      BettingRoundType bettingRound = roundBet.v1();
      List<Card> boardCards = boardCardsForRound(gameBoardCards, bettingRound);
      List<Bet> bets = roundBet.v2();

      for (int i = 0; i < bets.size(); i++) {
        Bet bet = bets.get(i);
        if (i == 0 || bet.getType() == BetType.FOLD) {
          if (i == 0) {
            latestUserIdToShowdownPercentage = null; // clean on new round
          }
          PercentageCalculatorContext calcContext =
              new PercentageCalculatorContext(
                  playingPlayers, boardCards, deadCards, latestUserIdToShowdownPercentage);
          PokerPercentageResult result = pokerPercentageCalculator.calculate(calcContext);
          gameStateIndexPlayerPercentage.put(
              new GameStateIndex(bettingRound, i), result.getPlayerPercentages());

          if (bet.getType() == BetType.FOLD) {
            String foldedUserId = bet.getUserId();
            Player player = findPlayer(playingPlayers, foldedUserId);
            playingPlayers.remove(player);
            deadCards.addAll(player.getCards());
          }

          if (i == 0) {
            latestUserIdToShowdownPercentage =
                mapUserIdToShowdownPercentage(result); // cache on new round
          }

          if (i == bets.size() - 1) {
            // last bet is fold then calculate percentage far all remaining players
            calcContext =
                new PercentageCalculatorContext(
                    playingPlayers, boardCards, deadCards, latestUserIdToShowdownPercentage);
            result = pokerPercentageCalculator.calculate(calcContext);
            gameStateIndexPlayerPercentage.put(
                new GameStateIndex(bettingRound, i + 1), result.getPlayerPercentages());
          }
        } else {
          gameStateIndexPlayerPercentage.put(
              new GameStateIndex(bettingRound, i),
              getRequiredPlayerPercentage(gameStateIndexPlayerPercentage, bettingRound, i - 1));
        }
      }
    }

    return Collections.unmodifiableMap(gameStateIndexPlayerPercentage);
  }

  private Player findPlayer(List<Player> players, String userId) {
    return players.stream()
        .filter(p -> p.getUserId().equals(userId))
        .reduce(
            (a, b) -> {
              throw new IllegalStateException(
                  "Multiple players found by user ID: " + a.getUserId());
            })
        .orElseThrow(() -> new IllegalStateException("Player not found by user ID: " + userId));
  }

  private @Nullable List<Card> boardCardsForRound(
      List<Card> gameBoardCards, BettingRoundType bettingRound) {
    return switch (bettingRound) {
      case PRE_FLOP -> null;
      case FLOP -> com.poker.util.CollectionUtils.safeSubList(gameBoardCards, 0, 3);
      case TURN -> com.poker.util.CollectionUtils.safeSubList(gameBoardCards, 0, 4);
      case RIVER -> com.poker.util.CollectionUtils.safeSubList(gameBoardCards, 0, 5);
    };
  }
}
