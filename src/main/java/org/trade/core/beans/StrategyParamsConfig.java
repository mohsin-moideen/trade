package org.trade.core.beans;

import org.ta4j.core.num.Num;

public class StrategyParamsConfig {
	private String paramName;
	private Num startValue;
	private Num endValue;
	private Num increment;

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public Num getStartValue() {
		return startValue;
	}

	public void setStartValue(Num startValue) {
		this.startValue = startValue;
	}

	public Num getEndValue() {
		return endValue;
	}

	public void setEndValue(Num endValue) {
		this.endValue = endValue;
	}

	public Num getIncrement() {
		return increment;
	}

	public void setIncrement(Num increment) {
		this.increment = increment;
	}

	public StrategyParamsConfig(String paramName, Num startValue, Num endValue, Num increment) {
		super();
		this.paramName = paramName;
		this.startValue = startValue;
		this.endValue = endValue;
		this.increment = increment;
	}

	@Override
	public String toString() {
		return "StrategyParamsConfig [paramName=" + paramName + ", startValue=" + startValue + ", endValue=" + endValue
				+ ", increment=" + increment + "]";
	}

}
