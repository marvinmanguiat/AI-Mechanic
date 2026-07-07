package com.marvin.AI_Mechanic.config;

import com.marvin.AI_Mechanic.model.Role;
import com.marvin.AI_Mechanic.service.AuthService;
import com.marvin.AI_Mechanic.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    
    @Autowired
    private CarService carService;

    @Autowired
    private AuthService authService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            carService.initializeSampleData();
            authService.createSeedUserIfMissing("admin", "admin@ai-mechanic.local", "admin123", Role.ADMIN);
            authService.createSeedUserIfMissing("user", "user@ai-mechanic.local", "user123", Role.USER);
            authService.createSeedUserIfMissing("mechanic", "mechanic@ai-mechanic.local", "mechanic123", Role.MECHANIC);
        };
    }
}
