package com.poker.rating.client.calc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.poker.model.game.Card;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@JsonInclude(Include.NON_NULL)
@Getter
public class WinPercentageRequest {

  @Nonnull
  @JsonProperty("players")
  private final List<String> players;

  @Nullable
  @JsonProperty("board")
  private final List<Card> board;

  @Nullable
  @JsonProperty("excludes")
  private final Set<Card> excludes;
}
