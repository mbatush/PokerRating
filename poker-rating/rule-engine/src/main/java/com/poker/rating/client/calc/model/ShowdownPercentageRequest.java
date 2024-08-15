package com.poker.rating.client.calc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.poker.model.game.Card;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("SameNameButDifferent")
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator(mode = Mode.PROPERTIES)))
@JsonInclude(Include.NON_NULL)
@Getter
public class ShowdownPercentageRequest {

  @Nonnull
  @JsonProperty("board")
  private final List<Card> board;

  @Nonnull
  @JsonProperty("player")
  private final List<Card> player;
}
