package com.leon.pricing.service;

import java.util.List;
import java.util.Map;

public interface PriceSimulationService {
    
    /**
     * Initialize the price simulator
     */
    void initialize();
    
    /**
     * Add an underlying asset for price simulation
     */
    void addUnderlying(String underlyingRIC, double priceMean, double priceVariance);
    
    /**
     * Remove an underlying asset from simulation
     */
    void removeUnderlying(String underlyingRIC);
    
    /**
     * Suspend all price simulations
     */
    void suspendAll();
    
    /**
     * Suspend price simulation for a specific underlying
     */
    void suspendUnderlying(String underlyingRIC);
    
    /**
     * Awaken all price simulations
     */
    void awakenAll();
    
    /**
     * Awaken price simulation for a specific underlying
     */
    void awakenUnderlying(String underlyingRIC);
    
    /**
     * Get current simulated prices for all underlyings
     */
    Map<String, Double> getCurrentPrices();
    
    /**
     * Get current simulated price for a specific underlying
     */
    Double getCurrentPrice(String underlyingRIC);
    
    /**
     * Get list of all underlying RICs being simulated
     */
    List<String> getUnderlyingRICs();
    
    /**
     * Terminate the price simulator
     */
    void terminate();
}
