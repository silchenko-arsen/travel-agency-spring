package com.example.travelagency.exception;

import com.example.travelagency.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException e,
            HttpServletRequest request,
            Model model
    ) {
        fillModel(
                model,
                HttpStatus.BAD_REQUEST,
                messageService.get(e.getCode()),
                request
        );

        return "/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(
            AccessDeniedException e,
            HttpServletRequest request,
            Model model
    ) {
        fillModel(
                model,
                HttpStatus.FORBIDDEN,
                messageService.get("error.accessDenied"),
                request
        );

        return "/error";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(
            NotFoundException e,
            HttpServletRequest request,
            Model model
    ) {
        fillModel(
                model,
                HttpStatus.NOT_FOUND,
                messageService.get("error.notFound"),
                request
        );

        return "/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception e,
            HttpServletRequest request,
            Model model
    ) {
        log.error("Unexpected error", e);

        fillModel(
                model,
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageService.get("error.unexpected"),
                request
        );

        return "/error";
    }

    private void fillModel(
            Model model,
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        model.addAttribute("status", status.value());
        model.addAttribute("error", status.getReasonPhrase());
        model.addAttribute("message", message);
    }
}