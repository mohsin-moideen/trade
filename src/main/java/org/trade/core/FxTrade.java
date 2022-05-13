package org.trade.core;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.num.Num;

public class FxTrade extends Trade {

	private static final long serialVersionUID = 586336055345289020L;

	public FxTrade(int index, BarSeries series, TradeType type, Num amount, CostModel transactionCostModel) {
		super(index, series, type, amount, transactionCostModel);
		// TODO Auto-generated constructor stub
	}

	public FxTrade(int index, BarSeries series, TradeType type, Num amount) {
		super(index, series, type, amount);
		// TODO Auto-generated constructor stub
	}

	public FxTrade(int index, BarSeries series, TradeType type) {
		super(index, series, type);
		// TODO Auto-generated constructor stub
	}

	public FxTrade(int index, TradeType type, Num pricePerAsset, Num amount, CostModel transactionCostModel) {
		super(index, type, pricePerAsset, amount, transactionCostModel);
		// TODO Auto-generated constructor stub
	}

	public FxTrade(int index, TradeType type, Num pricePerAsset, Num amount) {
		super(index, type, pricePerAsset, amount);
		// TODO Auto-generated constructor stub
	}

	public FxTrade(int index, TradeType type, Num pricePerAsset) {
		super(index, type, pricePerAsset);
		// TODO Auto-generated constructor stub
	}

}
