package org.trade.loaders;

import org.ta4j.core.BarSeries;
import org.trade.enums.Timeframe;

public interface DataLoader {
	public BarSeries getSeries(String symbol, Integer limit, Timeframe timeframe);
}
