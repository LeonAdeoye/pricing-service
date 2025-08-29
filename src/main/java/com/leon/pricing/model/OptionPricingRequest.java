package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class OptionPricingRequest
{
    @NotNull(message = "Strike price is required")
    @DecimalMin(value = "0.01", message = "Strike price must be greater than 0")
    @JsonProperty("strike")
    private Double strike;
    
    @NotNull(message = "Volatility is required")
    @DecimalMin(value = "0.01", message = "Volatility must be greater than 0")
    @JsonProperty("volatility")
    private Double volatility;
    
    @NotNull(message = "Underlying price is required")
    @DecimalMin(value = "0.01", message = "Underlying price must be greater than 0")
    @JsonProperty("underlyingPrice")
    private Double underlyingPrice;
    
    @NotNull(message = "Days to expiry is required")
    @DecimalMin(value = "0.0", message = "Days to expiry must be non-negative")
    @JsonProperty("daysToExpiry")
    private Double daysToExpiry;
    
    @NotNull(message = "Interest rate is required")
    @JsonProperty("interestRate")
    private Double interestRate;
    
    @NotNull(message = "Is call option flag is required")
    @JsonProperty("isCall")
    private Boolean isCall;
    
    @NotNull(message = "Is European option flag is required")
    @JsonProperty("isEuropean")
    private Boolean isEuropean;
    
    @NotNull(message = "Day count convention is required")
    @DecimalMin(value = "0.01", message = "Day count convention must be greater than 0")
    @JsonProperty("dayCountConvention")
    private Double dayCountConvention;
    
    public OptionPricingRequest() {}
    
    public OptionPricingRequest(Double strike, Double volatility, Double underlyingPrice, 
                               Double daysToExpiry, Double interestRate, Boolean isCall, 
                               Boolean isEuropean, Double dayCountConvention) {
        this.strike = strike;
        this.volatility = volatility;
        this.underlyingPrice = underlyingPrice;
        this.daysToExpiry = daysToExpiry;
        this.interestRate = interestRate;
        this.isCall = isCall;
        this.isEuropean = isEuropean;
        this.dayCountConvention = dayCountConvention;
    }
    
    // Getters and Setters
    public Double getStrike() { return strike; }
    public void setStrike(Double strike) { this.strike = strike; }
    
    public Double getVolatility() { return volatility; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }
    
    public Double getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(Double underlyingPrice) { this.underlyingPrice = underlyingPrice; }
    
    public Double getDaysToExpiry() { return daysToExpiry; }
    public void setDaysToExpiry(Double daysToExpiry) { this.daysToExpiry = daysToExpiry; }
    
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    
    public Boolean getIsCall() { return isCall; }
    public void setIsCall(Boolean isCall) { this.isCall = isCall; }
    
    public Boolean getIsEuropean() { return isEuropean; }
    public void setIsEuropean(Boolean isEuropean) { this.isEuropean = isEuropean; }
    
    public Double getDayCountConvention() { return dayCountConvention; }
    public void setDayCountConvention(Double dayCountConvention) { this.dayCountConvention = dayCountConvention; }
    
    /**
     * Calculates time to expiry in years.
     * 
     * NOTE: For production use, this calculation should consider:
     * - Business days vs calendar days
     * - Market holidays (query external holiday service)
     * - Day count conventions (30/360, Actual/365, etc.)
     * 
     * @return time to expiry in years
     */
    public double getTimeToExpiryInYears() {
        return daysToExpiry / dayCountConvention;
    }
}
