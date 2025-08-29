package com.leon.pricing.service.impl;

import com.leon.pricing.model.OptionPriceResult;
import com.leon.pricing.model.OptionPriceResultSet;
import com.leon.pricing.model.OptionPricingRequest;
import com.leon.pricing.model.RangeCalculationRequest;
import com.leon.pricing.model.BlackScholesModel;
import com.leon.pricing.service.OptionPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OptionPricingServiceImpl implements OptionPricingService {
    
    private static final Logger logger = LoggerFactory.getLogger(OptionPricingServiceImpl.class);
    
    private final BlackScholesModel blackScholesModel;
    
    @Autowired
    public OptionPricingServiceImpl(BlackScholesModel blackScholesModel) {
        this.blackScholesModel = blackScholesModel;
    }
    
    @Override
    public OptionPriceResult calculateOptionPrice(OptionPricingRequest request) {
        logger.info("Calculating option price for request: {}", request);
        
        validateRequest(request);
        
        // Set model parameters
        blackScholesModel.setToCall(request.getIsCall());
        blackScholesModel.setToEuropean(request.getIsEuropean());
        
        // Create input map for the model
        Map<String, Double> input = createInputMap(request);
        
        // Calculate option price
        OptionPriceResult result = blackScholesModel.calculate(input);
        
        logger.info("Option price calculation completed: {}", result);
        return result;
    }
    
    @Override
    public OptionPriceResultSet calculateRange(OptionPricingRequest baseRequest, String rangeKey, 
                                            double startValue, double endValue, double increment) {
        logger.info("Calculating range for {} from {} to {} with increment {}", rangeKey, startValue, endValue, increment);
        
        validateRequest(baseRequest);
        validateRangeParameters(rangeKey, startValue, endValue, increment);
        
        // Set model parameters
        blackScholesModel.setToCall(baseRequest.getIsCall());
        blackScholesModel.setToEuropean(baseRequest.getIsEuropean());
        
        // Create input map for the model
        Map<String, Double> input = createInputMap(baseRequest);
        
        // Calculate range
        OptionPriceResultSet resultSet = new OptionPriceResultSet();
        blackScholesModel.calculateRange(resultSet, input, rangeKey, startValue, endValue, increment);
        
        logger.info("Range calculation completed with {} results", resultSet.getTotalCount());
        return resultSet;
    }
    
    @Override
    public OptionPriceResultSet calculateRange(RangeCalculationRequest request) {
        return calculateRange(request.getBaseRequest(), request.getRangeKey(), 
                           request.getStartValue(), request.getEndValue(), request.getIncrement());
    }
    
    @Override
    public String getModelDetails() {
        return "Black-Scholes Model - European options only. Supports both call and put options.";
    }
    
    private void validateRequest(OptionPricingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getStrike() == null || request.getStrike() <= 0) {
            throw new IllegalArgumentException("Strike price must be greater than 0");
        }
        
        if (request.getVolatility() == null || request.getVolatility() <= 0) {
            throw new IllegalArgumentException("Volatility must be greater than 0");
        }
        
        if (request.getUnderlyingPrice() == null || request.getUnderlyingPrice() <= 0) {
            throw new IllegalArgumentException("Underlying price must be greater than 0");
        }
        
        if (request.getDaysToExpiry() == null || request.getDaysToExpiry() < 0) {
            throw new IllegalArgumentException("Days to expiry must be non-negative");
        }
        
        if (request.getDayCountConvention() == null || request.getDayCountConvention() <= 0) {
            throw new IllegalArgumentException("Day count convention must be greater than 0");
        }
        
        if (request.getIsCall() == null) {
            throw new IllegalArgumentException("Is call flag cannot be null");
        }
        
        if (request.getIsEuropean() == null) {
            throw new IllegalArgumentException("Is European flag cannot be null");
        }
        
        if (!request.getIsEuropean()) {
            throw new IllegalArgumentException("Only European options are supported");
        }
    }
    
    private void validateRangeParameters(String rangeKey, double startValue, double endValue, double increment) {
        if (rangeKey == null || rangeKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Range key cannot be null or empty");
        }
        
        if (increment <= 0) {
            throw new IllegalArgumentException("Increment must be greater than 0");
        }
        
        if (startValue >= endValue) {
            throw new IllegalArgumentException("Start value must be less than end value");
        }
    }
    
    private Map<String, Double> createInputMap(OptionPricingRequest request) {
        Map<String, Double> input = new HashMap<>();
        input.put(BlackScholesModel.STRIKE, request.getStrike());
        input.put(BlackScholesModel.VOLATILITY, request.getVolatility());
        input.put(BlackScholesModel.UNDERLYING_PRICE, request.getUnderlyingPrice());
        input.put(BlackScholesModel.TIME_TO_EXPIRY, request.getTimeToExpiryInYears());
        input.put(BlackScholesModel.INTEREST_RATE, request.getInterestRate());
        return input;
    }
}
