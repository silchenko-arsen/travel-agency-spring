package com.example.travelagency.controller;

import com.example.travelagency.domain.Tour;
import com.example.travelagency.dto.tour.TourRequest;
import com.example.travelagency.service.TourService;
import com.example.travelagency.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
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

class AdminControllerTest {

    private TourService tourService;
    private UserService userService;
    private ModelMapper modelMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tourService = mock(TourService.class);
        userService = mock(UserService.class);
        modelMapper = mock(ModelMapper.class);

        mockMvc = standaloneSetup(
                new AdminController(tourService, userService, modelMapper)
        ).build();
    }

    @Test
    void newTour_shouldReturnTourForm() throws Exception {
        mockMvc.perform(get("/admin/tours/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tour-form"))
                .andExpect(model().attributeExists("tourForm"))
                .andExpect(model().attribute("mode", "create"));
    }

    @Test
    void createTour_whenValid_shouldRedirectToTours() throws Exception {
        mockMvc.perform(post("/admin/tours")
                        .param("title", "Paris")
                        .param("description", "Paris tour")
                        .param("country", "France")
                        .param("city", "Paris")
                        .param("startDate", "2030-01-10")
                        .param("endDate", "2030-01-15")
                        .param("price", "1000")
                        .param("availablePlaces", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"));

        verify(tourService).create(any(TourRequest.class));
    }

    @Test
    void editTour_shouldReturnTourForm() throws Exception {
        Tour tour = new Tour();
        TourRequest request = new TourRequest();

        when(tourService.getById(1L)).thenReturn(tour);
        when(modelMapper.map(tour, TourRequest.class)).thenReturn(request);

        mockMvc.perform(get("/admin/tours/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tour-form"))
                .andExpect(model().attribute("tourId", 1L))
                .andExpect(model().attribute("tourForm", request))
                .andExpect(model().attribute("mode", "edit"));
    }

    @Test
    void deleteTour_shouldRedirectToTours() throws Exception {
        mockMvc.perform(post("/admin/tours/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"));

        verify(tourService).delete(1L);
    }

    @Test
    void users_shouldReturnAdminUsersView() throws Exception {
        when(userService.searchUsers(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admin/users").param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("keyword", "test"));
    }

    @Test
    void blockUser_shouldRedirectToAdminUsers() throws Exception {
        mockMvc.perform(post("/admin/users/1/block")
                        .param("blocked", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService).setBlocked(1L, true);
    }
}