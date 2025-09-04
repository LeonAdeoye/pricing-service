package com.leon.pricing.service;

import com.leon.pricing.model.Price;
import com.leon.pricing.repository.PriceRepository;
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
public class PriceServiceImpl implements PriceService
{
    private static final Logger logger = LoggerFactory.getLogger(PriceServiceImpl.class);
    private final Map<String, Price> priceCache = new HashMap<>();
    @Autowired
    private PriceRepository priceRepository;

    @PostConstruct
    private void initializeCache()
    {
        try
        {
            List<Price> prices = priceRepository.findAll();
            prices.forEach(p -> priceCache.put(p.getInstrumentCode(), p));
            logger.info("Initialized price cache with {} records", prices.size());
        }
        catch (Exception e)
        {
            logger.warn("Failed to initialize cache from database: {}", e.getMessage());
        }
    }
    
    @Override
    public List<Price> loadPrices()
    {
        if (priceCache.isEmpty())
            initializeCache();
        
        List<Price> result = List.copyOf(priceCache.values());
        logger.debug("Loaded {} price records from cache", result.size());
        return result;
    }
    
    @Override
    public Price updatePrice(String instrumentCode, Double closePrice, Double openPrice, String lastUpdatedBy)
    {
        Price existingPrice = priceCache.get(instrumentCode);
        if (existingPrice != null)
        {
            existingPrice.setClosePrice(closePrice);
            existingPrice.setOpenPrice(openPrice);
            existingPrice.setLastUpdatedBy(lastUpdatedBy);
            existingPrice.setLastUpdatedOn(LocalDate.now());
            Price savedPrice = priceRepository.save(existingPrice);
            priceCache.put(instrumentCode, savedPrice);
            logger.info("Updated price for instrument {}: close={}, open={} by user {}", instrumentCode, closePrice, openPrice, lastUpdatedBy);
            return savedPrice;
        }
        else
        {
            Price newPrice = new Price(instrumentCode, closePrice, openPrice, lastUpdatedBy);
            Price savedPrice = priceRepository.save(newPrice);
            priceCache.put(instrumentCode, savedPrice);
            logger.info("Created new price for instrument {}: close={}, open={} by user {}", instrumentCode, closePrice, openPrice, lastUpdatedBy);
            return savedPrice;
        }
    }
    
    @Override
    public Price getPrice(String instrumentCode)
    {
        Price cachedPrice = priceCache.get(instrumentCode);
        if (cachedPrice != null)
            return cachedPrice;
        
        try
        {
            Optional<Price> dbPrice = priceRepository.findByInstrumentCode(instrumentCode);
            if (dbPrice.isPresent())
            {
                Price price = dbPrice.get();
                priceCache.put(instrumentCode, price);
                return price;
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to retrieve price from database for {}: {}", instrumentCode, e.getMessage());
        }
        
        return null;
    }
}
