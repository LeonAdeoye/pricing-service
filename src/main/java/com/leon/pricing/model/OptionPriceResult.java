package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
public class OptionPriceResult {
    
    @JsonProperty("delta")
    private double delta;
    
    @JsonProperty("gamma")
    private double gamma;
    
    @JsonProperty("rho")
    private double rho;
    
    @JsonProperty("theta")
    private double theta;
    
    @JsonProperty("vega")
    private double vega;
    
    @JsonProperty("price")
    private double price;
    
    @JsonProperty("rangeVariable")
    private double rangeVariable;
    
    public OptionPriceResult() {}
    
    public OptionPriceResult(double delta, double gamma, double rho, double theta, double vega, double price)
    {
        this.delta = delta;
        this.gamma = gamma;
        this.rho = rho;
        this.theta = theta;
        this.vega = vega;
        this.price = price;
    }
    
    // Getters and Setters
    public double getDelta()
    {
        return delta;
    }
    public void setDelta(double delta)
    {
        this.delta = delta;
    }
    
    public double getGamma()
    {
        return gamma;
    }
    public void setGamma(double gamma)
    {
        this.gamma = gamma;
    }
    
    public double getRho()
    {
        return rho;
    }
    public void setRho(double rho)
    {
        this.rho = rho;
    }
    
    public double getTheta()
    {
        return theta;
    }
    public void setTheta(double theta)
    {
        this.theta = theta;
    }
    
    public double getVega()
    {
        return vega;
    }
    public void setVega(double vega)
    {
        this.vega = vega;
    }
    
    public double getPrice()
    {
        return price;
    }
    public void setPrice(double price)
    {
        this.price = price;
    }
    
    public double getRangeVariable()
    {
        return rangeVariable;
    }
    public void setRangeVariable(double rangeVariable)
    {
        this.rangeVariable = rangeVariable;
    }
    
    public void add(OptionPriceResult priceResult)
    {
        this.delta += priceResult.delta;
        this.gamma += priceResult.gamma;
        this.vega += priceResult.vega;
        this.theta += priceResult.theta;
        this.rho += priceResult.rho;
        this.price += priceResult.price;
    }
    
    @Override
    public String toString()
    {
        return String.format("OptionPriceResult{delta=%.6f, gamma=%.6f, vega=%.6f, theta=%.6f, rho=%.6f, price=%.6f, rangeVariable=%.6f}",
                delta, gamma, vega, theta, rho, price, rangeVariable);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof OptionPriceResult)) return false;
        OptionPriceResult that = (OptionPriceResult) o;
        return Double.compare(that.delta, delta) == 0 &&
               Double.compare(that.gamma, gamma) == 0 &&
               Double.compare(that.rho, rho) == 0 &&
               Double.compare(that.theta, theta) == 0 &&
               Double.compare(that.vega, vega) == 0 &&
               Double.compare(that.price, price) == 0 &&
               Double.compare(that.rangeVariable, rangeVariable) == 0;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(delta, gamma, rho, theta, vega, price, rangeVariable);
    }
}
