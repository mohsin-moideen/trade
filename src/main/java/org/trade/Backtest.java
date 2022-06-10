package org.trade;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.ExpectancyCriterion;
import org.ta4j.core.analysis.criteria.MaximumDrawdownCriterion;
import org.ta4j.core.analysis.criteria.NumberOfConsecutiveWinningPositionsCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.ProfitLossRatioCriterion;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.PerformanceReport;
import org.ta4j.core.reports.PositionStatsReport;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;
import org.trade.core.Strategies;
import org.trade.core.beans.StrategyParamsConfig;
import org.trade.enums.Timeframe;
import org.trade.loaders.SeriesUtil;

import com.opencsv.CSVWriter;

public class Backtest {

	private static final String SERIES_JOINER = "_";
	private static final String REPORT_BASE_PATH = "D:\\Trade\\Backtesting\\";
	private static final double volume = .1;
	private static final int lotSize = 100000;
	private static CSVWriter csvWriter;
	private static final TradingStatementGenerator tradingStatementGenerator = new TradingStatementGenerator();
	private static final AnalysisCriterion numberOfConsecutiveWinningPositionsCriterion = new NumberOfConsecutiveWinningPositionsCriterion();
	private static final AnalysisCriterion expectancyCriterion = new ExpectancyCriterion();
	private static final AnalysisCriterion profitLossRatioCriterion = new ProfitLossRatioCriterion();
	private static final AnalysisCriterion maximumDrawdownCriterion = new MaximumDrawdownCriterion();
	private static final AnalysisCriterion winningPositionsRatioCriterion = new WinningPositionsRatioCriterion();

	private static final Logger log = LogManager.getLogger(Backtest.class);

	public static void main(String[] args) throws IOException {
		// Step 1: Change strategy name
		final String strategyName = "Vwap9EmaSellStrategy";
		final String fileName = REPORT_BASE_PATH + strategyName + "-"
				+ new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()) + ".csv";
		log.info("Results will be saved to " + fileName);
		csvWriter = new CSVWriter(new FileWriter(fileName, true));
		// Step 2: Set currency pairs
		List<String> currencyPairs = new LinkedList<>(Arrays.asList(new String[] { "EURUSD" }));
		// Step 3: Set time frames
		List<Timeframe> timeFrames = new LinkedList<>(
				Arrays.asList(new Timeframe[] { Timeframe.five_min, Timeframe.fifteen_min, Timeframe.one_hour }));
		Map<String, BarSeries> series = initSeries(currencyPairs, timeFrames);
		List<StrategyParamsConfig> paramConfigs = new LinkedList<>();
		// Step 4: Create parameters required
		StrategyParamsConfig stopGainPercentage = new StrategyParamsConfig("stopGainPercentage",
				DecimalNum.valueOf(.01), DecimalNum.valueOf(.2), DecimalNum.valueOf(.01));
		StrategyParamsConfig stopLossPercentage = new StrategyParamsConfig("stopLossPercentage",
				DecimalNum.valueOf(.01), DecimalNum.valueOf(.2), DecimalNum.valueOf(.01));
		// Step 5: Add created parameters to list
		paramConfigs.add(stopGainPercentage);
		paramConfigs.add(stopLossPercentage);
		writeCsvHeaders(paramConfigs);
		List<Num> params = new LinkedList<>();
		for (String currencyPair : currencyPairs) {
			for (Timeframe timeFrame : timeFrames) {
				backtest(currencyPair, timeFrame, series, params, paramConfigs, paramConfigs.size() - 1);
			}
		}
		log.info("Backtesting complete!");
	}

	private static void backtest(String currencyPair, Timeframe timeFrame, Map<String, BarSeries> series,
			List<Num> params, List<StrategyParamsConfig> paramConfigs, int paramIndex) {
		StrategyParamsConfig strategyParamsConfig = paramConfigs.get(paramIndex);
		if (paramIndex == 0) {
			for (Num value = strategyParamsConfig.getStartValue(); value.isLessThanOrEqual(
					strategyParamsConfig.getEndValue()); value = value.plus(strategyParamsConfig.getIncrement())) {
				params.add(value);
				BarSeries barSeries = series.get(currencyPair + SERIES_JOINER + timeFrame.toString());
				BarSeriesManager seriesManager = new BarSeriesManager(barSeries);
				Collections.reverse(params);
				// Step 6: Change function call. You're set!
				Strategy strategy = Strategies.getVwap9EmaSellStrategy(barSeries, params);
				TradingRecord tradingRecordBuy = seriesManager.run(strategy, TradeType.BUY,
						DecimalNum.valueOf(volume * lotSize));
				TradingRecord tradingRecordSell = seriesManager.run(strategy, TradeType.SELL,
						DecimalNum.valueOf(volume * lotSize));
				final TradingStatement tradingStatementBuy = tradingStatementGenerator.generate(strategy,
						tradingRecordBuy, seriesManager.getBarSeries());
				final TradingStatement tradingStatementSell = tradingStatementGenerator.generate(strategy,
						tradingRecordSell, seriesManager.getBarSeries());
				writeBacktestResults(currencyPair, timeFrame, TradeType.BUY, params, barSeries, tradingRecordBuy,
						tradingStatementBuy.getPerformanceReport(), tradingStatementBuy.getPositionStatsReport());
				writeBacktestResults(currencyPair, timeFrame, TradeType.SELL, params, barSeries, tradingRecordSell,
						tradingStatementSell.getPerformanceReport(), tradingStatementSell.getPositionStatsReport());
				Collections.reverse(params);
				params.remove(params.size() - 1);
			}
		} else {
			for (Num value = strategyParamsConfig.getStartValue(); value.isLessThanOrEqual(
					strategyParamsConfig.getEndValue()); value = value.plus(strategyParamsConfig.getIncrement())) {
				params.add(value);
				backtest(currencyPair, timeFrame, series, params, paramConfigs, paramIndex - 1);
				params.remove(params.size() - 1);
			}
		}
	}

	private static void writeBacktestResults(String currencyPair, Timeframe timeFrame, TradeType tradeType,
			List<Num> params, BarSeries barSeries, TradingRecord tradingRecord, PerformanceReport performanceReport,
			PositionStatsReport positionStatsReport) {
		List<String> bactestData = new LinkedList<>(
				Arrays.asList(new String[] { currencyPair, timeFrame.value, tradeType.toString() }));
		for (Num param : params) {
			bactestData.add(param.toString());
		}
		bactestData.add(performanceReport.getTotalProfitLoss().toString());
		bactestData.add(performanceReport.getTotalProfit().toString());
		bactestData.add(performanceReport.getTotalLoss().toString());
		bactestData.add(positionStatsReport.getProfitCount()
				.plus(positionStatsReport.getLossCount().plus(positionStatsReport.getBreakEvenCount())).toString());
		bactestData.add(positionStatsReport.getProfitCount().toString());
		bactestData.add(positionStatsReport.getLossCount().toString());
		bactestData.add(String
				.valueOf(getCriterionValue(barSeries, tradingRecord, numberOfConsecutiveWinningPositionsCriterion)));
		bactestData.add(String.valueOf(getCriterionValue(barSeries, tradingRecord, expectancyCriterion)));
		bactestData.add(String.valueOf(getCriterionValue(barSeries, tradingRecord, profitLossRatioCriterion)));
		bactestData.add(String.valueOf(getCriterionValue(barSeries, tradingRecord, maximumDrawdownCriterion)));
		bactestData.add(String.valueOf(getCriterionValue(barSeries, tradingRecord, winningPositionsRatioCriterion)));

		csvWriter.writeNext(bactestData.toArray(new String[bactestData.size()]));
	}

	private static double getCriterionValue(BarSeries barSeries, TradingRecord tradingRecord,
			AnalysisCriterion criterion) {
		return criterion.calculate(barSeries, tradingRecord).doubleValue();
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

	private static void writeCsvHeaders(List<StrategyParamsConfig> paramConfigs) {
		List<String> csvHeaders = new LinkedList<>(
				Arrays.asList(new String[] { "Currency pair", "Timeframe", "Entry type" }));

		for (StrategyParamsConfig strategyParamsConfig : paramConfigs) {
			csvHeaders.add(strategyParamsConfig.getParamName());
		}
		// DO NOT CHANGE THE ORDER!! IF CHANGED, PLEASE CORRECT #writeBacktestResults
		// Please don't be an idiot
		csvHeaders.add("Absolute PnL");
		csvHeaders.add("Net profit");
		csvHeaders.add("Net loss");
		csvHeaders.add("Number of positions");
		csvHeaders.add("Number of winning positions");
		csvHeaders.add("Number of losing positions");
		csvHeaders.add("Number of consecutive winning positions");
		csvHeaders.add("Winning expectation");
		csvHeaders.add("Profit to loss ratio");
		csvHeaders.add("Maximum drawdown");
		csvHeaders.add("Percentage of profitable positions");
		csvWriter.writeNext(csvHeaders.toArray(new String[csvHeaders.size()]));
	}

}
