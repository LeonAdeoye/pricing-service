package com.leon.pricing.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import static java.lang.Math.*;

@Component
public class AmericanBlackScholesModel implements OptionModel
{
    private static final Logger logger = LoggerFactory.getLogger(AmericanBlackScholesModel.class);
    private static final int DEFAULT_ITERATIONS = 1000;
    
    private double adjustedNormalizedDrift = 0.0;
    private double adjustedNormalizedDriftOffsetByVolatility = 0.0;
    private double discountFactor = 0.0;
    private double timeToExpiryInYears = 0.0;
    private boolean isCallOption = true;
    private boolean isEuropeanOption = false;
    private int maxIterations = DEFAULT_ITERATIONS;
    
    @org.springframework.beans.factory.annotation.Autowired
    @Qualifier("rangeCalculationExecutor")
    private Executor rangeCalculationExecutor;
    
    public AmericanBlackScholesModel() {}
    
    public void setMaxIterations(int iterations)
    {
        this.maxIterations = iterations;
    }
    
    @Override
    public void setToCall(boolean isCallOption)
    {
        this.isCallOption = isCallOption;
    }

    @Override
    public void setToEuropean(boolean isEuropeanOption)
    {
        this.isEuropeanOption = isEuropeanOption;
    }

    @Override
    public OptionPriceResult calculate(Map<String, Double> input, boolean logCalculation)
    {
        OptionPriceResult optionResult = new OptionPriceResult();
        try
        {
            double volatility = input.get(VOLATILITY);
            double interestRate = input.get(INTEREST_RATE);
            double strike = input.get(STRIKE);
            double underlyingPrice = input.get(UNDERLYING_PRICE);
            double timeToExpiryInYears = input.get(TIME_TO_EXPIRY);
            double dayCountConvention = input.getOrDefault("DAY_COUNT_CONVENTION", 250.0);

            if(logCalculation)
                logger.info("Calculating American option price using Black-Scholes with early exercise - Volatility: {}, Interest Rate: {}, Strike: {}, Underlying Price: {}, Time to Expiry (years): {}",
                         volatility, interestRate, strike, underlyingPrice, timeToExpiryInYears);

            this.timeToExpiryInYears = timeToExpiryInYears;
            this.adjustedNormalizedDrift = (log(underlyingPrice/strike) + ((interestRate + ((volatility * volatility)/2)) * timeToExpiryInYears)) / (volatility * sqrt(timeToExpiryInYears));
            this.adjustedNormalizedDriftOffsetByVolatility = this.adjustedNormalizedDrift - (volatility * sqrt(timeToExpiryInYears));
            this.discountFactor = exp(-interestRate * timeToExpiryInYears);
            
            // Calculate American option price using Barone-Adesi and Whaley approximation
            double optionPrice = calculateAmericanOptionPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            optionResult.setPrice(optionPrice);
            
            // Calculate Greeks - for American calls, use European Greeks since early exercise is not optimal
            if (isCallOption)
            {
                // For American calls on non-dividend paying stocks, Greeks equal European Greeks
                optionResult.setDelta(calculateEuropeanDelta());
                optionResult.setGamma(calculateEuropeanGamma(underlyingPrice, volatility));
                optionResult.setVega(calculateEuropeanVega(underlyingPrice));
                optionResult.setRho(calculateEuropeanRho(strike, timeToExpiryInYears, interestRate));
                optionResult.setTheta(calculateEuropeanTheta(underlyingPrice, strike, interestRate, volatility, dayCountConvention));
            }
            else
            {
                // For American puts, use finite difference methods
                optionResult.setDelta(calculateAmericanDelta(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears));
                optionResult.setGamma(calculateAmericanGamma(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears));
                optionResult.setVega(calculateAmericanVega(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears));
                optionResult.setRho(calculateAmericanRho(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears));
                optionResult.setTheta(calculateAmericanTheta(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears, dayCountConvention));
            }
            
            return optionResult;
        }
        catch (Exception e)
        {
            logger.error("American Black-Scholes calculation error: {}", e.getMessage());
            throw new RuntimeException("American Black-Scholes calculation error: " + e.getMessage());
        }
    }
    
    @Override
    public void calculateRange(OptionPriceResultSet optionPriceResultSet, Map<String, Double> input, String rangeKey, double startValue, double endValue, double increment, boolean logCalculations)
    {
        try
        {
            int iterations = (int) Math.ceil((endValue - startValue) / increment) + 1;
            List<CompletableFuture<OptionPriceResult>> futures = new ArrayList<>();
            
            for (int i = 0; i < iterations; i++)
            {
                double value = startValue + (i * increment);
                if (value > endValue) break;
                
                final double currentValue = value;
                CompletableFuture<OptionPriceResult> future = CompletableFuture.supplyAsync(() -> 
                {
                    Map<String, Double> inputCopy = new HashMap<>(input);
                    inputCopy.put(rangeKey, currentValue);
                    OptionPriceResult result = calculate(inputCopy,logCalculations);
                    result.setRangeVariable(currentValue);
                    return result;
                }, rangeCalculationExecutor);
                
                futures.add(future);
            }
            
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.thenRun(() -> 
            {
                for (CompletableFuture<OptionPriceResult> future : futures)
                {
                    try
                    {
                        OptionPriceResult result = future.get();
                        optionPriceResultSet.merge(result);
                    }
                    catch (Exception e)
                    {
                        logger.error("Error getting future result: {}", e.getMessage());
                    }
                }
            }).join();
        }
        catch (Exception e)
        {
            logger.error("American Black-Scholes range calculation error: {}", e.getMessage());
            throw new RuntimeException("American Black-Scholes range calculation error: " + e.getMessage());
        }
    }

    private double calculateAmericanOptionPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        if (isCallOption)
            return calculateAmericanCallPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
    
        else
            return calculateAmericanPutPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);

    }
    
    private double calculateAmericanCallPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        // For American calls on non-dividend paying stocks, early exercise is never optimal
        // So American call price equals European call price
        return calculateEuropeanCallPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
    }
    
    private double calculateAmericanPutPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        // Use Barone-Adesi and Whaley approximation for American puts
        double europeanPutPrice = calculateEuropeanPutPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
        
        if (timeToExpiryInYears <= 0)
        {
            return max(strike - underlyingPrice, 0.0);
        }
        
        // No dividends
        double q2 = (1 - 2 * interestRate / (volatility * volatility));
        double q2Inverse = 1.0 / q2;
        double beta = (q2Inverse - 1) + sqrt(pow(q2Inverse - 1, 2) + 8 * interestRate / (volatility * volatility));
        beta = beta / 2.0;
        
        double alpha = -beta * strike / (1 - exp(-interestRate * timeToExpiryInYears));
        double criticalPrice = strike / (1 + 1.0 / beta);
        
        if (underlyingPrice >= criticalPrice)
        {
            return europeanPutPrice;
        }
        else
        {
            double americanAdjustment = alpha * pow(underlyingPrice / strike, beta);
            return europeanPutPrice + americanAdjustment;
        }
    }
    
    private double calculateEuropeanCallPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        return (underlyingPrice * cumulativeNormalDistribution(this.adjustedNormalizedDrift)) - 
               (strike * this.discountFactor * cumulativeNormalDistribution(this.adjustedNormalizedDriftOffsetByVolatility));
    }
    
    private double calculateEuropeanPutPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        return (strike * this.discountFactor * cumulativeNormalDistribution(-this.adjustedNormalizedDriftOffsetByVolatility)) - 
               (underlyingPrice * cumulativeNormalDistribution(-this.adjustedNormalizedDrift));
    }
    
    private double calculateAmericanDelta(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = underlyingPrice * 0.01;
        double priceUp = calculateAmericanOptionPrice(underlyingPrice + epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceDown = calculateAmericanOptionPrice(underlyingPrice - epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateAmericanGamma(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = underlyingPrice * 0.01;
        double priceUp = calculateAmericanOptionPrice(underlyingPrice + epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceDown = calculateAmericanOptionPrice(underlyingPrice - epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceCenter = calculateAmericanOptionPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
        return (priceUp - 2 * priceCenter + priceDown) / (epsilon * epsilon);
    }
    
    private double calculateAmericanVega(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = volatility * 0.01;
        double priceUp = calculateAmericanOptionPrice(underlyingPrice, strike, volatility + epsilon, interestRate, timeToExpiryInYears);
        double priceDown = calculateAmericanOptionPrice(underlyingPrice, strike, volatility - epsilon, interestRate, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateAmericanRho(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = interestRate * 0.01;
        double priceUp = calculateAmericanOptionPrice(underlyingPrice, strike, volatility, interestRate + epsilon, timeToExpiryInYears);
        double priceDown = calculateAmericanOptionPrice(underlyingPrice, strike, volatility, interestRate - epsilon, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateAmericanTheta(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears, double dayCountConvention)
    {
        double epsilon = timeToExpiryInYears * 0.01;
        double priceUp = calculateAmericanOptionPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears + epsilon);
        double priceDown = calculateAmericanOptionPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears - epsilon);
        return (priceUp - priceDown) / (2 * epsilon) / dayCountConvention;
    }
    
    // European Greeks methods for American calls
    private double calculateEuropeanDelta()
    {
        if (this.isCallOption)
            return cumulativeNormalDistribution(this.adjustedNormalizedDrift);
        else
            return cumulativeNormalDistribution(this.adjustedNormalizedDrift) - 1;
    }
    
    private double calculateEuropeanGamma(double underlyingPrice, double volatility)
    {
        return standardNormalProbabilityDensityFunction(this.adjustedNormalizedDrift) / (underlyingPrice * volatility * sqrt(timeToExpiryInYears));
    }
    
    private double calculateEuropeanVega(double underlyingPrice)
    {
        return underlyingPrice * standardNormalProbabilityDensityFunction(this.adjustedNormalizedDrift) * sqrt(timeToExpiryInYears) * 0.01;
    }
    
    private double calculateEuropeanRho(double strike, double timeToExpiryInYears, double interestRate)
    {
        if (this.isCallOption)
            return strike * timeToExpiryInYears * this.discountFactor * cumulativeNormalDistribution(this.adjustedNormalizedDriftOffsetByVolatility) * 0.01;
        else
            return -strike * timeToExpiryInYears * this.discountFactor * cumulativeNormalDistribution(-this.adjustedNormalizedDriftOffsetByVolatility) * 0.01;
    }
    
    private double calculateEuropeanTheta(double underlyingPrice, double strike, double interestRate, double volatility, double dayCountConvention)
    {
        double firstTerm = -(underlyingPrice * standardNormalProbabilityDensityFunction(this.adjustedNormalizedDrift) * volatility) / (2 * sqrt(timeToExpiryInYears));
        double secondTerm;

        if (this.isCallOption)
            secondTerm = -interestRate * strike * this.discountFactor * cumulativeNormalDistribution(this.adjustedNormalizedDriftOffsetByVolatility);
        else
            secondTerm = interestRate * strike * this.discountFactor * cumulativeNormalDistribution(-this.adjustedNormalizedDriftOffsetByVolatility);

        return (firstTerm + secondTerm) / dayCountConvention;
    }
    
    private double standardNormalProbabilityDensityFunction(double x)
    {
        return (1.0 / Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * x * x);
    }

    private double cumulativeNormalDistribution(double input)
    {
        // Coefficients for the Abramowitz and Stegun approximation
        double coefficient1 = 0.254829592;
        double coefficient2 = -0.284496736;
        double coefficient3 = 1.421413741;
        double coefficient4 = -1.453152027;
        double coefficient5 = 1.061405429;
        double scaleFactor = 0.3275911;

        // Determine the sign of the input
        int inputSign = input < 0 ? -1 : 1;

        // Normalize input for approximation
        double normalizedInput = Math.abs(input) / Math.sqrt(2.0);

        // Polynomial approximation term
        double t = 1.0 / (1.0 + scaleFactor * normalizedInput);

        // Approximate the error function
        double errorFunctionApprox = 1.0 - ((((coefficient5 * t + coefficient4) * t + coefficient3) * t + coefficient2) * t + coefficient1) * t * Math.exp(-normalizedInput * normalizedInput);

        // Return the cumulative probability
        return 0.5 * (1.0 + inputSign * errorFunctionApprox);
    }

    @Override
    public String getModelDetails()
    {
        return String.format("American Black-Scholes Model: %s %s options with early exercise capability", 
                isEuropeanOption ? "European" : "American", 
                isCallOption ? "Call" : "Put");
    }

    @Override
    public String toString()
    {
        return String.format("AmericanBlackScholesModel{isCall=%b, isEuropean=%b, maxIterations=%d}", 
                isCallOption, isEuropeanOption, maxIterations);
    }
}
