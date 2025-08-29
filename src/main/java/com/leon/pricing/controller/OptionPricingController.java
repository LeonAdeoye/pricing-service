package com.leon.pricing.controller;

import com.leon.pricing.model.OptionPriceResult;
import com.leon.pricing.model.OptionPriceResultSet;
import com.leon.pricing.model.OptionPricingRequest;
import com.leon.pricing.model.RangeCalculationRequest;
import com.leon.pricing.service.OptionPricingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pricing")
@CrossOrigin(origins = "*")
public class OptionPricingController
{
    private static final Logger logger = LoggerFactory.getLogger(OptionPricingController.class);
    @Autowired
    private OptionPricingService optionPricingService;

    @CrossOrigin
    @RequestMapping("/heartbeat")
    String heartbeat()
    {
        return "I am an Option Pricing Service and I am actively listening for pricing requests right now!";
    }

    @PostMapping("/calculate")
    public ResponseEntity<OptionPriceResult> calculateOptionPrice(@Valid @RequestBody OptionPricingRequest request)
    {
        try
        {
            logger.info("Received option pricing request: {}", request);
            OptionPriceResult result = optionPricingService.calculateOptionPrice(request);
            return ResponseEntity.ok(result);
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            logger.error("Error calculating option price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/range")
    public ResponseEntity<OptionPriceResultSet> calculateRange(@Valid @RequestBody RangeCalculationRequest request)
    {
        try
        {
            logger.info("Received range calculation request: {}", request);
            OptionPriceResultSet result = optionPricingService.calculateRange(request);
            return ResponseEntity.ok(result);
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid range request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            logger.error("Error calculating range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/range/simple")
    public ResponseEntity<OptionPriceResultSet> calculateRangeSimple(@Valid @RequestBody OptionPricingRequest baseRequest,
        @RequestParam String rangeKey, @RequestParam double startValue, @RequestParam double endValue,  @RequestParam double increment)
    {
        try
        {
            logger.info("Received simple range calculation request: baseRequest={}, rangeKey={}, start={}, end={}, increment={}", 
                       baseRequest, rangeKey, startValue, endValue, increment);
            
            OptionPriceResultSet result = optionPricingService.calculateRange(baseRequest, rangeKey, startValue, endValue, increment);
            return ResponseEntity.ok(result);
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid simple range request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            logger.error("Error calculating simple range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/model-details")
    public ResponseEntity<Map<String, String>> getModelDetails()
    {
        try
        {
            String details = optionPricingService.getModelDetails();
            Map<String, String> response = new HashMap<>();
            response.put("modelDetails", details);
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error getting model details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
