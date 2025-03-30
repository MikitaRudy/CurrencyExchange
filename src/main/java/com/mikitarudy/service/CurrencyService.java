package com.mikitarudy.service;

import com.mikitarudy.dao.JdbcCurrencyDAO;
import com.mikitarudy.dto.*;
import com.mikitarudy.exception.ConflictException;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.NotFoundException;
import com.mikitarudy.model.Currency;

import java.util.ArrayList;
import java.util.List;

public class CurrencyService {

    private final JdbcCurrencyDAO jdbcCurrencyDAO;

    public CurrencyService() {
        jdbcCurrencyDAO = JdbcCurrencyDAO.getInstance();
    }

    public List<CurrencyResponseDTO> findAll() throws DatabaseException {
        List<Currency> currencies = jdbcCurrencyDAO.findAll();
        List<CurrencyResponseDTO> currencyResponseDTOS = new ArrayList<>();
        for (Currency currency : currencies) {
            currencyResponseDTOS.add(new CurrencyResponseDTO(
                    currency.getId(),
                    currency.getFullName(),
                    currency.getCode(),
                    currency.getSign()
            ));
        }
        return currencyResponseDTOS;
    }

    public CurrencyResponseDTO findByCode(CurrencyRequestDTO currencyRequestDTO) throws DatabaseException {
        String code = currencyRequestDTO.getCode();
        Currency currency = jdbcCurrencyDAO.findByCode(code).orElseThrow(() -> new NotFoundException("Currency not found"));
        return new CurrencyResponseDTO(
                currency.getId(),
                currency.getFullName(),
                currency.getCode(),
                currency.getSign()
        );
    }

    public CurrencyResponseDTO save(CurrencyRequestDTO currencyRequestDTO) throws DatabaseException {
        if (jdbcCurrencyDAO.findByCode(currencyRequestDTO.getCode()).isPresent()) {
            throw new ConflictException("Currency already exists");
        }
        Currency currency = new Currency(
                currencyRequestDTO.getCode(),
                currencyRequestDTO.getName(),
                currencyRequestDTO.getSign()
        );
        currency = jdbcCurrencyDAO.save(currency);
        return new CurrencyResponseDTO(
                currency.getId(),
                currency.getFullName(),
                currency.getCode(),
                currency.getSign()
        );
    }
}
