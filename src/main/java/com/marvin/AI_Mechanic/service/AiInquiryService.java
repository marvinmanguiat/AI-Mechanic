package com.marvin.AI_Mechanic.service;

import com.marvin.AI_Mechanic.dto.DiagnosisRequest;
import com.marvin.AI_Mechanic.model.AiInquiry;
import com.marvin.AI_Mechanic.model.AppUser;
import com.marvin.AI_Mechanic.repository.AiInquiryRepository;
import com.marvin.AI_Mechanic.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class AiInquiryService {

    public static final long DAILY_LIMIT = 5;

    private final AiInquiryRepository aiInquiryRepository;
    private final AppUserRepository appUserRepository;

    public AiInquiryService(AiInquiryRepository aiInquiryRepository,
                            AppUserRepository appUserRepository) {
        this.aiInquiryRepository = aiInquiryRepository;
        this.appUserRepository = appUserRepository;
    }

    public void saveInquiry(String username, DiagnosisRequest request, String aiResponse) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        AiInquiry inquiry = new AiInquiry();
        inquiry.setUser(user);
        inquiry.setMake(request.getMake());
        inquiry.setModel(request.getModel());
        inquiry.setYear(request.getYear());
        inquiry.setEngine(request.getEngine());
        inquiry.setTransmission(request.getTransmission());
        inquiry.setSymptoms(String.join("\n", request.getSymptoms()));
        inquiry.setAiResponse(aiResponse);

        aiInquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public long getTodayInquiryCount(String username) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Instant from = today.atStartOfDay(zone).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(zone).toInstant();

        return aiInquiryRepository.countByUserUsernameAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            username,
            from,
            to
        );
    }

    @Transactional(readOnly = true)
    public long getRemainingToday(String username) {
        long used = getTodayInquiryCount(username);
        return Math.max(0, DAILY_LIMIT - used);
    }

    @Transactional(readOnly = true)
    public boolean hasReachedDailyLimit(String username) {
        return getTodayInquiryCount(username) >= DAILY_LIMIT;
    }

    @Transactional(readOnly = true)
    public List<AiInquiry> getInquiryHistory(String username) {
        return aiInquiryRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }
}