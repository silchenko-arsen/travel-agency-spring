package com.example.travelagency.controller;

import com.example.travelagency.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public String myBookings(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Authentication authentication,
                             Model model) {
        model.addAttribute("bookings", bookingService.getUserBookings(authentication.getName(), PageRequest.of(page, size)));
        return "bookings/list";
    }

    @PostMapping("/tour/{tourId}")
    public String book(@PathVariable Long tourId, Authentication authentication, RedirectAttributes redirectAttributes) {
        bookingService.bookTour(authentication.getName(), tourId);
        redirectAttributes.addFlashAttribute("success", "Tour booked.");
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        bookingService.payForBooking(authentication.getName(), id);
        redirectAttributes.addFlashAttribute("success", "Tour paid.");
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        bookingService.cancelUserBooking(authentication.getName(), id);
        redirectAttributes.addFlashAttribute("success", "Booking canceled.");
        return "redirect:/bookings";
    }
}
