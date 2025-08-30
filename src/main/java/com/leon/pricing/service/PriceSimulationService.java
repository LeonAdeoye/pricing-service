package com.leon.pricing.service;

import java.util.List;
import java.util.Map;

public interface PriceSimulationService
{
    void initialize();
    void addUnderlying(String underlyingRIC, double priceMean, double priceVariance);
    void removeUnderlying(String underlyingRIC);
    void suspendAll();
    void suspendUnderlying(String underlyingRIC);
    void awakenAll();
    void awakenUnderlying(String underlyingRIC);
    Map<String, Double> getCurrentPrices();
    Double getCurrentPrice(String underlyingRIC);
    List<String> getUnderlyingRICs();
    void terminate();
}
