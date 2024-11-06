package com.infinan.ema.candle.service.strategy.buy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.infinan.common.model.StrategyMainDataModel;
import com.infinan.common.model.StrategySupportingAttributes;
import com.infinan.common.service.HistoricalDataUtils;
import com.infinan.common.service.Indicators;
import com.infinan.common.service.M_HistoricalDataService;
import com.infinan.common.service.SelectStrike;
import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;
import com.infinan.ema.candle.utils.PropertyReader;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Order;

import jakarta.annotation.PostConstruct;

@Service
public class EmaAndCandleOptionStrategyMainParent {
		
	@Autowired
	SelectStrike selectStrike;
	
	@Autowired
	Indicators indicators;
	
	@Autowired
	M_HistoricalDataService historicalDataService;
		
	@Autowired
	StrategyMainDataModel strategyMainDataModel;
		
	@Autowired
	HistoricalDataUtils historicalDataUtils;
	
	@Autowired
	PropertyReader propertyReader;
	
	@Autowired 
	StrategySupportingAttributes<EmaAndCandleOptionStrategyTradeData> supportingAttributes;
		
	
	protected KiteConnect kiteConnect;
	@Autowired
	protected Environment environment;
	protected static final Logger LOGGER = LoggerFactory.getLogger("EmaAndCandleOptionStrategyMainParent");
	
	protected static final String SYSTEM_USER = System.getProperty("user.name");
	protected static final String CURRENT_DAY_SHORT = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
	
	protected OffsetDateTime candleSustainedAt = null;
	protected OffsetDateTime crossoverDoneAt = null;
	protected OffsetDateTime buySignalGeneratedAt = null;
	
	//protected double fastLaneMA= 0; // put it in new class and create getter/setter both 
	//protected double slowLaneMA= 0; // put it in new class and create getter/setter both  
	protected double fastLaneMultipler; // put it in new class and create getter only
	protected double slowLaneMultipler; // put it in new class and create getter only
	protected String tradingSymbol = "";// put it in new class and create getter/setter both
	protected HistoricalData currentIndexHistoricalData = null; // put it in new class and create getter/setter both
	protected boolean isCandleSustained = false; // put it in new class and create getter/setter both
	protected boolean candleBreakout = false; // put it in new class and create getter/setter both
	// This variable will be used to store the high price from the sustained candles. This will used to determine the break out.
	protected double sustainedCandleHighPrice = 0; // put it in new class and create getter/setter both
	protected double sustainedCandleLowPrice = Integer.MAX_VALUE; // put it in new class and create getter/setter both
	//fastLaneMACount and slowLaneMACount ko humko time se replace karna padega...kyuki counting ka kuch bharosha nahi h
	protected double highPriceToBeBreakForConfirmation = 0; // Not using for now, we can remove later
	protected boolean isTradeRunning = false;
	

	protected List<Order> orders = new ArrayList<>();
	protected double tradeBuyPrice =0;
	protected boolean takeTradeEntry=false;
	protected double runningProfitPercentage=0;
	
	/**
	 * ****************************************************************
	 * ************************ Constructors **************************
	 * ****************************************************************
	 */
	
	public EmaAndCandleOptionStrategyMainParent() {
     
    }
	
//	public EmaAndCandleOptionStrategyMainParent(Environment environment) {
//        this.environment = environment;
//    }
//    
     @PostConstruct
	 public void init() {
	 	fastLaneMultipler = indicators.calculateMultipler(propertyReader.getFastLane());
	 	slowLaneMultipler = indicators.calculateMultipler(propertyReader.getSlowLane());
	 }
	
	/**
	 * ****************************************************************
	 * *************** Loading Data from property File ****************
	 * ****************************************************************
	 */
	
//	protected void setLoggerLevel() {
//		String level = environment.getProperty("logger.level");
//		if("OFF".equals(level))
//			LOGGER.setLevel(2);
//		else
//			LOGGER.setLevel(Level.ALL);
//	}
	
	protected String getTradingSymbol() {
		return tradingSymbol;
	}
	protected void setTradingSymbol(String index, double price, boolean isPE, String strike, LocalDate localDate, LocalTime localTime) {
		if(!isTradeRunning && !takeTradeEntry && !candleBreakout && !isCandleSustained) {
			tradingSymbol = selectStrike.setTradingSymbol(index, price, isPE, strike, localDate, localTime,1);
		}
	}
	
	protected int getTradeAmount() {
		return Integer.parseInt(environment.getProperty("Strategy.tradeAmount"));
	}
	
	protected int getLotSize() {
		return Integer.parseInt(environment.getProperty("index.lot.size."+propertyReader.getIndex()));
	}
	
	protected double calculateBuyPrice(double close) {
		return close*Double.parseDouble(environment.getProperty("Strategy.changeBuyPriceby"));
	}
	
	
	protected int calculateLot(double buyPrice) {
		 int lot =  (int) (getTradeAmount()/(getLotSize()*buyPrice));
		 if(lot<=0)
			 return 1;
		 return lot;
	}
	
	protected double calculateLossPercentage(double currentPrice){
		 return (tradeBuyPrice-currentPrice)*100/tradeBuyPrice;
	}
		
	protected LocalDate getDateToUse() {
		String dateToUse = environment.getProperty("Strategy.dateToUse");
		if("AUTO".equalsIgnoreCase(dateToUse))
			return LocalDate.now();
		return LocalDate.parse(dateToUse);
	}
	
	
	
	/**
	 * Resetting variables 
	 */
	protected void resetVariables() {
		sustainedCandleHighPrice = 0;
		sustainedCandleLowPrice=Integer.MAX_VALUE;
		isCandleSustained = false;
		candleBreakout = false;
		takeTradeEntry = false;
		supportingAttributes.setFastLaneMA(0);
		supportingAttributes.setSlowLaneMA(0);
//		fastLaneMACount=0;
		highPriceToBeBreakForConfirmation=0;
		isTradeRunning=false;
		tradeBuyPrice=0;
		candleSustainedAt = null;
		crossoverDoneAt=null;
		buySignalGeneratedAt=null;
		tradingSymbol = "";
		runningProfitPercentage=0;
//		highestProfitPercentageOfTrade=0;
//		alerts.clear();
		orders.clear();
//		LIVE_TESTING_MODE = propertyReader.isLiveTesingMode();
//		LOCAL_TESTING_MODE = propertyReader.isLocalTestingMode();
//		setMaxDropInProfit();
	}
	
	
	
	/**
	 * ****************************************************************
	 * ************* Other Supporting Methods For Buy *****************
	 * ****************************************************************
	 */
	

	//validate if there is any need of currentCandleHistoricalData.close > trailingStopLossPrice. But Later, for now it should not impact
	//there is no condition for 1 minute wait
	protected boolean doConfirmation( HistoricalData currentCandleHistoricalData) {
		return takeTradeEntry && currentCandleHistoricalData.close > highPriceToBeBreakForConfirmation;	
	}
	
	protected void resetBuySignalAttributes() {
		isCandleSustained = false;
		candleBreakout = false;
		sustainedCandleHighPrice = 0;
		sustainedCandleLowPrice=Integer.MAX_VALUE;
		crossoverDoneAt=null;
		candleSustainedAt=null;
	}
	
	protected void setSustainedCandleHighPrice(List<HistoricalData> pastSixCandle){
		sustainedCandleHighPrice=0;
		for(int i=2;i<pastSixCandle.size();i++){
			if(pastSixCandle.get(i).high>sustainedCandleHighPrice){
				sustainedCandleHighPrice = pastSixCandle.get(i).high;
			}
		}
	}
	
	//According to Sustained candle logic lowPrice will be low of 1st candle 
	//because if 2nd/3rd/4th candle crosses low then candle will not be considered as sustained....need to review later
	protected void setSustainedCandleLowPrice(List<HistoricalData> pastSixCandle){
		sustainedCandleLowPrice=Integer.MAX_VALUE;
		for(int i=2;i<pastSixCandle.size();i++){
			if(pastSixCandle.get(i).low<sustainedCandleLowPrice){
				sustainedCandleLowPrice = pastSixCandle.get(i).low;
			}
		}
	}

	
	/**
	 * ****************************************************************
	 * ******************** Other Common Methods **********************
	 * ****************************************************************
	 */
	
	protected List<HistoricalData> getHistoricalData(String instrument, Date fromDate, Date toDate){
		System.out.println("getHistoricalData() 1 invoked");
	    try {
			return kiteConnect.getHistoricalData(fromDate, toDate, historicalDataService.getInstrumentToken(instrument), "minute", false, true).dataArrayList;
		} catch (JSONException | IOException e) {
			LOGGER.error("An error occurred: " + e.getMessage());
			LOGGER.info("Stack Trace : "+e);
//			e.printStackTrace();
		} catch (KiteException e) {
			LOGGER.error("An error occurred: " + e.getMessage());
			LOGGER.error("Stack trace: ", e);
//			e.printStackTrace();
			return getHistoricalData(instrument, fromDate, toDate);
			
		}
	    return null;
	}
	
	//We need only historical data of index for current minute in case of local testing
	protected void codeToExecuteAfterEveryOneMinuteForLiveMarket(boolean isPE, String strike) {
		try {
			List<HistoricalData> historicalData = null;
			Date toDate2 = new Date();
	        toDate2.setSeconds(00);
	        
	        Date fromDate2 = new Date();
	        fromDate2.setMinutes(fromDate2.getMinutes()-1);
	        fromDate2.setSeconds(00);  
			while(ObjectUtils.isEmpty(historicalData)) {
				if(propertyReader.getIndex().equalsIgnoreCase("NIFTY")) {
//					historicalData = getHistoricalData(getIndex()+"50", LastSecondCandleData.getLastSecondTime(10),LastSecondCandleData.getLastSecondTime(9));						
					historicalData = getHistoricalData(propertyReader.getIndex()+"50", fromDate2,toDate2);
				}else {
//					historicalData = getHistoricalData(getIndex(), LastSecondCandleData.getLastSecondTime(10),LastSecondCandleData.getLastSecondTime(9));
					historicalData = getHistoricalData(propertyReader.getIndex(), fromDate2,toDate2);
				}
			}				
			currentIndexHistoricalData= historicalData.get(0);
			setTradingSymbol(propertyReader.getIndex(), currentIndexHistoricalData.close, isPE, strike,LocalDate.now(), LocalTime.now());
			LOGGER.info(propertyReader.getIndex()+" 1234 currentIndexHistoricalData -> time : "+currentIndexHistoricalData.timeStamp+", close : "+currentIndexHistoricalData.close+", TradingSymbol -> "+tradingSymbol);
		} catch (Exception e) {
			LOGGER.error("An error occurred: " + e.getMessage());
			LOGGER.error("Stack trace: ", e);
		}
		LOGGER.info(getTradingSymbol());
		LOGGER.info("----------    Execution Done of codeToExecuteAfterEveryOneMinuteForLiveMarket ----------- -");
	}
	
	protected List<HistoricalData> getHistoricalCandle() {
        Date toDate2 = new Date();
        toDate2.setSeconds(00);
        Date fromDate2 = new Date();
        fromDate2.setMinutes(fromDate2.getMinutes()-propertyReader.getNoOfHistoricalCandle()+1);
        fromDate2.setSeconds(00);        
//      LOGGER.info("getCandleForEmaCalculation :: fromDate2 : "+fromDate2+", toDate2 : "+toDate2 +", historicalData : "+historicalData.timeStamp);
		try {
			return historicalDataUtils.getHistoricalData(tradingSymbol, fromDate2, toDate2);
		} catch (KiteException  e) {
			LOGGER.info("An error occured : "+e.message);
			LOGGER.info("Stack Trace : "+e);
//			e.printStackTrace();
		}catch (Exception e) {
			LOGGER.error("An error occurred: " + e.getMessage());
			LOGGER.info("Stack Trace : "+e);
//			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	protected void goToSleepState(int minute) {
		LOGGER.info("Going to Sleep for "+minute+" minute");
		try {
			Thread.sleep(minute*60*1000);
		} catch (InterruptedException e) {
			LOGGER.error("An error occurred: " + e.getMessage());
			LOGGER.error("Stack trace: ", e);
//			e.printStackTrace();
		}
	}
	
	// we might put it in another new class
	protected void setDataToMainDataModelAtTradeEntryTime() {
		strategyMainDataModel.setSystemUser(SYSTEM_USER);
		strategyMainDataModel.setStrategyName(this.getClass().getSimpleName());		
		strategyMainDataModel.setTimeFrame(propertyReader.getInterval());
		strategyMainDataModel.setFastLane(propertyReader.getFastLane()+"");
		strategyMainDataModel.setSlowLane(propertyReader.getSlowLane()+"");
		strategyMainDataModel.setWeekDay(CURRENT_DAY_SHORT);
		
		strategyMainDataModel.setTradingSymbol(getTradingSymbol());
		strategyMainDataModel.setTradeMode(supportingAttributes.isLocalTestingMode());
		strategyMainDataModel.setCe_pe(getTradingSymbol().substring(tradingSymbol.length()-2, tradingSymbol.length()));
		strategyMainDataModel.setItm_atm_otm("AUTO"); // this will be auto for now
	//	strategyMainDataModel.setBuyOrderPlaceTime(null);
		strategyMainDataModel.setBuyOrderCalaulatedPrice(tradeBuyPrice+"");
		//strategyMainDataModel.setStoplossPrice(trailingStopLossPrice+"");
//		strategyMainDataModel.setTrailingStopLossPrice(trailingStopLossPrice);
		//strategyMainDataModel.setCandleSustainedAt(candleSustainedAt.toString());
		//strategyMainDataModel.setCrossoverDoneAt(crossoverDoneAt.toString());
		//strategyMainDataModel.setBuySignalGeneratedAt(buySignalGeneratedAt.toString());
		strategyMainDataModel.setTargetPrice(1); 
		strategyMainDataModel.setTargetPercentage("1");
		strategyMainDataModel.setWaitingTimeForBuyOrderCancellation(null);
		strategyMainDataModel.setCandleHighPrice(sustainedCandleHighPrice); 
		strategyMainDataModel.setHighPriceToBeBreakForConfirmation(highPriceToBeBreakForConfirmation); 
		

	}
	
	
}
