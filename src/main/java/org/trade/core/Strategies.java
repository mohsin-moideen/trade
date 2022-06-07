package org.trade.core;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.UnstableIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.VWAPIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;

public class Strategies {

	public static Strategy getMacdRsiBuyStrategy(BarSeries series) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
		Rule entry = new CrossedUpIndicatorRule(macdIndicator.getShortTermEma(), macdIndicator.getLongTermEma())
				.and(new CrossedUpIndicatorRule(rsiIndicator, 30));
		Rule exit = new StopGainRule(closePrice, .1);
		Strategy strategy = new BaseStrategy(entry, exit);
		return strategy;
	}

	public static Strategy getMacdRsiSellStrategy(BarSeries series) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
		Rule entry = new CrossedDownIndicatorRule(macdIndicator.getShortTermEma(), macdIndicator.getLongTermEma())
				.and(new CrossedDownIndicatorRule(rsiIndicator, 70));
		Rule exit = new StopGainRule(closePrice, .15);
		Strategy strategy = new BaseStrategy(entry, exit);
		return strategy;
	}

	public static Strategy getVwap9EmaBuyStrategy(BarSeries series) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		VWAPIndicator vwapIndicator = new VWAPIndicator(series, 14);
		EMAIndicator emaIndicator = new EMAIndicator(closePrice, 9);
		Rule entryRule = new CrossedUpIndicatorRule(emaIndicator, vwapIndicator);
		Rule exitRule = new StopGainRule(closePrice, .15);
		BaseStrategy strategy = new BaseStrategy(entryRule, exitRule);
		return strategy;
	}

	public static Strategy getVwap9EmaSellStrategy(BarSeries series) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		VWAPIndicator vwapIndicator = new VWAPIndicator(series, 50);
		EMAIndicator emaIndicator = new EMAIndicator(closePrice, 9);
		Rule entryRule = new CrossedDownIndicatorRule(emaIndicator, vwapIndicator);
		Rule exitRule = new StopGainRule(closePrice, .1);
		BaseStrategy strategy = new BaseStrategy(entryRule, exitRule);
		return strategy;
	}

	public static Strategy getUnstableIndicatorStrategy(BarSeries series) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		int smaPeriod = 3;
		Indicator<Num> sma = new UnstableIndicator(new SMAIndicator(closePrice, smaPeriod), smaPeriod - 1);
		Rule entryRule = new CrossedUpIndicatorRule(closePrice, sma);
		// Rule exitRule = new StopGainRule(closePrice, .1);
		Rule exitRule = new CrossedDownIndicatorRule(closePrice, sma);
		BaseStrategy strategy = new BaseStrategy(entryRule, exitRule);
		strategy.setUnstablePeriod(3);
		return strategy;
	}

	private static void validate(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}
	}
}
