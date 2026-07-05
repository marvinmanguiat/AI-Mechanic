package com.marvin.AI_Mechanic.config;

import com.marvin.AI_Mechanic.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    
    @Autowired
    private CarService carService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> carService.initializeSampleData();
    }
}
