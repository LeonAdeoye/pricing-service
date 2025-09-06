package com.leon.pricing.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

@Service
public class PerformanceTrackingService 
{
    private final AtomicLong totalRangeCalculations = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxExecutionTime = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> modelPerformance = new ConcurrentHashMap<>();
    
    public void recordRangeCalculation(String modelType, long executionTimeMs) 
    {
        totalRangeCalculations.incrementAndGet();
        totalExecutionTime.addAndGet(executionTimeMs);
        
        // Update min/max
        long currentMin = minExecutionTime.get();
        while (executionTimeMs < currentMin && !minExecutionTime.compareAndSet(currentMin, executionTimeMs)) 
        {
            currentMin = minExecutionTime.get();
        }
        
        long currentMax = maxExecutionTime.get();
        while (executionTimeMs > currentMax && !maxExecutionTime.compareAndSet(currentMax, executionTimeMs)) 
        {
            currentMax = maxExecutionTime.get();
        }
        
        // Track per-model performance
        modelPerformance.merge(modelType, executionTimeMs, Long::sum);
    }
    
    public Map<String, Object> getRangeCalculationPerformance() 
    {
        Map<String, Object> performance = new ConcurrentHashMap<>();
        
        long total = totalRangeCalculations.get();
        performance.put("totalRangeCalculations", total);
        
        if (total > 0) 
        {
            performance.put("averageExecutionTimeMs", totalExecutionTime.get() / total);
            performance.put("minExecutionTimeMs", minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get());
            performance.put("maxExecutionTimeMs", maxExecutionTime.get());
        }
        
        performance.put("modelPerformance", new ConcurrentHashMap<>(modelPerformance));
        
        return performance;
    }
    
    public void resetMetrics() 
    {
        totalRangeCalculations.set(0);
        totalExecutionTime.set(0);
        minExecutionTime.set(Long.MAX_VALUE);
        maxExecutionTime.set(0);
        modelPerformance.clear();
    }
}
