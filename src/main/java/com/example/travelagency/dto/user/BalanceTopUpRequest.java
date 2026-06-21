package com.example.travelagency.dto.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BalanceTopUpRequest {
    @NotNull(message = "{validation.balance.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.balance.amount.min}")
    private BigDecimal amount;
}
