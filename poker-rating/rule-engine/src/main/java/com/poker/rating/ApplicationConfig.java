package com.poker.rating;

import com.poker.rating.client.calc.HttpJdkPokerHoldemCalculatorClient;
import com.poker.rating.client.calc.PokerHoldemCalculatorClient;
import com.poker.rating.client.calc.PokerHoldemCalculatorClientConfig;
import com.poker.rating.rule.extra.BetExtraDecisionSupplier;
import com.poker.rating.rule.extra.CallExtraDecisionSupplier;
import com.poker.rating.rule.extra.CrackedExtraDecisionSupplier;
import com.poker.rating.rule.extra.FoldExtraDecisionSupplier;
import com.poker.rating.rule.extra.ShowdownTypeMapper;
import com.poker.rating.rule.point.BetPointSupplier;
import com.poker.rating.rule.point.CallPointSupplier;
import com.poker.rating.rule.point.CrackedPointSupplier;
import com.poker.rating.rule.point.FoldPointSupplier;
import com.poker.rating.rule.point.RiverPointSupplier;
import com.poker.rating.service.DefaultPokerPercentageCalculator;
import com.poker.rating.service.PokerPercentageCalculator;
import com.poker.rating.service.PokerPercentageCalculatorConfig;
import com.poker.rating.service.PokerRatingCalculator;
import com.poker.rating.service.PreFlopShowdownPercentageCalc;
import com.poker.rating.service.player.PlayerRatingService;
import com.poker.util.task.TaskExecutor;
import com.poker.util.task.TaskExecutorFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
  PokerHoldemCalculatorClientConfig.class,
  PokerPercentageCalculatorConfig.class
})
@EnableRetry
public class ApplicationConfig {

  @Bean
  public ShowdownTypeMapper showdownTypeMapper() {
    return new ShowdownTypeMapper();
  }

  @Bean
  public BetPointSupplier betPointSupplier() {
    return new BetPointSupplier();
  }

  @Bean
  public BetExtraDecisionSupplier betExtraDecisionSupplier(
      BetPointSupplier betPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    return new BetExtraDecisionSupplier(betPointSupplier, showdownTypeMapper);
  }

  @Bean
  public CallPointSupplier callPointSupplier() {
    return new CallPointSupplier();
  }

  @Bean
  public CallExtraDecisionSupplier callExtraDecisionSupplier(
      CallPointSupplier callPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    return new CallExtraDecisionSupplier(callPointSupplier, showdownTypeMapper);
  }

  @Bean
  public FoldPointSupplier foldPointSupplier() {
    return new FoldPointSupplier();
  }

  @Bean
  public FoldExtraDecisionSupplier foldExtraDecisionSupplier(
      FoldPointSupplier foldPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    return new FoldExtraDecisionSupplier(foldPointSupplier, showdownTypeMapper);
  }

  @Bean
  public RiverPointSupplier riverPointSupplier() {
    return new RiverPointSupplier();
  }

  @Bean
  public CrackedPointSupplier crackedPointSupplier() {
    return new CrackedPointSupplier();
  }

  @Bean
  public CrackedExtraDecisionSupplier crackedExtraDecisionSupplier(
      CrackedPointSupplier crackedPointSupplier, ShowdownTypeMapper showdownTypeMapper) {
    return new CrackedExtraDecisionSupplier(crackedPointSupplier, showdownTypeMapper);
  }

  @Bean
  public RatingRuleRegister ratingRuleRegister(
      ShowdownTypeMapper showdownTypeMapper,
      BetPointSupplier betPointSupplier,
      BetExtraDecisionSupplier betExtraDecisionSupplier,
      CallPointSupplier callPointSupplier,
      CallExtraDecisionSupplier callExtraDecisionSupplier,
      FoldPointSupplier foldPointSupplier,
      FoldExtraDecisionSupplier foldExtraDecisionSupplier,
      RiverPointSupplier riverPointSupplier,
      CrackedPointSupplier crackedPointSupplier,
      CrackedExtraDecisionSupplier crackedExtraDecisionSupplier) {
    return new RatingRuleRegister(
        showdownTypeMapper,
        betPointSupplier,
        betExtraDecisionSupplier,
        callPointSupplier,
        callExtraDecisionSupplier,
        foldPointSupplier,
        foldExtraDecisionSupplier,
        riverPointSupplier,
        crackedPointSupplier,
        crackedExtraDecisionSupplier);
  }

  @Bean
  public PokerHoldemCalculatorClient pokerHoldemCalculatorClient(
      PokerHoldemCalculatorClientConfig config) {
    return new HttpJdkPokerHoldemCalculatorClient(config);
  }

  @Bean
  public PreFlopShowdownPercentageCalc preFlopShowdownPercentageCalc() {
    return new PreFlopShowdownPercentageCalc();
  }

  @Bean
  public TaskExecutorFactory taskExecutorFactory() {
    return new TaskExecutorFactory();
  }

  @Bean
  @Qualifier("showdownTaskExecutor")
  public TaskExecutor showdownTaskExecutor(
      TaskExecutorFactory taskExecutorFactory,
      PokerPercentageCalculatorConfig percentageCalcConfig) {
    return taskExecutorFactory.create(percentageCalcConfig.showdownCalcParallelism());
  }

  @Bean
  public PokerPercentageCalculator pokerPercentageCalculator(
      PokerHoldemCalculatorClient pokerCalculatorClient,
      PreFlopShowdownPercentageCalc preFlopShowdownPercentageCalc,
      @Qualifier("showdownTaskExecutor") TaskExecutor showdownTaskExecutor) {
    return new DefaultPokerPercentageCalculator(
        pokerCalculatorClient, preFlopShowdownPercentageCalc, showdownTaskExecutor);
  }

  @Bean
  public RatingRuleEngine ratingRuleEngine(
      RatingRuleRegister ratingRuleRegister, PokerPercentageCalculator pokerPercentageCalculator) {
    return new RatingRuleEngine(ratingRuleRegister, pokerPercentageCalculator);
  }

  @Bean
  public PokerRatingCalculator pokerRatingCalculator(
      RatingRuleEngine ratingRuleEngine, PlayerRatingService playerRatingService) {
    return new PokerRatingCalculator(ratingRuleEngine, playerRatingService);
  }
}
