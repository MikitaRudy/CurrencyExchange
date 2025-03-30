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

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        exchangeRateService = new ExchangeRateService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equals(req.getMethod())) {
            doPatch(req, resp);
        }
        else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                throw new ValidationException("Currency pair is required");
            }
            String currencies = pathInfo.substring(1);
            if (currencies.length() != 6){
                throw new ValidationException("Currencies must be 6 digits");
            }
            String baseCurrencyCode = currencies.substring(0, 3);
            String targetCurrencyCode = currencies.substring(3);

            Validator.checkCode(baseCurrencyCode);
            Validator.checkCode(targetCurrencyCode);

            ExchangeRateRequestDTO exchangeRateRequestDTO = new ExchangeRateRequestDTO(baseCurrencyCode, targetCurrencyCode, null);
            ExchangeRateResponseDTO exchangeRateResponseDTO = exchangeRateService.findByCode(exchangeRateRequestDTO);

            resp.setStatus(HttpServletResponse.SC_OK);
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

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                throw new ValidationException("Currency pair is required");
            }
            String currencies = pathInfo.substring(1);
            if (currencies.length() != 6){
                throw new ValidationException("Currencies must be 6 digits");
            }
            String parameter = req.getReader().readLine();
            if (parameter == null || !parameter.contains("rate")) {
                throw new ValidationException("Rate is missing");
            }
            String rate = parameter.replace("rate=", "");
            if (rate.isBlank()) {
                throw new ValidationException("Rate is missing");
            }

            String baseCurrencyCode = currencies.substring(0, 3);
            String targetCurrencyCode = currencies.substring(3);

            Validator.checkCode(baseCurrencyCode);
            Validator.checkCode(targetCurrencyCode);
            Validator.checkDecimal(rate);

            ExchangeRateRequestDTO exchangeRateRequestDTO = new ExchangeRateRequestDTO(baseCurrencyCode, targetCurrencyCode, new BigDecimal(rate));
            ExchangeRateResponseDTO exchangeRateResponseDTO = exchangeRateService.update(exchangeRateRequestDTO);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRateResponseDTO);
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (NotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        }  catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        }
    }
}