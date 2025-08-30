package com.leon.pricing.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;
import static java.lang.Math.*;

@Component
public class EuropeanBlackScholesModel implements OptionModel
{
    private static final Logger logger = LoggerFactory.getLogger(EuropeanBlackScholesModel.class);
    private double adjustedNormalizedDrift = 0.0;  // Normalized drift term adjusted for volatility and time.
    private double adjustedNormalizedDriftOffsetByVolatility = 0.0;  // Represents adjustedNormalizedDrift minus volatility over time.
    private double discountFactor = 0.0; // Present value factor for future cash flows based on interest rate and time to expiry.
    private double squareRootOfTimeToExpiryInYears = 0.0;
    private boolean isCallOption = true;
    private boolean isEuropeanOption = true;
    
    public EuropeanBlackScholesModel() {}
    
    @Override
    public void setToCall(boolean isCallOption)
    {
        this.isCallOption = isCallOption;
    }

    @Override
    public void setToEuropean(boolean isEuropeanOption)
    {
        this.isEuropeanOption = true;
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
            double dayCountConvention = input.getOrDefault("DAY_COUNT_CONVENTION", 250.0);

            logger.info("Calculating option price and greeks using European Black Scholes model with inputs - Volatility: {}, Interest Rate: {}, Strike: {}, Underlying Price: {}, Time to Expiry (years): {}",
                         volatility, interestRate, strike, underlyingPrice, timeToExpiryInYears);

            this.adjustedNormalizedDrift = (log(underlyingPrice/strike) + ((interestRate + ((volatility * volatility)/2)) * timeToExpiryInYears)) / (volatility * sqrt(timeToExpiryInYears));
            this.adjustedNormalizedDriftOffsetByVolatility = this.adjustedNormalizedDrift - (volatility * sqrt(timeToExpiryInYears));
            this.discountFactor = exp(-interestRate * timeToExpiryInYears);
            this.squareRootOfTimeToExpiryInYears = sqrt(timeToExpiryInYears);
            
            optionResult.setPrice(this.calculateOptionPrice(underlyingPrice, strike, timeToExpiryInYears, interestRate));
            optionResult.setDelta(this.calculateOptionDelta());
            optionResult.setGamma(this.calculateOptionGamma(underlyingPrice, volatility));
            optionResult.setVega(this.calculateOptionVega(underlyingPrice));
            optionResult.setRho(this.calculateOptionRho(strike, timeToExpiryInYears, interestRate));
            optionResult.setTheta(this.calculateOptionTheta(underlyingPrice, strike, interestRate, volatility, dayCountConvention));
            
            return optionResult;
        }
        catch (Exception e)
        {
            logger.error("Calculation error: {}", e.getMessage());
            throw new RuntimeException("Black-Scholes calculation error: " + e.getMessage());
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
                optionPriceResult.setRangeVariable(value);
                optionPriceResultSet.merge(optionPriceResult);
            }
        }
        catch (Exception e)
        {
            logger.error("Range calculation error: {}", e.getMessage());
            throw new RuntimeException("Black-Scholes range calculation error: " + e.getMessage());
        }
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

    private double standardNormalProbabilityDensityFunction(double x)
    {
        return (1.0 / Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * x * x);
    }

    private double calculateOptionPrice(double underlyingPrice, double strike, double timeToExpiryInYears, double interestRate)
    {
        try
        {
            if (this.isCallOption)
                return (underlyingPrice * cumulativeNormalDistribution(this.adjustedNormalizedDrift)) - (strike * this.discountFactor * cumulativeNormalDistribution(this.adjustedNormalizedDriftOffsetByVolatility));
            else
                return (strike * this.discountFactor * cumulativeNormalDistribution(-this.adjustedNormalizedDriftOffsetByVolatility)) - (underlyingPrice * cumulativeNormalDistribution(-this.adjustedNormalizedDrift));
        }
        catch (Exception e)
        {
            throw new RuntimeException("calculateOptionPrice ERROR: " + e.getMessage());
        }
    }

    private double calculateOptionDelta()
    {
        try
        {
            if (this.isCallOption)
                return cumulativeNormalDistribution(this.adjustedNormalizedDrift);
            else
                return cumulativeNormalDistribution(this.adjustedNormalizedDrift) - 1;
        }
        catch (Exception e)
        {
            throw new RuntimeException("calculateOptionDelta ERROR: " + e.getMessage());
        }
    }

    private double calculateOptionGamma(double underlyingPrice, double volatility)
    {
        try
        {
            //return cumulativeNormalDistribution(this.adjustedNormalizedDrift) / (underlyingPrice * volatility * this.squareRootOfTimeToExpiryInYears);
            return standardNormalProbabilityDensityFunction(this.adjustedNormalizedDrift) / (underlyingPrice * volatility * this.squareRootOfTimeToExpiryInYears);
        }
        catch (Exception e)
        {
            throw new RuntimeException("calculateOptionGamma ERROR: " + e.getMessage());
        }
    }

    private double calculateOptionVega(double underlyingPrice)
    {
        try
        {
            return underlyingPrice * standardNormalProbabilityDensityFunction(this.adjustedNormalizedDrift) * this.squareRootOfTimeToExpiryInYears * 0.01;
        }
        catch (Exception e)
        {
            throw new RuntimeException("calculateOptionVega ERROR: " + e.getMessage());
        }
    }


    private double calculateOptionRho(double strike, double timeToExpiryInYears, double interestRate)
    {
        try
        {
            if (this.isCallOption)
                return strike * timeToExpiryInYears * this.discountFactor * cumulativeNormalDistribution(this.adjustedNormalizedDriftOffsetByVolatility) * 0.01;
            else
                return -strike * timeToExpiryInYears * this.discountFactor * cumulativeNormalDistribution(-this.adjustedNormalizedDriftOffsetByVolatility) * 0.01;
        }
        catch (Exception e)
        {
            throw new RuntimeException("calculateOptionRho ERROR: " + e.getMessage());
        }
    }

    private double calculateOptionTheta(double underlyingPrice, double strike, double interestRate, double volatility, double dayCountConvention)
    {
        try
        {
            double firstTerm = -(underlyingPrice * standardNormalProbabilityDensityFunction(this.adjustedNormalizedDrift) * volatility) / (2 * this.squareRootOfTimeToExpiryInYears);
            double secondTerm;

            if (this.isCallOption)
                secondTerm = -interestRate * strike * this.discountFactor * cumulativeNormalDistribution(this.adjustedNormalizedDriftOffsetByVolatility);
            else
                secondTerm = interestRate * strike * this.discountFactor * cumulativeNormalDistribution(-this.adjustedNormalizedDriftOffsetByVolatility);

            return (firstTerm + secondTerm) / dayCountConvention;
        }
        catch (Exception e)
        {
            throw new RuntimeException("calculateOptionTheta ERROR: " + e.getMessage());
        }
    }

    @Override
    public String getModelDetails()
    {
        return "Black-Scholes Model: European Option Pricer only. Support both calls and puts.";
    }

    @Override
    public String toString()
    {
        return "BlackScholesModel{isCall=" + isCallOption + ", isEuropean=" + isEuropeanOption + "}";
    }
}
