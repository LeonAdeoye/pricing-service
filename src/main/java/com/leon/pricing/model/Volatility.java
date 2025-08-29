package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Document(collection = "volatilities")
public class Volatility {
    
    @Id
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("instrumentCode")
    private String instrumentCode;
    @JsonProperty("volatilityPercentage")
    private Double volatilityPercentage;
    @JsonProperty("lastUpdatedBy")
    private String lastUpdatedBy;
    @JsonProperty("lastUpdatedOn")
    private LocalDate lastUpdatedOn;

    public Volatility()
    {
        this.id = UUID.randomUUID();
        this.lastUpdatedOn = LocalDate.now();
    }

    public Volatility(String instrumentCode, Double volatilityPercentage, String lastUpdatedBy)
    {
        this();
        this.instrumentCode = instrumentCode;
        this.volatilityPercentage = volatilityPercentage;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getInstrumentCode() {
        return instrumentCode;
    }
    
    public void setInstrumentCode(String instrumentCode) {
        this.instrumentCode = instrumentCode;
    }
    
    public Double getVolatilityPercentage() {
        return volatilityPercentage;
    }
    
    public void setVolatilityPercentage(Double volatilityPercentage) {
        this.volatilityPercentage = volatilityPercentage;
    }
    
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }
    
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public LocalDate getLastUpdatedOn() {
        return lastUpdatedOn;
    }
    
    public void setLastUpdatedOn(LocalDate lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Volatility that = (Volatility) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString()
    {
        return String.format("Volatility{id=%s, instrumentCode='%s', volatilityPercentage=%.2f, lastUpdatedBy='%s', lastUpdatedOn=%s}",
                id, instrumentCode, volatilityPercentage, lastUpdatedBy, lastUpdatedOn);
    }
}
