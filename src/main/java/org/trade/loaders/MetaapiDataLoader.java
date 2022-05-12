package org.trade.loaders;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.trade.beans.Candle;
import org.trade.enums.Timeframe;
import org.trade.utils.meta_api.MarketData;

public class MetaapiDataLoader implements DataLoader {

	private static final Logger log = LogManager.getLogger(MetaapiDataLoader.class);

	@Override
	public BarSeries getSeries(String symbol, Integer limit, Timeframe timeframe) {

		List<Candle> candles = MarketData.getHistoricCandles(symbol, timeframe, limit);
		if (candles == null) {
			log.error("Failed to init bar series! No data retrived from historical data api of meta api for symbol"
					+ symbol);
			return null;
		}
		BarSeries series = new BaseBarSeries();

		for (Candle candle : candles) {
			ZonedDateTime date = candle.getZonedDate();
			double open = candle.getOpen();
			double high = candle.getHigh();
			double low = candle.getLow();
			double close = candle.getClose();
			double volume = candle.getTickVolume();
			series.addBar(date, open, high, low, close, volume);
		}
		return series;
	}

}
