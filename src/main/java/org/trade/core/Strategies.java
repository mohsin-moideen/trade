package org.trade.core;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.VWAPIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class Strategies {
	private static final Logger log = LogManager.getLogger(Strategies.class);

	public static Strategy getEmaRsiAdxBuyStrategy(BarSeries series, List<Num> params) {
		validateParamCount(params, 5);
		return getEmaRsiAdxBuyStrategy(series, params.get(0), params.get(1), params.get(2), params.get(3),
				params.get(4));
	}

	public static Strategy getEmaRsiAdxBuyStrategy(BarSeries series, Num rsiIndicatorLength, Num adxIndicatorLength,
			Num emaIndicatorLength, Num stopGainPercentage, Num stopLossPercentage) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiIndicatorLength.intValue());
		ADXIndicator adxIndicator = new ADXIndicator(series, adxIndicatorLength.intValue());
		EMAIndicator emaIndicator = new EMAIndicator(closePrice, emaIndicatorLength.intValue());
		Rule entryRule = new CrossedDownIndicatorRule(rsiIndicator, 80).and(new OverIndicatorRule(adxIndicator, 30))
				.and(new UnderIndicatorRule(emaIndicator, 50));
		Rule exitRule = new StopGainRule(closePrice, stopGainPercentage)
				.or(new StopLossRule(closePrice, stopLossPercentage));
		FxStrategy strategy = new FxStrategy(entryRule, exitRule, stopLossPercentage.doubleValue(),
				stopGainPercentage.doubleValue());
		return strategy;
	}

	public static Strategy getEmaRsiAdxSellStrategy(BarSeries series, List<Num> params) {
		validateParamCount(params, 5);
		return getEmaRsiAdxSellStrategy(series, params.get(0), params.get(1), params.get(2), params.get(3),
				params.get(4));
	}

	public static Strategy getEmaRsiAdxSellStrategy(BarSeries series, Num rsiIndicatorLength, Num adxIndicatorLength,
			Num emaIndicatorLength, Num stopGainPercentage, Num stopLossPercentage) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiIndicatorLength.intValue());
		ADXIndicator adxIndicator = new ADXIndicator(series, adxIndicatorLength.intValue());
		EMAIndicator emaIndicator = new EMAIndicator(closePrice, emaIndicatorLength.intValue());
		Rule entryRule = new UnderIndicatorRule(rsiIndicator, 30).and(new OverIndicatorRule(adxIndicator, 30))
				.and(new OverIndicatorRule(emaIndicator, 50));
		Rule exitRule = new StopGainRule(closePrice, stopGainPercentage)
				.or(new StopLossRule(closePrice, stopLossPercentage));
		FxStrategy strategy = new FxStrategy(entryRule, exitRule, stopLossPercentage.doubleValue(),
				stopGainPercentage.doubleValue());
		return strategy;
	}

	// Created to simplify backtesting
	public static Strategy getVwap9EmaSellStrategy(BarSeries series, List<Num> params) {
		validateParamCount(params, 2);
		return getVwap9EmaSellStrategy(series, params.get(0), params.get(1));
	}

	public static Strategy getVwap9EmaSellStrategy(BarSeries series, Num stopGainPercentage, Num stopLossPercentage) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		VWAPIndicator vwapIndicator = new VWAPIndicator(series, 8);
		EMAIndicator emaIndicator = new EMAIndicator(closePrice, 9);
		Rule entryRule = new CrossedDownIndicatorRule(emaIndicator, vwapIndicator);
		Rule exitRule = new StopGainRule(closePrice, stopGainPercentage)
				.or(new StopLossRule(closePrice, stopLossPercentage))
				.or(new CrossedUpIndicatorRule(emaIndicator, vwapIndicator));
		FxStrategy strategy = new FxStrategy(entryRule, exitRule, stopLossPercentage.doubleValue(),
				stopGainPercentage.doubleValue());
		return strategy;
	}

	// Created to simplify backtesting
	public static Strategy getVwap9EmaBuyStrategy(BarSeries series, List<Num> params) {
		validateParamCount(params, 2);
		return getVwap9EmaBuyStrategy(series, params.get(0), params.get(1));
	}

	public static Strategy getVwap9EmaBuyStrategy(BarSeries series, Num stopGainPercentage, Num stopLossPercentage) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		VWAPIndicator vwapIndicator = new VWAPIndicator(series, 8);
		EMAIndicator emaIndicator = new EMAIndicator(closePrice, 9);
		Rule entryRule = new CrossedUpIndicatorRule(emaIndicator, vwapIndicator);
		Rule exitRule = new StopGainRule(closePrice, stopGainPercentage)
				.or(new StopLossRule(closePrice, stopLossPercentage))
				.or(new CrossedDownIndicatorRule(emaIndicator, vwapIndicator));
		FxStrategy strategy = new FxStrategy(entryRule, exitRule, stopLossPercentage.doubleValue(),
				stopGainPercentage.doubleValue());
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

	// Created to simplify backtesting
	public static Strategy getMacdRsiBuyStrategy(BarSeries series, List<Num> params) {
		if (params.size() != 1) {
			log.error("Invalid number of parameters!");
			return null;
		}
		return getMacdRsiBuyStrategy(series, params.get(0));
	}

	public static Strategy getMacdRsiBuyStrategy(BarSeries series, Num stopGainPercentage) {
		validate(series);
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
		Rule entryRule = new CrossedUpIndicatorRule(macdIndicator.getShortTermEma(), macdIndicator.getLongTermEma())
				.and(new CrossedUpIndicatorRule(rsiIndicator, 30));
		Rule exitRule = new StopGainRule(closePrice, stopGainPercentage);
		FxStrategy strategy = new FxStrategy(entryRule, exitRule, null, stopGainPercentage.doubleValue());
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

	private static void validate(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}
	}

	private static void validateParamCount(List<Num> params, int expectedCount) {
		if (params.size() != expectedCount) {
			String message = "Invalid number of parameters!" + " Expected" + expectedCount + ", found " + params.size();
			log.error(message);
			throw new IllegalArgumentException(message);
		}
	}
}
