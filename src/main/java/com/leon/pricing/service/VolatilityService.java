package com.leon.pricing.service;

import com.leon.pricing.model.Volatility;
import java.util.List;

public interface VolatilityService
{
    List<Volatility> loadVolatilities();
    Volatility updateVolatility(String instrumentCode, Double volatilityPercentage, String lastUpdatedBy);
    Volatility getVolatility(String instrumentCode);
}
