package com.mikitarudy.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikitarudy.dto.CurrencyRequestDTO;
import com.mikitarudy.dto.CurrencyResponseDTO;
import com.mikitarudy.dto.ErrorResponseDTO;
import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.exception.NotFoundException;
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

@WebServlet("/currency/*")
public class CurrencyServlet  extends HttpServlet {

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
            String code = req.getPathInfo().substring(1);
            Validator.checkCode(code);
            CurrencyRequestDTO currencyRequestDTO = new CurrencyRequestDTO(null, code, null);
            CurrencyResponseDTO currencyResponseDTO = currencyService.findByCode(currencyRequestDTO);
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), currencyResponseDTO);
        } catch (ValidationException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (NotFoundException e){
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(e.getMessage()));
        } catch (DatabaseException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO("Database error"));
        }
    }
}
