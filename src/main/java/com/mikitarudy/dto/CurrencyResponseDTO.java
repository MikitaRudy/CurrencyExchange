package com.mikitarudy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CurrencyResponseDTO {
    private Integer id;
    private String name;
    private String code;
    private String sign;
}
