package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Document(collection = "prices")
public class Price
{
    @Id
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("instrumentCode")
    private String instrumentCode;
    
    @JsonProperty("closePrice")
    private Double closePrice;
    
    @JsonProperty("openPrice")
    private Double openPrice;
    
    @JsonProperty("lastUpdatedBy")
    private String lastUpdatedBy;
    
    @JsonProperty("lastUpdatedOn")
    private LocalDate lastUpdatedOn;
    
    public Price()
    {
        this.id = UUID.randomUUID();
        this.lastUpdatedOn = LocalDate.now();
    }
    
    public Price(String instrumentCode, Double closePrice, Double openPrice, String lastUpdatedBy)
    {
        this();
        this.instrumentCode = instrumentCode;
        this.closePrice = closePrice;
        this.openPrice = openPrice;
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
    
    public String getInstrumentCode()
    {
        return instrumentCode;
    }
    
    public void setInstrumentCode(String instrumentCode)
    {
        this.instrumentCode = instrumentCode;
    }
    
    public Double getClosePrice()
    {
        return closePrice;
    }
    
    public void setClosePrice(Double closePrice)
    {
        this.closePrice = closePrice;
    }
    
    public Double getOpenPrice()
    {
        return openPrice;
    }
    
    public void setOpenPrice(Double openPrice)
    {
        this.openPrice = openPrice;
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
        Price price = (Price) o;
        return Objects.equals(id, price.id);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
    
    @Override
    public String toString()
    {
        return String.format("Price{id=%s, instrumentCode='%s', closePrice=%.2f, openPrice=%.2f, lastUpdatedBy='%s', lastUpdatedOn=%s}",
                id, instrumentCode, closePrice, openPrice, lastUpdatedBy, lastUpdatedOn);
    }
}
