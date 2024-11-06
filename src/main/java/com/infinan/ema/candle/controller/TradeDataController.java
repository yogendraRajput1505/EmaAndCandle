package com.infinan.ema.candle.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infinan.common.model.TradeProfitCalculationModel;
import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;
import com.infinan.ema.candle.service.EmaAndCandleOptionStrategyTradeDataService;

@RestController
@CrossOrigin
public class TradeDataController {

	@Autowired
	EmaAndCandleOptionStrategyTradeDataService tradeDataService;
		
	@GetMapping(value="/kite/algoTrading/historicalData/get/tradeData")
	public List<EmaAndCandleOptionStrategyTradeData> getTradeDataByTimeStampAndTradingSymbol(String tradingSymbol, String timeStamp, String orderId){
		System.out.println("Request Received : tradeData");
		return tradeDataService.getTradeData(tradingSymbol,timeStamp,orderId);
	}
	
	@GetMapping(value="/kite/algoTrading/historicalData/get/tradeData/tradingSymbol")
	public List<String> getDistinctTradingSymbolsByTimeStampLike(String timeStamp, String orderId){
		System.out.println("Request Received : tradeData/tradingSymbol");
		return tradeDataService.getDistinctTradingSymbolsByTimeStampLike(timeStamp, orderId);
	}
	
	@GetMapping(value="/kite/algoTrading/get/tradeProfitCalculation")
	public Map<String, List<TradeProfitCalculationModel>> tradeDataProfitCalculation(String tradingSymbol, String timeStamp, Integer tradeAmount, String orderId, Integer filterPrice){
		System.out.println("Request Received : tradeData Profit Calculation");
		return tradeDataService.tradeDataProfitCalculation(tradingSymbol,timeStamp,tradeAmount,orderId, filterPrice);
	}
	
}
	