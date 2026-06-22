package com.example.travelagency.controller;

import com.example.travelagency.domain.Tour;
import com.example.travelagency.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TourControllerTest {

    private TourService tourService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tourService = mock(TourService.class);
        mockMvc = standaloneSetup(new TourController(tourService)).build();
    }

    @Test
    void list_shouldReturnToursListView() throws Exception {
        when(tourService.search(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(tour())));

        mockMvc.perform(get("/tours")
                        .param("keyword", "paris")
                        .param("page", "0")
                        .param("size", "8")
                        .param("sort", "price")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("tours/list"))
                .andExpect(model().attributeExists("tours"))
                .andExpect(model().attribute("keyword", "paris"))
                .andExpect(model().attribute("sort", "price"))
                .andExpect(model().attribute("direction", "desc"))
                .andExpect(model().attribute("size", 8));

        verify(tourService).search("paris", 0, 8, "price", "desc");
    }

    @Test
    void details_shouldReturnTourDetailView() throws Exception {
        Tour tour = tour();

        when(tourService.getById(1L)).thenReturn(tour);

        mockMvc.perform(get("/tours/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("tours/detail"))
                .andExpect(model().attribute("tour", tour));
    }

    private Tour tour() {
        return Tour.builder()
                .title("Paris")
                .description("Paris tour")
                .country("France")
                .city("Paris")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(15))
                .price(new BigDecimal("1000"))
                .availablePlaces(10)
                .build();
    }
}