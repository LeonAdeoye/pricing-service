package com.leon.pricing.model;

import java.util.Map;

public interface OptionModel
{
    String STRIKE = "strike";
    String VOLATILITY = "volatility";
    String UNDERLYING_PRICE = "underlyingPrice";
    String TIME_TO_EXPIRY = "timeToExpiry";
    String INTEREST_RATE = "interestRate";

    void setToCall(boolean isCallOption);
    void setToEuropean(boolean isEuropeanOption);
    OptionPriceResult calculate(Map<String, Double> input);
    void calculateRange(OptionPriceResultSet optionPriceResultSet, Map<String, Double> input, String rangeKey, double startValue, double endValue, double increment);
    String getModelDetails();
}
