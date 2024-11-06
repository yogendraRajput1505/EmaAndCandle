package com.infinan.ema.candle.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.infinan.ema.candle.entity.EmaAndCandleOptionStrategyTradeData;

public interface EmaAndCandleOptionStrategyTradeDataRepository extends JpaRepository<EmaAndCandleOptionStrategyTradeData, String>{
	@Query("SELECT DISTINCT h.tradingSymbol FROM TEma_And_Candle_Option_Strategy_TradeData h WHERE h.buyOrderPlaceTime LIKE %:timeStamp% and h.orderId LIKE %:orderId")
    List<String> findDistinctTradingSymbolsByTimeStampLike(String timeStamp, String orderId);

	@Query("SELECT h FROM TEma_And_Candle_Option_Strategy_TradeData h WHERE h.buyOrderPlaceTime LIKE %:timeStamp% and h.tradingSymbol LIKE %:tradingSymbol%")
	List<EmaAndCandleOptionStrategyTradeData> findTradeData(String tradingSymbol, String timeStamp);
	
	@Query("SELECT h FROM TEma_And_Candle_Option_Strategy_TradeData h WHERE h.buyOrderPlaceTime LIKE %:timeStamp% and h.tradingSymbol LIKE %:tradingSymbol% and h.orderId LIKE %:orderId")
	List<EmaAndCandleOptionStrategyTradeData> findTradeData(String tradingSymbol, String timeStamp, String orderId);
}
