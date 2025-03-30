package com.mikitarudy.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikitarudy.dto.CurrencyRequestDTO;
import com.mikitarudy.dto.CurrencyResponseDTO;
import com.mikitarudy.dto.ErrorResponseDTO;
import com.mikitarudy.exception.ConflictException;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.ValidationException;
import com.mikitarudy.service.CurrencyService;
import com.mikitarudy.util.Validator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        currencyService = new CurrencyService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyResponseDTO> currencies = currencyService.findAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), currencies);
        } catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO("Database error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String name = req.getParameter("name");
            String code = req.getParameter("code");
            String sign = req.getParameter("sign");

            Validator.checkName(name);
            Validator.checkCode(code);
            Validator.checkSign(sign);

            CurrencyRequestDTO requestDTO = new CurrencyRequestDTO(name, code, sign);

            CurrencyResponseDTO responseDTO = currencyService.save(requestDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), responseDTO);
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (ConflictException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO("Database error"));
        }

    }
}
