package com.mikitarudy.dao;

import com.mikitarudy.model.Currency;

import java.util.Optional;

public interface CurrencyDAO extends CrudDAO<Currency> {
    Optional<Currency> findByCode(String code);
}
