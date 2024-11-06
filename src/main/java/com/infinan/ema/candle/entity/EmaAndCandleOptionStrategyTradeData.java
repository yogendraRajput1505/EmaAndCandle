package com.infinan.ema.candle.entity;


import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Component
@Entity(name = "TEma_And_Candle_Option_Strategy_TradeData")
public class EmaAndCandleOptionStrategyTradeData {
	@Id
	private String orderId;
	private String systemUser;
	private String strategyName;
	private String tradingSymbol;
	private String timeFrame;
	private boolean localTestingMode;
	private String ce_pe;
	private String itm_atm_otm;
	private String fastLane;
	private String slowLane;
	private String weekDay;
	private String buyOrderPlaceTime;
	private String buyOrderExecutionTime;
	private String buyOrderCalaulatedPrice;
	private String buyOrderRealPrice;
	private String quantity;
	private String sellOrderPlaceTime;
	private String sellOrderExecutionTime;
	private String sellOrderCalaulatedPrice;
	private String sellOrderRealPrice;
	private String tradeTotalTimeTaken;
	private String sellOrderPriceModified = "False";
	private String stoplossPrice;
	private double stoplossPercentage;
	private String maxPriceDropPercentage;
	private String targetPercentage;
	private double profitAmount;	
	private double profitPercentage;
	private String waitingTimeForBuyOrderCancellation;
	private String candleSustainedAt = null;
	private String crossoverDoneAt = null;
	private String buySignalGeneratedAt = null;
	private double candleHighPrice = 0;
	private double highPriceToBeBreakForConfirmation = 0;
	private String alertRaisedTime = "";
	private String midAlertRaisedTime = "";
	private int redCandleInRow=0;
	private double trailingStopLossPrice=0;
	private double highestProfitPercentageOfTrade=0;
	private double maxDropInProfit = 0;
	private double doMaxDropInProfitCheckAfter;
	private double targetPrice =0;
	private String exitReason;
	private String additionalNotes;
	private double averageVolume;
	private double volumeAtSustainedAt;
	private double nextCandleVolume;
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getSystemUser() {
		return systemUser;
	}
	public void setSystemUser(String systemUser) {
		this.systemUser = systemUser;
	}
	public String getStrategyName() {
		return strategyName;
	}
	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}
	public String getTradingSymbol() {
		return tradingSymbol;
	}
	public void setTradingSymbol(String tradingSymbol) {
		this.tradingSymbol = tradingSymbol;
	}
	public String getTimeFrame() {
		return timeFrame;
	}
	public void setTimeFrame(String timeFrame) {
		this.timeFrame = timeFrame;
	}
	public boolean isLocalTestingMode() {
		return localTestingMode;
	}
	public void setLocalTestingMode(boolean localTestingMode) {
		this.localTestingMode = localTestingMode;
	}
	public String getCe_pe() {
		return ce_pe;
	}
	public void setCe_pe(String ce_pe) {
		this.ce_pe = ce_pe;
	}
	public String getItm_atm_otm() {
		return itm_atm_otm;
	}
	public void setItm_atm_otm(String itm_atm_otm) {
		this.itm_atm_otm = itm_atm_otm;
	}
	public String getFastLane() {
		return fastLane;
	}
	public void setFastLane(String fastLane) {
		this.fastLane = fastLane;
	}
	public String getSlowLane() {
		return slowLane;
	}
	public void setSlowLane(String slowLane) {
		this.slowLane = slowLane;
	}
	public String getWeekDay() {
		return weekDay;
	}
	public void setWeekDay(String weekDay) {
		this.weekDay = weekDay;
	}
	public String getBuyOrderPlaceTime() {
		return buyOrderPlaceTime;
	}
	public void setBuyOrderPlaceTime(String buyOrderPlaceTime) {
		this.buyOrderPlaceTime = buyOrderPlaceTime;
	}
	public String getBuyOrderExecutionTime() {
		return buyOrderExecutionTime;
	}
	public void setBuyOrderExecutionTime(String buyOrderExecutionTime) {
		this.buyOrderExecutionTime = buyOrderExecutionTime;
	}
	public String getBuyOrderCalaulatedPrice() {
		return buyOrderCalaulatedPrice;
	}
	public void setBuyOrderCalaulatedPrice(String buyOrderCalaulatedPrice) {
		this.buyOrderCalaulatedPrice = buyOrderCalaulatedPrice;
	}
	public String getBuyOrderRealPrice() {
		return buyOrderRealPrice;
	}
	public void setBuyOrderRealPrice(String buyOrderRealPrice) {
		this.buyOrderRealPrice = buyOrderRealPrice;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public String getSellOrderPlaceTime() {
		return sellOrderPlaceTime;
	}
	public void setSellOrderPlaceTime(String sellOrderPlaceTime) {
		this.sellOrderPlaceTime = sellOrderPlaceTime;
	}
	public String getSellOrderExecutionTime() {
		return sellOrderExecutionTime;
	}
	public void setSellOrderExecutionTime(String sellOrderExecutionTime) {
		this.sellOrderExecutionTime = sellOrderExecutionTime;
	}
	public String getSellOrderCalaulatedPrice() {
		return sellOrderCalaulatedPrice;
	}
	public void setSellOrderCalaulatedPrice(String sellOrderCalaulatedPrice) {
		this.sellOrderCalaulatedPrice = sellOrderCalaulatedPrice;
	}
	public String getSellOrderRealPrice() {
		return sellOrderRealPrice;
	}
	public void setSellOrderRealPrice(String sellOrderRealPrice) {
		this.sellOrderRealPrice = sellOrderRealPrice;
	}
	public String getTradeTotalTimeTaken() {
		return tradeTotalTimeTaken;
	}
	public void setTradeTotalTimeTaken(String tradeTotalTimeTaken) {
		this.tradeTotalTimeTaken = tradeTotalTimeTaken;
	}
	public String getSellOrderPriceModified() {
		return sellOrderPriceModified;
	}
	public void setSellOrderPriceModified(String sellOrderPriceModified) {
		this.sellOrderPriceModified = sellOrderPriceModified;
	}
	public String getStoplossPrice() {
		return stoplossPrice;
	}
	public void setStoplossPrice(String stoplossPrice) {
		this.stoplossPrice = stoplossPrice;
	}
	public double getStoplossPercentage() {
		return stoplossPercentage;
	}
	public void setStoplossPercentage(double stoplossPercentage) {
		this.stoplossPercentage = stoplossPercentage;
	}
	public String getMaxPriceDropPercentage() {
		return maxPriceDropPercentage;
	}
	public void setMaxPriceDropPercentage(String maxPriceDropPercentage) {
		this.maxPriceDropPercentage = maxPriceDropPercentage;
	}
	public String getTargetPercentage() {
		return targetPercentage;
	}
	public void setTargetPercentage(String targetPercentage) {
		this.targetPercentage = targetPercentage;
	}
	public double getProfitAmount() {
		return profitAmount;
	}
	public void setProfitAmount(double profitAmount) {
		this.profitAmount = profitAmount;
	}
	public double getProfitPercentage() {
		return profitPercentage;
	}
	public void setProfitPercentage(double profitPercentage) {
		this.profitPercentage = profitPercentage;
	}
	public String getWaitingTimeForBuyOrderCancellation() {
		return waitingTimeForBuyOrderCancellation;
	}
	public void setWaitingTimeForBuyOrderCancellation(String waitingTimeForBuyOrderCancellation) {
		this.waitingTimeForBuyOrderCancellation = waitingTimeForBuyOrderCancellation;
	}
	public String getCandleSustainedAt() {
		return candleSustainedAt;
	}
	public void setCandleSustainedAt(String candleSustainedAt) {
		this.candleSustainedAt = candleSustainedAt;
	}
	public String getCrossoverDoneAt() {
		return crossoverDoneAt;
	}
	public void setCrossoverDoneAt(String crossoverDoneAt) {
		this.crossoverDoneAt = crossoverDoneAt;
	}
	public String getBuySignalGeneratedAt() {
		return buySignalGeneratedAt;
	}
	public void setBuySignalGeneratedAt(String buySignalGeneratedAt) {
		this.buySignalGeneratedAt = buySignalGeneratedAt;
	}
	public double getCandleHighPrice() {
		return candleHighPrice;
	}
	public void setCandleHighPrice(double candleHighPrice) {
		this.candleHighPrice = candleHighPrice;
	}
	public double getHighPriceToBeBreakForConfirmation() {
		return highPriceToBeBreakForConfirmation;
	}
	public void setHighPriceToBeBreakForConfirmation(double highPriceToBeBreakForConfirmation) {
		this.highPriceToBeBreakForConfirmation = highPriceToBeBreakForConfirmation;
	}
	public String getAlertRaisedTime() {
		return alertRaisedTime;
	}
	public void setAlertRaisedTime(String alertRaisedTime) {
		this.alertRaisedTime = alertRaisedTime;
	}
	public String getMidAlertRaisedTime() {
		return midAlertRaisedTime;
	}
	public void setMidAlertRaisedTime(String midAlertRaisedTime) {
		this.midAlertRaisedTime = midAlertRaisedTime;
	}
	public int getRedCandleInRow() {
		return redCandleInRow;
	}
	public void setRedCandleInRow(int redCandleInRow) {
		this.redCandleInRow = redCandleInRow;
	}
	public double getTrailingStopLossPrice() {
		return trailingStopLossPrice;
	}
	public void setTrailingStopLossPrice(double trailingStopLossPrice) {
		this.trailingStopLossPrice = trailingStopLossPrice;
	}
	public double getHighestProfitPercentageOfTrade() {
		return highestProfitPercentageOfTrade;
	}
	public void setHighestProfitPercentageOfTrade(double highestProfitPercentageOfTrade) {
		this.highestProfitPercentageOfTrade = highestProfitPercentageOfTrade;
	}
	public double getMaxDropInProfit() {
		return maxDropInProfit;
	}
	public void setMaxDropInProfit(double maxDropInProfit) {
		this.maxDropInProfit = maxDropInProfit;
	}
	public double getDoMaxDropInProfitCheckAfter() {
		return doMaxDropInProfitCheckAfter;
	}
	public void setDoMaxDropInProfitCheckAfter(double doMaxDropInProfitCheckAfter) {
		this.doMaxDropInProfitCheckAfter = doMaxDropInProfitCheckAfter;
	}
	public double getTargetPrice() {
		return targetPrice;
	}
	public void setTargetPrice(double targetPrice) {
		this.targetPrice = targetPrice;
	}
	public String getExitReason() {
		return exitReason;
	}
	public void setExitReason(String exitReason) {
		this.exitReason = exitReason;
	}
	public String getAdditionalNotes() {
		return additionalNotes;
	}
	public void setAdditionalNotes(String additionalNotes) {
		this.additionalNotes = additionalNotes;
	}	
	public double getAverageVolume() {
		return averageVolume;
	}
	public void setAverageVolume(double averageVolume) {
		this.averageVolume = averageVolume;
	}
	public double getVolumeAtSustainedAt() {
		return volumeAtSustainedAt;
	}
	public void setVolumeAtSustainedAt(double volumeAtSustainedAt) {
		this.volumeAtSustainedAt = volumeAtSustainedAt;
	}
	public double getNextCandleVolume() {
		return nextCandleVolume;
	}
	public void setNextCandleVolume(double nextCandleVolume) {
		this.nextCandleVolume = nextCandleVolume;
	}
	@Override
	public String toString() {
		return "EmaAndCandleOptionStrategyTradeData [orderId=" + orderId + ", systemUser=" + systemUser
				+ ", strategyName=" + strategyName + ", tradingSymbol=" + tradingSymbol + ", timeFrame=" + timeFrame
				+ ", localTestingMode=" + localTestingMode + ", ce_pe=" + ce_pe + ", itm_atm_otm=" + itm_atm_otm
				+ ", fastLane=" + fastLane + ", slowLane=" + slowLane + ", weekDay=" + weekDay + ", buyOrderPlaceTime="
				+ buyOrderPlaceTime + ", buyOrderExecutionTime=" + buyOrderExecutionTime + ", buyOrderCalaulatedPrice="
				+ buyOrderCalaulatedPrice + ", buyOrderRealPrice=" + buyOrderRealPrice + ", quantity=" + quantity
				+ ", sellOrderPlaceTime=" + sellOrderPlaceTime + ", sellOrderExecutionTime=" + sellOrderExecutionTime
				+ ", sellOrderCalaulatedPrice=" + sellOrderCalaulatedPrice + ", sellOrderRealPrice="
				+ sellOrderRealPrice + ", tradeTotalTimeTaken=" + tradeTotalTimeTaken + ", sellOrderPriceModified="
				+ sellOrderPriceModified + ", stoplossPrice=" + stoplossPrice + ", stoplossPercentage="
				+ stoplossPercentage + ", maxPriceDropPercentage=" + maxPriceDropPercentage + ", targetPercentage="
				+ targetPercentage + ", profitAmount=" + profitAmount + ", profitPercentage=" + profitPercentage
				+ ", waitingTimeForBuyOrderCancellation=" + waitingTimeForBuyOrderCancellation + ", candleSustainedAt="
				+ candleSustainedAt + ", crossoverDoneAt=" + crossoverDoneAt + ", buySignalGeneratedAt="
				+ buySignalGeneratedAt + ", candleHighPrice=" + candleHighPrice + ", highPriceToBeBreakForConfirmation="
				+ highPriceToBeBreakForConfirmation + ", alertRaisedTime=" + alertRaisedTime + ", midAlertRaisedTime="
				+ midAlertRaisedTime + ", redCandleInRow=" + redCandleInRow + ", trailingStopLossPrice="
				+ trailingStopLossPrice + ", highestProfitPercentageOfTrade=" + highestProfitPercentageOfTrade
				+ ", maxDropInProfit=" + maxDropInProfit + ", doMaxDropInProfitCheckAfter="
				+ doMaxDropInProfitCheckAfter + ", targetPrice=" + targetPrice + ", exitReason=" + exitReason
				+ ", additionalNotes=" + additionalNotes + ", averageVolume=" + averageVolume + ", volumeAtSustainedAt="
				+ volumeAtSustainedAt + ", nextCandleVolume=" + nextCandleVolume + "]";
	}
}
