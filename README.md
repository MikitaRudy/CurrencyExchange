# Проект "Обменник валют"
## Описание
REST API для описания валют и обменных курсов. Позволяет просматривать и редактировать списки валют и обменных курсов, и совершать расчёт конвертации произвольных сумм из одной валюты в другую.

## Использованные технологии / инструменты
- Jakarta Servlet
- Apache Tomcat
- Apache Maven
- JDBC
- SQLite
- Postman
- Hikari Connection Pool
- Jackson

## Возможности API

### Валюты

GET `/currencies`

Получение списка валют. Пример ответа:

```json
[
    {
        "id": 0,
        "name": "US dollar",
        "code": "USD",
        "sign": "$"
    },   
    {
        "id": 1,
        "name": "Yen",
        "code": "JPY",
        "sign": "¥"
    }
]
```

GET `/currency/JPY`

Получение конкретной валюты. Пример ответа:

```json
{
    "id": 1,
    "name": "Yen",
    "code": "JPY",
    "sign": "¥"
}
```

POST `/currencies`

Добавление новой валюты в базу данных. Данные передаются в теле запроса в виде полей формы (`x-www-form-urlencoded`). Поля формы - `name`, `code`, `sign`. Пример ответа - JSON представление вставленной в базу записи, включая её ID:

```json
{
    "id": 0,
    "name": "US Dollar",
    "code": "USD",
    "sign": "$"
}
```

### Обменные курсы

GET `/exchangeRates`

Получение списка всех обменных курсов. Пример ответа:

```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "US dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 3,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```

GET `/exchangeRate/USDRUB`

Получение конкретного обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. Пример ответа:

```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "US dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 2,
            "name": "Russian Ruble",
            "code": "RUB",
            "sign": "₽"
        },
        "rate": 80
    }
]
```

POST `/exchangeRates`

Добавление нового обменного курса в базу. Данные передаются в теле запроса в виде полей формы (`x-www-form-urlencoded`). Поля формы - `baseCurrencyCode`, `targetCurrencyCode`, `rate`. Пример полей формы:

- `baseCurrencyCode` - USD
- `targetCurrencyCode` - EUR
- `rate` - 0.99

Пример ответа - JSON представление вставленной в базу записи, включая её ID:

```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "US dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```
PATCH `/exchangeRate/USDRUB`

Обновление существующего в базе обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. Данные передаются в теле запроса в виде полей формы (`x-www-form-urlencoded`). Единственное поле формы - `rate`.

Пример ответа - JSON представление обновлённой записи в базе данных, включая её ID:

```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 1,
            "name": "US dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 2,
            "name": "Russian Ruble",
            "code": "RUB",
            "sign": "₽"
        },
        "rate": 80
    }
]
```

### Обмен валюты

GET `/exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`

Расчёт перевода определённого количества средств из одной валюты в другую. Пример запроса - GET `/exchange?from=USD&to=AUD&amount=10`.

Пример ответа:

```json
{
    "baseCurrency": {
        "id": 1,
        "name": "Yen",
        "code": "JPY",
        "sign": "¥"
    },
    "targetCurrency": {
        "id": 4,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 0.0099,
    "amount": 10000.00,
    "convertedAmount": 99.40
}
```

Получение курса для обмена может пройти по одному из трёх сценариев. Допустим, совершаем перевод из валюты A в валюту B:

1. В таблице ExchangeRates существует валютная пара AB - берём её курс
2. В таблице ExchangeRates существует валютная пара BA - берем её курс, и считаем обратный, чтобы получить AB
3. В таблице ExchangeRates существуют валютные пары USD-A и USD-B - вычисляем из этих курсов курс AB

## Зависимости
- Java 17+
- Tomcat 10
