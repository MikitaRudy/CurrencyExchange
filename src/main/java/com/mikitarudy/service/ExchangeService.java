package com.mikitarudy.service;

import com.mikitarudy.dao.JdbcExchangeRateDAO;
import com.mikitarudy.dto.CurrencyResponseDTO;
import com.mikitarudy.dto.ExchangeRequestDTO;
import com.mikitarudy.dto.ExchangeResponseDTO;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.NotFoundException;
import com.mikitarudy.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {

    private final JdbcExchangeRateDAO jdbcExchangeRateDAO;

    public ExchangeService() {
        jdbcExchangeRateDAO = JdbcExchangeRateDAO.getInstance();
    }

    public ExchangeResponseDTO exchange(ExchangeRequestDTO exchangeRequestDTO) throws NotFoundException, DatabaseException {
        Optional<ExchangeRate> exchangeRate = findExchangeRate(exchangeRequestDTO);
        if (exchangeRate.isEmpty()) {
            throw new NotFoundException("Exchange rate not found");
        }

        BigDecimal convertedAmount = exchangeRequestDTO.getAmount().multiply(exchangeRate.get().getRate()).setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponseDTO(
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
                exchangeRate.get().getRate(),
                exchangeRequestDTO.getAmount(),
                convertedAmount
        );
    }

    private Optional<ExchangeRate> findExchangeRate(ExchangeRequestDTO exchangeRequestDTO) throws DatabaseException {
        Optional<ExchangeRate> exchangeRate = findDirectRate(exchangeRequestDTO);
        if (exchangeRate.isEmpty()) {
            exchangeRate = findReverseRate(exchangeRequestDTO);
            if (exchangeRate.isEmpty()) {
                exchangeRate = findCrossRate(exchangeRequestDTO);
            }
        }
        return exchangeRate;
    }

    private Optional<ExchangeRate> findDirectRate(ExchangeRequestDTO exchangeRequestDTO) throws DatabaseException {
        return jdbcExchangeRateDAO.findByCode(exchangeRequestDTO.getBaseCurrency(), exchangeRequestDTO.getTargetCurrency());
    }

    private Optional<ExchangeRate> findReverseRate(ExchangeRequestDTO exchangeRequestDTO) throws DatabaseException {
        Optional<ExchangeRate> exchangeRate = jdbcExchangeRateDAO.findByCode(exchangeRequestDTO.getTargetCurrency(), exchangeRequestDTO.getBaseCurrency());
        if (exchangeRate.isPresent()) {
            BigDecimal rate = BigDecimal.ONE.divide(exchangeRate.get().getRate(), MathContext.DECIMAL64).setScale(6, RoundingMode.HALF_EVEN);
            exchangeRate = Optional.of(new ExchangeRate(
                    exchangeRate.get().getTargetCurrency(),
                    exchangeRate.get().getBaseCurrency(),
                    rate
            ));
        }
        return exchangeRate;
    }

    private Optional<ExchangeRate> findCrossRate(ExchangeRequestDTO exchangeRequestDTO) throws DatabaseException {
        Optional<ExchangeRate> usdToBase = jdbcExchangeRateDAO.findByCode("USD", exchangeRequestDTO.getBaseCurrency());
        Optional<ExchangeRate> usdToTarget = jdbcExchangeRateDAO.findByCode("USD", exchangeRequestDTO.getTargetCurrency());
        if (usdToBase.isEmpty() || usdToTarget.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal rate = usdToTarget.get().getRate().divide(usdToBase.get().getRate(), MathContext.DECIMAL64).setScale(6, RoundingMode.HALF_EVEN);
        return Optional.of(new ExchangeRate(
                usdToBase.get().getTargetCurrency(),
                usdToTarget.get().getTargetCurrency(),
                rate
        ));
    }
}
