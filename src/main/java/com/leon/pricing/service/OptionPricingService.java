package com.leon.pricing.service;

import com.leon.pricing.model.OptionPriceResult;
import com.leon.pricing.model.OptionPriceResultSet;
import com.leon.pricing.model.OptionPricingRequest;
import com.leon.pricing.model.RangeCalculationRequest;

public interface OptionPricingService {
    
    /**
     * Calculate option price and Greeks using the Black-Scholes model
     */
    OptionPriceResult calculateOptionPrice(OptionPricingRequest request);
    
    /**
     * Calculate option prices for a range of values
     */
    OptionPriceResultSet calculateRange(OptionPricingRequest baseRequest, String rangeKey, 
                                      double startValue, double endValue, double increment);
    
    /**
     * Calculate option prices for a range using a range calculation request
     */
    OptionPriceResultSet calculateRange(RangeCalculationRequest request);
    
    /**
     * Get model details
     */
    String getModelDetails();
}
