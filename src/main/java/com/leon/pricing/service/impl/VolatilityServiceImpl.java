package com.leon.pricing.service.impl;

import com.leon.pricing.model.Volatility;
import com.leon.pricing.repository.VolatilityRepository;
import com.leon.pricing.service.VolatilityService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VolatilityServiceImpl implements VolatilityService
{
    private static final Logger logger = LoggerFactory.getLogger(VolatilityServiceImpl.class);
    private final Map<String, Volatility> volatilityCache = new HashMap<>();
    @Autowired
    private VolatilityRepository volatilityRepository;

    @PostConstruct
    private void initializeCache()
    {
        try
        {
            List<Volatility> volatilities = volatilityRepository.findAll();
            volatilities.forEach(v -> volatilityCache.put(v.getInstrumentCode(), v));
            logger.info("Initialized volatility cache with {} records", volatilities.size());
        }
        catch (Exception e)
        {
            logger.warn("Failed to initialize cache from database: {}", e.getMessage());
        }
    }
    
    @Override
    public List<Volatility> loadVolatilities()
    {
        if (volatilityCache.isEmpty())
            initializeCache();
        
        List<Volatility> result = List.copyOf(volatilityCache.values());
        logger.debug("Loaded {} volatility records from cache", result.size());
        return result;
    }
    
    @Override
    public Volatility updateVolatility(String instrumentCode, Double volatilityPercentage, String lastUpdatedBy)
    {
        Volatility existingVolatility = volatilityCache.get(instrumentCode);
        if (existingVolatility != null)
        {
            existingVolatility.setVolatilityPercentage(volatilityPercentage);
            existingVolatility.setLastUpdatedBy(lastUpdatedBy);
            existingVolatility.setLastUpdatedOn(LocalDate.now());
            Volatility savedVolatility = volatilityRepository.save(existingVolatility);
            volatilityCache.put(instrumentCode, savedVolatility);
            logger.info("Updated volatility for instrument {}: {}% by user {}", instrumentCode, volatilityPercentage, lastUpdatedBy);
            return savedVolatility;

        }
        else
        {
            Volatility newVolatility = new Volatility(instrumentCode, volatilityPercentage, lastUpdatedBy);
            Volatility savedVolatility = volatilityRepository.save(newVolatility);
            volatilityCache.put(instrumentCode, savedVolatility);
            logger.info("Created new volatility for instrument {}: {}% by user {}", instrumentCode, volatilityPercentage, lastUpdatedBy);
            return savedVolatility;
        }
    }
    
    @Override
    public Volatility getVolatility(String instrumentCode)
    {
        Volatility cachedVolatility = volatilityCache.get(instrumentCode);
        if (cachedVolatility != null)
            return cachedVolatility;

        try
        {
            Optional<Volatility> dbVolatility = volatilityRepository.findByInstrumentCode(instrumentCode);
            if (dbVolatility.isPresent())
            {
                Volatility volatility = dbVolatility.get();
                volatilityCache.put(instrumentCode, volatility);
                return volatility;
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to retrieve volatility from database for {}: {}", instrumentCode, e.getMessage());
        }
        
        return null;
    }
}
