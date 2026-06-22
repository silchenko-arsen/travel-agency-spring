package com.example.travelagency.service;

import com.example.travelagency.domain.Tour;
import com.example.travelagency.dto.tour.TourRequest;
import com.example.travelagency.exception.NotFoundException;
import com.example.travelagency.repository.TourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TourService tourService;

    @Test
    void getById_whenExists_shouldReturnTour() {
        Tour tour = new Tour();
        tour.setTitle("Paris");

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        Tour result = tourService.getById(1L);

        assertThat(result).isEqualTo(tour);
    }

    @Test
    void getById_whenNotExists_shouldThrowNotFoundException() {
        when(tourRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldMapAndSaveTour() {
        TourRequest request = request();

        Tour mappedTour = new Tour();
        mappedTour.setTitle("Paris");

        when(modelMapper.map(request, Tour.class)).thenReturn(mappedTour);
        when(tourRepository.save(mappedTour)).thenReturn(mappedTour);

        Tour result = tourService.create(request);

        assertThat(result).isEqualTo(mappedTour);
        verify(tourRepository).save(mappedTour);
    }

    @Test
    void update_shouldMapRequestToExistingTour() {
        TourRequest request = request();

        Tour existingTour = new Tour();
        existingTour.setTitle("Old title");

        when(tourRepository.findById(1L)).thenReturn(Optional.of(existingTour));

        Tour result = tourService.update(1L, request);

        assertThat(result).isEqualTo(existingTour);
        verify(modelMapper).map(request, existingTour);
    }

    @Test
    void delete_shouldDeleteExistingTour() {
        Tour tour = new Tour();
        tour.setTitle("Paris");

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        tourService.delete(1L);

        verify(tourRepository).delete(tour);
    }

    @Test
    void setHot_shouldChangeHotFlag() {
        Tour tour = new Tour();
        tour.setHot(false);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        tourService.setHot(1L, true);

        assertThat(tour.isHot()).isTrue();
    }

    @Test
    void search_shouldCallRepositorySearch() {
        tourService.search("paris", 0, 8, "price", "desc");

        verify(tourRepository).search(eq("paris"), any(Pageable.class));
    }

    private TourRequest request() {
        TourRequest request = new TourRequest();
        request.setTitle("Paris");
        request.setDescription("Paris tour");
        request.setCountry("France");
        request.setCity("Paris");
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now().plusDays(15));
        request.setPrice(new BigDecimal("1000"));
        request.setAvailablePlaces(10);
        request.setHot(false);
        return request;
    }
}