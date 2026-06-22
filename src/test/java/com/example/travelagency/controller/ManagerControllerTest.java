package com.example.travelagency.controller;

import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.service.BookingService;
import com.example.travelagency.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ManagerControllerTest {

    private TourService tourService;
    private BookingService bookingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tourService = mock(TourService.class);
        bookingService = mock(BookingService.class);

        mockMvc = standaloneSetup(
                new ManagerController(tourService, bookingService)
        ).build();
    }

    @Test
    void setHot_shouldRedirectToTours() throws Exception {
        mockMvc.perform(post("/manager/tours/1/hot")
                        .param("hot", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"));

        verify(tourService).setHot(1L, true);
    }

    @Test
    void bookings_shouldReturnManagerBookingsView() throws Exception {
        when(bookingService.searchAllBookings(anyString(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/manager/bookings")
                        .param("keyword", "paris")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/bookings"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("keyword", "paris"))
                .andExpect(model().attribute("selectedStatus", "PAID"));

        verify(bookingService).searchAllBookings(eq("paris"), eq(BookingStatus.PAID), any());
    }

    @Test
    void changeStatus_shouldRedirectWithFilters() throws Exception {
        mockMvc.perform(post("/manager/bookings/10/status")
                        .param("status", "CANCELED")
                        .param("keyword", "Paris tour")
                        .param("selectedStatus", "PAID")
                        .param("page", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings?page=2&keyword=Paris+tour&status=PAID"));

        verify(bookingService).changeStatus(10L, BookingStatus.CANCELED);
    }
}