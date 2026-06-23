package com.example.travelagency.exception;

import com.example.travelagency.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private MessageService messageService;
    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;
    private Model model;

    @BeforeEach
    void setUp() {
        messageService = mock(MessageService.class);
        handler = new GlobalExceptionHandler(messageService);
        request = new MockHttpServletRequest("GET", "/test");
        model = new ExtendedModelMap();
    }

    @Test
    void handleBusinessException_shouldFillBadRequestModel() {
        when(messageService.get("booking.error.noPlaces"))
                .thenReturn("Немає доступних місць");

        String view = handler.handleBusinessException(
                new BusinessException("booking.error.noPlaces"),
                request,
                model
        );

        assertThat(view).isEqualTo("/error");
        assertThat(model.asMap()).containsEntry("status", 400);
        assertThat(model.asMap()).containsEntry("error", "Bad Request");
        assertThat(model.asMap()).containsEntry("message", "Немає доступних місць");
    }

    @Test
    void handleAccessDenied_shouldFillForbiddenModel() {
        when(messageService.get("error.accessDenied"))
                .thenReturn("Доступ заборонено");

        String view = handler.handleAccessDenied(
                new AccessDeniedException("denied"),
                request,
                model
        );

        assertThat(view).isEqualTo("/error");
        assertThat(model.asMap()).containsEntry("status", 403);
        assertThat(model.asMap()).containsEntry("error", "Forbidden");
        assertThat(model.asMap()).containsEntry("message", "Доступ заборонено");
    }

    @Test
    void handleNotFoundException_shouldFillNotFoundModel() {
        when(messageService.get("error.notFound"))
                .thenReturn("Сторінку не знайдено");

        String view = handler.handleNotFoundException(
                new NotFoundException("Tour not found"),
                request,
                model
        );

        assertThat(view).isEqualTo("/error");
        assertThat(model.asMap()).containsEntry("status", 404);
        assertThat(model.asMap()).containsEntry("error", "Not Found");
        assertThat(model.asMap()).containsEntry("message", "Сторінку не знайдено");
    }

    @Test
    void handleException_shouldFillInternalServerErrorModel() {
        when(messageService.get("error.unexpected"))
                .thenReturn("Непередбачена помилка");

        String view = handler.handleException(
                new RuntimeException("boom"),
                request,
                model
        );

        assertThat(view).isEqualTo("/error");
        assertThat(model.asMap()).containsEntry("status", 500);
        assertThat(model.asMap()).containsEntry("error", "Internal Server Error");
        assertThat(model.asMap()).containsEntry("message", "Непередбачена помилка");
    }
}