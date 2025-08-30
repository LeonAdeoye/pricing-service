package com.leon.pricing.service;

import com.leon.pricing.model.OptionPriceResult;
import com.leon.pricing.model.OptionPriceResultSet;
import com.leon.pricing.model.OptionPricingRequest;
import com.leon.pricing.model.RangeCalculationRequest;

public interface OptionPricingService
{
    OptionPriceResult calculateOptionPrice(OptionPricingRequest request);
    OptionPriceResultSet calculateRange(OptionPricingRequest baseRequest, String rangeKey, double startValue, double endValue, double increment);
    OptionPriceResultSet calculateRange(RangeCalculationRequest request);
    String getModelDetails();
}
