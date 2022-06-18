package org.trade.core;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;

public class FxStrategy extends BaseStrategy {

	/**
	 * stopLoss and takeProfit are provided in percentage. Not absolute prices!
	 */
	private Double stopLoss;
	private Double takeProfit;

	public FxStrategy(Rule entryRule, Rule exitRule, int unstablePeriod, Double stopLoss, Double takeProfit) {
		super(entryRule, exitRule, unstablePeriod);
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
	}

	public FxStrategy(Rule entryRule, Rule exitRule, Double stopLoss, Double takeProfit) {
		super(entryRule, exitRule);
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
	}

	public FxStrategy(String name, Rule entryRule, Rule exitRule, int unstablePeriod, Double stopLoss,
			Double takeProfit) {
		super(name, entryRule, exitRule, unstablePeriod);
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
	}

	public FxStrategy(String name, Rule entryRule, Rule exitRule, Double stopLoss, Double takeProfit) {
		super(name, entryRule, exitRule);
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

}
