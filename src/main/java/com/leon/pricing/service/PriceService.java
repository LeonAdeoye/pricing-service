package com.leon.pricing.service;

import com.leon.pricing.model.Price;
import java.util.List;

public interface PriceService
{
    List<Price> loadPrices();
    Price updatePrice(String instrumentCode, Double closePrice, Double openPrice, String lastUpdatedBy);
    Price getPrice(String instrumentCode);
}
