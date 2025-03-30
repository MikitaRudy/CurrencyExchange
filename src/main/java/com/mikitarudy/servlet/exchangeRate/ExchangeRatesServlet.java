package com.mikitarudy.servlet.exchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikitarudy.dto.ErrorResponseDTO;
import com.mikitarudy.dto.ExchangeRateRequestDTO;
import com.mikitarudy.dto.ExchangeRateResponseDTO;
import com.mikitarudy.exception.ConflictException;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.NotFoundException;
import com.mikitarudy.exception.ValidationException;
import com.mikitarudy.service.ExchangeRateService;
import com.mikitarudy.util.Validator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        exchangeRateService = new ExchangeRateService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<ExchangeRateResponseDTO> exchangeRates = exchangeRateService.findAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO("Database error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String baseCurrencyCode = req.getParameter("baseCurrencyCode");
            String targetCurrencyCode = req.getParameter("targetCurrencyCode");
            String rate = req.getParameter("rate");

            Validator.checkCode(baseCurrencyCode);
            Validator.checkCode(targetCurrencyCode);
            Validator.checkDecimal(rate);

            ExchangeRateRequestDTO exchangeRateRequestDTO = new ExchangeRateRequestDTO(baseCurrencyCode, targetCurrencyCode, new BigDecimal(rate));

            ExchangeRateResponseDTO exchangeRateResponseDTO = exchangeRateService.save(exchangeRateRequestDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), exchangeRateResponseDTO);
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (NotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (ConflictException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        }
    }
}
