package com.example.travelagency.controller;

import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.service.BookingService;
import com.example.travelagency.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

class ManagerControllerTest {

    private TourService tourService;
    private BookingService bookingService;
    private ManagerController managerController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tourService = mock(TourService.class);
        bookingService = mock(BookingService.class);

        managerController = new ManagerController(tourService, bookingService);

        mockMvc = standaloneSetup(managerController).build();
    }

    @Test
    void bookings_whenNoParams_shouldReturnBookingsView() throws Exception {
        when(bookingService.searchAllBookings(eq(""), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/manager/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/bookings"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attribute("selectedStatus", ""));

        verify(bookingService).searchAllBookings(eq(""), eq(null), any());
    }

    @Test
    void bookings_whenStatusIsBlank_shouldPassNullStatus() throws Exception {
        when(bookingService.searchAllBookings(eq(""), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/manager/bookings")
                        .param("status", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/bookings"))
                .andExpect(model().attribute("selectedStatus", ""));

        verify(bookingService).searchAllBookings(eq(""), eq(null), any());
    }

    @Test
    void bookings_whenStatusIsInvalid_shouldPassNullStatus() throws Exception {
        when(bookingService.searchAllBookings(eq("paris"), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/manager/bookings")
                        .param("keyword", "paris")
                        .param("status", "WRONG"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/bookings"))
                .andExpect(model().attribute("keyword", "paris"))
                .andExpect(model().attribute("selectedStatus", "WRONG"));

        verify(bookingService).searchAllBookings(eq("paris"), eq(null), any());
    }

    @Test
    void bookings_whenStatusIsValid_shouldPassParsedStatus() throws Exception {
        when(bookingService.searchAllBookings(eq("paris"), eq(BookingStatus.PAID), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/manager/bookings")
                        .param("keyword", "paris")
                        .param("status", "PAID")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/bookings"))
                .andExpect(model().attribute("keyword", "paris"))
                .andExpect(model().attribute("selectedStatus", "PAID"));

        verify(bookingService).searchAllBookings(eq("paris"), eq(BookingStatus.PAID), any());
    }

    @Test
    void changeStatus_whenSelectedStatusIsNull_shouldRedirectWithoutStatusParam() {
        String result = managerController.changeStatus(
                10L,
                BookingStatus.PAID,
                "hot tour",
                null,
                2
        );

        assertThat(result).isEqualTo(
                "redirect:/manager/bookings?page=2&keyword=hot+tour"
        );

        verify(bookingService).changeStatus(10L, BookingStatus.PAID);
    }

    @Test
    void changeStatus_whenSelectedStatusIsBlank_shouldRedirectWithoutStatusParam() throws Exception {
        mockMvc.perform(post("/manager/bookings/10/status")
                        .param("status", "PAID")
                        .param("keyword", "hot tour")
                        .param("selectedStatus", "")
                        .param("page", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings?page=2&keyword=hot+tour"));

        verify(bookingService).changeStatus(10L, BookingStatus.PAID);
    }

    @Test
    void changeStatus_whenSelectedStatusIsNotBlank_shouldRedirectWithStatusParam() throws Exception {
        mockMvc.perform(post("/manager/bookings/10/status")
                        .param("status", "CANCELED")
                        .param("keyword", "hot tour")
                        .param("selectedStatus", "PAID")
                        .param("page", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings?page=2&keyword=hot+tour&status=PAID"));

        verify(bookingService).changeStatus(10L, BookingStatus.CANCELED);
    }

    @Test
    void setHot_shouldChangeHotFlagAndRedirectToTourDetails() throws Exception {
        mockMvc.perform(post("/manager/tours/1/hot")
                        .param("hot", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"));

        verify(tourService).setHot(1L, true);
    }

    @Test
    void parseStatus_whenStatusIsNull_shouldReturnNull() {
        BookingStatus result = ReflectionTestUtils.invokeMethod(
                managerController,
                "parseStatus",
                (String) null
        );

        assertThat(result).isNull();
    }

    @Test
    void parseStatus_whenStatusIsBlank_shouldReturnNull() {
        BookingStatus result = ReflectionTestUtils.invokeMethod(
                managerController,
                "parseStatus",
                "   "
        );

        assertThat(result).isNull();
    }

    @Test
    void parseStatus_whenStatusIsValid_shouldReturnBookingStatus() {
        BookingStatus result = ReflectionTestUtils.invokeMethod(
                managerController,
                "parseStatus",
                "PAID"
        );

        assertThat(result).isEqualTo(BookingStatus.PAID);
    }

    @Test
    void parseStatus_whenStatusIsInvalid_shouldReturnNull() {
        BookingStatus result = ReflectionTestUtils.invokeMethod(
                managerController,
                "parseStatus",
                "WRONG_STATUS"
        );

        assertThat(result).isNull();
    }
}