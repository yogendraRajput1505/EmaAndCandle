package com.infinan.ema.candle.strategy;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infinan.common.model.TradeEntryPropertiesForStrategy;
import com.infinan.common.service.CreateStrategyClassObject;
import com.infinan.ema.candle.service.strategy.buy.EmaAndCandleOptionStrategyMainBuy;

@Service
public class HandleTradeEntry {
private static final Logger LOGGER = Logger.getLogger("HandleTradeExit");
	
//	@Autowired
//	PropertyReader propertyReader;
//	
	@Autowired
	EmaAndCandleOptionStrategyMainBuy emaAndCandleOptionStrategyMainBuy;
	
//	@Autowired
//	HftEmaStrategyBuy hftEmaStrategyBuy;
	
	//We can modify this method for adding more selling strategy
		public void handleTradeEntry(TradeEntryPropertiesForStrategy propertiesForStrategy, String buyStrategyClass) {
			Object buyClassObject = CreateStrategyClassObject.getSellObjectClassObject(buyStrategyClass);
			if(buyClassObject instanceof EmaAndCandleOptionStrategyMainBuy) {
				//why do we need to pass multiple parameters? we can't pass only that object? I think we should, please check
				emaAndCandleOptionStrategyMainBuy.executeStrategyForLiveMarket(propertiesForStrategy.isPE(), propertiesForStrategy.getStrike(),propertiesForStrategy.getStopLossPercentage(),
						propertiesForStrategy.getCrossoverTime(),propertiesForStrategy.getVolumeForCondition(), propertiesForStrategy.isLive());
			}
//			else if(buyClassObject instanceof HftEmaStrategyBuy) {
//				hftEmaStrategyBuy.executeStrategyForLiveMarket(propertiesForStrategy.isPE(),propertiesForStrategy.getStrike());
//			}
			else {
				LOGGER.info("Buy Strategy Class name does not match with available Strategy");
			}
			
		}	
		
		
		public void handleTradeEntryForLocal(TradeEntryPropertiesForStrategy propertiesForStrategy, String buyStrategyClass) {
			Object buyClassObject = CreateStrategyClassObject.getSellObjectClassObject(buyStrategyClass);
		
			if(buyClassObject instanceof EmaAndCandleOptionStrategyMainBuy) {
				emaAndCandleOptionStrategyMainBuy.executeStrategyForLocalTesting(propertiesForStrategy.getStrike(), propertiesForStrategy.getRequestBody());
			}
//			else if(buyClassObject instanceof HftEmaStrategyBuy) {
//				hftEmaStrategyBuy.executeStrategyForLocalTesting(propertiesForStrategy.getStrike(), propertiesForStrategy.getRequestBody());
//			}
			else {
				LOGGER.info("Sell Strategy Class name does not match with available Strategy");
			}

		}	
}
