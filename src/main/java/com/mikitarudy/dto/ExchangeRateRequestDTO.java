package com.mikitarudy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public final class ExchangeRateRequestDTO {
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal rate;
}
