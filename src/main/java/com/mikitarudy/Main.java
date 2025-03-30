package com.mikitarudy;

import com.mikitarudy.dao.JdbcCurrencyDAO;
import com.mikitarudy.model.Currency;
import com.mikitarudy.util.ConnectionManager;


import java.math.BigDecimal;
import java.util.List;


public class Main {
    public static void main(String[] args){
//        JdbcCurrencyDAO dao = JdbcCurrencyDAO.getInstance();
//        List<Currency> currencies = dao.findAll();
//        for (Currency currency : currencies) {
//            System.out.println(currency);
//        }
//        ConnectionManager.closeConnection();

        BigDecimal usd = new BigDecimal("0.01");
        System.out.println(usd);
    }
}
