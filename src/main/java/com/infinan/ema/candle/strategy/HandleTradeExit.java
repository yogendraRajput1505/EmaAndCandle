package com.infinan.ema.candle.strategy;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infinan.common.model.TradeExitPropertiesForStrategy;
import com.infinan.common.service.CreateStrategyClassObject;
import com.infinan.ema.candle.service.strategy.sell.EmaAndCandleOptionStrategyMainSell;
import com.infinan.ema.candle.utils.PropertyReader;

@Service
public class HandleTradeExit {
	private static final Logger LOGGER = Logger.getLogger("HandleTradeExit");
	
	@Autowired
	PropertyReader propertyReader;
	
	@Autowired
	EmaAndCandleOptionStrategyMainSell emaAndCandleOptionStrategyMainSell; 
	
//	@Autowired
//	HftEmaStrategySell hftEmaStrategySell;

	
	//We can modify this method for adding more selling strategy
	public boolean handleTradeExit(TradeExitPropertiesForStrategy tradeExitPropertiesForStrategy) {
		Object sellObjectClassObject = CreateStrategyClassObject.getSellObjectClassObject(propertyReader.getSellStrategyClass());
		boolean handleTradeExit = false;
		if(sellObjectClassObject instanceof EmaAndCandleOptionStrategyMainSell) {
			handleTradeExit = emaAndCandleOptionStrategyMainSell.handleTradeExit(tradeExitPropertiesForStrategy.getCurrentCandleHistoricalData(),tradeExitPropertiesForStrategy.getPastCandle(),
					tradeExitPropertiesForStrategy.getstrategyMainDataModel(),tradeExitPropertiesForStrategy.getOrders());
		}
//		else if(sellObjectClassObject instanceof HftEmaStrategySell) {
//			handleTradeExit = hftEmaStrategySell.handleTradeExit(tradeExitPropertiesForStrategy.getCurrentCandleHistoricalData(),
//					tradeExitPropertiesForStrategy.getstrategyMainDataModel(),tradeExitPropertiesForStrategy.getOrders());
//		}
		else {
			LOGGER.info("Sell Strategy Class name does not match with available Strategy");
		}
		return handleTradeExit;
	}	
}
