package com.marvin.AI_Mechanic.repository;

import com.marvin.AI_Mechanic.model.AiInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiInquiryRepository extends JpaRepository<AiInquiry, Long> {
    List<AiInquiry> findByUserUsernameOrderByCreatedAtDesc(String username);

    long countByUserUsernameAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        String username,
        java.time.Instant from,
        java.time.Instant to
    );
}