package com.example.travelagency.service;

import com.example.travelagency.domain.Tour;
import com.example.travelagency.dto.tour.TourRequest;
import org.springframework.data.domain.Page;

public interface TourService {

    Page<Tour> search(String keyword, int page, int size, String sort, String direction);

    Tour getById(Long id);

    Tour create(TourRequest request);

    Tour update(Long id, TourRequest request);

    void delete(Long id);

    void setHot(Long id, boolean hot);
}