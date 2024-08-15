package com.poker.model.rating;

import com.andrebreves.tuple.Tuple2;
import com.poker.model.game.Bet;
import com.poker.model.game.BetPosition;
import com.poker.model.game.BettingRoundType;
import com.poker.model.game.Player;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@SuppressWarnings("SameNameButDifferent")
@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor
public final class GameState {

  @Nonnull private final String userId;
  @Nonnull private final List<Player> players;
  @Nonnull private final GameStateIndex gameStateIndex;
  @Nonnull private final Bet currentBet;
  @Nonnull private final BetPosition betPosition;

  @Nonnull
  private final List<Tuple2<BettingRoundType, List<Bet>>> roundBets; // do we need all round bets?

  @Nonnull
  private final Map<GameStateIndex, Map<String, PlayerPercentage>> gameStateIndexPlayerPercentage;
}
