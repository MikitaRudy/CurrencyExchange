package com.mikitarudy.util;

import com.mikitarudy.exception.ValidationException;
import com.mikitarudy.model.Currency;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z]{3}");
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z\\s]+");

    public static void checkCode(String code) {
        if (code == null) {
            throw new ValidationException("Currency code cannot be null");
        }
        if (code.length() != 3) {
            throw new ValidationException("Currency code must be exactly 3 characters long");
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new ValidationException("Currency code must contain only uppercase letters");
        }
    }

    public static void checkName(String name) {
        if (name == null) {
            throw new ValidationException("Currency name cannot be null");
        }
        if (name.isEmpty()) {
            throw new ValidationException("Currency name cannot be empty");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException("Currency name must contain only letters and spaces");
        }
        if (name.length() >= 25) {
            throw new ValidationException("Currency name must be shorter than 25 characters");
        }
    }

    public static void checkSign(String sign) {
        if (sign == null) {
            throw new ValidationException("Currency sign cannot be null");
        }
        if (sign.length() == 0) {
            throw new ValidationException("Currency sign cannot be empty");
        }
        if (sign.length() > 4) {
            throw new ValidationException("Currency sign must not exceed 4 characters");
        }
    }

    public static void checkDecimal(String rate) {
        try {
            BigDecimal rateBigDecimal = new BigDecimal(rate);
            if (rateBigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Rate must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Rate must be a number");
        }
    }

    public static void validateCurrency(Currency currency){
        if (currency == null) {
            throw new ValidationException("Currency is null");
        }
        Validator.checkCode(currency.getCode());
        Validator.checkName(currency.getFullName());
        Validator.checkSign(currency.getSign());
    }
}
