package org.trade.core.beans;

public class Order {
	private Integer numericCode;
	private String Code;
	private String message;
	private String orderId;
	private String positionId;

	public Integer getNumericCode() {
		return numericCode;
	}

	public void setNumericCode(Integer numericCode) {
		this.numericCode = numericCode;
	}

	public String getCode() {
		return Code;
	}

	public void setCode(String code) {
		Code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPositionId() {
		return positionId;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}

	@Override
	public String toString() {
		return "Order [numericCode=" + numericCode + ", Code=" + Code + ", message=" + message + ", orderId=" + orderId
				+ ", positionId=" + positionId + "]";
	}
}
