package com.example.travelagency.controller;

import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.service.BookingService;
import com.example.travelagency.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final TourService tourService;
    private final BookingService bookingService;

    @PostMapping("/tours/{id}/hot")
    public String setHot(@PathVariable Long id, @RequestParam boolean hot) {
        tourService.setHot(id, hot);
        return "redirect:/tours";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings(PageRequest.of(page, size)));
        model.addAttribute("statuses", BookingStatus.values());
        return "manager/bookings";
    }

    @PostMapping("/bookings/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        bookingService.changeRegisteredStatus(id, status);
        return "redirect:/manager/bookings";
    }
}
