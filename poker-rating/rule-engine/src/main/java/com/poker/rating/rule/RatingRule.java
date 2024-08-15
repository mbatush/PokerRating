package com.poker.rating.rule;

import com.poker.model.rating.RuleContext;
import com.poker.model.rating.RuleDecision;
import java.util.List;

public interface RatingRule {

  List<RuleDecision> execute(RuleContext ruleContext);

  String name();
}
