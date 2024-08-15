package com.poker.rating.client.calc;

import com.poker.rating.client.calc.model.ShowdownPercentageRequest;
import com.poker.rating.client.calc.model.ShowdownPercentageResponse;
import com.poker.rating.client.calc.model.WinPercentageRequest;
import com.poker.rating.client.calc.model.WinPercentageResponse;

public interface PokerHoldemCalculatorClient {
  String WIN_PERCENTAGE_RESOURCE = "/holdem/calc/win/percentage";
  String SHOWDOWN_PERCENTAGE_RESOURCE = "/holdem/calc/showdown/percentage";

  WinPercentageResponse winPercentage(WinPercentageRequest winPercentageRequest);

  ShowdownPercentageResponse showdownPercentage(
      ShowdownPercentageRequest showdownPercentageRequest);
}
