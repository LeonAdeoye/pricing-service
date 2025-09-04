package com.leon.pricing.controller;

import com.leon.pricing.model.Price;
import com.leon.pricing.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/price")
@CrossOrigin(origins = "*")
public class ParametricPriceController
{
    private static final Logger logger = LoggerFactory.getLogger(ParametricPriceController.class);

    @Autowired
    private PriceService priceService;
    
    @GetMapping
    public ResponseEntity<List<Price>> loadPrices()
    {
        try
        {
            logger.info("Loading all price records");
            List<Price> prices = priceService.loadPrices();
            
            if (prices.isEmpty())
            {
                logger.info("No price records found");
                return ResponseEntity.ok(prices);
            }
            else
            {
                logger.info("Loaded {} price records", prices.size());
                return ResponseEntity.ok(prices);
            }
        }
        catch (Exception e)
        {
            logger.error("Error loading prices: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping
    public ResponseEntity<Price> updatePrice(@RequestBody Price priceData)
    {
        try
        {
            logger.info("Updating price for instrument {}: close={}, open={} by user {}", 
                    priceData.getInstrumentCode(), priceData.getClosePrice(), priceData.getOpenPrice(), priceData.getLastUpdatedBy());

            if (priceData.getInstrumentCode() == null || priceData.getInstrumentCode().trim().isEmpty())
            {
                logger.warn("Invalid request: instrument code is null or empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (priceData.getClosePrice() == null || priceData.getClosePrice() < 0)
            {
                logger.warn("Invalid request: close price must be non-negative");
                return ResponseEntity.badRequest().build();
            }
            
            if (priceData.getOpenPrice() == null || priceData.getOpenPrice() < 0)
            {
                logger.warn("Invalid request: open price must be non-negative");
                return ResponseEntity.badRequest().build();
            }
            
            if (priceData.getLastUpdatedBy() == null || priceData.getLastUpdatedBy().trim().isEmpty())
            {
                logger.warn("Invalid request: last updated by is null or empty");
                return ResponseEntity.badRequest().build();
            }

            Price updatedPrice = priceService.updatePrice(priceData.getInstrumentCode(), 
                    priceData.getClosePrice(), priceData.getOpenPrice(), priceData.getLastUpdatedBy());
            logger.info("Successfully updated price for instrument {}: {}", priceData.getInstrumentCode(), updatedPrice);
            return ResponseEntity.ok(updatedPrice);
            
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid request data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            logger.error("Error updating price for instrument {}: {}", 
                    priceData.getInstrumentCode(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{instrumentCode}")
    public ResponseEntity<Price> getPrice(@PathVariable String instrumentCode)
    {
        try
        {
            logger.debug("Getting price for instrument: {}", instrumentCode);
            Price price = priceService.getPrice(instrumentCode);
            
            if (price == null)
            {
                logger.debug("No price found for instrument: {}", instrumentCode);
                return ResponseEntity.notFound().build();
            }
            else
                return ResponseEntity.ok(price);

        }
        catch (Exception e)
        {
            logger.error("Error getting price for instrument {}: {}", instrumentCode, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
