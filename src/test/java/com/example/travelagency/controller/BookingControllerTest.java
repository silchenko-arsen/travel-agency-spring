package com.example.travelagency.controller;

import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.service.BookingService;
import com.example.travelagency.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class BookingControllerTest {

    private BookingService bookingService;
    private MessageService messageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        messageService = mock(MessageService.class);

        mockMvc = standaloneSetup(
                new BookingController(bookingService, messageService)
        ).build();
    }

    @Test
    void myBookings_shouldReturnBookingsListView() throws Exception {
        when(bookingService.getUserBookings(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/bookings")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/list"))
                .andExpect(model().attributeExists("bookings"));

        verify(bookingService).getUserBookings(eq("user@email.com"), any());
    }

    @Test
    void book_whenSuccess_shouldUseTranslatedSuccessMessage() throws Exception {
        when(messageService.get("booking.success.booked"))
                .thenReturn("Тур успішно заброньовано");

        mockMvc.perform(post("/bookings/tour/1")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("success", "Тур успішно заброньовано"));

        verify(bookingService).bookTour("user@email.com", 1L);
        verify(messageService).get("booking.success.booked");
    }

    @Test
    void book_whenBusinessException_shouldUseTranslatedErrorMessage() throws Exception {
        doThrow(new BusinessException("booking.error.noPlaces"))
                .when(bookingService)
                .bookTour("user@email.com", 1L);

        when(messageService.get("booking.error.noPlaces"))
                .thenReturn("Немає доступних місць для цього туру");

        mockMvc.perform(post("/bookings/tour/1")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("error", "Немає доступних місць для цього туру"));

        verify(messageService).get("booking.error.noPlaces");
    }

    @Test
    void pay_whenSuccess_shouldUseTranslatedSuccessMessage() throws Exception {
        when(messageService.get("booking.success.paid"))
                .thenReturn("Тур успішно оплачено");

        mockMvc.perform(post("/bookings/10/pay")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("success", "Тур успішно оплачено"));

        verify(bookingService).payForBooking("user@email.com", 10L);
        verify(messageService).get("booking.success.paid");
    }

    @Test
    void pay_whenBusinessException_shouldUseTranslatedErrorMessage() throws Exception {
        doThrow(new BusinessException("booking.error.notEnoughBalance"))
                .when(bookingService)
                .payForBooking("user@email.com", 10L);

        when(messageService.get("booking.error.notEnoughBalance"))
                .thenReturn("Недостатньо коштів на балансі");

        mockMvc.perform(post("/bookings/10/pay")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("error", "Недостатньо коштів на балансі"));

        verify(messageService).get("booking.error.notEnoughBalance");
    }

    @Test
    void cancel_whenSuccess_shouldUseTranslatedSuccessMessage() throws Exception {
        when(messageService.get("booking.success.canceled"))
                .thenReturn("Бронювання успішно скасовано");

        mockMvc.perform(post("/bookings/10/cancel")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("success", "Бронювання успішно скасовано"));

        verify(bookingService).cancelUserBooking("user@email.com", 10L);
        verify(messageService).get("booking.success.canceled");
    }

    @Test
    void cancel_whenBusinessException_shouldUseTranslatedErrorMessage() throws Exception {
        doThrow(new BusinessException("booking.error.cancelNotAllowed"))
                .when(bookingService)
                .cancelUserBooking("user@email.com", 10L);

        when(messageService.get("booking.error.cancelNotAllowed"))
                .thenReturn("Тур уже розпочався, скасування недоступне");

        mockMvc.perform(post("/bookings/10/cancel")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("error", "Тур уже розпочався, скасування недоступне"));

        verify(messageService).get("booking.error.cancelNotAllowed");
    }
}