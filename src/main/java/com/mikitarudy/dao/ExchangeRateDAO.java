package com.mikitarudy.dao;

import com.mikitarudy.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRateDAO extends CrudDAO<ExchangeRate> {
    Optional<ExchangeRate> findByCode(String baseCurrencyCode, String targetCurrencyCode);
    int update (ExchangeRate t);
}
