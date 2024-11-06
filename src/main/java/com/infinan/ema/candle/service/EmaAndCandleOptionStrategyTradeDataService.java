package com.infinan.ema.candle.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.infinan.common.entity.TInfinanStrikeMinuteHistoricalData;
import com.infinan.common.model.TradeProfitCalculationModel;
import com.infinan.common.service.HistoricalDataUtils;
import com.infinan.common.service.ReadInfinanHistoricalDataService;
import com.infinan.common.service.StockUtils;
import com.infinan.common.service.TransactionsUtils;
import com.infinan.common.utils.CommonUtils;
import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;
import com.infinan.ema.candle.repo.EmaAndCandleOptionStrategyTradeDataRepository;
import com.zerodhatech.models.HistoricalData;

@Component
@Service
public class EmaAndCandleOptionStrategyTradeDataService {

	@Autowired
	EmaAndCandleOptionStrategyTradeDataRepository tradeDataRepository;
	
	@Autowired
	StockUtils stockUtils;
	
	@Autowired
	TransactionsUtils transactionsUtils;
	
	@Autowired
	ReadInfinanHistoricalDataService readInfinanHistoricalDataService;
	
	@Autowired
	HistoricalDataUtils historicalDataUtils;
	
	public List<String> getDistinctTradingSymbolsByTimeStampLike(String timeStamp, String orderId){
		return tradeDataRepository.findDistinctTradingSymbolsByTimeStampLike(timeStamp,orderId);
	}


	public List<EmaAndCandleOptionStrategyTradeData> getTradeData(String tradingSymbol, String timeStamp) {
		return tradeDataRepository.findTradeData(tradingSymbol,timeStamp);
	}
	
	/**
	 * 
	 * @param tradingSymbol
	 * @param timeStamp
	 * @param orderId
	 * @return List<EmaAndCandleOptionStrategyTradeData> based on given conditions
	 */
	public List<EmaAndCandleOptionStrategyTradeData> getTradeData(String tradingSymbol, String timeStamp, String orderId) {
		if("Live".equals(orderId) || "Market".equals(orderId))
			orderId=orderId+"%";
		return tradeDataRepository.findTradeData(tradingSymbol,timeStamp,orderId)
		.stream()
		.sorted((v1,v2) -> Double.compare(v1.getProfitPercentage(), v2.getProfitPercentage()))
		.collect(Collectors.toList());
	}


	/**
	 * 
	 * @param tradingSymbol
	 * @param timeStamp
	 * @param tradeAmount
	 * @param orderId
	 * @param filterPrice - filter the trades if buy price is lower then given filter price
	 * @return
	 */
	public Map<String, List<TradeProfitCalculationModel>> tradeDataProfitCalculation(String tradingSymbol, String timeStamp,	Integer tradeAmount, String orderId, Integer filterPrice) {
		List<EmaAndCandleOptionStrategyTradeData> tradeData = getTradeData(tradingSymbol, timeStamp, orderId);
		List<TradeProfitCalculationModel> tradeProfitCalculationList = new ArrayList<>(); 
		tradeData.stream()
			.filter(data -> Double.parseDouble(data.getBuyOrderRealPrice()) > filterPrice )
			.filter(data -> {
				return timeFilter(data);
			})
//			.filter(data-> !data.getTradingSymbol().startsWith("MIDCP") && !data.getTradingSymbol().startsWith("SENSEX"))
//			.filter(data-> data.getTradingSymbol().startsWith("NIF"))
			.filter(data -> data.getBuyOrderPlaceTime().startsWith("2024"))
			.filter(data -> !checkCongicutiveRedcandle(data,3))
			.forEach( data -> {
				tradeProfitCalculationList.add(doProfitCalculations(data,tradeAmount));
		});	
		tradeProfitCalculationList.sort((t1,t2)-> {
			LocalTime t1Time = CommonUtils.getOffsetDateTime(t1.getBuyOrderExecutionTime()).toLocalTime();
			LocalTime t2Time = CommonUtils.getOffsetDateTime(t2.getBuyOrderExecutionTime()).toLocalTime();
			if(t1Time.isAfter(t2Time)) {
				return 1;
			}
			return -1;
		});
		Map<String, List<TradeProfitCalculationModel>> map = new TreeMap<>();
		tradeProfitCalculationList.stream().forEach(data -> {
			String date = data.getBuyOrderExecutionTime().substring(0, 10);
			if(map.containsKey(date)) {
				map.get(date).add(data);
			}else {
				List<TradeProfitCalculationModel> list = new ArrayList<>();
				list.add(data);
				map.put(date, list);
			}
		});
		
		printProfitPercentage(map,orderId);
		
		return map;
	}


	private boolean timeFilter(EmaAndCandleOptionStrategyTradeData data) {
		LocalTime localTime = CommonUtils.getOffsetDateTime(data.getBuyOrderExecutionTime()).toLocalTime();
		return localTime.isAfter(LocalTime.of(10, 00)) && localTime.isBefore(LocalTime.of(12, 00));
	}


	private boolean checkCongicutiveRedcandle(EmaAndCandleOptionStrategyTradeData data,int noOfCongicutiveCandles) {
		System.out.println("Checking Congiquitive red candle Logic for : "+data.getTradingSymbol());
        //String dateString = "2024-09-03T11:12:36+0530";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        LocalDateTime date = LocalDateTime.parse(data.getBuyOrderPlaceTime(), formatter);
        LocalDateTime dateBefore = date.minus(10, ChronoUnit.MINUTES);
        dateBefore = dateBefore.withSecond(0);
        LocalDateTime dateAfter = date.plus(3, ChronoUnit.MINUTES);
        dateAfter = dateAfter.withSecond(0);
        String fromDate = dateBefore.toString(); //format(formatter);
        String toDate = dateAfter.toString();//.format(formatter);
//        System.out.println("Date Before (10 minutes): " + fromDate);
//        System.out.println("Date After (3 minutes): " + toDate);
		
        List<TInfinanStrikeMinuteHistoricalData> strikeMinuteHistoricalData = readInfinanHistoricalDataService.getStrikeMinuteHistoricalData(data.getTradingSymbol(),fromDate,toDate);
        List<HistoricalData> historicalDataList = historicalDataUtils.convertInfinanMinuteHistoricalDataListToHistoricalDataList(strikeMinuteHistoricalData, data.getTradingSymbol());
		return StockUtils.checkCongicutiveRedcandle(historicalDataList,noOfCongicutiveCandles);	
	}
	
	private void printProfitPercentage(Map<String, List<TradeProfitCalculationModel>> map, String orderId) {
		System.out.println(orderId);
		map.forEach((key,list) -> {
			Double[] netProfit = {0.0};
			Double[] profit = {0.0};
			list.forEach(data -> {
				profit[0] += data.getProfitPercentage();
				netProfit[0] += data.getNetProfitPercentage();
				
			});
			System.out.println(key+"	"+list.get(0).getTradingSymbol()+"	"+profit[0]+"	"+netProfit[0]);
//			System.out.println(profit[0]+"	"+netProfit[0]);
		});
		
	}


	private TradeProfitCalculationModel doProfitCalculations(EmaAndCandleOptionStrategyTradeData data, int tradeAmount) {
		TradeProfitCalculationModel model = new TradeProfitCalculationModel();
		model.setOrderId(data.getOrderId());
		model.setTradingSymbol(data.getTradingSymbol());
		model.setBuyOrderExecutionTime(data.getBuyOrderExecutionTime());
		model.setBuyOrderRealPrice(Double.parseDouble(data.getBuyOrderRealPrice()));
		model.setSellOrderRealPrice(Double.parseDouble(data.getSellOrderRealPrice()));
		model.setAmountAvailableForTrade(tradeAmount);
		model.setLots(stockUtils.calculateLot(model.getBuyOrderRealPrice(), tradeAmount, model.getTradingSymbol()));
		model.setQuantity(stockUtils.getQuantity(model.getTradingSymbol(), model.getLots()));
		model.setBuyValue(model.getBuyOrderRealPrice()*model.getQuantity());
		model.setProfitAmount((model.getSellOrderRealPrice()-model.getBuyOrderRealPrice())*model.getQuantity());
		model.setProfitPercentage(model.getProfitAmount()*100/model.getBuyValue());
		model.setBrokerage(transactionsUtils.getBrokerageForOneTrade(model.getQuantity(), model.getTradingSymbol()));
		model.setTaxes(transactionsUtils.calculateTaxes(model.getBuyValue(),model.getSellOrderRealPrice()*model.getQuantity(), model.getBrokerage()));
		model.setNetProfit(model.getProfitAmount()-model.getTaxes()-model.getBrokerage());
		model.setNetProfitPercentage(model.getNetProfit()*100/model.getBuyValue());
		return model;
	}
	
}
