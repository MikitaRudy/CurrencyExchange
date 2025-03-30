package com.mikitarudy.dao;

import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.model.Currency;
import com.mikitarudy.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyDAO implements CurrencyDAO {

    private static final JdbcCurrencyDAO INSTANCE = new JdbcCurrencyDAO();

    private static final String FIND_ALL_SQL = """
            SELECT id, code, fullName, sign
            FROM Currencies;
            """;

    private static final String SAVE_SQL = """
            INSERT INTO Currencies(code, fullName, sign)
            VALUES (?, ?, ?);
            """;

    private static final String FIND_BY_CODE_SQL = """
            SELECT id, code, fullName, sign
            FROM Currencies
            WHERE code = ?;
            """;

    private JdbcCurrencyDAO() {
    }

    public static JdbcCurrencyDAO getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            Currency currency = null;
            if (resultSet.next()) {
                currency = new Currency(
                        resultSet.getInt("id"),
                        resultSet.getString("code"),
                        resultSet.getString("fullName"),
                        resultSet.getString("sign"));
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find currency '" + code + "' from the database");
        }
    }

    @Override
    public Currency save(Currency currency) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                currency.setId(generatedKeys.getInt(1));
            }
            return currency;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save currency '" + currency.getCode() + "' to the database");
        }
    }

    @Override
    public List<Currency> findAll() {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (resultSet.next()) {
                currencies.add(new Currency(
                        resultSet.getInt("id"),
                        resultSet.getString("code"),
                        resultSet.getString("fullName"),
                        resultSet.getString("sign")));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read currencies from the database");
        }
    }
}
