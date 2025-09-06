package com.leon.pricing.model;

import java.util.Map;

public interface OptionModel
{
    String STRIKE = "STRIKE";
    String VOLATILITY = "VOLATILITY";
    String UNDERLYING_PRICE = "UNDERLYING_PRICE";
    String TIME_TO_EXPIRY = "TIME_TO_EXPIRY";
    String INTEREST_RATE = "INTEREST_RATE";

    void setToCall(boolean isCallOption);
    void setToEuropean(boolean isEuropeanOption);
    OptionPriceResult calculate(Map<String, Double> input, boolean logCalculation);
    void calculateRange(OptionPriceResultSet optionPriceResultSet, Map<String, Double> input, String rangeKey, double startValue, double endValue, double increment, boolean logCalculation);
    String getModelDetails();
}
