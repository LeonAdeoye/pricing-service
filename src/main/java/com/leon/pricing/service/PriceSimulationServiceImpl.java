package com.leon.pricing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class PriceSimulationServiceImpl implements PriceSimulationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceSimulationServiceImpl.class);
    
    private final Map<String, PriceGenerator> priceGenerators = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean isRunning = false;
    private volatile boolean isSuspended = false;
    
    private static class PriceGenerator {
        private final double priceMean;
        private final double priceVariance;
        private volatile boolean isAwake = true;
        private volatile double currentPrice;
        private final Random random = new Random();
        
        public PriceGenerator(double priceMean, double priceVariance) {
            this.priceMean = priceMean;
            this.priceVariance = priceVariance;
            this.currentPrice = priceMean;
        }
        
        public void suspend() {
            this.isAwake = false;
        }
        
        public void awaken() {
            this.isAwake = true;
        }
        
        public boolean isAwake() {
            return this.isAwake;
        }
        
        public double getCurrentPrice() {
            return currentPrice;
        }
        
        public void generateNextPrice() {
            if (isAwake) {
                // Generate new price using normal distribution
                double newPrice = priceMean + (random.nextGaussian() * priceVariance);
                // Ensure price doesn't go negative
                this.currentPrice = Math.max(newPrice, priceMean * 0.1);
            }
        }
    }
    
    @Override
    public void initialize() {
        if (isRunning) {
            logger.warn("Price simulator is already running");
            return;
        }
        
        isRunning = true;
        isSuspended = false;
        
        // Schedule price updates every 1 second
        scheduler.scheduleAtFixedRate(this::updatePrices, 0, 1, TimeUnit.SECONDS);
        
        logger.info("Price simulator initialized and started");
    }
    
    /**
     * Adds an underlying asset for price simulation.
     * 
     * NOTE: For production use, consider:
     * - Query external market data service for current prices and volatility
     * - Use real-time volatility data for more accurate price variance
     * - Consider market hours and trading sessions
     * - Validate RIC format and existence in market data
     */
    @Override
    public void addUnderlying(String underlyingRIC, double priceMean, double priceVariance) {
        if (underlyingRIC == null || underlyingRIC.trim().isEmpty()) {
            throw new IllegalArgumentException("Underlying RIC cannot be null or empty");
        }
        
        if (priceMean <= 0) {
            throw new IllegalArgumentException("Price mean must be greater than 0");
        }
        
        if (priceVariance <= 0) {
            throw new IllegalArgumentException("Price variance must be greater than 0");
        }
        
        PriceGenerator generator = new PriceGenerator(priceMean, priceVariance);
        priceGenerators.put(underlyingRIC, generator);
        
        logger.info("Added underlying {} with mean={}, variance={}", underlyingRIC, priceMean, priceVariance);
    }
    
    @Override
    public void removeUnderlying(String underlyingRIC) {
        if (underlyingRIC == null || underlyingRIC.trim().isEmpty()) {
            throw new IllegalArgumentException("Underlying RIC cannot be null or empty");
        }
        
        PriceGenerator removed = priceGenerators.remove(underlyingRIC);
        if (removed != null) {
            logger.info("Removed underlying {}", underlyingRIC);
        } else {
            logger.warn("Underlying {} not found for removal", underlyingRIC);
        }
    }
    
    @Override
    public void suspendAll() {
        isSuspended = true;
        priceGenerators.values().forEach(PriceGenerator::suspend);
        logger.info("All price simulations suspended");
    }
    
    @Override
    public void suspendUnderlying(String underlyingRIC) {
        if (underlyingRIC == null || underlyingRIC.trim().isEmpty()) {
            throw new IllegalArgumentException("Underlying RIC cannot be null or empty");
        }
        
        PriceGenerator generator = priceGenerators.get(underlyingRIC);
        if (generator != null) {
            generator.suspend();
            logger.info("Price simulation suspended for underlying {}", underlyingRIC);
        } else {
            logger.warn("Underlying {} not found for suspension", underlyingRIC);
        }
    }
    
    @Override
    public void awakenAll() {
        isSuspended = false;
        priceGenerators.values().forEach(PriceGenerator::awaken);
        logger.info("All price simulations awakened");
    }
    
    @Override
    public void awakenUnderlying(String underlyingRIC) {
        if (underlyingRIC == null || underlyingRIC.trim().isEmpty()) {
            throw new IllegalArgumentException("Underlying RIC cannot be null or empty");
        }
        
        PriceGenerator generator = priceGenerators.get(underlyingRIC);
        if (generator != null) {
            generator.awaken();
            logger.info("Price simulation awakened for underlying {}", underlyingRIC);
        } else {
            logger.warn("Underlying {} not found for awakening", underlyingRIC);
        }
    }
    
    @Override
    public Map<String, Double> getCurrentPrices() {
        Map<String, Double> prices = new HashMap<>();
        priceGenerators.forEach((ric, generator) -> 
            prices.put(ric, generator.getCurrentPrice()));
        return prices;
    }
    
    @Override
    public Double getCurrentPrice(String underlyingRIC) {
        if (underlyingRIC == null || underlyingRIC.trim().isEmpty()) {
            throw new IllegalArgumentException("Underlying RIC cannot be null or empty");
        }
        
        PriceGenerator generator = priceGenerators.get(underlyingRIC);
        return generator != null ? generator.getCurrentPrice() : null;
    }
    
    @Override
    public List<String> getUnderlyingRICs() {
        return new ArrayList<>(priceGenerators.keySet());
    }
    
    @Override
    public void terminate() {
        isRunning = false;
        isSuspended = true;
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Clear all generators
        priceGenerators.clear();
        
        logger.info("Price simulator terminated");
    }
    
    private void updatePrices() {
        if (!isRunning || isSuspended) {
            return;
        }
        
        try {
            priceGenerators.values().forEach(PriceGenerator::generateNextPrice);
        } catch (Exception e) {
            logger.error("Error updating prices: {}", e.getMessage(), e);
        }
    }
    
    public void destroy() {
        terminate();
    }
}
