package com.infinan.ema.candle.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infinan.common.model.TradeEntryPropertiesForStrategy;
import com.infinan.ema.candle.strategy.HandleTradeEntry;
import com.infinan.ema.candle.utils.PropertyReader;

@RestController
@CrossOrigin
public class MainController {
	
	@Autowired
	HandleTradeEntry handleTradeEntry;
	
	@Autowired
	PropertyReader propertyReader;
	
	@GetMapping(value="status")
	public String status() {
		System.out.println("Working fine");
		return "Working fine";
	}
	/**
	 * Used for Live Market only
	 * @param isPE
	 * @param stopLossPercentage
	 * @param crossoverTime
	 * @param volumeForCondition
	 * @return
	 */
	@PostMapping(value="/kite/execute/live/buyStrategy")			
	public String executeEmaAndCandleOptionStrategyMain(@RequestBody TradeEntryPropertiesForStrategy propertiesForStrategy) {

		handleTradeEntry.handleTradeEntry(propertiesForStrategy, propertyReader.getBuyStrategyClass());
		
		
		return "Strategy Execution Completed. See DB Table for Details";
	}
	
	@PostMapping(value="/kite/execute/local/allTestCasesForBuyStrategy")			
	public String executeAllTestCasesForEmaAndCandleOptionStrategyMain(@RequestBody Map<String, List<Double>> requestBody, @RequestParam String strike) {
		long currentTimeMillis = System.currentTimeMillis();
		TradeEntryPropertiesForStrategy propertiesForStrategy = new TradeEntryPropertiesForStrategy();
		propertiesForStrategy.setRequestBody(requestBody);
		propertiesForStrategy.setStrike(strike);
		
		handleTradeEntry.handleTradeEntryForLocal(propertiesForStrategy, propertyReader.getBuyStrategyClass());;     
			
		System.out.println("Complete execution Time : "+(System.currentTimeMillis() - currentTimeMillis)/1000+" sec");
		return "Strategy Execution Completed. See DB Table for Details";
	}
}
