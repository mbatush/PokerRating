package com.poker.rating.rule.interceptor;

import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import java.util.List;

public interface AfterRatingRuleInterceptor {

  List<RuleDecision> afterRuleExecute(List<RuleDecision> ruleDecisions, RuleContext ruleContext);
}
