package com.leon.pricing.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;
import static java.lang.Math.*;

@Component
public class BinomialTreeOptionModel implements OptionModel
{
    private static final Logger logger = LoggerFactory.getLogger(BinomialTreeOptionModel.class);
    private static final int DEFAULT_STEPS = 1000;
    
    private boolean isCallOption = true;
    private boolean isEuropeanOption = true;
    private int numberOfSteps = DEFAULT_STEPS;
    
    public BinomialTreeOptionModel() {}
    
    public void setNumberOfSteps(int steps)
    {
        this.numberOfSteps = steps;
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
    public OptionPriceResult calculate(Map<String, Double> input)
    {
        OptionPriceResult optionResult = new OptionPriceResult();
        try
        {
            double volatility = input.get(VOLATILITY);
            double interestRate = input.get(INTEREST_RATE);
            double strike = input.get(STRIKE);
            double underlyingPrice = input.get(UNDERLYING_PRICE);
            double timeToExpiryInYears = input.get(TIME_TO_EXPIRY);

            logger.info("Calculating option price using Binomial Tree with {} steps - Volatility: {}, Interest Rate: {}, Strike: {}, Underlying Price: {}, Time to Expiry (years): {}",
                         numberOfSteps, volatility, interestRate, strike, underlyingPrice, timeToExpiryInYears);

            // Calculate option price using Binomial Tree
            double optionPrice = calculateBinomialPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            optionResult.setPrice(optionPrice);
            
            // Calculate Greeks using finite difference method
            double delta = calculateDelta(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            double gamma = calculateGamma(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            double vega = calculateVega(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            double rho = calculateRho(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            double theta = calculateTheta(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
            
            optionResult.setDelta(delta);
            optionResult.setGamma(gamma);
            optionResult.setVega(vega);
            optionResult.setRho(rho);
            optionResult.setTheta(theta);
            
            return optionResult;
        }
        catch (Exception e)
        {
            logger.error("Binomial Tree calculation error: {}", e.getMessage());
            throw new RuntimeException("Binomial Tree calculation error: " + e.getMessage());
        }
    }
    
    @Override
    public void calculateRange(OptionPriceResultSet optionPriceResultSet, Map<String, Double> input, String rangeKey, double startValue, double endValue, double increment)
    {
        try
        {
            for (double value = startValue; value <= endValue; value += increment)
            {
                input.put(rangeKey, value);
                OptionPriceResult optionPriceResult = calculate(input);
                logger.info("Range key: {} Input parameters: {} Calculated OptionPriceResult: {}", rangeKey, input, optionPriceResult);
                optionPriceResult.setRangeVariable(value);
                optionPriceResultSet.merge(optionPriceResult);
            }
        }
        catch (Exception e)
        {
            logger.error("Binomial Tree range calculation error: {}", e.getMessage());
            throw new RuntimeException("Binomial Tree range calculation error: " + e.getMessage());
        }
    }

    private double calculateBinomialPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double dt = timeToExpiryInYears / numberOfSteps;
        double u = exp(volatility * sqrt(dt));
        double d = 1.0 / u;
        double p = (exp(interestRate * dt) - d) / (u - d);
        double discountFactor = exp(-interestRate * dt);
        
        // Create arrays to store option values at each node
        double[] optionValues = new double[numberOfSteps + 1];
        
        // Calculate option values at expiration
        for (int i = 0; i <= numberOfSteps; i++)
        {
            double stockPrice = underlyingPrice * pow(u, numberOfSteps - i) * pow(d, i);
            optionValues[i] = calculatePayoff(stockPrice, strike);
        }
        
        // Work backwards through the tree
        for (int step = numberOfSteps - 1; step >= 0; step--)
        {
            for (int i = 0; i <= step; i++)
            {
                double stockPrice = underlyingPrice * pow(u, step - i) * pow(d, i);
                double exerciseValue = calculatePayoff(stockPrice, strike);
                
                if (isEuropeanOption)
                {
                    // European option: only exercise at expiration
                    optionValues[i] = discountFactor * (p * optionValues[i] + (1 - p) * optionValues[i + 1]);
                }
                else
                {
                    // American option: can exercise early
                    double holdValue = discountFactor * (p * optionValues[i] + (1 - p) * optionValues[i + 1]);
                    optionValues[i] = max(exerciseValue, holdValue);
                }
            }
        }
        
        return optionValues[0];
    }
    
    private double calculatePayoff(double stockPrice, double strike)
    {
        if (isCallOption)
        {
            return max(stockPrice - strike, 0.0);
        }
        else
        {
            return max(strike - stockPrice, 0.0);
        }
    }
    
    private double calculateDelta(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = underlyingPrice * 0.01;
        double priceUp = calculateBinomialPrice(underlyingPrice + epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceDown = calculateBinomialPrice(underlyingPrice - epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateGamma(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = underlyingPrice * 0.01;
        double priceUp = calculateBinomialPrice(underlyingPrice + epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceDown = calculateBinomialPrice(underlyingPrice - epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceCenter = calculateBinomialPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
        return (priceUp - 2 * priceCenter + priceDown) / (epsilon * epsilon);
    }
    
    private double calculateVega(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = volatility * 0.01;
        double priceUp = calculateBinomialPrice(underlyingPrice, strike, volatility + epsilon, interestRate, timeToExpiryInYears);
        double priceDown = calculateBinomialPrice(underlyingPrice, strike, volatility - epsilon, interestRate, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateRho(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = interestRate * 0.01;
        double priceUp = calculateBinomialPrice(underlyingPrice, strike, volatility, interestRate + epsilon, timeToExpiryInYears);
        double priceDown = calculateBinomialPrice(underlyingPrice, strike, volatility, interestRate - epsilon, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateTheta(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = timeToExpiryInYears * 0.01;
        double priceUp = calculateBinomialPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears + epsilon);
        double priceDown = calculateBinomialPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears - epsilon);
        return (priceUp - priceDown) / (2 * epsilon);
    }

    @Override
    public String getModelDetails()
    {
        return String.format("Binomial Tree Option Model: %s %s options with %d steps", 
                isEuropeanOption ? "European" : "American", 
                isCallOption ? "Call" : "Put", 
                numberOfSteps);
    }

    @Override
    public String toString()
    {
        return String.format("BinomialTreeOptionModel{isCall=%b, isEuropean=%b, steps=%d}", 
                isCallOption, isEuropeanOption, numberOfSteps);
    }
}
