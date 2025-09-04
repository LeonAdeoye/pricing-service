package com.leon.pricing.repository;

import com.leon.pricing.model.Price;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceRepository extends MongoRepository<Price, UUID>
{
    Optional<Price> findByInstrumentCode(String instrumentCode);
}
