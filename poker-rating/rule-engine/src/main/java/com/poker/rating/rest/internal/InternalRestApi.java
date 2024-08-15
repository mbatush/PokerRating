package com.poker.rating.rest.internal;

import static com.poker.util.FluentUtils.not;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.poker.model.game.Bet;
import com.poker.model.game.BetType;
import com.poker.model.game.Card;
import com.poker.model.game.GameHand;
import com.poker.model.game.Player;
import com.poker.model.game.RoundBets;
import com.poker.model.rating.PlayerRating;
import com.poker.model.rating.RatingCalcResult;
import com.poker.model.rating.RuleDecision;
import com.poker.rating.rest.internal.exception.PlayerRatingDocNotFoundProblem;
import com.poker.rating.rest.internal.exception.PlayerStatisticsDocNotFoundProblem;
import com.poker.rating.rest.internal.model.RatingGameCalcResponse;
import com.poker.rating.service.PokerRatingCalculator;
import com.poker.rating.service.player.PlayerRatingService;
import com.poker.rating.service.player.model.PlayerRatingDoc;
import com.poker.rating.service.player.model.PlayerStatisticDoc;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("SameNameButDifferent")
@RestController
@RequiredArgsConstructor
@Tag(name = "internal", description = "Internal APIs")
public class InternalRestApi {

  @Nonnull private final PokerRatingCalculator pokerRatingCalculator;
  @Nonnull private final PlayerRatingService playerRatingService;
  @Nonnull private final Validator validator;

  @GetMapping(path = "/internal/rating/app/{applicationId}/user/{userId}")
  public PlayerRatingDoc getPlayerRatingDoc(
      @PathVariable String applicationId, @PathVariable String userId) {
    return playerRatingService
        .getPlayerRatingDoc(applicationId, userId)
        .orElseThrow(() -> new PlayerRatingDocNotFoundProblem(applicationId, userId));
  }

  @GetMapping(path = "/internal/rating/app/{applicationId}/user/{userId}/statistics")
  public PlayerStatisticDoc getPlayerStatisticDoc(
      @PathVariable String applicationId, @PathVariable String userId) {
    return playerRatingService
        .getPlayerStatisticDoc(applicationId, userId)
        .orElseThrow(() -> new PlayerStatisticsDocNotFoundProblem(applicationId, userId));
  }

  @PostMapping(path = "/internal/rating/app/{applicationId}/user/{userId}/reset/rating/{rating}")
  public PlayerRatingDoc resetPlayerRating(
      @PathVariable String applicationId, @PathVariable String userId, @PathVariable Long rating) {
    return playerRatingService.resetPlayerRating(
        applicationId, userId, Objects.requireNonNullElse(rating, 10000L));
  }

  @PostMapping(path = "/internal/rating/game/calc")
  public RatingGameCalcResponse ratingGameCalculate(@Valid @RequestBody GameHand gameHand) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    RatingCalcResult ratingCalcResult = pokerRatingCalculator.calculate(gameHand);
    long operationTimeMillis = stopwatch.stop().elapsed().toMillis();
    List<PlayerRating> newRatings =
        ratingCalcResult.newRatings().stream()
            .sorted(Comparator.comparing(PlayerRating::getUserId))
            .toList();
    List<PlayerRating> prevRatings =
        ratingCalcResult.prevRatings().stream()
            .sorted(Comparator.comparing(PlayerRating::getUserId))
            .toList();
    List<RuleDecision> decisions =
        ratingCalcResult.ruleExecResult().decisions().stream()
            .sorted(Comparator.comparing(RuleDecision::gameStateIndex))
            .toList();
    return new RatingGameCalcResponse(
        newRatings,
        prevRatings,
        ratingCalcResult.sumRuleRatingChangeByUserId(),
        decisions,
        operationTimeMillis);
  }

  @PostMapping(path = "/internal/generate/game/hand")
  public GameHand generateGameHand(@RequestBody String payload) {
    List<String> lines = Splitter.onPattern("[\\r\\n]+").splitToList(payload);
    if (lines.size() < 4) {
      throw new IllegalStateException(
          "Please provide at least 4 lines: Players, Winners, Board, PreFlop");
    }
    String winnersLine = StringUtils.removeStart(StringUtils.trim(lines.get(1)), "Winners:").trim();
    List<String> winnerCards =
        Splitter.on(" ").omitEmptyStrings().trimResults().splitToList(winnersLine);
    Set<String> winners =
        winnerCards.stream()
            .map(
                cards -> {
                  if (cards.length() < 4) {
                    throw new IllegalStateException("Winner card should be >= 4: " + cards);
                  }
                  return cards;
                })
            .collect(Collectors.toSet());
    if (winners.isEmpty()) {
      throw new IllegalStateException("Winners is empty");
    }
    String playersLine = StringUtils.removeStart(StringUtils.trim(lines.get(0)), "Players:").trim();
    List<String> playerCards =
        Splitter.on(" ").omitEmptyStrings().trimResults().splitToList(playersLine);
    List<Player> players =
        playerCards.stream()
            .map(
                cards -> {
                  if (cards.length() < 4) {
                    throw new IllegalStateException("Player card should be >= 4: " + cards);
                  }
                  char c1V = cards.charAt(0);
                  char c1S = cards.charAt(1);
                  char c2V = cards.charAt(2);
                  char c2S = cards.charAt(3);
                  final String userId;
                  if (cards.length() > 4) {
                    userId = StringUtils.strip(cards.substring(4), "#");
                  } else {
                    userId = cards;
                  }
                  return new Player(
                      userId,
                      winners.contains(cards),
                      List.of(Card.of("" + c1V + c1S), Card.of("" + c2V + c2S)));
                })
            .toList();
    Map<String, String> cardsToUserId =
        players.stream()
            .map(
                p ->
                    Map.entry(
                        p.getCards().stream().map(Card::toString).collect(Collectors.joining()),
                        p.getUserId()))
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));

    String boardLine = StringUtils.removeStart(StringUtils.trim(lines.get(2)), "Board:").trim();
    List<Card> boardCards =
        Splitter.on(" ").omitEmptyStrings().trimResults().splitToList(boardLine).stream()
            .map(
                card -> {
                  if (card.length() != 2) {
                    throw new IllegalStateException("Board card should be 2 length: " + card);
                  }

                  return Card.of(card);
                })
            .toList();

    String preFlopLine = StringUtils.removeStart(StringUtils.trim(lines.get(3)), "PreFlop:").trim();
    List<Bet> preFlop = toBets(preFlopLine, cardsToUserId);
    List<Bet> flop = List.of();
    if (lines.size() > 4) {
      flop =
          toBets(
              StringUtils.removeStart(StringUtils.trim(lines.get(4)), "Flop:").trim(),
              cardsToUserId);
    }
    List<Bet> turn = List.of();
    if (lines.size() > 5) {
      turn =
          toBets(
              StringUtils.removeStart(StringUtils.trim(lines.get(5)), "Turn:").trim(),
              cardsToUserId);
    }
    List<Bet> river = List.of();
    if (lines.size() > 6) {
      river =
          toBets(
              StringUtils.removeStart(StringUtils.trim(lines.get(6)), "River:").trim(),
              cardsToUserId);
    }
    RoundBets roundBets = new RoundBets(preFlop, flop, turn, river);
    return new GameHand("app1", UUID.randomUUID().toString(), boardCards, players, roundBets);
  }

  private List<Bet> toBets(String line, Map<String, String> cardsToUserId) {
    List<String> betData = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(line);
    return betData.stream().map(l -> toBet(l, cardsToUserId)).toList();
  }

  private Bet toBet(String betData, Map<String, String> cardsToUserId) {
    List<String> betDataList =
        Splitter.on(":").omitEmptyStrings().trimResults().splitToList(betData);
    if (betDataList.size() != 4) {
      throw new IllegalStateException(
          "Bet data is wrong, please provide: player-cards:bet-type:amount:pot");
    }
    var cards = betDataList.get(0);
    return new Bet(
        Objects.requireNonNull(
            cardsToUserId.get(cards), "Missing mapping to user id for cards: " + cards),
        Double.valueOf(betDataList.get(2)),
        BetType.of(betDataList.get(1)),
        Double.parseDouble(betDataList.get(3)));
  }

  @PostMapping(path = "/internal/rating/game/calc/text")
  public RatingGameCalcResponse ratingGameHandCalc(@RequestBody String payload) {
    GameHand gamehand = generateGameHand(payload);
    Set<ConstraintViolation<GameHand>> constraintViolations = validator.validate(gamehand);
    if (not(constraintViolations.isEmpty())) {
      throw new ConstraintViolationException("Invalid game hand", constraintViolations);
    }

    return ratingGameCalculate(gamehand);
  }
}
