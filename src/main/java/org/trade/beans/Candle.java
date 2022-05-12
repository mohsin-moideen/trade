package org.trade.beans;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class Candle {
	private String symbol;
	private Date time;
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Integer tickVolume;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Integer getTickVolume() {
		return tickVolume;
	}

	public void setTickVolume(Integer tickVolume) {
		this.tickVolume = tickVolume;
	}

	public ZonedDateTime getZonedDate() {
		return ZonedDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
	}

	@Override
	public String toString() {
		return "Candle [symbol=" + symbol + ", time=" + time + ", open=" + open + ", high=" + high + ", low=" + low
				+ ", close=" + close + ", tickVolume=" + tickVolume + "]";
	}

}
