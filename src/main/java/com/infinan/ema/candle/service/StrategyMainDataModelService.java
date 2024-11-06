package com.infinan.ema.candle.service;

import org.springframework.stereotype.Service;

import com.infinan.common.model.StrategyMainDataModel;
import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;

@Service
public class StrategyMainDataModelService {
		public EmaAndCandleOptionStrategyTradeData convertDataModelToEntity(StrategyMainDataModel mainDataModel) {
			EmaAndCandleOptionStrategyTradeData tradeData = new EmaAndCandleOptionStrategyTradeData();
			tradeData.setOrderId(mainDataModel.getOrderId());
	        tradeData.setSystemUser(mainDataModel.getSystemUser());
	        tradeData.setStrategyName(mainDataModel.getStrategyName());        
	        tradeData.setTradingSymbol(mainDataModel.getTradingSymbol());
	        tradeData.setTimeFrame(mainDataModel.getTimeFrame());
	        tradeData.setLocalTestingMode(mainDataModel.isTradeMode());
	        tradeData.setCe_pe(mainDataModel.getCe_pe());
	        tradeData.setItm_atm_otm(mainDataModel.getItm_atm_otm());
	        tradeData.setFastLane(mainDataModel.getFastLane());
	        tradeData.setSlowLane(mainDataModel.getSlowLane());
	        tradeData.setWeekDay(mainDataModel.getWeekDay());
	        tradeData.setBuyOrderPlaceTime(mainDataModel.getBuyOrderPlaceTime());
	        tradeData.setBuyOrderExecutionTime(mainDataModel.getBuyOrderExecutionTime());
	        tradeData.setBuyOrderCalaulatedPrice(mainDataModel.getBuyOrderCalaulatedPrice());
	        tradeData.setBuyOrderRealPrice(mainDataModel.getBuyOrderRealPrice()+"");
	        tradeData.setQuantity(mainDataModel.getQuantity());
	        tradeData.setSellOrderPlaceTime(mainDataModel.getSellOrderPlaceTime());
	        tradeData.setSellOrderExecutionTime(mainDataModel.getSellOrderExecutionTime());
	        tradeData.setSellOrderCalaulatedPrice(mainDataModel.getSellOrderCalaulatedPrice());
	        tradeData.setSellOrderRealPrice(mainDataModel.getSellOrderRealPrice());
	        tradeData.setTradeTotalTimeTaken(mainDataModel.getTradeTotalTimeTaken());
	        tradeData.setSellOrderPriceModified(mainDataModel.getSellOrderPriceModified());
	        tradeData.setStoplossPrice(mainDataModel.getStoplossPrice());
	        tradeData.setStoplossPercentage(mainDataModel.getStoplossPercentage());
	        tradeData.setMaxPriceDropPercentage(mainDataModel.getMaxPriceDropPercentage());
	        tradeData.setTargetPercentage(mainDataModel.getTargetPercentage());
	        tradeData.setProfitAmount(mainDataModel.getProfitAmount());
	        tradeData.setProfitPercentage(mainDataModel.getProfitPercentage());
	        tradeData.setWaitingTimeForBuyOrderCancellation(mainDataModel.getWaitingTimeForBuyOrderCancellation());
	        tradeData.setCandleSustainedAt(mainDataModel.getCandleSustainedAt());
	        tradeData.setCrossoverDoneAt(mainDataModel.getCrossoverDoneAt());
	        tradeData.setBuySignalGeneratedAt(mainDataModel.getBuySignalGeneratedAt());
	        tradeData.setCandleHighPrice(mainDataModel.getCandleHighPrice());
	        tradeData.setHighPriceToBeBreakForConfirmation(mainDataModel.getHighPriceToBeBreakForConfirmation());
	        tradeData.setAlertRaisedTime(mainDataModel.getAlertRaisedTime());
	        tradeData.setMidAlertRaisedTime(mainDataModel.getMidAlertRaisedTime());
	        tradeData.setRedCandleInRow(mainDataModel.getRedCandleInRow());
	        tradeData.setTrailingStopLossPrice(mainDataModel.getTrailingStopLossPrice());
	        tradeData.setHighestProfitPercentageOfTrade(mainDataModel.getHighestProfitPercentageOfTrade());
	        tradeData.setMaxDropInProfit(mainDataModel.getMaxDropInProfit());
	        tradeData.setDoMaxDropInProfitCheckAfter(mainDataModel.getDoMaxDropInProfitCheckAfter());
	        tradeData.setTargetPrice(mainDataModel.getTargetPrice());
	        tradeData.setExitReason(mainDataModel.getExitReason());
	        tradeData.setAdditionalNotes(mainDataModel.getAdditionalNotes());
	        tradeData.setAverageVolume(mainDataModel.getAverageVolume());
	        tradeData.setVolumeAtSustainedAt(mainDataModel.getVolumeAtSustainedAt());
	        tradeData.setNextCandleVolume(mainDataModel.getNextCandleVolume());
	        
//	        Create setter and getter for below attributes in tradeData object
//	        String changeBuyPriceBy;
//	    	String stopLossDurationInSecond;
//	    	String crossLowOfPreviousCandle = "False";
//	    	String fastLaneGreaterThenSlowLaneCount;
	        
			return tradeData;
		}
}
