package com.leon.pricing.service;

import com.leon.pricing.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Service
public class OptionPricingServiceImpl implements OptionPricingService
{
    private static final Logger logger = LoggerFactory.getLogger(OptionPricingServiceImpl.class);
    
    @Autowired
    private EuropeanBlackScholesModel europeanBlackScholesModel;
    
    @Autowired
    private MonteCarloOptionModel monteCarloOptionModel;
    
    @Autowired
    private BinomialTreeOptionModel binomialTreeOptionModel;
    
    @Autowired
    private AmericanBlackScholesModel americanBlackScholesModel;
    
    @Autowired
    private PerformanceTrackingService performanceTrackingService;
    
    @Autowired
    @Qualifier("rangeCalculationExecutor")
    private Executor rangeCalculationExecutor;

    @Value("${log.single.calculation}")
    private boolean logSingleCalculation;

    @Value("${log.range.calculations}")
    private boolean logRangeCalculations;


    private OptionModel getOptionModel(String modelType)
    {
        if (modelType == null || modelType.trim().isEmpty())
            return europeanBlackScholesModel; // Default model
        
        switch (modelType.toLowerCase())
        {
            case "european":
            case "european_black_scholes":
            case "black_scholes":
                return europeanBlackScholesModel;
            case "monte_carlo":
            case "monte_carlo_simulation":
                return monteCarloOptionModel;
            case "binomial":
            case "binomial_tree":
                return binomialTreeOptionModel;
            case "american":
            case "american_black_scholes":
                return americanBlackScholesModel;
            default:
                logger.warn("Unknown model type: {}, using default European Black-Scholes model", modelType);
                return europeanBlackScholesModel;
        }
    }
    
    @Override
    public OptionPriceResult calculateOptionPrice(OptionPricingRequest request)
    {
        validateRequest(request);
        OptionModel model = getOptionModel(request.getModelType());
        model.setToCall(request.getIsCall());
        model.setToEuropean(request.getIsEuropean());
        Map<String, Double> input = createInputMap(request);
        OptionPriceResult result = model.calculate(input, logSingleCalculation);
        logger.info("Option price calculation completed using {}: {}", model.getClass().getSimpleName(), result);
        return result;
    }
    
    @Override
    public OptionPriceResultSet calculateRange(OptionPricingRequest baseRequest, String rangeKey, double startValue, double endValue, double increment)
    {
        long startTime = System.currentTimeMillis();
        logger.info("Calculating range for {} from {} to {} with increment {}", rangeKey, startValue, endValue, increment);
        
        try
        {
            validateRequest(baseRequest);
            validateRangeParameters(rangeKey, startValue, endValue, increment);
            OptionModel model = getOptionModel(baseRequest.getModelType());
            model.setToCall(baseRequest.getIsCall());
            model.setToEuropean(baseRequest.getIsEuropean());
            Map<String, Double> input = createInputMap(baseRequest);
            OptionPriceResultSet resultSet = new OptionPriceResultSet();
            model.calculateRange(resultSet, input, rangeKey, startValue, endValue, increment, logRangeCalculations);
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("Range calculation completed using {} with {} results in {}ms", model.getClass().getSimpleName(), resultSet.getTotalCount(), executionTime);
            performanceTrackingService.recordRangeCalculation(baseRequest.getModelType(), executionTime);
            return resultSet;
        }
        catch (Exception e)
        {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Range calculation failed after {}ms: {}", executionTime, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public OptionPriceResultSet calculateRange(RangeCalculationRequest request)
    {
        return calculateRange(request.getBaseRequest(), request.getRangeKey(), request.getStartValue(), request.getEndValue(), request.getIncrement());
    }
    
    @Override
    public String getModelDetails()
    {
        return "Available Models: European Black-Scholes, Monte Carlo Simulation, Binomial Tree, American Black-Scholes";
    }
    
    private void validateRequest(OptionPricingRequest request)
    {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null");
        
        if (request.getStrike() == null || request.getStrike() <= 0)
            throw new IllegalArgumentException("Strike price must be greater than 0");
        
        if (request.getVolatility() == null || request.getVolatility() <= 0)
            throw new IllegalArgumentException("Volatility must be greater than 0");
        
        if (request.getUnderlyingPrice() == null || request.getUnderlyingPrice() <= 0)
            throw new IllegalArgumentException("Underlying price must be greater than 0");
        
        if (request.getDaysToExpiry() == null || request.getDaysToExpiry() < 0)
            throw new IllegalArgumentException("Days to expiry must be non-negative");
        
        if (request.getDayCountConvention() == null || request.getDayCountConvention() <= 0)
            throw new IllegalArgumentException("Day count convention must be greater than 0");
        
        if (request.getIsCall() == null)
            throw new IllegalArgumentException("Is call flag cannot be null");
        
        if (request.getIsEuropean() == null)
            throw new IllegalArgumentException("Is European flag cannot be null");
    }
    
    private void validateRangeParameters(String rangeKey, double startValue, double endValue, double increment)
    {
        if (rangeKey == null || rangeKey.trim().isEmpty())
            throw new IllegalArgumentException("Range key cannot be null or empty");
        
        if (increment <= 0)
            throw new IllegalArgumentException("Increment must be greater than 0");
        
        if (startValue >= endValue)
            throw new IllegalArgumentException("Start value must be less than end value");
    }
    
    private Map<String, Double> createInputMap(OptionPricingRequest request)
    {
        Map<String, Double> input = new HashMap<>();
        input.put(OptionModel.STRIKE, request.getStrike());
        input.put(OptionModel.VOLATILITY, request.getVolatility());
        input.put(OptionModel.UNDERLYING_PRICE, request.getUnderlyingPrice());
        input.put(OptionModel.TIME_TO_EXPIRY, request.getTimeToExpiryInYears());
        input.put(OptionModel.INTEREST_RATE, request.getInterestRate());
        return input;
    }
}
