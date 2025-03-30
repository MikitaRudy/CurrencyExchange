package com.mikitarudy.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Currency {
    private Integer id;
    private String code;
    private String fullName;
    private String sign;

    public Currency(String code, String fullName, String sign) {
        this.code = code;
        this.fullName = fullName;
        this.sign = sign;
    }
}
