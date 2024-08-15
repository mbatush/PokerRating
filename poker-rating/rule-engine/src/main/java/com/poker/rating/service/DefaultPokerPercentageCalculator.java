package com.poker.rating.service;

import com.poker.model.game.Card;
import com.poker.model.game.Player;
import com.poker.model.rating.PlayerPercentage;
import com.poker.rating.client.calc.PokerHoldemCalculatorClient;
import com.poker.rating.client.calc.model.HandRank;
import com.poker.rating.client.calc.model.ShowdownPercentageRequest;
import com.poker.rating.client.calc.model.ShowdownPercentageResponse;
import com.poker.rating.client.calc.model.WinPercentagePlayer;
import com.poker.rating.client.calc.model.WinPercentageRequest;
import com.poker.rating.client.calc.model.WinPercentageResponse;
import com.poker.util.task.TaskExecutor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor
public class DefaultPokerPercentageCalculator implements PokerPercentageCalculator {

  private static final String CARDS_DELIMITER = "|";
  @Nonnull private final PokerHoldemCalculatorClient pokerCalculatorClient;
  @Nonnull private final PreFlopShowdownPercentageCalc preFlopShowdownPercentageCalc;
  @Nonnull private final TaskExecutor showdownTaskExecutor;

  private static @Nonnull String playerCardsToString(@Nonnull Player player) {
    return player.getCards().stream()
        .map(Card::toString)
        .collect(Collectors.joining(CARDS_DELIMITER));
  }

  private static @Nonnull Map<String, String> playerCardsToUserId(@Nonnull List<Player> players) {
    return players.stream()
        .collect(
            Collectors.toUnmodifiableMap(
                DefaultPokerPercentageCalculator::playerCardsToString, Player::getUserId));
  }

  private static String getUserId(Map<String, String> playerCardsToUserId, String cards) {
    return Objects.requireNonNull(
        playerCardsToUserId.get(cards),
        () -> "Unexpected missing player user ID mapping for cards: " + cards);
  }

  @Override
  public PokerPercentageResult calculate(PercentageCalculatorContext calculatorContext) {
    var playingPlayers = calculatorContext.getPlayingPlayers();
    Map<String, WinPercentagePlayer> userIdToWinPercentage =
        userIdToWinPercentage(calculatorContext);
    Map<String, Double> userIdToShowdownPercentage = userIdToShowdownPercentage(calculatorContext);

    Map<String, PlayerPercentage> playerPercentages =
        playingPlayers.stream()
            .map(
                p -> {
                  var winPercentagePlayer =
                      Objects.requireNonNull(
                          userIdToWinPercentage.get(p.getUserId()),
                          () ->
                              "Win percentage player missing mapping for user id:" + p.getUserId());
                  var showdownPercentage =
                      Objects.requireNonNull(
                          userIdToShowdownPercentage.get(p.getUserId()),
                          () -> "Showdown percentage missing mapping for user id:" + p.getUserId());
                  return new PlayerPercentage(
                      p.getUserId(),
                      winPercentagePlayer.getWinPercentage(),
                      showdownPercentage,
                      Optional.ofNullable(winPercentagePlayer.getHandRank())
                          .map(HandRank::getRank)
                          .orElse(null),
                      Optional.ofNullable(winPercentagePlayer.getHandRank())
                          .map(HandRank::getName)
                          .orElse(null));
                })
            .collect(
                Collectors.toUnmodifiableMap(PlayerPercentage::getUserId, Function.identity()));
    return new PokerPercentageResult(playerPercentages);
  }

  private Map<String, Double> userIdToShowdownPercentage(
      PercentageCalculatorContext calculatorContext) {
    var contextUserIdToShowdownPercentage = calculatorContext.getUserIdToShowdownPercentage();
    if (contextUserIdToShowdownPercentage != null && !contextUserIdToShowdownPercentage.isEmpty()) {
      return contextUserIdToShowdownPercentage;
    }

    List<Card> boardCards = calculatorContext.getBoardCards();
    final Function<List<Card>, ShowdownPercentageResponse> showdownCalcFunc;
    if (CollectionUtils.isEmpty(boardCards)) {
      showdownCalcFunc = preFlopShowdownPercentageCalc::calculate;
    } else {
      showdownCalcFunc =
          cards ->
              pokerCalculatorClient.showdownPercentage(
                  new ShowdownPercentageRequest(boardCards, cards));
    }

    var playingPlayers = calculatorContext.getPlayingPlayers();
    List<Supplier<ShowdownPercentageResponse>> showdownCalcTasks =
        playingPlayers.stream()
            .map(player -> showdownSupplyCalcFunc(showdownCalcFunc, player))
            .toList();

    List<ShowdownPercentageResponse> showdownPercentages =
        showdownTaskExecutor.execute(showdownCalcTasks);

    Map<String, Double> userIdToShowdownPercentage =
        CollectionUtils.newHashMap(playingPlayers.size());

    for (int i = 0; i < playingPlayers.size(); i++) {
      Player player = playingPlayers.get(i);
      userIdToShowdownPercentage.put(
          player.getUserId(),
          Objects.requireNonNull(
                  showdownPercentages.get(i),
                  String.format(
                      "Showdown percentage is missing on calculated values for index '%d' and player ID '%s'",
                      i, player.getUserId()))
              .getShowdownPercentage());
    }

    return userIdToShowdownPercentage;
  }

  private Supplier<ShowdownPercentageResponse> showdownSupplyCalcFunc(
      Function<List<Card>, ShowdownPercentageResponse> showdownCalcFunc, Player player) {
    return () -> showdownCalcFunc.apply(player.getCards());
  }

  private Map<String, WinPercentagePlayer> userIdToWinPercentage(
      PercentageCalculatorContext calculatorContext) {
    var playingPlayers = calculatorContext.getPlayingPlayers();
    if (playingPlayers.size() == 1) {
      // only one player remained == 100 % of wining
      var firstPlayer = playingPlayers.get(0);
      return Map.of(
          firstPlayer.getUserId(),
          new WinPercentagePlayer(playerCardsToString(firstPlayer), 100.0, new HandRank("N/A", 0)));
    }

    Map<String, String> playerCardsToUserId = playerCardsToUserId(playingPlayers);
    var winPercentageRequest = winPercentageRequest(calculatorContext);
    WinPercentageResponse winPercentageRsp =
        pokerCalculatorClient.winPercentage(winPercentageRequest);

    return winPercentageRsp.getPlayers().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                winPercentagePlayer ->
                    getUserId(playerCardsToUserId, winPercentagePlayer.getCards()),
                Function.identity()));
  }

  private WinPercentageRequest winPercentageRequest(PercentageCalculatorContext calculatorContext) {
    List<String> playerCards =
        calculatorContext.getPlayingPlayers().stream()
            .map(DefaultPokerPercentageCalculator::playerCardsToString)
            .toList();
    List<Card> boardCards = calculatorContext.getBoardCards();
    Set<Card> excludeCards = calculatorContext.getDeadCards();
    return new WinPercentageRequest(playerCards, boardCards, excludeCards);
  }
}
