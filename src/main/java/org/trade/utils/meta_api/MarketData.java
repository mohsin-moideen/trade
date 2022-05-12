package org.trade.utils.meta_api;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.trade.Constants;
import org.trade.beans.Candle;
import org.trade.enums.Timeframe;
import org.trade.utils.HttpUtils;
import org.trade.utils.JsonUtils;

import com.google.gson.reflect.TypeToken;

public class MarketData {
	private static final Logger log = LogManager.getLogger(MarketData.class);

	public static List<Candle> getHistoricCandles(String symbol) {
		return getHistoricCandles(symbol, Timeframe.one_min);
	}

	public static List<Candle> getHistoricCandles(String symbol, Timeframe timeframe) {
		return getHistoricCandles(symbol, timeframe, 100);
	}

	public static List<Candle> getHistoricCandles(String symbol, Integer limit) {
		return getHistoricCandles(symbol, Timeframe.one_min, 100);
	}

	public static List<Candle> getHistoricCandles(String symbol, Timeframe timeframe, Integer limit) {
		return getHistoricCandles(symbol, timeframe, null, limit);
	}

	public static List<Candle> getHistoricCandles(String symbol, Timeframe timeframe, Date startDate, Integer limit) {
		if (limit > 1000)
			limit = 1000; // max limit is 1000
		final String historicCandlesEndpoint = Constants.HISTORIC_DATA_ENDPOINT.replace("{symbol}", symbol)
				.replace("{timeframe}", timeframe.toString());
		HttpGet request = new HttpGet(historicCandlesEndpoint);
		request.addHeader("auth-token", Constants.META_API_API_KEY);

		List<Candle> historicCandles = null;
		try {
			String response = HttpUtils.getStringResponse(request);
			log.debug("result HistoricCandlesResponse " + response);
			historicCandles = parseHistoricCandlesResponse(response);
		} catch (Exception e) {
			log.error("Error invoking historic Candles Endpoint from metaapi", e);
		}
		log.debug("historicCandles " + historicCandles);
		return historicCandles;
	}

	@SuppressWarnings("unchecked")
	private static List<Candle> parseHistoricCandlesResponse(String response) {
		Type type = new TypeToken<List<Candle>>() {
		}.getType();
		return (List<Candle>) JsonUtils.getObject(response, type);
	}

	public static Candle getCurrentCandle(String symbol, Timeframe timeframe) {
		final String historicCandlesEndpoint = Constants.CURRENT_CANDLE_ENDPOINT.replace("{symbol}", symbol)
				.replace("{timeframe}", timeframe.toString());
		HttpGet request = new HttpGet(historicCandlesEndpoint);
		request.addHeader("auth-token", Constants.META_API_API_KEY);
		Candle candle = null;
		try {
			String response = HttpUtils.getStringResponse(request);
			log.debug("result CurrentCandleResponse " + response);
			candle = parseCurrentCandleResponse(response);
		} catch (Exception e) {
			log.error("Error invoking current Candles Endpoint from metaapi", e);
		}
		log.info("Current candle " + candle);
		return candle;

	}

	private static Candle parseCurrentCandleResponse(String response) {
		return (Candle) JsonUtils.getObject(response, Candle.class);
	}

	public static void main(String[] args) {
		getCurrentCandle("EURUSD", Timeframe.one_min);
	}

}
