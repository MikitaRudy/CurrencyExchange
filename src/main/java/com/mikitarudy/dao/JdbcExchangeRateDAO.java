package com.mikitarudy.dao;

import com.mikitarudy.exception.DatabaseException;
import com.mikitarudy.model.Currency;
import com.mikitarudy.model.ExchangeRate;
import com.mikitarudy.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExchangeRateDAO implements ExchangeRateDAO {

    private static final JdbcExchangeRateDAO INSTANCE = new JdbcExchangeRateDAO();

    private static final String FIND_ALL_SQL = """
            SELECT
                ER.id AS ExchangeRateId,
                BC.id AS BaseCurrencyId,
                BC.code AS BaseCurrencyCode,
                BC.fullName AS BaseCurrencyName,
                BC.sign AS BaseCurrencySign,
                TC.id AS TargetCurrencyId,
                TC.code AS TargetCurrencyCode,
                TC.fullName AS TargetCurrencyName,
                TC.sign AS TargetCurrencySign,
                ER.rate
            FROM ExchangeRates ER
                     LEFT JOIN
                 Currencies BC ON ER.baseCurrencyId = BC.id
                     LEFT JOIN
                 Currencies TC ON ER.targetCurrencyId = TC.id;
            """;

    private static final String SAVE_SQL = """
            INSERT INTO ExchangeRates(baseCurrencyId, targetCurrencyId, rate)
            VALUES ((SELECT id FROM Currencies WHERE code = ?),
                    (SELECT id FROM Currencies WHERE code = ?),
                    ?);
            """;

    private static final String UPDATE_SQL = """
            UPDATE ExchangeRates
            SET rate = ?
            WHERE baseCurrencyId = (SELECT id FROM Currencies WHERE code = ?)
                AND targetCurrencyId = (SELECT id FROM Currencies WHERE code = ?);
            """;

    private static final String FIND_BY_CODE_SQL = """
            SELECT
                ER.id AS ExchangeRateId,
                BC.id AS BaseCurrencyId,
                BC.code AS BaseCurrencyCode,
                BC.fullName AS BaseCurrencyName,
                BC.sign AS BaseCurrencySign,
                TC.id AS TargetCurrencyId,
                TC.code AS TargetCurrencyCode,
                TC.fullName AS TargetCurrencyName,
                TC.sign AS TargetCurrencySign,
                ER.rate
            FROM ExchangeRates ER
                     LEFT JOIN
                 Currencies BC ON ER.baseCurrencyId = BC.id
                     LEFT JOIN
                 Currencies TC ON ER.targetCurrencyId = TC.id
            WHERE BaseCurrencyCode = ? AND TargetCurrencyCode = ?;
            """;

    private JdbcExchangeRateDAO() {
    }

    public static JdbcExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<ExchangeRate> findByCode(String baseCurrencyCode, String targetCurrencyCode) {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)){
            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            ExchangeRate exchangeRate = null;
            if (resultSet.next()) {
                exchangeRate = new ExchangeRate(
                        resultSet.getInt("ExchangeRateId"),
                        new Currency(
                                resultSet.getInt("BaseCurrencyId"),
                                resultSet.getString("BaseCurrencyCode"),
                                resultSet.getString("BaseCurrencyName"),
                                resultSet.getString("BaseCurrencySign")
                        ),
                        new Currency(
                                resultSet.getInt("TargetCurrencyId"),
                                resultSet.getString("TargetCurrencyCode"),
                                resultSet.getString("TargetCurrencyName"),
                                resultSet.getString("TargetCurrencySign")
                        ),
                        resultSet.getBigDecimal("rate")
                );
            }
            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            throw new DatabaseException(
                    String.format("Failed to read exchange rate '%s' to '%s' from the database",
                            baseCurrencyCode, targetCurrencyCode)
            );
        }
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, exchangeRate.getBaseCurrency().getCode());
            preparedStatement.setString(2, exchangeRate.getTargetCurrency().getCode());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                exchangeRate.setId(generatedKeys.getInt(1));
            }
            return exchangeRate;
        } catch (SQLException e){
            throw new DatabaseException(
                    String.format("Failed to add exchange rate '%s' to '%s' to the database",
                            exchangeRate.getBaseCurrency().getCode(), exchangeRate.getTargetCurrency().getCode()));
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                exchangeRates.add(new ExchangeRate(
                        resultSet.getInt("ExchangeRateId"),
                        new Currency(
                                resultSet.getInt("BaseCurrencyId"),
                                resultSet.getString("BaseCurrencyCode"),
                                resultSet.getString("BaseCurrencyName"),
                                resultSet.getString("BaseCurrencySign")
                        ),
                        new Currency(
                                resultSet.getInt("TargetCurrencyId"),
                                resultSet.getString("TargetCurrencyCode"),
                                resultSet.getString("TargetCurrencyName"),
                                resultSet.getString("TargetCurrencySign")
                        ),
                        resultSet.getBigDecimal("rate")
                        ));
            }
            return exchangeRates;
        } catch (SQLException e){
            throw new DatabaseException("Failed to read exchange rates from the database");
        }
    }

    @Override
    public int update(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)){
            preparedStatement.setBigDecimal(1, exchangeRate.getRate());
            preparedStatement.setString(2, exchangeRate.getBaseCurrency().getCode());
            preparedStatement.setString(3, exchangeRate.getTargetCurrency().getCode());
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(
                    String.format("Failed to update exchange rate '%s' to '%s' in the database",
                            exchangeRate.getBaseCurrency().getCode(), exchangeRate.getTargetCurrency().getCode())
            );
        }
    }
}
