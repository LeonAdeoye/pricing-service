package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Document(collection = "interestRates")
public class InterestRate
{
    @Id
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("currencyCode")
    private String currencyCode;
    
    @JsonProperty("interestRatePercentage")
    private Double interestRatePercentage;
    
    @JsonProperty("lastUpdatedBy")
    private String lastUpdatedBy;
    
    @JsonProperty("lastUpdatedOn")
    private LocalDate lastUpdatedOn;
    
    public InterestRate()
    {
        this.id = UUID.randomUUID();
        this.lastUpdatedOn = LocalDate.now();
    }
    
    public InterestRate(String currencyCode, Double interestRatePercentage, String lastUpdatedBy)
    {
        this();
        this.currencyCode = currencyCode;
        this.interestRatePercentage = interestRatePercentage;
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public UUID getId()
    {
        return id;
    }
    
    public void setId(UUID id)
    {
        this.id = id;
    }
    
    public String getCurrencyCode()
    {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode)
    {
        this.currencyCode = currencyCode;
    }
    
    public Double getInterestRatePercentage()
    {
        return interestRatePercentage;
    }
    
    public void setInterestRatePercentage(Double interestRatePercentage)
    {
        this.interestRatePercentage = interestRatePercentage;
    }
    
    public String getLastUpdatedBy()
    {
        return lastUpdatedBy;
    }
    
    public void setLastUpdatedBy(String lastUpdatedBy)
    {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public LocalDate getLastUpdatedOn()
    {
        return lastUpdatedOn;
    }
    
    public void setLastUpdatedOn(LocalDate lastUpdatedOn)
    {
        this.lastUpdatedOn = lastUpdatedOn;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterestRate that = (InterestRate) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
    
    @Override
    public String toString()
    {
        return String.format("InterestRate{id=%s, currencyCode='%s', interestRatePercentage=%.2f, lastUpdatedBy='%s', lastUpdatedOn=%s}",
                id, currencyCode, interestRatePercentage, lastUpdatedBy, lastUpdatedOn);
    }
}
