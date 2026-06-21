package com.example.travelagency.controller;

import com.example.travelagency.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping({"/", "/tours"})
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "8") int size,
                       @RequestParam(defaultValue = "startDate") String sort,
                       @RequestParam(defaultValue = "asc") String direction,
                       Model model) {
        model.addAttribute("tours", tourService.search(keyword, page, size, sort, direction));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);
        return "tours/list";
    }

    @GetMapping("/tours/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("tour", tourService.getById(id));
        return "tours/detail";
    }
}
