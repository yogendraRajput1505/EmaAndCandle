package com.infinan.ema.candle.service.strategy.sell;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infinan.common.model.StrategyMainDataModel;
import com.infinan.common.model.StrategySupportingAttributes;
import com.infinan.common.model.StrategySupportingMethods;
import com.infinan.common.service.CustomBuySellService;
import com.infinan.common.service.StockUtils;
import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;
import com.infinan.ema.candle.repo.EmaAndCandleOptionStrategyTradeDataRepository;
import com.infinan.ema.candle.service.StrategyMainDataModelService;
import com.infinan.ema.candle.utils.PropertyReader;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;

@Service
public class EmaAndCandleOptionStrategyMainSell {
	protected static final Logger LOGGER = Logger.getLogger("EmaAndCandleOptionStrategyMainSell");
	
	@Autowired
	PropertyReader propertyReader;
	@Autowired
	CustomBuySellService customBuySellService;
	
	@Autowired 
	StrategySupportingAttributes<EmaAndCandleOptionStrategyTradeData> supportingAttributes;

	@Autowired
	EmaAndCandleOptionStrategyTradeDataRepository emaAndCandleOptionStrategyTradeDataRepository;
	
	@Autowired
	StrategyMainDataModelService strategyMainDataModelService; 
	
	@Autowired
	StrategySupportingMethods strategySupportingMethods;
	
	protected int DO_MAX_DROP_IN_PROFIT_CHECK_AFTER = 50; // put in prop file
	
	protected Map<String,Integer> alerts = new HashMap<String, Integer>();
	protected static final String ALERT = "ALERT";
	protected static final String MID_ALERT = "MID_ALERT";
	protected static final String RED_CANDLE = "RED_CANDLE";
	protected static final String VERY_HIGH_ALERT = "VERY_HIGH_ALERT";
	protected String exitReason="";
	protected LinkedHashMap<String,Boolean> noAlertRaisedCandleCount = new LinkedHashMap<>();
	protected double highestProfitPercentageOfTrade=0;
	protected String alertRaisedTime = "";
	protected String midAlertRaisedTime = "";
	protected String previousCandleTime="";
	protected int redCandleInRow=0;
	protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");	
	protected double maxDropInProfit = 0;
	protected String orderId = "";
//	protected double testingTargetPrice =0;
//	List <EmaAndCandleOptionStrategyTradeData> tradesList = new ArrayList<>(); - make this as local variable
	
	StrategyMainDataModel strategyMainDataModel;

	public boolean handleTradeExit(HistoricalData currentCandleHistoricalData, List<HistoricalData> pastCandle,
				StrategyMainDataModel strategyMainDataModel,List<Order> orders) {
		this.strategyMainDataModel=strategyMainDataModel;
		if(orders.isEmpty()) {
			return true;
		}
		//This if block is important, the RED_CANDLE was not resetting when orders was changed. So this condition was retten to reset that attribute when order changed
		if(!orderId.equals(orders.get(0).orderId)) {
			orderId = orders.get(0).orderId;
			redCandleInRow = 0;
			alerts.put(RED_CANDLE,0);
		}
		boolean exitFromTrade = exitFromTrade(currentCandleHistoricalData,pastCandle.get(pastCandle.size()-2),strategyMainDataModel);
		if(supportingAttributes.isLocalTestingMode() || supportingAttributes.isLiveTestingMode()) {
			if(exitFromTrade) {
				strategyMainDataModel.setSellOrderExecutionTime(currentCandleHistoricalData.timeStamp);
				strategyMainDataModel.setSellOrderPlaceTime(currentCandleHistoricalData.timeStamp);
				//LOGGER.info("Testing Sell Order Placed : getTradingSymbol: "+getTradingSymbol() +", tradeBuyPrice: "+tradeBuyPrice+", testingTargetPrice: "+testingTargetPrice+", exitPrice: "+currentCandleHistoricalData.close);
				//LOGGER.info("highestProfitPercentageOfTrade: "+highestProfitPercentageOfTrade);
				
				resetAndSaveDateAtTradeExit(currentCandleHistoricalData.close);
				resetAlerts(true);
				return true;
			}
		}else {
			Map<String, String> openSellOrders = customBuySellService.openOrders(orders,strategyMainDataModel);		
			if(openSellOrders.size()==0) {
				resetAndSaveDateAtTradeExit(currentCandleHistoricalData.close);
				LOGGER.info("No Open orders");
				resetAlerts(true);
				return true;
			}
			else if(exitFromTrade && !orders.isEmpty()) {			
				modifyOpenOrders(currentCandleHistoricalData.close, openSellOrders);
			}
		}
		return false;
	}
	
	
	/**
	 * Take low of sustained candle and validate if that low can be used for exit. This low and getStoplossPercentage both can be used for immediate exit
	 * @param currentCandleHistoricalData
	 * @param previousCandlehistoricalData
	 * @return
	 */
	public boolean exitFromTrade(HistoricalData currentCandleHistoricalData,HistoricalData previousCandlehistoricalData, StrategyMainDataModel strategyMainDataModel){		
		//Need to check why this block is not executing - - we might not need next candle volume
		if(previousCandlehistoricalData.volume != strategyMainDataModel.getVolumeAtSustainedAt() && strategyMainDataModel.getNextCandleVolume()!=0) {
			strategyMainDataModel.setNextCandleVolume(previousCandlehistoricalData.volume);
		}
		setAlerts(currentCandleHistoricalData, previousCandlehistoricalData);
		printAlerts();
		LOGGER.info("currentCandleHistoricalData : "+currentCandleHistoricalData.timeStamp+", previousCandlehistoricalData:"+previousCandlehistoricalData.timeStamp);
		return validateAlerts();
	}
	
	
	protected void resetAlerts(boolean forceReset) {
		if (noAlertRaisedCandleCount.size() >=2 || forceReset) {
			alerts.clear();
			noAlertRaisedCandleCount.clear();
			exitReason="";
		}
	}
	protected void printAlerts() {
		Set<Entry<String, Integer>> entrySet = alerts.entrySet();
		for(Map.Entry<String, Integer> entry : entrySet) {
			LOGGER.info(entry.getKey()+" : "+entry.getValue());
		}
	}
	
	//This should work fine...let's see later
	protected void modifyOpenOrders(double closePrice, Map<String, String> openSellOrders) {
		OrderParams orderParams = new OrderParams();	
		orderParams.price = closePrice;
		customBuySellService.modifyOrders(new ArrayList<>(openSellOrders.keySet()), orderParams, Constants.VARIETY_REGULAR);
		strategyMainDataModel.setSellOrderPriceModified("True");
		LOGGER.info("Exit Order Modified Successfully");
	}
	
	protected boolean validateAlerts() {
		int veryHighAlertCount = alerts.getOrDefault(VERY_HIGH_ALERT, 0);
		int redCandleCount = alerts.getOrDefault(RED_CANDLE, 0);
		int midAlertCount = alerts.getOrDefault(MID_ALERT, 0);
		int alertCount = alerts.getOrDefault(ALERT, 0);

		return (veryHighAlertCount > 0 || redCandleCount >= 3) ||
		       (redCandleCount >= 2 && midAlertCount > 0) ||
		       (midAlertCount > 0 && alertCount > 0 &&  redCandleCount > 0) ||
		       (redCandleCount >= 2 && alertCount >= 2) ||
		       (alertCount > 0 && midAlertCount >= 2);

	}
	
	/**
	 * 
	 * @param currentCandleHistoricalData - data of incomplete candle
	 * @param previousCandlehistoricalData - this will change after every minute
	 */
	private void setAlerts(HistoricalData currentCandleHistoricalData,HistoricalData previousCandlehistoricalData) {
		double profitPercentage = calculateProfitPercentage(currentCandleHistoricalData);
		setHighestProfitPercentageOfTrade(profitPercentage);
		
//		if(profitPercentage>=getTargetPercentage()) {
//			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
//			exitReason = "Target Hit ";
//			LOGGER.info("Exit Reason Target Hit");
//			return;
//		}
		
		//If maxProfit is >50% then we will book profit when profit fall more then MaxDop provided
		//for example - highestProfitPercentageOfTrade = 60, proftPercentage = 49 then total drop is 11%. if this 11% is >= getMaxDropInProfit() then exit ASAP 
		if(highestProfitPercentageOfTrade> DO_MAX_DROP_IN_PROFIT_CHECK_AFTER && highestProfitPercentageOfTrade-profitPercentage >= propertyReader.getMaxDropInProfit() ) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason="HighestProfitPercentage reached "+DO_MAX_DROP_IN_PROFIT_CHECK_AFTER+"%, and there is drop of profit "+propertyReader.getMaxDropInProfit()+"%";
			LOGGER.info("Exit Reason : HighestProfitPercentage >= "+DO_MAX_DROP_IN_PROFIT_CHECK_AFTER+"%, and there is drop of profit "+propertyReader.getMaxDropInProfit()+"%");
			return;
		}
		
		//We might not need that much condition for exit, put some efficient conditions
		if(highestProfitPercentageOfTrade>35 && profitPercentage <25) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason = "highestProfitPercentageOfTrade>35 && profitPercentage <25 ";
			LOGGER.info("Exit Reason highestProfitPercentageOfTrade>35 && profitPercentage <25");
			return;
		}
		
		if(highestProfitPercentageOfTrade>25 && profitPercentage <15) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason = "highestProfitPercentageOfTrade>25 && profitPercentage <15 ";
			LOGGER.info("Exit Reason highestProfitPercentageOfTrade>25 && profitPercentage <15");
			return;
		}
		
		if(highestProfitPercentageOfTrade>13 && profitPercentage <5) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason = "highestProfitPercentageOfTrade>15 && profitPercentage <5 ";
			LOGGER.info("Exit Reason highestProfitPercentageOfTrade>15 && profitPercentage <5");
			return;
		}
		
		if(profitPercentage <= 5) {
			OffsetDateTime currentDate = OffsetDateTime.parse(currentCandleHistoricalData.timeStamp, formatter);
			LOGGER.info(strategyMainDataModel.getBuyOrderExecutionTime());
			OffsetDateTime buyOrderExecutionTime = OffsetDateTime.parse(strategyMainDataModel.getBuyOrderExecutionTime(),formatter);
			if(Duration.between(buyOrderExecutionTime , currentDate).toSeconds() > 90) {
				alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
				exitReason = "profitPercentage <= 5 && Duration.between(suatainedDate , currentDate).toSeconds() > 90 ";
				LOGGER.info("Exit Reason : profitPercentage <= 5 && Duration.between(BuyOrderPlaceTime , currentDate).toSeconds() > 90");
				return;	
			}
		}
		
		if(profitPercentage < -supportingAttributes.getStoplossPercentage()) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason = "SL HIT : profitPercentage < "+supportingAttributes.getStoplossPercentage();
			LOGGER.info("SL HIT : profitPercentage < "+supportingAttributes.getStoplossPercentage());
			return;
		}
		
		boolean alertRaised = false;
		alertRaised = alertsAlert(currentCandleHistoricalData, previousCandlehistoricalData, alertRaised);
		alertRaised = alertsMidAlert(currentCandleHistoricalData, previousCandlehistoricalData, alertRaised);
		alertRaised = alertsRedCandle(previousCandlehistoricalData, alertRaised);
		
		noAlertRaisedCandleCount(previousCandlehistoricalData, alertRaised);
		resetAlerts(false);
		
		if(redCandleInRow>=3) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason = "redCandleInRow>=3";
			LOGGER.info("Exit Reason redCandleInRow>=3");
			return;
		}
		
		if(supportingAttributes.getFastLaneMA()<=supportingAttributes.getSlowLaneMA()) {
			alerts.put(VERY_HIGH_ALERT, alerts.getOrDefault(VERY_HIGH_ALERT,0)+1);
			exitReason = "fastLaneMA<=slowLaneMA";
			LOGGER.info("Exit Reason fastLaneMA<=slowLaneMA");
		}
	}
	
	/**
	 * But using alertRaisedTime, only one Alert will be generated for one candle;
	 * Using previousCandlehistoricalData for time comparison because for currentCandleHistoricalData time will be different because of seconds
	 * @param currentCandleHistoricalData
	 * @param alertRaised
	 * @return alertRaised
	 */
	//Looks like we don't need this alert
	protected boolean alertsAlert(HistoricalData currentCandleHistoricalData, HistoricalData previousCandlehistoricalData, boolean alertRaised) {
		if(currentCandleHistoricalData.low <= supportingAttributes.getSlowLaneMA() && !alertRaisedTime.equals(previousCandlehistoricalData.timeStamp)) {
			alerts.put(ALERT, alerts.getOrDefault(ALERT,0)+1);
			alertRaised = true;
			exitReason = exitReason+"||Alert";
			alertRaisedTime = previousCandlehistoricalData.timeStamp;
		}
		return alertRaised;
	}
	
	protected boolean alertsMidAlert(HistoricalData currentCandleHistoricalData, HistoricalData previousCandlehistoricalData, boolean alertRaised) {
		if(currentCandleHistoricalData.low <previousCandlehistoricalData.low  && !midAlertRaisedTime.equals(previousCandlehistoricalData.timeStamp)) {
			alerts.put(MID_ALERT, alerts.getOrDefault(MID_ALERT,0)+1);
			alertRaised = true;
			exitReason = exitReason+"||MidAlert";
			midAlertRaisedTime = previousCandlehistoricalData.timeStamp;
		}
		return alertRaised;
	}
	
	protected boolean alertsRedCandle(HistoricalData previousCandlehistoricalData, boolean alertRaised) {
		if(!previousCandleTime.equals(previousCandlehistoricalData.timeStamp)) {
			if(StockUtils.isRedCandle(previousCandlehistoricalData)){
				alerts.put(RED_CANDLE, alerts.getOrDefault(RED_CANDLE,0)+1);
				alertRaised = true;
				redCandleInRow++;
				exitReason = exitReason+"||RedAlert"+redCandleInRow;
			}else {
				redCandleInRow=0;
				alerts.put(RED_CANDLE,0);
			}
			previousCandleTime = previousCandlehistoricalData.timeStamp;
		}
		return alertRaised;
	}
	
	protected double calculateProfitPercentage(HistoricalData currentCandleHistoricalData) {
		return ((currentCandleHistoricalData.close - strategyMainDataModel.getBuyOrderRealPrice()) / strategyMainDataModel.getBuyOrderRealPrice()) * 100;
	}
	
	private void noAlertRaisedCandleCount(HistoricalData previousCandlehistoricalData, boolean alertRaised) {
		if(noAlertRaisedCandleCount.getOrDefault(previousCandlehistoricalData.timeStamp, false)) {
			alertRaised = true;
		}
		noAlertRaisedCandleCount.put(previousCandlehistoricalData.timeStamp, alertRaised);

		if(noAlertRaisedCandleCount.size()>1) {
			Object[] keysArray = noAlertRaisedCandleCount.keySet().toArray();
			boolean removeRest = false;
	        for (int i = keysArray.length - 1; i >= 0; i--) {
	        	String key = (String) keysArray[i];
	        	if(noAlertRaisedCandleCount.get(key)) {
	        		removeRest = true;
	        	}
	        	if(removeRest) {
	        		noAlertRaisedCandleCount.remove(key);
	        	}
	        }
		}
	}
	
	protected void setHighestProfitPercentageOfTrade(double profitPercentage) {
		if(profitPercentage > highestProfitPercentageOfTrade) {
			highestProfitPercentageOfTrade = profitPercentage;
			strategyMainDataModel.setHighestProfitPercentageOfTrade(profitPercentage);
		}
	}
	
	// reframe this method, some method will be put in another class and we might not need to use isTradeRunning, takeTradEntry, buySignal etc
	protected void resetAndSaveDateAtTradeExit(double exitPrice) {
		setDataToMainDataModelAtTradeExitTime(exitPrice);
		EmaAndCandleOptionStrategyTradeData convertDataModelToEntity = strategyMainDataModelService.convertDataModelToEntity(strategyMainDataModel);
		if(supportingAttributes.isLocalTestingMode()){
			supportingAttributes.getTradesList().add(convertDataModelToEntity);
		}else {
			emaAndCandleOptionStrategyTradeDataRepository.save(convertDataModelToEntity);
		}
		LOGGER.info(convertDataModelToEntity.toString());
		highestProfitPercentageOfTrade=Integer.MIN_VALUE;
		strategyMainDataModel = new StrategyMainDataModel();
		LOGGER.info("Data saved in DB");
	}
	
	
	// we might put it in another new class
	protected void setDataToMainDataModelAtTradeExitTime(double exitPrice) {
		strategyMainDataModel.setStoplossPercentage(supportingAttributes.getStoplossPercentage());//need to calculate
		strategyMainDataModel.setTradeTotalTimeTaken(null);//need to check
		strategyMainDataModel.setMaxPriceDropPercentage(null);//need to check and implement
		strategyMainDataModel.setAlertRaisedTime(null);//Not required now
		strategyMainDataModel.setMidAlertRaisedTime(null);//not required now
		strategyMainDataModel.setRedCandleInRow(redCandleInRow);//not required now
		strategyMainDataModel.setHighestProfitPercentageOfTrade(highestProfitPercentageOfTrade); 
		strategyMainDataModel.setMaxDropInProfit(maxDropInProfit); 
		strategyMainDataModel.setDoMaxDropInProfitCheckAfter(DO_MAX_DROP_IN_PROFIT_CHECK_AFTER);
		strategyMainDataModel.setExitReason(exitReason);
		strategyMainDataModel.setSellOrderCalaulatedPrice(exitPrice+"");
		String additionalNotes = "crossOverTime="+supportingAttributes.getCrossoverTime();
		additionalNotes += ":: SL="+supportingAttributes.getStoplossPercentage()+"%";
		additionalNotes += ":: volume="+supportingAttributes.getVolumeForCondition();
		additionalNotes += ":: Duration.between(candleSustainedAt, date2).toMinutes()>4";
		strategyMainDataModel.setAdditionalNotes(additionalNotes);
		
		if(supportingAttributes.isLocalTestingMode() || supportingAttributes.isLiveTestingMode()) {
			strategyMainDataModel.setSellOrderRealPrice(exitPrice+"");
			strategyMainDataModel.setProfitAmount(strategySupportingMethods.getProfitAmount(strategyMainDataModel));
			strategyMainDataModel.setProfitPercentage(strategySupportingMethods.getProfitPercentage(strategyMainDataModel));
		}else {
			strategyMainDataModel.setProfitAmount(strategySupportingMethods.getLiveProfitAmount(strategyMainDataModel));
			strategyMainDataModel.setProfitPercentage(strategySupportingMethods.getLiveProfitPercentage(strategyMainDataModel));
			strategyMainDataModel.setOrderId("Market_"+strategyMainDataModel.getBuySignalGeneratedAt()+"_"+strategyMainDataModel.getCandleSustainedAt());
		}
		
		if(supportingAttributes.isLocalTestingMode()) {
			strategyMainDataModel.setOrderId(strategyMainDataModel.getBuySignalGeneratedAt()+"_"+strategyMainDataModel.getCandleSustainedAt()+"_Local_Testing_3"+propertyReader.getTargetPercentage()+"_"+supportingAttributes.getStoplossPercentage()+"_"+supportingAttributes.getCrossoverTime()+"_"+supportingAttributes.getVolumeForCondition()+"");
		}
		if (supportingAttributes.isLiveTestingMode()) {
			strategyMainDataModel.setOrderId(strategyMainDataModel.getBuySignalGeneratedAt()+"_"+strategyMainDataModel.getCandleSustainedAt()+"_Live_Testing_"+propertyReader.getTargetPercentage()+"_"+supportingAttributes.getStoplossPercentage()+"_"+supportingAttributes.getCrossoverTime()+"_"+supportingAttributes.getVolumeForCondition()+"");
		}
	}
	

}
