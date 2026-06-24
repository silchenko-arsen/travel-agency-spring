package com.example.travelagency.controller;

import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.service.BookingService;
import com.example.travelagency.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerController {

    private final TourService tourService;
    private final BookingService bookingService;

    @PostMapping("/tours/{id}/hot")
    public String setHot(@PathVariable Long id, @RequestParam boolean hot) {
        tourService.setHot(id, hot);
        return "redirect:/tours";
    }

    @GetMapping("/bookings")
    public String bookings(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        BookingStatus selectedStatus = parseStatus(status);

        model.addAttribute(
                "bookings",
                bookingService.searchAllBookings(keyword, selectedStatus, PageRequest.of(page, size))
        );
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("size", size);

        return "manager/bookings";
    }

    @PostMapping("/bookings/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String selectedStatus,
            @RequestParam(defaultValue = "0") int page
    ) {
        bookingService.changeStatus(id, status);

        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        String redirect = "redirect:/manager/bookings?page=" + page + "&keyword=" + encodedKeyword;

        if (selectedStatus != null && !selectedStatus.isBlank()) {
            redirect += "&status=" + selectedStatus;
        }

        return redirect;
    }

    private BookingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return BookingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}