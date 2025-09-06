package com.leon.pricing.controller;

import com.leon.pricing.service.PerformanceTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/performance")
@CrossOrigin(origins = "*")
public class PerformanceController 
{
    @Autowired
    private PerformanceTrackingService performanceTrackingService;

    @GetMapping("/range-calculations")
    public ResponseEntity<Map<String, Object>> getRangeCalculationPerformance() 
    {
        try 
        {
            Map<String, Object> performance = performanceTrackingService.getRangeCalculationPerformance();
            return ResponseEntity.ok(performance);
        } 
        catch (Exception e) 
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/range-calculations/reset")
    public ResponseEntity<String> resetPerformanceMetrics() 
    {
        try 
        {
            performanceTrackingService.resetMetrics();
            return ResponseEntity.ok("Performance metrics reset successfully");
        } 
        catch (Exception e) 
        {
            return ResponseEntity.internalServerError().build();
        }
    }
}
