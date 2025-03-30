package com.mikitarudy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CurrencyRequestDTO {
    private String name;
    private String code;
    private String sign;
}
