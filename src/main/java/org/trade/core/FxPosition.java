package org.trade.core;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.trade.utils.JsonUtils;
import org.trade.utils.TelegramUtils;
import org.trade.utils.meta_api.MetaApiUtil;
import org.trade.utils.meta_api.TradeUtil;
import org.trade.utils.meta_api.beans.TradeRequest;

import cloud.metaapi.sdk.clients.meta_api.TradeException;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderPosition;
import cloud.metaapi.sdk.clients.meta_api.models.MetatraderTradeResponse;
import cloud.metaapi.sdk.meta_api.MetaApiConnection;

public class FxPosition extends Position {

	private static final long serialVersionUID = -916068891251660413L;
	private static final Logger log = LogManager.getLogger(FxPosition.class);

	private MetatraderPosition mtPosition;
	private FxTrade entry;
	private FxTrade exit;
	private TradeType startingType;
	private CostModel transactionCostModel;
	private CostModel holdingCostModel;
	private String mtOrderId;

	public String getMtOrderId() {
		return mtOrderId;
	}

	public void setMtOrderId(String mtOrderId) {
		this.mtOrderId = mtOrderId;
	}

	public MetatraderPosition getMtPosition() {
		return mtPosition;
	}

	public void setMtPosition(MetatraderPosition mtPosition) {
		this.mtPosition = mtPosition;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Trade getEntry() {
		return entry;
	}

	public Trade getExit() {
		return exit;
	}

	public TradeType getStartingType() {
		return startingType;
	}

	public CostModel getTransactionCostModel() {
		return transactionCostModel;
	}

	public CostModel getHoldingCostModel() {
		return holdingCostModel;
	}

	/**
	 * Constructor.
	 * 
	 * @param startingType         the starting {@link TradeType trade type} of the
	 *                             position (i.e. type of the entry trade)
	 * @param transactionCostModel the cost model for transactions of the asset
	 * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
	 */
	public FxPosition(TradeType startingType, CostModel transactionCostModel, CostModel holdingCostModel) {
		super(startingType, transactionCostModel, holdingCostModel);
		if (startingType == null) {
			throw new IllegalArgumentException("Starting type must not be null");
		}
		this.startingType = startingType;
		this.transactionCostModel = transactionCostModel;
		this.holdingCostModel = holdingCostModel;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position p = (Position) obj;
			return (entry == null ? p.getEntry() == null : entry.equals(p.getEntry()))
					&& (exit == null ? p.getExit() == null : exit.equals(p.getExit()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(entry, exit);
	}

	public FxTrade forcedExit(int index, Num price, Num amount) {
		FxTrade trade = new FxTrade(index, startingType.complementType(), price, amount, transactionCostModel);
		exit = trade;
		return trade;
	}

	/**
	 * Operates the position at the index-th position
	 * 
	 * @param index      the bar index
	 * @param price      the price
	 * @param amount     the amount
	 * @param symbol
	 * @param takeProfit
	 * @param stopLoss
	 * @return the trade
	 */
	public FxTrade operate(int index, Num price, Num amount, String symbol, Double stopLoss, Double takeProfit) {
		FxTrade trade = null;
		if (isNew()) {
			// redundant check to prevent multiple open orders on same entry. do not remove!
			if (mtOrderId == null && mtPosition == null) {
				MetatraderTradeResponse tradeOrder = executeTrade(startingType, amount, symbol, stopLoss, takeProfit);
				if (tradeOrder != null && mtPosition != null) {
					trade = new FxTrade(index, startingType, DecimalNum.valueOf(mtPosition.openPrice), amount,
							transactionCostModel);
					entry = trade;
					mtOrderId = tradeOrder.orderId;
				}
			}

		} else if (isOpened()) {
			if (index < entry.getIndex()) {
				throw new IllegalStateException("The index i is less than the entryTrade index");
			}
			MetaApiConnection connection = MetaApiUtil.getMetaApiConnection();
			connection.closePosition(mtPosition.id, null); // on fail exit will not be recorded. will retry.
			trade = new FxTrade(index, startingType.complementType(), DecimalNum.valueOf(mtPosition.currentPrice),
					amount, transactionCostModel);
			exit = trade;
		}
		return trade;
	}

	/**
	 * 
	 * @param tradeType
	 * @param price
	 * @param amount
	 * @param symbol
	 * @param stopLoss
	 * @param takeProfit
	 * @return trade reponse after placing MARKET ORDER
	 */
	private MetatraderTradeResponse executeTrade(TradeType tradeType, Num amount, String symbol, Double stopLoss,
			Double takeProfit) {
		TradeRequest request = new TradeRequest();
		Double buyPrice = TradeUtil.getCurrentBuyPrice(tradeType);
		Double sellPrice = TradeUtil.getCurrentBuyPrice(tradeType.complementType());
		request.setVolume(amount.doubleValue());
		request.setSymbol(symbol);
		request.setActionType(tradeType);
		request.setStopLoss(getStopLossPrice(tradeType, sellPrice, stopLoss));
		request.setTakeProfit(getTakeProfitPrice(tradeType, buyPrice, takeProfit));
		MetatraderTradeResponse createOrderResponse = null;
		try {
			log.info("Placing market price order!");
			createOrderResponse = TradeUtil.createOrder(request);
			if (createOrderResponse != null && createOrderResponse.orderId != null) {
				try {
					mtPosition = MetaApiUtil.getMetaApiConnection().getPosition(createOrderResponse.orderId).get();
					log.info("Position opened " + JsonUtils.getString(mtPosition));
				} catch (Exception e1) {
					log.error("<<<FAILED TO FETCH POSITON AFTER MARKET ORDER >>>");
					log.info("Attempting to close position!");
					TelegramUtils.sendMessage("⚠️⚠️⚠️⚠️⚠️ FAILED TO FETCH POSITON AFTER MARKET ORDER ⚠️⚠️⚠️⚠️⚠️");
					TelegramUtils.sendMessage(
							"⚠️⚠️⚠️⚠️⚠️ Possible stray positions! Please close all trades and restart trading bot ⚠️⚠️⚠️⚠️⚠️");

					try {
						MetaApiUtil.getMetaApiConnection().closePosition(createOrderResponse.orderId, null).get();
					} catch (Exception e2) {
						log.error(
								"<<<STRAY OPEN POSITION!! SHIT'S ON FIRE. FAILED TO CLOSE POSITON AFTER STRAY MARKET ORDER >>>");

						TelegramUtils
								.sendMessage("⚠️⚠️⚠️⚠️⚠️  FAILED TO CLOSE POSITON AFTER STRAY MARKET ORDER ⚠️⚠️⚠️⚠️⚠️");
						TelegramUtils.sendMessage(
								"⚠️⚠️⚠️⚠️⚠️ Possible stray positions! Please close all trades and restart trading bot ⚠️⚠️⚠️⚠️⚠️");

					}
				}
			}
		} catch (TradeException e1) {
			log.error("Failed to place market order", e1);
		}
		return createOrderResponse;
	}

	private Double getTakeProfitPrice(TradeType tradeType, Double price, Double takeProfit) {
		if (tradeType == TradeType.BUY) {
			return getUpwardChange(price, takeProfit);

		} else {
			return getDownwardChange(price, takeProfit);

		}
	}

	private Double getStopLossPrice(TradeType tradeType, Double price, Double stopLoss) {
		if (tradeType == TradeType.BUY) {
			return getDownwardChange(price, stopLoss);

		} else {
			return getUpwardChange(price, stopLoss);

		}
	}

	private Double getUpwardChange(Double price, Double changePercentage) {
		return changePercentage == null ? null : (price + (price * changePercentage / 100));
	}

	private Double getDownwardChange(Double price, Double changePercentage) {
		return changePercentage == null ? null : (price - (price * changePercentage / 100));
	}

	/**
	 * @return true if the position is closed, false otherwise
	 */
	public boolean isClosed() {
		return (entry != null) && (exit != null);
	}

	/**
	 * @return true if the position is opened, false otherwise
	 */
	public boolean isOpened() {
		return (entry != null) && (exit == null);
	}

	/**
	 * @return true if the position is new, false otherwise
	 */
	public boolean isNew() {
		return (entry == null) && (exit == null);
	}

	public boolean isPending() {
		return isOpened() && mtPosition == null;
	}

	@Override
	public String toString() {
		return "Entry: " + entry + " exit: " + exit;
	}

}
