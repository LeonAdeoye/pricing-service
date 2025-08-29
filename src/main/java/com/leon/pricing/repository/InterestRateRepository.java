package com.leon.pricing.repository;

import com.leon.pricing.model.InterestRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterestRateRepository extends MongoRepository<InterestRate, UUID>
{
    Optional<InterestRate> findByCurrencyCode(String currencyCode);
}
