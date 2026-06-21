package com.example.travelagency.dto.tour;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TourRequest {

    @NotBlank(message = "{validation.tour.title.required}")
    private String title;

    @NotBlank(message = "{validation.tour.description.required}")
    private String description;

    @NotBlank(message = "{validation.tour.country.required}")
    private String country;

    @NotBlank(message = "{validation.tour.city.required}")
    private String city;

    @NotNull(message = "{validation.tour.start.required}")
    @FutureOrPresent(message = "{validation.tour.start.future}")
    private LocalDate startDate;

    @NotNull(message = "{validation.tour.end.required}")
    @Future(message = "{validation.tour.end.future}")
    private LocalDate endDate;

    @NotNull(message = "{validation.tour.price.required}")
    @DecimalMin(value = "0.01", message = "{validation.tour.price.min}")
    private BigDecimal price;

    @Min(value = 1, message = "{validation.tour.places.min}")
    private int availablePlaces;

    private boolean hot;

    @AssertTrue(message = "{validation.tour.dates.invalid}")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return startDate.isBefore(endDate);
    }
}