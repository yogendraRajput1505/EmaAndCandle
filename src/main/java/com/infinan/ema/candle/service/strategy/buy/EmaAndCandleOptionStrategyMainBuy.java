package com.infinan.ema.candle.service.strategy.buy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.infinan.common.entity.TIndexHistoricalData;
import com.infinan.common.entity.TInfinanHistoricalData;
import com.infinan.common.entity.TInfinanStrikeMinuteHistoricalData;
import com.infinan.common.model.TradeExitPropertiesForStrategy;
import com.infinan.common.service.GetLivePriceUsingWebSocket;
import com.infinan.common.service.CustomBuySellService;
import com.infinan.common.service.LoginToKite;
import com.infinan.common.service.ReadInfinanHistoricalDataService;
import com.infinan.common.service.StockUtils;
import com.infinan.common.utils.CommonUtils;
import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;
import com.infinan.ema.candle.repo.EmaAndCandleOptionStrategyTradeDataRepository;
import com.infinan.ema.candle.strategy.HandleTradeExit;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Order;


@Service
public class EmaAndCandleOptionStrategyMainBuy extends EmaAndCandleOptionStrategyMainParent{
	
	public EmaAndCandleOptionStrategyMainBuy() {
		super();
	}
	
//	public EmaAndCandleOptionStrategyMainBuy(Environment environment) {
//		super(environment);
//	}
//	
	@Autowired
	GetLivePriceUsingWebSocket getLivePriceUsingWebSocket;
		
	@Autowired
	LoginToKite kite;
	
	@Autowired
	CustomBuySellService customBuySellService;

	@Autowired
	EmaAndCandleOptionStrategyTradeData emaAndCandleOptionStrategyTradeData;
	
	@Autowired
	EmaAndCandleOptionStrategyTradeDataRepository emaAndCandleOptionStrategyTradeDataRepository;
		
	@Autowired
	ReadInfinanHistoricalDataService readInfinanHistoricalDataService;
	
	@Autowired
	HandleTradeExit handleTradeExit;
	
	@Autowired
	TradeExitPropertiesForStrategy tradeExitPropertiesForStrategy;
	
	protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");	
	
	//int nonCongicutiveRedCandleCount=0;

	
	public void executeStrategyForLocalTesting(String strike, Map<String, List<Double>> requestBody){
		resetVariables();	
//		setLoggerLevel();
		List<LocalDate> dateToUse = propertyReader.getDateToUseForLocalTesting();
		List<String> indexes = propertyReader.getIndexesForLocalTesting();
		supportingAttributes.setLocalTestingMode(true);
		if(indexes.size() != dateToUse.size()) {
			throw new RuntimeException("Error : indexes and dateToUse are of different sizes, unable to perform local testing");
		}
		
		List<Double> volumeForCondition = requestBody.get("volumeForCondition");
        List<Double> stopLossPercentage = requestBody.get("stopLossPercentage");
        List<Double> crossoverTime = requestBody.get("crossoverTime");
		
		for(int position=0;position<indexes.size();position++) {
			long currentTimeMillis = System.currentTimeMillis();
			LOGGER.info(indexes.get(position)+", "+dateToUse.get(position));
			Map<String, TIndexHistoricalData> indexHistoricalData = readInfinanHistoricalDataService.rearrangeIndexHistoricalData(indexes.get(position), dateToUse.get(position));
			Map<String, TInfinanStrikeMinuteHistoricalData> strikeMinuteHistoricalData = readInfinanHistoricalDataService.rearrangeStrikeMinuteHistoricalData(indexes.get(position), dateToUse.get(position));
			Map<String, TInfinanHistoricalData> collectSecondHistoricalData = readInfinanHistoricalDataService.rearrangeInfinanSecondHistoricalDataList(indexes.get(position), dateToUse.get(position));
			
	        for(int i=0;i<stopLossPercentage.size();i++) {
				for(int j=0;j<crossoverTime.size();j++) {
					for(int k=0;k<volumeForCondition.size();k++) {
						supportingAttributes.setStoplossPercentage(stopLossPercentage.get(i));
						supportingAttributes.setCrossoverTime(crossoverTime.get(j));
						supportingAttributes.setVolumeForCondition(volumeForCondition.get(k)); 
						
						System.out.println("Strategy executing for : "+stopLossPercentage.get(i)+"_"+crossoverTime.get(j)+"_"+volumeForCondition.get(k));
						
						localTesting(true, strike, dateToUse, indexes, position, indexHistoricalData, strikeMinuteHistoricalData,
								collectSecondHistoricalData);
						resetVariables();
						localTesting(false, strike, dateToUse, indexes, position, indexHistoricalData, strikeMinuteHistoricalData,
								collectSecondHistoricalData);
						resetVariables();
						supportingAttributes.getTradesList().stream().forEach(val -> System.out.println(val.toString()));
//						System.out.println(supportingAttributes.getTradesList());
						emaAndCandleOptionStrategyTradeDataRepository.saveAll(supportingAttributes.getTradesList());
						supportingAttributes.getTradesList().clear();
						System.gc();
					}
				}
			}
	        
	        System.gc();
			resetVariables();
			System.out.println("Testing Ended For : "+indexes.get(position)+", "+dateToUse.get(position)+", execution Time : "+(System.currentTimeMillis() - currentTimeMillis)/1000+" sec");
		}		
		LOGGER.info("Execution Completed . . . . .");
	}

	private void localTesting(boolean isPE, String strike, List<LocalDate> dateToUse, List<String> indexes, int position,
			Map<String, TIndexHistoricalData> indexHistoricalData,
			Map<String, TInfinanStrikeMinuteHistoricalData> strikeMinuteHistoricalData,
			Map<String, TInfinanHistoricalData> collectSecondHistoricalData) {
		String currentMinute;
		List<HistoricalData> pastCandle;
		for(Entry<String, TIndexHistoricalData> entrySet : indexHistoricalData.entrySet()) {
			currentMinute = entrySet.getKey().substring(0, 19);
			LocalDateTime parse = LocalDateTime.parse(currentMinute);
			currentIndexHistoricalData = historicalDataUtils.convertIndexHistoricalDataToHistoricalData(entrySet.getValue());
			setTradingSymbol(indexes.get(position), currentIndexHistoricalData.close, isPE, strike, dateToUse.get(position), parse.toLocalTime());				
//				if(parse.toLocalTime().isBefore(LocalTime.of(10, 00)) || parse.toLocalTime().isAfter(LocalTime.of(12, 00))){
//					continue;
//				}
			LOGGER.info(currentIndexHistoricalData.timeStamp+" : "+currentIndexHistoricalData.close+" : "+tradingSymbol);
			pastCandle = readInfinanHistoricalDataService.getHistoricalCandle(propertyReader.getNoOfHistoricalCandle(),getTradingSymbol(),currentMinute,strikeMinuteHistoricalData);
			int currentSecond = 1;
			//if("BANKNIFTY2432046500CE".equals(tradingSymbol))
			while(currentSecond<=59) {
				if(isTradeRunning || candleBreakout || (parse.toLocalTime().isBefore(LocalTime.of(15, 5)) && parse.toLocalTime().isAfter(LocalTime.of(9, 15+propertyReader.getNoOfHistoricalCandle())))) {
					try {
						HistoricalData currentCandleHistoricalData = readInfinanHistoricalDataService.loadCurrentStrikeSecondData(getTradingSymbol(),currentMinute,currentSecond,collectSecondHistoricalData);
						if(!ObjectUtils.isEmpty(currentCandleHistoricalData)) {
							if(pastCandle.size()>=propertyReader.getNoOfHistoricalCandle()) {
								pastCandle.remove(propertyReader.getNoOfHistoricalCandle()-1);
							}
							pastCandle.add(currentCandleHistoricalData);
							calculateEMA(pastCandle);
				    		if(!isTradeRunning) {
				    			handleTradeEntry(pastCandle, currentCandleHistoricalData);
				    			handleTradeEntry(currentCandleHistoricalData);
				    		}else {
				    			tradeExitPropertiesForStrategy.setCurrentCandleHistoricalData(currentCandleHistoricalData);
				    			tradeExitPropertiesForStrategy.setstrategyMainDataModel(strategyMainDataModel);
				    			tradeExitPropertiesForStrategy.setOrders(orders);
				    			tradeExitPropertiesForStrategy.setPastCandle(pastCandle);
				    			supportingAttributes.setTradeBuyPrice(tradeBuyPrice);
				    			boolean exitDone = handleTradeExit.handleTradeExit(tradeExitPropertiesForStrategy);
				    			if(exitDone) {
				    				isTradeRunning = false;
				    				takeTradeEntry=false;
				    				buySignalGeneratedAt=null;
				    				//I have to modify this for not to take trade entry next 5 minute 
				    				//goToSleepState(5);
				    			}
				    		}
				    	}else {
				    		LOGGER.info("currentCandleHistoricalData is not available for second "+currentSecond+", "+tradingSymbol);
				    	}
					}
					catch(Exception e) {
						LOGGER.error("An error occurred: " + e.getMessage());
						LOGGER.error("Stack trace: ", e);
//						e.printStackTrace();
			    	}
				}
				currentSecond++;
			}	
		}
	}

	
	
	public void executeStrategyForLiveMarket(boolean isPE, String strike, double stopLossPercentage, double crossoverTime, double volumeForCondition,boolean isLive){
		resetVariables();
		kiteConnect = kite.getKiteConnect();
//		setLoggerLevel();
		supportingAttributes.setStoplossPercentage(stopLossPercentage);
		supportingAttributes.setCrossoverTime(crossoverTime);
		supportingAttributes.setVolumeForCondition(volumeForCondition);
		supportingAttributes.setLiveTestingMode(!isLive);// if is Live is true then we have to set liveTestingMode as false
		List<HistoricalData> pastCandle = null;
		int lastMinute=-1;
		doSleepTill(9,15+propertyReader.getNoOfHistoricalCandle());
		
		while( !stopExecutionAfter(12, 10) || isTradeRunning ) {
			try {
				
				//doSleepAfter(12, 30);
				
				if(isTradeRunning || candleBreakout || LocalTime.now().isBefore(LocalTime.of(15, 30))) {
					long currentTimeMillis = System.currentTimeMillis();
					int currentMinute = java.time.LocalDateTime.now().getMinute();
					
					if(currentMinute != lastMinute) {
						LOGGER.info("Minute Changed...loading past candles");
						codeToExecuteAfterEveryOneMinuteForLiveMarket(isPE, strike);
						pastCandle = getHistoricalCandle();
						lastMinute = currentMinute;
					}
					
					HistoricalData currentCandleHistoricalData = getLivePriceUsingWebSocket.loadLivePriceData(kiteConnect, getTradingSymbol());
					if(pastCandle.size()>=propertyReader.getNoOfHistoricalCandle()) {
						pastCandle.remove(propertyReader.getNoOfHistoricalCandle()-1);
					}
					LOGGER.info("Price for "+getTradingSymbol()+ " is "+currentCandleHistoricalData.close+" for "+currentCandleHistoricalData.timeStamp);					
					pastCandle.add(currentCandleHistoricalData);
					calculateEMA(pastCandle);
			    	if(!ObjectUtils.isEmpty(currentCandleHistoricalData)) {
			    		if(!isTradeRunning) {
			    			handleTradeEntry(pastCandle, currentCandleHistoricalData);
			    			handleTradeEntry(currentCandleHistoricalData);
			    		}else {
			    			tradeExitPropertiesForStrategy.setCurrentCandleHistoricalData(currentCandleHistoricalData);
			    			tradeExitPropertiesForStrategy.setstrategyMainDataModel(strategyMainDataModel);
			    			tradeExitPropertiesForStrategy.setOrders(orders);
			    			tradeExitPropertiesForStrategy.setPastCandle(pastCandle);
			    			LOGGER.info("tradeExitPropertiesForStrategy : "+tradeExitPropertiesForStrategy);
			    			boolean exitDone = handleTradeExit.handleTradeExit(tradeExitPropertiesForStrategy);
			    			if(exitDone) {
			    				isTradeRunning = false;
			    				takeTradeEntry=false;
			    				buySignalGeneratedAt=null;
			    			//	goToSleepState(5);
			    			}
			    		}			    		
			    	}
			    	//Setting this sleep so that more then 5 request should not go in one second.
			    	if(System.currentTimeMillis() - currentTimeMillis <230 ) {
			    		//long val = System.currentTimeMillis() - currentTimeMillis;
			    		Thread.sleep(230-(System.currentTimeMillis() - currentTimeMillis));
			    	}
				}else {
					LOGGER.info("Execution Completed... We are done for today");
					return;
				}
		    }catch(Exception e) {
		    	LOGGER.error("An error occurred: " + e.getMessage());
				LOGGER.error("Stack trace: ", e);
//				e.printStackTrace();
	    	}
	    }
		
		LOGGER.info("Strategy Execution Done For Today ");
		
	}

	private void doSleepTill(int hour, int minute) {
		LocalTime time = LocalTime.now();
		System.out.println("Sleepping Till : "+hour +":"+minute);
		while( time.getHour() <=hour-1 || (time.getHour() ==hour && time.getMinute() <minute)) {
			try {
				Thread.sleep(10000);//10sec
			} catch (InterruptedException e) {
				LOGGER.error("An error occurred: " + e.getMessage());
				LOGGER.error("Stack trace: ", e);
			}
			time = LocalTime.now();
			System.out.println(time);
		}
		System.out.println("Sleeping Over");
	}
	
	private boolean stopExecutionAfter(int hour, int minute) {
		LocalTime time = LocalTime.now();
		if( time.getHour() >hour || (time.getHour() ==hour && time.getMinute() >minute)) {
			LOGGER.info("Sleepping After : "+hour +":"+minute);
				return true;			
		}
		return false;
	}

	protected void handleTradeEntry(List<HistoricalData> pastCandle, HistoricalData currentCandleHistoricalData) {
		if(!takeTradeEntry) {
			takeTradeEntry = generateBuySignal(currentCandleHistoricalData,pastCandle);		    				
		}
		else if(buySignalGeneratedAt!=null 
				&& (Duration.between(buySignalGeneratedAt, OffsetDateTime.parse(currentCandleHistoricalData.timeStamp, formatter))
						.toSeconds() > 120)) {
			takeTradeEntry=false;
			buySignalGeneratedAt=null;
			highPriceToBeBreakForConfirmation = 0;
			LOGGER.info("Time Limit Exceeds for Buy Signal Confirmation. Resetting takeTradeEntry to false");
		}
	}
	
	private void handleTradeEntry(HistoricalData currentCandleHistoricalData) {
//		if(doConfirmation(currentCandleHistoricalData)) {
		if(takeTradeEntry) {
//		if(true) {
			LOGGER.info("Prepairing for Buy order ");
			tradeBuyPrice = calculateBuyPrice(currentCandleHistoricalData.close);
			int lot = calculateLot(tradeBuyPrice);
			setDataToMainDataModelAtTradeEntryTime();
			LOGGER.info(strategyMainDataModel.toString());
			highPriceToBeBreakForConfirmation=Integer.MIN_VALUE;//resetting
			strategyMainDataModel.setBuyOrderPlaceTime(currentCandleHistoricalData.timeStamp);
			if(supportingAttributes.isLocalTestingMode() || supportingAttributes.isLiveTestingMode()) {
				//testingTargetPrice = customBuySellService.roundToNearestMultiple(customBuySellService.roundToNearestMultiple(tradeBuyPrice,0.05)+customBuySellService.getTargetPrice(tradeBuyPrice, getTargetPercentage()), 0.05);
				strategyMainDataModel.setBuyOrderExecutionTime(currentCandleHistoricalData.timeStamp);
				strategyMainDataModel.setBuyOrderRealPrice(tradeBuyPrice);
				strategyMainDataModel.setQuantity(1+""); // why setting 1?
				//This order Id we are setting so that in sell side we could identify a new buy order is executed....
				//it will be used as a flag to do some work for new order execution
				Order order = new Order();
				order.exchangeOrderId = currentCandleHistoricalData.timeStamp;
				order.orderId = currentCandleHistoricalData.timeStamp;
				orders.add(order);
				LOGGER.info("Testing Buy Order Placed : getTradingSymbol: "+getTradingSymbol() +", tradeBuyPrice: "+tradeBuyPrice+", lot: "+lot);
			}else {
				orders = customBuySellService.prepareForPlaceOrder(tradingSymbol,lot,tradeBuyPrice,propertyReader.getTargetPercentage(),false,strategyMainDataModel);		
				LOGGER.info("Buy Order Executed");
			}
			isTradeRunning=true;
		}
	}

	private void calculateEMA(List<HistoricalData> candleForEmaCalculation) {
		supportingAttributes.setFastLaneMA(indicators.getEmaValueNew(candleForEmaCalculation, fastLaneMultipler, propertyReader.getFastLane()));
		supportingAttributes.setSlowLaneMA(indicators.getEmaValueNew(candleForEmaCalculation, slowLaneMultipler, propertyReader.getSlowLane()));
		
		
		System.out.println(tradingSymbol+", FastLane : "+supportingAttributes.getFastLaneMA()+", SlowLane : "+supportingAttributes.getSlowLaneMA()+", time : "+candleForEmaCalculation.get(candleForEmaCalculation.size()-1).timeStamp);
	}
	
	private boolean generateBuySignal(HistoricalData historicalData, List<HistoricalData> pastCandle) {
		
		if(!isCandleSustained) {
			highPriceToBeBreakForConfirmation=0;
			isCandleSustained = isCandlesSustained(getCandleToCheckSustainability(pastCandle));
		}
		//Resetting variable if no  break out detected after candles are sustained
		if(isCandleSustained) {
			LOGGER.info("isCandleSustained : "+isCandleSustained);
			OffsetDateTime date2 = OffsetDateTime.parse(historicalData.timeStamp, formatter);
	        if (Duration.between(candleSustainedAt, date2).toMinutes() > 4) {
	        	resetBuySignalAttributes();
	        	LOGGER.info("Time Duration exceeds,time is 4 min > candleSustainedAt Resetting Buy signal varaibles. candleSustainedAt : "+candleSustainedAt+", date2 : "+date2);
	        }
		}
	
		if(crossoverDoneAt == null && supportingAttributes.getFastLaneMA() >= supportingAttributes.getSlowLaneMA()) {
			crossoverDoneAt = OffsetDateTime.parse(historicalData.timeStamp, formatter);
			LOGGER.info("crossoverDoneAT : "+crossoverDoneAt);
		}else if(crossoverDoneAt != null && supportingAttributes.getFastLaneMA() < supportingAttributes.getSlowLaneMA()){
			crossoverDoneAt = null;
			LOGGER.info("Reseting crossoverDoneAT : "+crossoverDoneAt);
		}
		
		if(isCandleSustained && historicalData.high >= sustainedCandleHighPrice) {// && historicalData.high >=highPriceToBeBreakForConfirmation) {
			candleBreakout = true;
			//highPriceToBeBreakForConfirmation = historicalData.high;	
//			trailingStopLossPrice = pastCandle.get(pastCandle.size()-2).low;//take low of second last candle
			LOGGER.info("candleBreakout : "+candleBreakout+", highPriceToBeBreakForConfirmation inner : "+highPriceToBeBreakForConfirmation);
		} 
		
		OffsetDateTime date2 = OffsetDateTime.parse(historicalData.timeStamp, formatter);
		if(isCandleSustained && candleBreakout && crossoverDoneAt!=null && 
				(Duration.between(crossoverDoneAt, date2).toSeconds() > supportingAttributes.getCrossoverTime())) {
			
			if(!checkTwoNonCongicutiveRedCandle(pastCandle) && !StockUtils.checkCongicutiveRedcandle(pastCandle,3)) {
				buySignalGeneratedAt = OffsetDateTime.parse(historicalData.timeStamp, formatter);
				strategyMainDataModel.setCandleSustainedAt(candleSustainedAt.toString());
				strategyMainDataModel.setCrossoverDoneAt(crossoverDoneAt.toString());
				strategyMainDataModel.setBuySignalGeneratedAt(buySignalGeneratedAt.toString());
				resetBuySignalAttributes();
				LOGGER.info("Buy Signal Generated, waiting for Confirmation. highPriceToBeBreakForConfirmation : "+highPriceToBeBreakForConfirmation);
				return true;				
			}

		}
		return false;
	}
		
	//This condition is based on some candle size combination 
	private boolean checkTwoNonCongicutiveRedCandle(List<HistoricalData> pastCandle) {
		//Buy order place hone wali candle se just previous wali candle
		HistoricalData candle = pastCandle.get(pastCandle.size()-2);
		int nonCongicutiveRedCandleCount=0;
		for(int i=0;i<pastCandle.size()-2;i++) {
			if(StockUtils.getCandlesSizeRatio(candle, pastCandle.get(i))>=2 && StockUtils.isRedCandle(pastCandle.get(i))) {
				nonCongicutiveRedCandleCount++;
			}
			if(nonCongicutiveRedCandleCount>=2) {
				nonCongicutiveRedCandleCount=0;						
				return true;
			}
		}
		return false;
	}

	private List<HistoricalData> getCandleToCheckSustainability(List<HistoricalData> pastCandles) {
		List<HistoricalData> candles = new ArrayList<>();
		for(int i=pastCandles.size()-6;i<pastCandles.size();i++) {
			candles.add(pastCandles.get(i));
		}
		return candles;
	}

	/**
	 * past candle have 6 candles, 0th not taking and 5th running 1-4 
	 * 
	 */
	private boolean isCandlesSustained(List<HistoricalData> pastCandle) {
		if(pastCandle.size()<propertyReader.getSlowLane())
			return false;
		//i=1, ignoring 0th index candle
		double firstCandleHigh = pastCandle.get(1).high;
		double firstCandleLow = pastCandle.get(1).low;
		for(int i=1;i<pastCandle.size();i++) {
			HistoricalData object = pastCandle.get(pastCandle.size()-i);
			if(!CommonUtils.isOneBetweenRange(object.open, object.close, firstCandleLow, firstCandleHigh)) {
				return false;
			}
//			if(firstCandleLow > object.low) {
//				return false;
//			}			
//			if(!CommonUtils.isOneBetweenRange(object.open, object.close, CommonUtils.calculatePercentageOfRange(firstCandleLow, firstCandleHigh, 50), firstCandleHigh)) {
//				return false;
//			}
		}
		double averageVolume = calculateAverageVolume(pastCandle);
		strategyMainDataModel.setAverageVolume(averageVolume);
		strategyMainDataModel.setVolumeAtSustainedAt(pastCandle.get(pastCandle.size()-2).volume);		
		LOGGER.info("Avg Volume : "+averageVolume);
		LOGGER.info("Sustained Candle : "+pastCandle.get(pastCandle.size()-2).timeStamp + ", Volume : "+pastCandle.get(pastCandle.size()-2).volume);
		// For live market we can set like if in 30 sec volume is greater then 50% then buy (or any other condition but we should not wait for complete candle creation)
		// why are we taking sustained candle volume? We can take running candle volume.
		// It's good to  add one OR condition. saying if running candle volume exceeds 50% of averageVolume*supportingAttributes.getVolumeForCondition() in 30 sec then do entry
		// I think we can't do this because current running candle will give current second volume data.
		// we have to find a way to save running candle till volume
		if(pastCandle.get(pastCandle.size()-2).volume > averageVolume*supportingAttributes.getVolumeForCondition()) {
			setSustainedCandleHighPrice(pastCandle);
			candleSustainedAt = OffsetDateTime.parse(pastCandle.get(pastCandle.size()-1).timeStamp, formatter);
			LOGGER.info("Candle sustained : "+true+", sustainedCandleHighPrice : "+sustainedCandleHighPrice);
			return true;
		}else {
			LOGGER.info("Volume condition doesn't satisfy for sustainability.");
		}		
		return false;
	}
	
	private double calculateAverageVolume(List<HistoricalData> pastCandle) {
		double volume=0;
		for(int i=2;i<6;i++) {
			volume = volume+pastCandle.get(pastCandle.size()-i-1).volume;
//			LOGGER.info((pastCandle.size()-i-1)+" Candle Used : "+pastCandle.get(i).timeStamp+", volume : "+pastCandle.get(i).volume);
		}
		return volume/4;
	}


		


	

}
