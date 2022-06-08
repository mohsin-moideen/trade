package org.trade;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.ExpectancyCriterion;
import org.ta4j.core.analysis.criteria.MaximumDrawdownCriterion;
import org.ta4j.core.analysis.criteria.NumberOfConsecutiveWinningPositionsCriterion;
import org.ta4j.core.analysis.criteria.NumberOfLosingPositionsCriterion;
import org.ta4j.core.analysis.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.analysis.criteria.NumberOfWinningPositionsCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.NetLossCriterion;
import org.ta4j.core.analysis.criteria.pnl.NetProfitCriterion;
import org.ta4j.core.analysis.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.analysis.criteria.pnl.ProfitLossRatioCriterion;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.trade.core.Strategies;
import org.trade.core.beans.StrategyParamsConfig;
import org.trade.enums.Timeframe;
import org.trade.loaders.SeriesUtil;

public class Backtest {

	private static final String SERIES_JOINER = "_";
	private static final double volume = .1;
	private static final int lotSize = 100000;

	public static void main(String[] args) {
		// BarSeries series = SeriesUtil.initMovingBarSeries("EURUSD",
		// Timeframe.fifteen_min, 16);
		String strategyName = "getMacdRsiBuyStrategy";
		List<String> currencyPairs = new LinkedList<>(Arrays.asList(new String[] { "EURUSD", "GBPUSD" }));
		List<Timeframe> timeFrames = new LinkedList<>(
				Arrays.asList(new Timeframe[] { Timeframe.one_min, Timeframe.five_min, Timeframe.fifteen_min }));
		Map<String, BarSeries> series = initSeries(currencyPairs, timeFrames);
		List<StrategyParamsConfig> paramConfigs = new LinkedList<>();
		StrategyParamsConfig macdIndicatorShortBarCount = new StrategyParamsConfig("macdIndicatorShortBarCount",
				DecimalNum.valueOf(1.0), DecimalNum.valueOf(20.0), DecimalNum.valueOf(1.0));
		StrategyParamsConfig macdIndicatorLongBarCount = new StrategyParamsConfig("macdIndicatorLongBarCount",
				DecimalNum.valueOf(1.0), DecimalNum.valueOf(20.0), DecimalNum.valueOf(1.0));
		StrategyParamsConfig rsiIndicatorBarCount = new StrategyParamsConfig("rsiIndicatorBarCount",
				DecimalNum.valueOf(1.0), DecimalNum.valueOf(20.0), DecimalNum.valueOf(1.0));
		StrategyParamsConfig rsiCrossedUpValue = new StrategyParamsConfig("rsiCrossedUpValue", DecimalNum.valueOf(1.0),
				DecimalNum.valueOf(20.0), DecimalNum.valueOf(1.0));
		StrategyParamsConfig stopGainPercentage = new StrategyParamsConfig("stopGainPercentage", DecimalNum.valueOf(.1),
				DecimalNum.valueOf(20.0), DecimalNum.valueOf(.1));
		paramConfigs.add(macdIndicatorShortBarCount);
		paramConfigs.add(macdIndicatorLongBarCount);
		paramConfigs.add(rsiIndicatorBarCount);
		paramConfigs.add(rsiCrossedUpValue);
		paramConfigs.add(stopGainPercentage);
		List<Num> params = new LinkedList<>();
		for (String currencyPair : currencyPairs) {
			for (Timeframe timeFrame : timeFrames) {
				backtest(currencyPair, timeFrame, series, params, paramConfigs, paramConfigs.size() - 1);
			}
		}
	}

	private static void backtest(String currencyPair, Timeframe timeFrame, Map<String, BarSeries> series,
			List<Num> params, List<StrategyParamsConfig> paramConfigs, int paramIndex) {
		System.out.println("paramIndex " + paramIndex);
		StrategyParamsConfig strategyParamsConfig = paramConfigs.get(paramIndex);

		if (paramIndex == 0) {
			for (Num value = strategyParamsConfig.getStartValue(); value.isLessThanOrEqual(
					strategyParamsConfig.getEndValue()); value = value.plus(strategyParamsConfig.getIncrement())) {
				params.add(value);
				// System.out.println("if params " + params);
				BarSeries barSeries = series.get(currencyPair + SERIES_JOINER + timeFrame.toString());
				BarSeriesManager seriesManager = new BarSeriesManager(barSeries);
				Collections.reverse(params);
				Strategy strategy = Strategies.getMacdRsiBuyStrategy(barSeries, params);
				Collections.reverse(params);
				TradingRecord tradingRecord1 = seriesManager.run(strategy, TradeType.BUY,
						DecimalNum.valueOf(volume * lotSize));
				TradingRecord tradingRecord2 = seriesManager.run(strategy, TradeType.SELL,
						DecimalNum.valueOf(volume * lotSize));
				AnalysisCriterion profitLossCriterion = new ProfitLossCriterion();
				AnalysisCriterion x2 = new ExpectancyCriterion();
				AnalysisCriterion x3 = new NetLossCriterion();
				AnalysisCriterion x4 = new NetProfitCriterion();
				AnalysisCriterion x5 = new NumberOfPositionsCriterion();
				AnalysisCriterion x6 = new NumberOfWinningPositionsCriterion();
				AnalysisCriterion x7 = new NumberOfConsecutiveWinningPositionsCriterion();
				AnalysisCriterion x8 = new NumberOfLosingPositionsCriterion();
				AnalysisCriterion x9 = new ProfitLossRatioCriterion();
				AnalysisCriterion x10 = new MaximumDrawdownCriterion();
				AnalysisCriterion x11 = new WinningPositionsRatioCriterion();

//				System.out.println("strategy 1 -> " + (criterion.calculate(barSeries, tradingRecord1).doubleValue()));
//				System.out.println("strategy 2 -> " + (criterion.calculate(barSeries, tradingRecord2).doubleValue()));
				params.remove(params.size() - 1);
			}
		} else {
			for (Num value = strategyParamsConfig.getStartValue(); value.isLessThanOrEqual(
					strategyParamsConfig.getEndValue()); value = value.plus(strategyParamsConfig.getIncrement())) {
				params.add(value);
				System.out.println("else params " + params);
				backtest(currencyPair, timeFrame, series, params, paramConfigs, paramIndex - 1);
				params.remove(params.size() - 1);
			}
		}
	}

	private static Map<String, BarSeries> initSeries(List<String> currencyPairs, List<Timeframe> timeFrames) {
		Map<String, BarSeries> series = new HashMap<>();
		for (String currencyPair : currencyPairs) {
			for (Timeframe timeFrame : timeFrames) {
				series.put(currencyPair + SERIES_JOINER + timeFrame.toString(),
						SeriesUtil.initMovingBarSeries(currencyPair, timeFrame, 1000));
			}
		}
		return series;
	}
}
