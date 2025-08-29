package com.leon.pricing.controller;

import com.leon.pricing.model.InterestRate;
import com.leon.pricing.service.InterestRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rate")
@CrossOrigin(origins = "*")
public class ParametricInterestRateController
{
    private static final Logger logger = LoggerFactory.getLogger(ParametricInterestRateController.class);

    @Autowired
    private InterestRateService interestRateService;
    
    @GetMapping
    public ResponseEntity<List<InterestRate>> loadRates()
    {
        try
        {
            logger.info("Loading all interest rate records");
            List<InterestRate> rates = interestRateService.loadRates();
            
            if (rates.isEmpty())
            {
                logger.info("No interest rate records found");
                return ResponseEntity.ok(rates);
            }
            else
            {
                logger.info("Loaded {} interest rate records", rates.size());
                return ResponseEntity.ok(rates);
            }
        }
        catch (Exception e)
        {
            logger.error("Error loading interest rates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping
    public ResponseEntity<InterestRate> updateRate(@RequestBody InterestRate rateData)
    {
        try
        {
            logger.info("Updating interest rate for currency {}: {}% by user {}", rateData.getCurrencyCode(), rateData.getInterestRatePercentage(), rateData.getLastUpdatedBy());

            if (rateData.getCurrencyCode() == null || rateData.getCurrencyCode().trim().isEmpty())
            {
                logger.warn("Invalid request: currency code is null or empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (rateData.getInterestRatePercentage() == null || rateData.getInterestRatePercentage() < 0 || rateData.getInterestRatePercentage() > 100)
            {
                logger.warn("Invalid request: interest rate percentage must be between 0 and 100");
                return ResponseEntity.badRequest().build();
            }
            
            if (rateData.getLastUpdatedBy() == null || rateData.getLastUpdatedBy().trim().isEmpty())
            {
                logger.warn("Invalid request: last updated by is null or empty");
                return ResponseEntity.badRequest().build();
            }

            InterestRate updatedRate = interestRateService.updateRate(rateData.getCurrencyCode(), rateData.getInterestRatePercentage(), rateData.getLastUpdatedBy());
            logger.info("Successfully updated interest rate for currency {}: {}", rateData.getCurrencyCode(), updatedRate);
            return ResponseEntity.ok(updatedRate);
            
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid request data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            logger.error("Error updating interest rate for currency {}: {}", 
                    rateData.getCurrencyCode(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{currencyCode}")
    public ResponseEntity<InterestRate> getRate(@PathVariable String currencyCode)
    {
        try
        {
            logger.debug("Getting interest rate for currency: {}", currencyCode);
            InterestRate rate = interestRateService.getRate(currencyCode);
            
            if (rate == null)
            {
                logger.debug("No interest rate found for currency: {}", currencyCode);
                return ResponseEntity.notFound().build();
            }
            else
                return ResponseEntity.ok(rate);

        }
        catch (Exception e)
        {
            logger.error("Error getting interest rate for currency {}: {}", currencyCode, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
