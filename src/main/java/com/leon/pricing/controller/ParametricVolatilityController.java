package com.leon.pricing.controller;

import com.leon.pricing.model.Volatility;
import com.leon.pricing.service.VolatilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/volatility")
@CrossOrigin(origins = "*")
public class ParametricVolatilityController
{
    private static final Logger logger = LoggerFactory.getLogger(ParametricVolatilityController.class);

    @Autowired
    private  VolatilityService volatilityService;
    
    @GetMapping
    public ResponseEntity<List<Volatility>> loadVolatilities()
    {
        try
        {
            logger.info("Loading all volatility records");
            List<Volatility> volatilities = volatilityService.loadVolatilities();
            
            if (volatilities.isEmpty())
            {
                logger.info("No volatility records found");
                return ResponseEntity.ok(volatilities);
            }
            else
            {
                logger.info("Loaded {} volatility records", volatilities.size());
                return ResponseEntity.ok(volatilities);
            }
        }
        catch (Exception e)
        {
            logger.error("Error loading volatilities: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping
    public ResponseEntity<Volatility> updateVolatility(@RequestBody Volatility volatilityData)
    {
        try
        {
            logger.info("Updating volatility for instrument {}: {}% by user {}", volatilityData.getInstrumentCode(), volatilityData.getVolatilityPercentage(), volatilityData.getLastUpdatedBy());

            if (volatilityData.getInstrumentCode() == null || volatilityData.getInstrumentCode().trim().isEmpty())
            {
                logger.warn("Invalid request: instrument code is null or empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (volatilityData.getVolatilityPercentage() == null || volatilityData.getVolatilityPercentage() < 0 || volatilityData.getVolatilityPercentage() > 100)
            {
                logger.warn("Invalid request: volatility percentage must be between 0 and 100");
                return ResponseEntity.badRequest().build();
            }
            
            if (volatilityData.getLastUpdatedBy() == null || volatilityData.getLastUpdatedBy().trim().isEmpty())
            {
                logger.warn("Invalid request: last updated by is null or empty");
                return ResponseEntity.badRequest().build();
            }

            Volatility updatedVolatility = volatilityService.updateVolatility(volatilityData.getInstrumentCode(), volatilityData.getVolatilityPercentage(), volatilityData.getLastUpdatedBy());
            logger.info("Successfully updated volatility for instrument {}: {}", volatilityData.getInstrumentCode(), updatedVolatility);
            return ResponseEntity.ok(updatedVolatility);
            
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid request data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            logger.error("Error updating volatility for instrument {}: {}", 
                    volatilityData.getInstrumentCode(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{instrumentCode}")
    public ResponseEntity<Volatility> getVolatility(@PathVariable String instrumentCode)
    {
        try
        {
            logger.debug("Getting volatility for instrument: {}", instrumentCode);
            
            Volatility volatility = volatilityService.getVolatility(instrumentCode);
            
            if (volatility != null) {
                return ResponseEntity.ok(volatility);
            } else {
                logger.debug("No volatility found for instrument: {}", instrumentCode);
                return ResponseEntity.notFound().build();
            }
        }
        catch (Exception e)
        {
            logger.error("Error getting volatility for instrument {}: {}", instrumentCode, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
