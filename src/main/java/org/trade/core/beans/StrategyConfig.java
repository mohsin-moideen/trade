package org.trade.core.beans;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.num.Num;
import org.trade.enums.Timeframe;

public class StrategyConfig {
	private String name;
	private String symbol;
	private Timeframe timeframe;
	private Num volume;
	private BarSeries series;
	private Strategy strategy;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Timeframe getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(Timeframe timeframe) {
		this.timeframe = timeframe;
	}

	public Num getVolume() {
		return volume;
	}

	public void setVolume(Num volume) {
		this.volume = volume;
	}

	public BarSeries getSeries() {
		return series;
	}

	public void setSeries(BarSeries series) {
		this.series = series;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "StrategyConfig [symbol=" + symbol + ", timeframe=" + timeframe + ", volume=" + volume + ", series="
				+ series + ", strategy=" + strategy + "]";
	}

	public StrategyConfig(String name, String symbol, Timeframe timeframe, Num volume, BarSeries series,
			Strategy strategy) {
		super();
		this.name = name;
		this.symbol = symbol;
		this.timeframe = timeframe;
		this.volume = volume;
		this.series = series;
		this.strategy = strategy;
	}

}
