package com.leon.pricing.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import static java.lang.Math.*;

@Component
public class BlackScholesModel {
    
    private static final Logger logger = LoggerFactory.getLogger(BlackScholesModel.class);
    
    // Constants for input keys
    public static final String STRIKE = "strike";
    public static final String VOLATILITY = "volatility";
    public static final String UNDERLYING_PRICE = "underlyingPrice";
    public static final String TIME_TO_EXPIRY = "timeToExpiry";
    public static final String INTEREST_RATE = "interestRate";
    
    // Variables for intermediate calculations
    private double d1 = 0.0;
    private double d2 = 0.0;
    private double e = 0.0;
    private double t = 0.0;
    private boolean isCallOption = true;
    private boolean isEuropeanOption = true;
    
    public BlackScholesModel() {}
    
    public void setToCall(boolean isCallOption) {
        this.isCallOption = isCallOption;
    }
    
    public void setToEuropean(boolean isEuropeanOption) {
        this.isEuropeanOption = isEuropeanOption;
    }
    
    /**
     * Calculates option price and Greeks using the Black-Scholes model.
     * 
     * NOTE: For production use, consider external data sources:
     * - Volatility: Query volatility surface service for market-implied volatility
     * - Interest Rate: Query yield curve service for risk-free rates
     * - Underlying Price: Query market data service for real-time prices
     * - Time to Expiry: Consider business days and market holidays from holiday service
     * 
     * @param input Map containing pricing parameters
     * @return OptionPriceResult with price and Greeks
     */
    public OptionPriceResult calculate(Map<String, Double> input) {
        OptionPriceResult optionResult = new OptionPriceResult();
        
        try {
            double volatility = input.get(VOLATILITY);
            double interestRate = input.get(INTEREST_RATE);
            double strike = input.get(STRIKE);
            double underlyingPrice = input.get(UNDERLYING_PRICE);
            double timeToExpiryInYears = input.get(TIME_TO_EXPIRY);
            
            // Calculate intermediate values
            this.d1 = (log(underlyingPrice/strike) + ((interestRate + ((volatility * volatility)/2)) * timeToExpiryInYears)) / (volatility * sqrt(timeToExpiryInYears));
            this.d2 = this.d1 - (volatility * sqrt(timeToExpiryInYears));
            this.e = exp(-interestRate * timeToExpiryInYears);
            this.t = sqrt(timeToExpiryInYears);
            
            optionResult.setPrice(this.calculateOptionPrice(underlyingPrice, strike, timeToExpiryInYears, interestRate));
            optionResult.setDelta(this.calculateOptionDelta());
            optionResult.setGamma(this.calculateOptionGamma(underlyingPrice, volatility));
            optionResult.setVega(this.calculateOptionVega(underlyingPrice));
            optionResult.setRho(this.calculateOptionRho(strike, timeToExpiryInYears, interestRate));
            optionResult.setTheta(this.calculateOptionTheta(underlyingPrice, strike, interestRate, volatility));
            
            return optionResult;
        } catch (Exception e) {
            logger.error("Calculation error: {}", e.getMessage());
            throw new RuntimeException("Black-Scholes calculation error: " + e.getMessage());
        }
    }
    
    public void calculateRange(OptionPriceResultSet optionPriceResultSet, Map<String, Double> input, 
                             String rangeKey, double startValue, double endValue, double increment) {
        try {
            for (double value = startValue; value <= endValue; value += increment) {
                input.put(rangeKey, value);
                OptionPriceResult optionPriceResult = calculate(input);
                optionPriceResult.setRangeVariable(value);
                optionPriceResultSet.merge(optionPriceResult);
            }
        } catch (Exception e) {
            logger.error("Range calculation error: {}", e.getMessage());
            throw new RuntimeException("Black-Scholes range calculation error: " + e.getMessage());
        }
    }
    
    private double calculateOptionPrice(double underlyingPrice, double strike, double timeToExpiryInYears, double interestRate) {
        try {
            if (this.isCallOption) {
                return (underlyingPrice * CND(this.d1)) - (strike * this.e * CND(this.d2));
            } else {
                return (strike * this.e * CND(-this.d2)) - (underlyingPrice * CND(-this.d1));
            }
        } catch (Exception e) {
            throw new RuntimeException("calculateOptionPrice ERROR: " + e.getMessage());
        }
    }
    
    private double calculateOptionDelta() {
        try {
            if (this.isCallOption) {
                return CND(this.d1);
            } else {
                return CND(this.d1) - 1;
            }
        } catch (Exception e) {
            throw new RuntimeException("calculateOptionDelta ERROR: " + e.getMessage());
        }
    }
    
    private double calculateOptionGamma(double underlyingPrice, double volatility) {
        try {
            return CND(this.d1) / (underlyingPrice * volatility * this.t);
        } catch (Exception e) {
            throw new RuntimeException("calculateOptionGamma ERROR: " + e.getMessage());
        }
    }
    
    private double calculateOptionVega(double underlyingPrice) {
        try {
            return underlyingPrice * CND(this.d1) * this.t;
        } catch (Exception e) {
            throw new RuntimeException("calculateOptionVega ERROR: " + e.getMessage());
        }
    }
    
    private double calculateOptionRho(double strike, double timeToExpiryInYears, double interestRate) {
        try {
            if (this.isCallOption) {
                return strike * timeToExpiryInYears * this.e * CND(this.d2);
            } else {
                return -strike * timeToExpiryInYears * this.e * CND(-this.d2);
            }
        } catch (Exception e) {
            throw new RuntimeException("calculateOptionRho ERROR: " + e.getMessage());
        }
    }
    
    private double calculateOptionTheta(double underlyingPrice, double strike, double interestRate, double volatility) {
        try {
            double theta = -(underlyingPrice * volatility * CND(this.d1)) / (2 * this.t);
            
            if (this.isCallOption) {
                theta -= interestRate * strike * this.e * CND(this.d2);
            } else {
                theta += interestRate * strike * this.e * CND(-this.d2);
            }
            
            return theta;
        } catch (Exception e) {
            throw new RuntimeException("calculateOptionTheta ERROR: " + e.getMessage());
        }
    }
    
    // Cumulative Normal Distribution function
    private double CND(double x) {
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;
        
        int sign = 1;
        if (x < 0) sign = -1;
        x = abs(x) / sqrt(2.0);
        
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-x * x);
        
        return 0.5 * (1.0 + sign * y);
    }
    
    @Override
    public String toString() {
        return "BlackScholesModel{isCall=" + isCallOption + ", isEuropean=" + isEuropeanOption + "}";
    }
}
