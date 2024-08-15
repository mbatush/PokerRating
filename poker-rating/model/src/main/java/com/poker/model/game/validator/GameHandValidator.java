package com.poker.model.game.validator;

import com.andrebreves.tuple.Tuple2;
import com.andrebreves.tuple.Tuple3;
import com.poker.model.game.Bet;
import com.poker.model.game.Card;
import com.poker.model.game.GameHand;
import com.poker.model.game.InvalidCard;
import com.poker.model.game.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GameHandValidator implements ConstraintValidator<GameHandConstraint, GameHand> {

  private static boolean not(boolean b) {
    return !b;
  }

  private static void addPlayerCardViolation(
      Tuple3<Integer, Integer, Card> playerCardIndex,
      String violationMessage,
      ConstraintValidatorContext context) {
    context
        .buildConstraintViolationWithTemplate(violationMessage + ": " + playerCardIndex.v3())
        .addPropertyNode("players")
        .addContainerElementNode("players", List.class, 0)
        .inIterable()
        .atKey(playerCardIndex.v1())
        .addPropertyNode("cards")
        .addContainerElementNode("cards", List.class, 0)
        .inIterable()
        .atKey(playerCardIndex.v2())
        .addConstraintViolation()
        .disableDefaultConstraintViolation();
  }

  private static void addRoundBetUserIdMissingViolation(
      String roundBetFieldName, Entry<Integer, Bet> indexBet, ConstraintValidatorContext context) {
    context
        .buildConstraintViolationWithTemplate(
            "User id does not exists in given players: " + indexBet.getValue().getUserId())
        .addPropertyNode("roundBets")
        .addPropertyNode(roundBetFieldName)
        .addContainerElementNode(roundBetFieldName, List.class, 0)
        .inIterable()
        .atKey(indexBet.getKey())
        .addPropertyNode("userId")
        .addConstraintViolation()
        .disableDefaultConstraintViolation();
  }

  private static List<Entry<Integer, Bet>> betUserIdDoesNotExist(
      List<Bet> bets, Set<String> userIds) {
    return IntStream.range(0, bets.size())
        .mapToObj(i -> Map.entry(i, bets.get(i)))
        .filter(
            e -> Optional.of(e.getValue()).map(Bet::getUserId).filter(userIds::contains).isEmpty())
        .toList();
  }

  @Override
  public boolean isValid(GameHand gameHand, ConstraintValidatorContext context) {
    if (not(isValidBoardCards(gameHand, context))) {
      return false;
    }
    if (not(isValidPlayers(gameHand, context))) {
      return false;
    }
    if (not(isValidRoundBets(gameHand, context))) {
      return false;
    }
    return true;
  }

  private boolean isValidRoundBets(GameHand gameHand, ConstraintValidatorContext context) {
    Set<String> userIds =
        gameHand.getPlayers().stream()
            .map(Player::getUserId)
            .collect(Collectors.toUnmodifiableSet());
    int boardCardSize = gameHand.getBoardCards().size();
    if (not(isValidPreFlopBets(gameHand.getRoundBets().getPreFlop(), userIds, context))) {
      return false;
    }
    if (not(isValidFlopBets(gameHand.getRoundBets().getFlop(), userIds, boardCardSize, context))) {
      return false;
    }
    if (not(isValidTurnBets(gameHand.getRoundBets().getTurn(), userIds, boardCardSize, context))) {
      return false;
    }
    if (not(
        isValidRiverBets(gameHand.getRoundBets().getRiver(), userIds, boardCardSize, context))) {
      return false;
    }
    return true;
  }

  private boolean isValidPreFlopBets(
      List<Bet> bets, Set<String> userIds, ConstraintValidatorContext context) {
    if (bets.size() < 2) {
      return false;
    }

    List<Entry<Integer, Bet>> betUserIdDoesNotExist = betUserIdDoesNotExist(bets, userIds);
    if (not(betUserIdDoesNotExist.isEmpty())) {
      betUserIdDoesNotExist.forEach(e -> addRoundBetUserIdMissingViolation("preFlop", e, context));
      return false;
    }

    return true;
  }

  private boolean isValidFlopBets(
      List<Bet> bets, Set<String> userIds, int boardCardSize, ConstraintValidatorContext context) {
    if (boardCardSize < 3 && not(bets.isEmpty())) {
      context
          .buildConstraintViolationWithTemplate(
              "The board cards size must be at least 3 card size on given flop bets")
          .addPropertyNode("boardCards")
          .addConstraintViolation()
          .disableDefaultConstraintViolation();
      return false;
    }

    List<Entry<Integer, Bet>> betUserIdDoesNotExist = betUserIdDoesNotExist(bets, userIds);
    if (not(betUserIdDoesNotExist.isEmpty())) {
      betUserIdDoesNotExist.forEach(e -> addRoundBetUserIdMissingViolation("flop", e, context));
      return false;
    }

    return true;
  }

  private boolean isValidTurnBets(
      List<Bet> bets, Set<String> userIds, int boardCardSize, ConstraintValidatorContext context) {
    if (boardCardSize < 4 && not(bets.isEmpty())) {
      context
          .buildConstraintViolationWithTemplate(
              "The board cards size must be at least 4 card size on given turn bets")
          .addPropertyNode("boardCards")
          .addConstraintViolation()
          .disableDefaultConstraintViolation();
      return false;
    }

    List<Entry<Integer, Bet>> betUserIdDoesNotExist = betUserIdDoesNotExist(bets, userIds);
    if (not(betUserIdDoesNotExist.isEmpty())) {
      betUserIdDoesNotExist.forEach(e -> addRoundBetUserIdMissingViolation("turn", e, context));
      return false;
    }

    return true;
  }

  private boolean isValidRiverBets(
      List<Bet> bets, Set<String> userIds, int boardCardSize, ConstraintValidatorContext context) {
    if (boardCardSize < 5 && not(bets.isEmpty())) {
      context
          .buildConstraintViolationWithTemplate(
              "The board cards size must be equals to 5 on given river bets")
          .addPropertyNode("boardCards")
          .addConstraintViolation()
          .disableDefaultConstraintViolation();
      return false;
    }

    List<Entry<Integer, Bet>> betUserIdDoesNotExist = betUserIdDoesNotExist(bets, userIds);
    if (not(betUserIdDoesNotExist.isEmpty())) {
      betUserIdDoesNotExist.forEach(e -> addRoundBetUserIdMissingViolation("river", e, context));
      return false;
    }

    return true;
  }

  private boolean isValidPlayers(GameHand gameHand, ConstraintValidatorContext context) {
    List<Player> players = gameHand.getPlayers();
    if (players.isEmpty()) {
      return false;
    }

    var playerCards =
        IntStream.range(0, players.size())
            .mapToObj(
                i ->
                    IntStream.range(0, players.get(i).getCards().size())
                        .mapToObj(j -> Tuple3.of(i, j, players.get(i).getCards().get(j))))
            .flatMap(Function.identity())
            .toList();

    var invalidPlayerCards =
        playerCards.stream().filter(t3 -> t3.v3() instanceof InvalidCard).toList();
    invalidPlayerCards.forEach(t3 -> addPlayerCardViolation(t3, "Invalid card", context));

    if (not(invalidPlayerCards.isEmpty())) {
      return false;
    }

    Set<Card> boardCards =
        gameHand.getBoardCards().stream().collect(Collectors.toUnmodifiableSet());
    Map<Card, Tuple2<Integer, Integer>> cardToPlayerCardIndex = new HashMap<>();
    boolean playerCardAlreadyUsed = false;
    for (Tuple3<Integer, Integer, Card> playerCardIndex : playerCards) {
      if (boardCards.contains(playerCardIndex.v3())) {
        playerCardAlreadyUsed = true;
        addPlayerCardViolation(playerCardIndex, "Card already used by board", context);
      }
      Tuple2<Integer, Integer> addedPlayerCardIndex =
          cardToPlayerCardIndex.putIfAbsent(
              playerCardIndex.v3(), Tuple2.of(playerCardIndex.v1(), playerCardIndex.v2()));
      if (addedPlayerCardIndex != null) {
        playerCardAlreadyUsed = true;
        // card already associated to another player
        addPlayerCardViolation(
            playerCardIndex,
            String.format(
                "Card already used by players[%d].cards[%d]",
                addedPlayerCardIndex.v1(), addedPlayerCardIndex.v2()),
            context);
      }
    }
    if (playerCardAlreadyUsed) {
      return false;
    }

    boolean playerIdAlreadyUsed = false;
    Map<String, Integer> userIdToIndex = new HashMap<>();
    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      var userId = player.getUserId();
      if (userId != null) {
        Integer addedUserIdIndex = userIdToIndex.putIfAbsent(userId, i);
        if (addedUserIdIndex != null) {
          playerIdAlreadyUsed = true;
          // user id already associated to another player
          context
              .buildConstraintViolationWithTemplate(
                  String.format(
                      "User id '%s' already used at players[%d]", userId, addedUserIdIndex))
              .addPropertyNode("players")
              .addContainerElementNode("players", List.class, 0)
              .inIterable()
              .atKey(i)
              .addConstraintViolation()
              .disableDefaultConstraintViolation();
        }
      }
    }
    if (playerIdAlreadyUsed) {
      return false;
    }

    boolean atLeastOneWinnerExists = players.stream().anyMatch(Player::isWinner);
    if (not(atLeastOneWinnerExists)) {
      context
          .buildConstraintViolationWithTemplate(
              "At least one player from players must be a winner. Please provide winner: true for winner player(s).")
          .addPropertyNode("players")
          .addConstraintViolation()
          .disableDefaultConstraintViolation();
      return false;
    }

    return true;
  }

  private boolean isValidBoardCards(GameHand gameHand, ConstraintValidatorContext context) {
    List<Card> boardCards = gameHand.getBoardCards();
    if (boardCards.isEmpty()) {
      return true;
    }
    var invalidCards =
        IntStream.range(0, boardCards.size())
            .mapToObj(i -> Map.entry(i, boardCards.get(i)))
            .filter(e -> e.getValue() instanceof InvalidCard)
            .toList();
    invalidCards.forEach(
        e ->
            context
                .buildConstraintViolationWithTemplate("Invalid card: " + e.getValue())
                .addPropertyNode("boardCards")
                .addBeanNode()
                .inIterable()
                .atIndex(e.getKey())
                .addConstraintViolation()
                .disableDefaultConstraintViolation());

    return invalidCards.isEmpty();
  }
}
