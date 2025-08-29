package com.leon.pricing.repository;

import com.leon.pricing.model.Volatility;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface VolatilityRepository extends MongoRepository<Volatility, UUID> {

    Optional<Volatility> findByInstrumentCode(String instrumentCode);

    boolean existsByInstrumentCode(String instrumentCode);
}
