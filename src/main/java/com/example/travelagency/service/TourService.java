package com.example.travelagency.service;

import com.example.travelagency.domain.Tour;
import com.example.travelagency.dto.tour.TourRequest;
import com.example.travelagency.exception.NotFoundException;
import com.example.travelagency.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("title", "country", "city", "price", "startDate", "endDate", "availablePlaces");

    private final TourRepository tourRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<Tour> search(String keyword, int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort, direction));
        return tourRepository.search(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Tour getById(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found"));
    }

    @Transactional
    public Tour create(TourRequest request) {
        Tour tour = modelMapper.map(request, Tour.class);
        Tour saved = tourRepository.save(tour);
        log.info("Tour created: id={} title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Transactional
    public Tour update(Long id, TourRequest request) {
        Tour tour = getById(id);
        modelMapper.map(request, tour);
        log.info("Tour updated: id={} title={}", tour.getId(), tour.getTitle());
        return tour;
    }

    @Transactional
    public void delete(Long id) {
        Tour tour = getById(id);
        tourRepository.delete(tour);
        log.warn("Tour deleted: id={} title={}", tour.getId(), tour.getTitle());
    }

    @Transactional
    public void setHot(Long id, boolean hot) {
        Tour tour = getById(id);
        tour.setHot(hot);
        log.info("Tour hot flag changed: id={} hot={}", id, hot);
    }

    private Sort resolveSort(String sort, String direction) {
        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "startDate";
        Sort.Direction safeDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(Sort.Direction.DESC, "hot").and(Sort.by(safeDirection, safeSort));
    }
}
