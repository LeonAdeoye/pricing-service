package com.leon.pricing.service.impl;

import com.leon.pricing.model.InterestRate;
import com.leon.pricing.repository.InterestRateRepository;
import com.leon.pricing.service.InterestRateService;
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
public class InterestRateServiceImpl implements InterestRateService
{
    private static final Logger logger = LoggerFactory.getLogger(InterestRateServiceImpl.class);
    private final Map<String, InterestRate> rateCache = new HashMap<>();
    @Autowired
    private InterestRateRepository interestRateRepository;

    @PostConstruct
    private void initializeCache()
    {
        try
        {
            List<InterestRate> rates = interestRateRepository.findAll();
            rates.forEach(r -> rateCache.put(r.getCurrencyCode(), r));
            logger.info("Initialized interest rate cache with {} records", rates.size());
        }
        catch (Exception e)
        {
            logger.warn("Failed to initialize cache from database: {}", e.getMessage());
        }
    }
    
    @Override
    public List<InterestRate> loadRates()
    {
        if (rateCache.isEmpty())
            initializeCache();
        
        List<InterestRate> result = List.copyOf(rateCache.values());
        logger.debug("Loaded {} interest rate records from cache", result.size());
        return result;
    }
    
    @Override
    public InterestRate updateRate(String currencyCode, Double interestRatePercentage, String lastUpdatedBy)
    {
        InterestRate existingRate = rateCache.get(currencyCode);
        if (existingRate != null)
        {
            existingRate.setInterestRatePercentage(interestRatePercentage);
            existingRate.setLastUpdatedBy(lastUpdatedBy);
            existingRate.setLastUpdatedOn(LocalDate.now());
            InterestRate savedRate = interestRateRepository.save(existingRate);
            rateCache.put(currencyCode, savedRate);
            logger.info("Updated interest rate for currency {}: {}% by user {}", currencyCode, interestRatePercentage, lastUpdatedBy);
            return savedRate;
        }
        else
        {
            InterestRate newRate = new InterestRate(currencyCode, interestRatePercentage, lastUpdatedBy);
            InterestRate savedRate = interestRateRepository.save(newRate);
            rateCache.put(currencyCode, savedRate);
            logger.info("Created new interest rate for currency {}: {}% by user {}", currencyCode, interestRatePercentage, lastUpdatedBy);
            return savedRate;
        }
    }
    
    @Override
    public InterestRate getRate(String currencyCode)
    {
        InterestRate cachedRate = rateCache.get(currencyCode);
        if (cachedRate != null)
            return cachedRate;
        
        try
        {
            Optional<InterestRate> dbRate = interestRateRepository.findByCurrencyCode(currencyCode);
            if (dbRate.isPresent())
            {
                InterestRate rate = dbRate.get();
                rateCache.put(currencyCode, rate);
                return rate;
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to retrieve interest rate from database for {}: {}", currencyCode, e.getMessage());
        }
        
        return null;
    }
}
