package com.mikitarudy.service;

import com.mikitarudy.dao.JdbcCurrencyDAO;
import com.mikitarudy.dao.JdbcExchangeRateDAO;
import com.mikitarudy.dto.CurrencyResponseDTO;
import com.mikitarudy.dto.ExchangeRateRequestDTO;
import com.mikitarudy.dto.ExchangeRateResponseDTO;
import com.mikitarudy.exception.ConflictException;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.NotFoundException;
import com.mikitarudy.model.Currency;
import com.mikitarudy.model.ExchangeRate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {

    private final JdbcExchangeRateDAO jdbcExchangeRateDAO;
    private final JdbcCurrencyDAO jdbcCurrencyDAO;

    public ExchangeRateService() {
        jdbcExchangeRateDAO = JdbcExchangeRateDAO.getInstance();
        jdbcCurrencyDAO = JdbcCurrencyDAO.getInstance();
    }

    public List<ExchangeRateResponseDTO> findAll() throws DatabaseException {
        List<ExchangeRate> exchangeRates = jdbcExchangeRateDAO.findAll();
        List<ExchangeRateResponseDTO> exchangeRateResponseDTOs = new ArrayList<>();
        for (ExchangeRate exchangeRate : exchangeRates) {
            exchangeRateResponseDTOs.add(new ExchangeRateResponseDTO(
                    exchangeRate.getId(),
                    new CurrencyResponseDTO(
                            exchangeRate.getBaseCurrency().getId(),
                            exchangeRate.getBaseCurrency().getFullName(),
                            exchangeRate.getBaseCurrency().getCode(),
                            exchangeRate.getBaseCurrency().getSign()
                    ),
                    new CurrencyResponseDTO(
                            exchangeRate.getTargetCurrency().getId(),
                            exchangeRate.getTargetCurrency().getFullName(),
                            exchangeRate.getTargetCurrency().getCode(),
                            exchangeRate.getTargetCurrency().getSign()
                    ),
                    exchangeRate.getRate()
            ));
        }
        return exchangeRateResponseDTOs;
    }

    public ExchangeRateResponseDTO save(ExchangeRateRequestDTO exchangeRateRequestDTO) throws DatabaseException {
        Optional<Currency> baseCurrency = jdbcCurrencyDAO.findByCode(exchangeRateRequestDTO.getBaseCurrency());
        if (baseCurrency.isEmpty()) {
            throw new NotFoundException("Base currency not found");
        }
        Optional<Currency> targetCurrency = jdbcCurrencyDAO.findByCode(exchangeRateRequestDTO.getTargetCurrency());
        if (targetCurrency.isEmpty()) {
            throw new NotFoundException("Target currency not found");
        }
        if (jdbcExchangeRateDAO.findByCode(exchangeRateRequestDTO.getBaseCurrency(), exchangeRateRequestDTO.getTargetCurrency()).isPresent()) {
            throw new ConflictException("Exchange rate already exists");
        }

        ExchangeRate exchangeRate = new ExchangeRate(baseCurrency.get(), targetCurrency.get(), exchangeRateRequestDTO.getRate());
        exchangeRate = jdbcExchangeRateDAO.save(exchangeRate);

        return new ExchangeRateResponseDTO(
                exchangeRate.getId(),
                new CurrencyResponseDTO(
                        exchangeRate.getBaseCurrency().getId(),
                        exchangeRate.getBaseCurrency().getFullName(),
                        exchangeRate.getBaseCurrency().getCode(),
                        exchangeRate.getBaseCurrency().getSign()
                ),
                new CurrencyResponseDTO(
                        exchangeRate.getTargetCurrency().getId(),
                        exchangeRate.getTargetCurrency().getFullName(),
                        exchangeRate.getTargetCurrency().getCode(),
                        exchangeRate.getTargetCurrency().getSign()
                ),
                exchangeRate.getRate()
        );
    }

    public ExchangeRateResponseDTO findByCode(ExchangeRateRequestDTO exchangeRateRequestDTO) throws DatabaseException {
        Optional<ExchangeRate> exchangeRate = jdbcExchangeRateDAO.findByCode(exchangeRateRequestDTO.getBaseCurrency(), exchangeRateRequestDTO.getTargetCurrency());
        if (exchangeRate.isEmpty()) {
            throw new NotFoundException("Exchange rate not found");
        }
        return new ExchangeRateResponseDTO(
                exchangeRate.get().getId(),
                new CurrencyResponseDTO(
                        exchangeRate.get().getBaseCurrency().getId(),
                        exchangeRate.get().getBaseCurrency().getFullName(),
                        exchangeRate.get().getBaseCurrency().getCode(),
                        exchangeRate.get().getBaseCurrency().getSign()
                ),
                new CurrencyResponseDTO(
                        exchangeRate.get().getTargetCurrency().getId(),
                        exchangeRate.get().getTargetCurrency().getFullName(),
                        exchangeRate.get().getTargetCurrency().getCode(),
                        exchangeRate.get().getTargetCurrency().getSign()
                ),
                exchangeRate.get().getRate()
        );
    }

    public ExchangeRateResponseDTO update(ExchangeRateRequestDTO exchangeRateRequestDTO) throws DatabaseException, NotFoundException {
        int updatedRows = jdbcExchangeRateDAO.update(
                new ExchangeRate(
                        null,
                        new Currency(null, exchangeRateRequestDTO.getBaseCurrency(), null, null),
                        new Currency(null, exchangeRateRequestDTO.getTargetCurrency(), null, null),
                        exchangeRateRequestDTO.getRate()
                )
        );
        if (updatedRows == 0) {
            throw new NotFoundException("Exchange rate not found");
        }
        return findByCode(exchangeRateRequestDTO);
    }
}