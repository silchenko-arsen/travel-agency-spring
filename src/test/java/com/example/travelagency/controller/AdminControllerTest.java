package com.example.travelagency.controller;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import com.example.travelagency.domain.Tour;
import com.example.travelagency.dto.tour.TourRequest;
import com.example.travelagency.service.TourService;
import com.example.travelagency.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
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

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(
                new AdminController(tourService, userService, modelMapper)
        )
                .setValidator(validator)
                .build();
    }

    @Test
    void newTour_shouldReturnTourFormView() throws Exception {
        mockMvc.perform(get("/admin/tours/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tour-form"))
                .andExpect(model().attributeExists("tourForm"))
                .andExpect(model().attribute("mode", "create"));
    }

    @Test
    void createTour_whenValid_shouldCreateTourAndRedirectToTours() throws Exception {
        mockMvc.perform(post("/admin/tours")
                        .param("title", "Paris")
                        .param("description", "Paris tour")
                        .param("country", "France")
                        .param("city", "Paris")
                        .param("startDate", LocalDate.now().plusDays(10).toString())
                        .param("endDate", LocalDate.now().plusDays(15).toString())
                        .param("price", "1000")
                        .param("availablePlaces", "10")
                        .param("hot", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"));

        verify(tourService).create(any(TourRequest.class));
    }

    @Test
    void createTour_whenValidationErrors_shouldReturnTourFormView() throws Exception {
        mockMvc.perform(post("/admin/tours")
                        .param("title", "")
                        .param("description", "")
                        .param("country", "")
                        .param("city", "")
                        .param("startDate", LocalDate.now().minusDays(1).toString())
                        .param("endDate", LocalDate.now().minusDays(2).toString())
                        .param("price", "0")
                        .param("availablePlaces", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tour-form"))
                .andExpect(model().attribute("mode", "create"))
                .andExpect(model().attributeHasFieldErrors(
                        "tourForm",
                        "title",
                        "description",
                        "country",
                        "city",
                        "startDate",
                        "endDate",
                        "price",
                        "availablePlaces"
                ));
    }

    @Test
    void editTour_shouldReturnTourFormView() throws Exception {
        Tour tour = tour();

        TourRequest tourRequest = new TourRequest();
        tourRequest.setTitle("Paris");
        tourRequest.setDescription("Paris tour");
        tourRequest.setCountry("France");
        tourRequest.setCity("Paris");
        tourRequest.setStartDate(LocalDate.now().plusDays(10));
        tourRequest.setEndDate(LocalDate.now().plusDays(15));
        tourRequest.setPrice(new BigDecimal("1000"));
        tourRequest.setAvailablePlaces(10);
        tourRequest.setHot(true);

        when(tourService.getById(1L)).thenReturn(tour);
        when(modelMapper.map(tour, TourRequest.class)).thenReturn(tourRequest);

        mockMvc.perform(get("/admin/tours/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tour-form"))
                .andExpect(model().attribute("tourId", 1L))
                .andExpect(model().attribute("tourForm", tourRequest))
                .andExpect(model().attribute("mode", "edit"));

        verify(tourService).getById(1L);
        verify(modelMapper).map(tour, TourRequest.class);
    }

    @Test
    void updateTour_whenValid_shouldUpdateTourAndRedirectToTourDetails() throws Exception {
        mockMvc.perform(post("/admin/tours/1")
                        .param("title", "Updated Paris")
                        .param("description", "Updated Paris tour")
                        .param("country", "France")
                        .param("city", "Paris")
                        .param("startDate", LocalDate.now().plusDays(10).toString())
                        .param("endDate", LocalDate.now().plusDays(15).toString())
                        .param("price", "1500")
                        .param("availablePlaces", "12")
                        .param("hot", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours/1"));

        verify(tourService).update(eq(1L), any(TourRequest.class));
    }

    @Test
    void updateTour_whenValidationErrors_shouldReturnTourFormView() throws Exception {
        mockMvc.perform(post("/admin/tours/1")
                        .param("title", "")
                        .param("description", "")
                        .param("country", "")
                        .param("city", "")
                        .param("startDate", LocalDate.now().minusDays(1).toString())
                        .param("endDate", LocalDate.now().minusDays(2).toString())
                        .param("price", "0")
                        .param("availablePlaces", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tour-form"))
                .andExpect(model().attribute("tourId", 1L))
                .andExpect(model().attribute("mode", "edit"))
                .andExpect(model().attributeHasFieldErrors(
                        "tourForm",
                        "title",
                        "description",
                        "country",
                        "city",
                        "startDate",
                        "endDate",
                        "price",
                        "availablePlaces"
                ));
    }

    @Test
    void deleteTour_shouldDeleteTourAndRedirectToTours() throws Exception {
        mockMvc.perform(post("/admin/tours/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"));

        verify(tourService).delete(1L);
    }

    @Test
    void users_shouldReturnUsersView() throws Exception {
        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setFirstName("Arsen");
        user.setLastName("Silchenko");
        user.setRole(Role.ROLE_USER);

        when(userService.searchUsers(eq("arsen"), any()))
                .thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/admin/users")
                        .param("keyword", "arsen")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("keyword", "arsen"));

        verify(userService).searchUsers(eq("arsen"), any());
    }

    @Test
    void blockUser_shouldChangeBlockedStatusAndRedirectToUsers() throws Exception {
        mockMvc.perform(post("/admin/users/1/block")
                        .param("blocked", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService).setBlocked(1L, true);
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
                .hot(true)
                .build();
    }
}