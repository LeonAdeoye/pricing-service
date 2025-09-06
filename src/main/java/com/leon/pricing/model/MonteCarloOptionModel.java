package com.leon.pricing.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import static java.lang.Math.*;

@Component
public class MonteCarloOptionModel implements OptionModel
{
    private static final Logger logger = LoggerFactory.getLogger(MonteCarloOptionModel.class);
    private static final int DEFAULT_SIMULATIONS = 100000;
    private static final Random random = new Random();
    
    private boolean isCallOption = true;
    private boolean isEuropeanOption = true;
    private int numberOfSimulations = DEFAULT_SIMULATIONS;
    
    @org.springframework.beans.factory.annotation.Autowired
    @Qualifier("rangeCalculationExecutor")
    private Executor rangeCalculationExecutor;
    
    public MonteCarloOptionModel() {}
    
    public void setNumberOfSimulations(int simulations)
    {
        this.numberOfSimulations = simulations;
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

            logger.info("Calculating option price using Monte Carlo simulation with {} simulations - Volatility: {}, Interest Rate: {}, Strike: {}, Underlying Price: {}, Time to Expiry (years): {}",
                         numberOfSimulations, volatility, interestRate, strike, underlyingPrice, timeToExpiryInYears);

            // Calculate option price using Monte Carlo
            double optionPrice = calculateMonteCarloPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
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
            logger.error("Monte Carlo calculation error: {}", e.getMessage());
            throw new RuntimeException("Monte Carlo calculation error: " + e.getMessage());
        }
    }
    
    @Override
    public void calculateRange(OptionPriceResultSet optionPriceResultSet, Map<String, Double> input, String rangeKey, double startValue, double endValue, double increment)
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
                    OptionPriceResult result = calculate(inputCopy);
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
            logger.error("Monte Carlo range calculation error: {}", e.getMessage());
            throw new RuntimeException("Monte Carlo range calculation error: " + e.getMessage());
        }
    }

    private double calculateMonteCarloPrice(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double sumPayoffs = 0.0;
        double dt = timeToExpiryInYears;
        
        for (int i = 0; i < numberOfSimulations; i++)
        {
            // Generate random price path using geometric Brownian motion
            double randomShock = random.nextGaussian();
            double futurePrice = underlyingPrice * exp((interestRate - 0.5 * volatility * volatility) * dt + volatility * sqrt(dt) * randomShock);
            
            // Calculate payoff
            double payoff = calculatePayoff(futurePrice, strike);
            sumPayoffs += payoff;
        }
        
        // Average payoff discounted to present value
        double averagePayoff = sumPayoffs / numberOfSimulations;
        return averagePayoff * exp(-interestRate * timeToExpiryInYears);
    }
    
    private double calculatePayoff(double futurePrice, double strike)
    {
        if (isCallOption)
        {
            return max(futurePrice - strike, 0.0);
        }
        else
        {
            return max(strike - futurePrice, 0.0);
        }
    }
    
    private double calculateDelta(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = underlyingPrice * 0.01; // 1% perturbation
        double priceUp = calculateMonteCarloPrice(underlyingPrice + epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceDown = calculateMonteCarloPrice(underlyingPrice - epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateGamma(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = underlyingPrice * 0.01;
        double priceUp = calculateMonteCarloPrice(underlyingPrice + epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceDown = calculateMonteCarloPrice(underlyingPrice - epsilon, strike, volatility, interestRate, timeToExpiryInYears);
        double priceCenter = calculateMonteCarloPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears);
        return (priceUp - 2 * priceCenter + priceDown) / (epsilon * epsilon);
    }
    
    private double calculateVega(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = volatility * 0.01;
        double priceUp = calculateMonteCarloPrice(underlyingPrice, strike, volatility + epsilon, interestRate, timeToExpiryInYears);
        double priceDown = calculateMonteCarloPrice(underlyingPrice, strike, volatility - epsilon, interestRate, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateRho(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = interestRate * 0.01;
        double priceUp = calculateMonteCarloPrice(underlyingPrice, strike, volatility, interestRate + epsilon, timeToExpiryInYears);
        double priceDown = calculateMonteCarloPrice(underlyingPrice, strike, volatility, interestRate - epsilon, timeToExpiryInYears);
        return (priceUp - priceDown) / (2 * epsilon);
    }
    
    private double calculateTheta(double underlyingPrice, double strike, double volatility, double interestRate, double timeToExpiryInYears)
    {
        double epsilon = timeToExpiryInYears * 0.01;
        double priceUp = calculateMonteCarloPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears + epsilon);
        double priceDown = calculateMonteCarloPrice(underlyingPrice, strike, volatility, interestRate, timeToExpiryInYears - epsilon);
        return (priceUp - priceDown) / (2 * epsilon);
    }

    @Override
    public String getModelDetails()
    {
        return String.format("Monte Carlo Option Model: %s %s options with %d simulations", 
                isEuropeanOption ? "European" : "American", 
                isCallOption ? "Call" : "Put", 
                numberOfSimulations);
    }

    @Override
    public String toString()
    {
        return String.format("MonteCarloOptionModel{isCall=%b, isEuropean=%b, simulations=%d}", 
                isCallOption, isEuropeanOption, numberOfSimulations);
    }
}
