package com.example.travelagency.controller;

import com.example.travelagency.dto.tour.TourRequest;
import com.example.travelagency.service.TourService;
import com.example.travelagency.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TourService tourService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/tours/new")
    public String newTour(Model model) {
        model.addAttribute("tourForm", new TourRequest());
        model.addAttribute("mode", "create");
        return "admin/tour-form";
    }

    @PostMapping("/tours")
    public String createTour(@Valid @ModelAttribute("tourForm") TourRequest request,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/tour-form";
        }
        tourService.create(request);
        return "redirect:/tours";
    }

    @GetMapping("/tours/{id}/edit")
    public String editTour(@PathVariable Long id, Model model) {
        model.addAttribute("tourId", id);
        model.addAttribute("tourForm", modelMapper.map(tourService.getById(id), TourRequest.class));
        model.addAttribute("mode", "edit");
        return "admin/tour-form";
    }

    @PostMapping("/tours/{id}")
    public String updateTour(@PathVariable Long id,
                             @Valid @ModelAttribute("tourForm") TourRequest request,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tourId", id);
            model.addAttribute("mode", "edit");
            return "admin/tour-form";
        }
        tourService.update(id, request);
        return "redirect:/tours/" + id;
    }

    @PostMapping("/tours/{id}/delete")
    public String deleteTour(@PathVariable Long id) {
        tourService.delete(id);
        return "redirect:/tours";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        model.addAttribute("users", userService.searchUsers(keyword, PageRequest.of(page, size)));
        model.addAttribute("keyword", keyword);
        return "admin/users";
    }

    @PostMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id, @RequestParam boolean blocked) {
        userService.setBlocked(id, blocked);
        return "redirect:/admin/users";
    }
}
