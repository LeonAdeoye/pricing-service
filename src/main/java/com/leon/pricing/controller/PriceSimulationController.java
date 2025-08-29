package com.leon.pricing.controller;

import com.leon.pricing.service.PriceSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simulation")
@CrossOrigin(origins = "*")
public class PriceSimulationController
{
    private static final Logger logger = LoggerFactory.getLogger(PriceSimulationController.class);

    @Autowired
    private PriceSimulationService priceSimulationService;
    

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initialize()
    {
        try
        {
            priceSimulationService.initialize();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Price simulator initialized successfully");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error initializing price simulator: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to initialize price simulator: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Add an underlying asset for price simulation
     */
    @PostMapping("/underlying")
    public ResponseEntity<Map<String, String>> addUnderlying(@RequestParam String underlyingRIC, @RequestParam double priceMean, @RequestParam double priceVariance)
    {
        try
        {
            if (underlyingRIC == null || underlyingRIC.trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Underlying RIC cannot be null or empty"));
            
            priceSimulationService.addUnderlying(underlyingRIC, priceMean, priceVariance);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Underlying " + underlyingRIC + " added successfully");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Invalid request: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.badRequest().body(response);
        }
        catch (Exception e)
        {
            logger.error("Error adding underlying: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to add underlying: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Remove an underlying asset from simulation
     */
    @DeleteMapping("/underlying/{underlyingRIC}")
    public ResponseEntity<Map<String, String>> removeUnderlying(@PathVariable String underlyingRIC)
    {
        try
        {
            priceSimulationService.removeUnderlying(underlyingRIC);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Underlying " + underlyingRIC + " removed successfully");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error removing underlying: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to remove underlying: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Suspend all price simulations
     */
    @PostMapping("/suspend/all")
    public ResponseEntity<Map<String, String>> suspendAll()
    {
        try
        {
            priceSimulationService.suspendAll();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All price simulations suspended");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error suspending all simulations: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to suspend simulations: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Suspend price simulation for a specific underlying
     */
    @PostMapping("/suspend/{underlyingRIC}")
    public ResponseEntity<Map<String, String>> suspendUnderlying(@PathVariable String underlyingRIC)
    {
        try
        {
            priceSimulationService.suspendUnderlying(underlyingRIC);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Price simulation suspended for " + underlyingRIC);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error suspending underlying: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to suspend underlying: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Awaken all price simulations
     */
    @PostMapping("/awaken/all")
    public ResponseEntity<Map<String, String>> awakenAll()
    {
        try
        {
            priceSimulationService.awakenAll();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All price simulations awakened");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error awakening all simulations: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to awaken simulations: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Awaken price simulation for a specific underlying
     */
    @PostMapping("/awaken/{underlyingRIC}")
    public ResponseEntity<Map<String, String>> awakenUnderlying(@PathVariable String underlyingRIC)
    {
        try
        {
            priceSimulationService.awakenUnderlying(underlyingRIC);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Price simulation awakened for " + underlyingRIC);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error awakening underlying: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to awaken underlying: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get current simulated prices for all underlyings
     */
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getCurrentPrices()
    {
        try
        {
            Map<String, Double> prices = priceSimulationService.getCurrentPrices();
            return ResponseEntity.ok(prices);
        }
        catch (Exception e)
        {
            logger.error("Error getting current prices: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get current simulated price for a specific underlying
     */
    @GetMapping("/prices/{underlyingRIC}")
    public ResponseEntity<Map<String, Double>> getCurrentPrice(@PathVariable String underlyingRIC)
    {
        try
        {
            Double price = priceSimulationService.getCurrentPrice(underlyingRIC);
            if (price != null)
            {
                Map<String, Double> response = new HashMap<>();
                response.put(underlyingRIC, price);
                return ResponseEntity.ok(response);
            }
            else
                return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            logger.error("Error getting current price for {}: {}", underlyingRIC, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get list of all underlying RICs being simulated
     */
    @GetMapping("/underlyings")
    public ResponseEntity<List<String>> getUnderlyingRICs()
    {
        try
        {
            List<String> rics = priceSimulationService.getUnderlyingRICs();
            return ResponseEntity.ok(rics);
        }
        catch (Exception e)
        {
            logger.error("Error getting underlying RICs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Terminate the price simulator
     */
    @PostMapping("/terminate")
    public ResponseEntity<Map<String, String>> terminate()
    {
        try
        {
            priceSimulationService.terminate();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Price simulator terminated successfully");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            logger.error("Error terminating price simulator: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to terminate price simulator: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
