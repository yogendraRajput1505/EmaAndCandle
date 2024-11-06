package com.infinan.ema.candle.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class PropertyReader {
	protected final Environment environment;
	
//	private String localTestingMode = "";
//	private String liveTestingMode = "";
	private double maxDropInProfit;
	private String interval;
	private int fastLane;
	private int slowLane;
	private String index;
	private String sellStrategyClass;
	private String buyStrategyClass;
	
	private double targetPercentage;
	private double stopLossDurationInSecond;
	private double stopLossPercentage;
	private int tradeAmount;
	private int lot;
	private double fastLaneGreaterThenSlowLaneCount;
	private double changeBuyPriceby;
	private int tradeExecutionGap;
	
	private int noOfHistoricalCandle;
	
//	private int bankniftyMaxLotCount;
//	private int niftyMaxLotCount;
//	private int midniftyMaxLotCount;
//	private int finniftyMaxLotCount;
//	private int sensexMaxLotCount;
//	private int bankniftyLotSize;
//	private int niftyLotSize;
//	private int midniftyLotSize;
//	private int finniftyLotSize;
//	private int sensexLotSize;
	
	
	
	
	protected PropertyReader(Environment environment) {
        this.environment = environment;
//        localTestingMode = environment.getProperty("Strategy.localTestingMode");
//        liveTestingMode = environment.getProperty("Strategy.liveTestingMode");
//        if(isLocalTestingMode() && isLiveTesingMode()) {
//        	throw new RuntimeException("localTestingMode and liveTestingMode bot can't be set true at same time");
//        }
        maxDropInProfit = Double.parseDouble(environment.getProperty("Strategy.maxDropInProfit"));
        interval = environment.getProperty("Strategy.interval");
        fastLane = Integer.parseInt(environment.getProperty("Strategy.fastLane"));
        slowLane = Integer.parseInt(environment.getProperty("Strategy.slowLane"));
        index = environment.getProperty("Strategy.index");
        sellStrategyClass = environment.getProperty("sellStrategyClassName");
        buyStrategyClass = environment.getProperty("buyStrategyClassName");
        
        
        targetPercentage =  Double.parseDouble(environment.getProperty("Strategy.targetPercentage"));
        stopLossDurationInSecond = Double.parseDouble(environment.getProperty("Strategy.stopLossDurationInSecond"));
        stopLossPercentage = Double.parseDouble(environment.getProperty("Strategy.stopLossPercentage"));
        tradeAmount = Integer.parseInt(environment.getProperty("Strategy.tradeAmount"));
        lot = Integer.parseInt(environment.getProperty("index.lot.size."+getIndex()));
        fastLaneGreaterThenSlowLaneCount = Double.parseDouble(environment.getProperty("Strategy.fastLaneGreaterThenSlowLaneCount"));
        changeBuyPriceby = Double.parseDouble(environment.getProperty("Strategy.changeBuyPriceby"));
        tradeExecutionGap = Integer.parseInt(environment.getProperty("Strategy.tradeExecutionGap"));
        
        noOfHistoricalCandle = Integer.parseInt(environment.getProperty("Strategy.noOfHistoricalCandle"));
        
//        bankniftyMaxLotCount = Integer.parseInt(environment.getProperty("index.lot.max.BANKNIFTY"));
//        niftyMaxLotCount = Integer.parseInt(environment.getProperty("index.lot.max.NIFTY"));
//        midniftyMaxLotCount = Integer.parseInt(environment.getProperty("index.lot.max.MIDCPNIFTY"));
//        finniftyMaxLotCount = Integer.parseInt(environment.getProperty("index.lot.max.FINNIFTY"));
//        sensexMaxLotCount = Integer.parseInt(environment.getProperty("index.lot.max.SENSEX"));
//        bankniftyLotSize = Integer.parseInt(environment.getProperty("index.lot.size.BANKNIFTY"));
//        niftyLotSize = Integer.parseInt(environment.getProperty("index.lot.size.NIFTY"));
//        midniftyLotSize = Integer.parseInt(environment.getProperty("index.lot.size.MIDCPNIFTY"));
//        finniftyLotSize = Integer.parseInt(environment.getProperty("index.lot.size.FINNIFTY"));
//        sensexLotSize = Integer.parseInt(environment.getProperty("index.lot.size.SENSEX"));
	}
	
	public LocalDate getDateToUse() {
		String dateToUse = environment.getProperty("Strategy.dateToUse");
		if("AUTO".equalsIgnoreCase(dateToUse))
			return LocalDate.now();
		return LocalDate.parse(dateToUse);
	}
	
	public List<LocalDate> getDateToUseForLocalTesting() {
		String dateToUse = environment.getProperty("Strategy.local.testing.dateToUse");
		if(!ObjectUtils.isEmpty(dateToUse)) {
			 return Arrays.asList(dateToUse.split(",")).stream().map(e -> LocalDate.parse(e)).collect(Collectors.toList());
		 }
		 return new ArrayList<>();
	}
	
	public List<String> getIndexesForLocalTesting() {
		 String property = environment.getProperty("Strategy.local.testing.indexes");
		 if(!ObjectUtils.isEmpty(property)) {
			 return Arrays.asList(property.split(","));
		 }
		 return new ArrayList<>();
	}
	
//	public boolean isLocalTestingMode() {
//		return "true".equalsIgnoreCase(localTestingMode);
//	}
//	
//	public boolean isLiveTesingMode() {
//		return "true".equalsIgnoreCase(liveTestingMode);
//	}
	
	public double getMaxDropInProfit() {
		return maxDropInProfit;
	}
	
	public String getInterval() {
		return interval;
	}
	public int getFastLane(){
		return fastLane;
	}
	
	public int getSlowLane() {
		return slowLane;
	}
	
	public String getIndex(){
		return index;
	}
		
	public String getSellStrategyClass() {
		return sellStrategyClass;
	}
	
	public String getBuyStrategyClass() {
		return buyStrategyClass;
	}

	public double getTargetPercentage() {
		return targetPercentage;
	}
	
	public double getStoplossDuration() {
		return stopLossDurationInSecond;
	}
	
	public double getStoplossPercentage() {
		return stopLossPercentage;
	}
	
	public int getTradeAmount() {
		return tradeAmount;
	}
	
	public int getLotSize() {
		return lot;
	}
	
	public double getRequiredFastLaneGreaterThenSlowLaneCount() {
		return fastLaneGreaterThenSlowLaneCount;
	}
	
	public double changeBuyPriceBy() {
		return changeBuyPriceby;
	}
	
	public int getTradeExecutionGap() {
		return  tradeExecutionGap;
	}
	
	public int getNoOfHistoricalCandle() {
		return noOfHistoricalCandle;
	}

//	public int getBankniftyMaxLotCount() {
//		return bankniftyMaxLotCount;
//	}
//
//	public int getNiftyMaxLotCount() {
//		return niftyMaxLotCount;
//	}
//
//	public int getMidniftyMaxLotCount() {
//		return midniftyMaxLotCount;
//	}
//
//	public int getFinniftyMaxLotCount() {
//		return finniftyMaxLotCount;
//	}
//
//	public int getSensexMaxLotCount() {
//		return sensexMaxLotCount;
//	}
//
//	public int getBankniftyLotSize() {
//		return bankniftyLotSize;
//	}
//
//	public int getNiftyLotSize() {
//		return niftyLotSize;
//	}
//
//	public int getMidniftyLotSize() {
//		return midniftyLotSize;
//	}
//
//	public int getFinniftyLotSize() {
//		return finniftyLotSize;
//	}
//
//	public int getSensexLotSize() {
//		return sensexLotSize;
//	}
	
}
