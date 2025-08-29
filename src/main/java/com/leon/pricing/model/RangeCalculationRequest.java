package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class RangeCalculationRequest {
    
    @NotNull(message = "Base pricing request is required")
    @JsonProperty("baseRequest")
    private OptionPricingRequest baseRequest;
    
    @NotNull(message = "Range key is required")
    @JsonProperty("rangeKey")
    private String rangeKey;
    
    @NotNull(message = "Start value is required")
    @JsonProperty("startValue")
    private Double startValue;
    
    @NotNull(message = "End value is required")
    @JsonProperty("endValue")
    private Double endValue;
    
    @NotNull(message = "Increment is required")
    @DecimalMin(value = "0.0001", message = "Increment must be greater than 0")
    @JsonProperty("increment")
    private Double increment;
    
    public RangeCalculationRequest() {}
    
    public RangeCalculationRequest(OptionPricingRequest baseRequest, String rangeKey, 
                                 Double startValue, Double endValue, Double increment) {
        this.baseRequest = baseRequest;
        this.rangeKey = rangeKey;
        this.startValue = startValue;
        this.endValue = endValue;
        this.increment = increment;
    }
    
    // Getters and Setters
    public OptionPricingRequest getBaseRequest() { return baseRequest; }
    public void setBaseRequest(OptionPricingRequest baseRequest) { this.baseRequest = baseRequest; }
    
    public String getRangeKey() { return rangeKey; }
    public void setRangeKey(String rangeKey) { this.rangeKey = rangeKey; }
    
    public Double getStartValue() { return startValue; }
    public void setStartValue(Double startValue) { this.startValue = startValue; }
    
    public Double getEndValue() { return endValue; }
    public void setEndValue(Double endValue) { this.endValue = endValue; }
    
    public Double getIncrement() { return increment; }
    public void setIncrement(Double increment) { this.increment = increment; }
}
