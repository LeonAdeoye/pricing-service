package com.leon.pricing.service;

import com.leon.pricing.model.InterestRate;
import java.util.List;

public interface InterestRateService
{
    List<InterestRate> loadRates();
    InterestRate updateRate(String currencyCode, Double interestRatePercentage, String lastUpdatedBy);
    InterestRate getRate(String currencyCode);

}
