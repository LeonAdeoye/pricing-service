package com.leon.pricing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class OptionPriceResultSet
{
    
    @JsonProperty("results")
    private List<OptionPriceResult> results;
    
    @JsonProperty("totalCount")
    private int totalCount;
    
    public OptionPriceResultSet()
    {
        this.results = new ArrayList<>();
        this.totalCount = 0;
    }
    
    public void merge(OptionPriceResult optionPriceResult)
    {
        this.results.add(optionPriceResult);
        this.totalCount++;
    }
    
    public void addAll(OptionPriceResultSet otherSet)
    {
        this.results.addAll(otherSet.getResults());
        this.totalCount = this.results.size();
    }
    
    public List<OptionPriceResult> getResults()
    {
        return results;
    }
    
    public void setResults(List<OptionPriceResult> results)
    {
        this.results = results;
        this.totalCount = results != null ? results.size() : 0;
    }
    
    public int getTotalCount()
    {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount)
    {
        this.totalCount = totalCount;
    }
    
    public boolean isEmpty()
    {
        return results.isEmpty();
    }
    
    public int size()
    {
        return results.size();
    }
    
    public void clear()
    {
        results.clear();
        totalCount = 0;
    }
}
