package com.mikitarudy.servlet.exchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikitarudy.dto.*;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.NotFoundException;
import com.mikitarudy.exception.ValidationException;
import com.mikitarudy.service.ExchangeService;
import com.mikitarudy.util.Validator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeService exchangeService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        exchangeService = new ExchangeService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String fromCurrency = req.getParameter("from");
            String toCurrency = req.getParameter("to");
            String amount = req.getParameter("amount");

            Validator.checkCode(fromCurrency);
            Validator.checkCode(toCurrency);
            Validator.checkDecimal(amount);

            ExchangeRequestDTO exchangeRequestDTO = new ExchangeRequestDTO(fromCurrency, toCurrency, new BigDecimal(amount));
            ExchangeResponseDTO exchangeResponseDTO = exchangeService.exchange(exchangeRequestDTO);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeResponseDTO);
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (NotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        }
    }
}
