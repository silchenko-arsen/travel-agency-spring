package com.example.travelagency;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class TravelAgencyApplicationTest {

    @Test
    void main_shouldRunSpringApplication() {
        String[] args = {"--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            TravelAgencyApplication.main(args);

            springApplication.verify(() ->
                    SpringApplication.run(TravelAgencyApplication.class, args)
            );
        }
    }
}