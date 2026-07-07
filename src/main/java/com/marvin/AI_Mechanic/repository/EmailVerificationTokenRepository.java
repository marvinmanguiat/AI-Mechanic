package com.marvin.AI_Mechanic.repository;

import com.marvin.AI_Mechanic.model.AppUser;
import com.marvin.AI_Mechanic.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser(AppUser user);
}
