package com.jalpha_vantage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.jalpha_vantage.enums.CryptoSymbol;
import com.jalpha_vantage.exception.*;
import com.jalpha_vantage.domain.Currency;
import com.jalpha_vantage.domain.DigitalCurrency;
import com.jalpha_vantage.domain.DigitalCurrencyDaily;
import com.jalpha_vantage.domain.DigitalCurrencyIntraDay;
import com.jalpha_vantage.enums.CurrencySymbol;
import com.jalpha_vantage.enums.MarketList;
import com.jalpha_vantage.service.ICryptoCurrencyService;
import com.jalpha_vantage.util.ExceptionUtil;
import com.jalpha_vantage.util.Utils;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CryptoCurrencyTemplate implements ICryptoCurrencyService {
    private static String apiKey;
    private final RestTemplate restTemplate;

    public CryptoCurrencyTemplate(RestTemplate restTemplate, String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public Currency exchangeRate(CurrencySymbol fromCurrency, CurrencySymbol toCurrency) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String fromCurrencyStr = String.valueOf(fromCurrency);
        String toCurrencyStr = String.valueOf(toCurrency);
        Currency currency = requestApiData(fromCurrencyStr, toCurrencyStr);
        return currency;
    }

    @Override
    public Currency exchangeRate(CurrencySymbol fromCurrency, DigitalCurrency toCurrency) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String fromCurrencyStr = String.valueOf(fromCurrency);
        String toCurrencyStr = String.valueOf(toCurrency).replace("_", "");
        Currency currency = requestApiData(fromCurrencyStr, toCurrencyStr);
        return currency;
    }


    @Override
    public Currency exchangeRate(CryptoSymbol fromCurrency, CurrencySymbol toCurrency) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String fromCurrencyStr = String.valueOf(fromCurrency).replace("_", "");
        String toCurrencyStr = String.valueOf(toCurrency);
        Currency currency = requestApiData(fromCurrencyStr, toCurrencyStr);
        return currency;
    }

    @Override
    public Currency exchangeRate(CryptoSymbol fromCurrency, CryptoSymbol toCurrency) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String fromCurrencyStr = String.valueOf(fromCurrency).replace("_", "");
        String toCurrencyStr = String.valueOf(toCurrency).replace("_", "");
        Currency currency = requestApiData(fromCurrencyStr, toCurrencyStr);
        return currency;
    }

    @Override
    public List<DigitalCurrency> intraday(CryptoSymbol symbol, MarketList market) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String function = "DIGITAL_CURRENCY_INTRADAY";
        String queryString = new StringBuilder()
                .append("function=")
                .append(function)
                .append("&symbol=")
                .append(String.valueOf(symbol).replace("_", ""))
                .append("&market=")
                .append(market.toString())
                .append("&apikey=")
                .append(apiKey).append("&").toString();

        JsonNode jsonNode = restTemplate.getForObject(queryString, JsonNode.class);
        Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();

        List<DigitalCurrency> result = new ArrayList<>();

        while (it.hasNext()) {
            Map.Entry<String, JsonNode> mapEntry = it.next();
            ExceptionUtil.handleException(mapEntry, function);

            if (mapEntry.getKey().equals("Error Message")) {
                String value = mapEntry.getValue().toString();
                ExceptionUtil.handleExceptions(value, function);
            }

            if (mapEntry.getKey().equals("Information")) {
                String value = mapEntry.getValue().toString();
                ExceptionUtil.handleExceptions(value, function);
            }
            if (mapEntry.getKey().equals("Time Series (Digital Currency Intraday)")) {
                JsonNode node = mapEntry.getValue();
                Iterator<Map.Entry<String, JsonNode>> timeSeriesIter = node.fields();
                while (timeSeriesIter.hasNext()) {
                    Map.Entry<String, JsonNode> timeSeriesMap = timeSeriesIter.next();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(timeSeriesMap.getKey(), formatter);
                    String priceVal = new StringBuilder().append("1a. price (").append(market).append(")").toString();
                    String price = String.valueOf(timeSeriesMap.getValue().get(priceVal)).replaceAll("\"", "");
                    String usdPrice = String.valueOf(timeSeriesMap.getValue().get("1b. price (USD)")).replaceAll("\"", "");
                    String vol = String.valueOf(timeSeriesMap.getValue().get("2. volume")).replaceAll("\"", "");
                    String marketCap = String.valueOf(timeSeriesMap.getValue().get("3. market cap (USD)")).replaceAll("\"", "");
                    result.add(DigitalCurrency.intraDay(symbol, market.name(), price, usdPrice,vol,marketCap, dateTime));
                }

            }
        }

        return result;
    }

    @Override
    public List<DigitalCurrency> daily(CryptoSymbol symbol, MarketList market) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String function = "DIGITAL_CURRENCY_DAILY";
        List<DigitalCurrency> result = requestApiData(function, symbol, market);
        return result;
    }

    @Override
    public List<DigitalCurrency> weekly(CryptoSymbol symbol, MarketList market) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String function = "DIGITAL_CURRENCY_WEEKLY";
        List<DigitalCurrency> result = requestApiData(function, symbol, market);
        return result;
    }

    @Override
    public List<DigitalCurrency> monthly(CryptoSymbol symbol, MarketList market) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String function = "DIGITAL_CURRENCY_MONTHLY";
        List<DigitalCurrency> result = requestApiData(function, symbol, market);
        return result;
    }

    private List<DigitalCurrency> requestApiData(String function, CryptoSymbol symbol, MarketList market) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
//        String queryString = ALPHA_VANTAGE_API_URL + "function=" + function + "&symbol=" + String.valueOf(symbol).replace("_", "") + "&market=" + market.toString() + "&apikey=" + apiKey + "&";
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.BASE_URI)
                .append("function=")
                .append(function)
                .append("&symbol=")
                .append(String.valueOf(symbol).replace("_", ""))
                .append("&market=")
                .append(market.toString())
                .append("&apikey=")
                .append(apiKey)
                .append("&");

        JsonNode jsonNode = restTemplate.getForObject(sb.toString(), JsonNode.class);
        Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();

        List<DigitalCurrency> result = new ArrayList<>();

        while (it.hasNext()) {
            Map.Entry<String, JsonNode> mapEntry = it.next();
            ExceptionUtil.handleException(mapEntry, function);
            String duration = function.substring(17).toLowerCase();
            String durChar = Character.toString(duration.charAt(0)).toUpperCase();

            String titleKey = new StringBuilder().append("Time Series (Digital Currency ").append(durChar).append(duration.substring(1)).append(")").toString();
            //String titleCase = String.valueOf(duration.charAt(0)).toUpperCase() + duration.substring(1);

            if (mapEntry.getKey().equals(titleKey)) {
                JsonNode node = mapEntry.getValue();
                Iterator<Map.Entry<String, JsonNode>> timeSeriesIter = node.fields();
                while (timeSeriesIter.hasNext()) {
                    Map.Entry<String, JsonNode> timeSeriesMap = timeSeriesIter.next();
                    LocalDate localDate = LocalDate.parse(timeSeriesMap.getKey(), DateTimeFormatter.ISO_LOCAL_DATE);
                    String openVal = new StringBuilder().append("1a. open (").append(market).append(")").toString();
                    String open = String.valueOf(timeSeriesMap.getValue().get(openVal)).replaceAll("\"", "");
                    String usdOpen = String.valueOf(timeSeriesMap.getValue().get("1b. open (USD)")).replaceAll("\"", "");
                    String highVal = new StringBuilder().append("2a. high (" ).append(market).append(")").toString();
                    String high = String.valueOf(timeSeriesMap.getValue().get(highVal)).replaceAll("\"", "");
                    String usdHigh = String.valueOf(timeSeriesMap.getValue().get("2b. high (USD)")).replaceAll("\"", "");
                    String lowVal = new StringBuilder().append("3a. low (").append(market).append(")").toString();
                    String low = String.valueOf(timeSeriesMap.getValue().get(lowVal)).replaceAll("\"", "");
                    String usdLow = String.valueOf(timeSeriesMap.getValue().get("3b. low (USD)")).replaceAll("\"", "");
                    String closeVal = new StringBuilder().append("4a. close (").append(market).append(")").toString();
                    String close = String.valueOf(timeSeriesMap.getValue().get(closeVal)).replaceAll("\"", "");
                    String usdClose = String.valueOf(timeSeriesMap.getValue().get("4b. close (USD)")).replaceAll("\"", "");

                    String vol = String.valueOf(timeSeriesMap.getValue().get("volume")).replaceAll("\"", "");

                    String marketCap = String.valueOf(timeSeriesMap.getValue().get("6. market cap (USD)")).replaceAll("\"", "");
                    result.add(DigitalCurrency.daily(symbol, market.name(), open, usdOpen, high,usdHigh, low,usdLow, close, usdClose, vol, marketCap, localDate));
                }

            }
        }

        return result;
    }

    private Currency requestApiData(String fromCurrency, String toCurrency) throws UnsupportedEncodingException, InvalidApiKeyException, InvalidFunctionOptionException, MalFormattedFunctionException, MissingApiKeyException, UltraHighFrequencyRequestException, ApiLimitExceeded {
        String function = "CURRENCY_EXCHANGE_RATE";

        StringBuilder sb = new StringBuilder();
        sb.append(Utils.BASE_URI)
                .append("function=")
                .append(function)
                .append("&from_currency=")
                .append(fromCurrency)
                .append("&to_currency=")
                .append(toCurrency)
                .append("&apikey=")
                .append(apiKey)
                .append("&");


        JsonNode jsonNode = restTemplate.getForObject(sb.toString(), JsonNode.class);
        Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> mapEntry = it.next();
            ExceptionUtil.handleException(mapEntry, function);
            if (mapEntry.getKey().equals("Realtime Currency Exchange Rate")) {
                JsonNode node = mapEntry.getValue();
                Iterator<Map.Entry<String, JsonNode>> timeSeriesIter = node.fields();
                while (timeSeriesIter.hasNext()) {
                    String fromCurrencyCode = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    String fromCurrencyName = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    String toCurrencyCode = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    String toCurrencyName = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    String exchangeRate = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    String lastRefreshed = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    String timezone = String.valueOf(timeSeriesIter.next().getValue()).replaceAll("\"", "");
                    return Currency.newCurrencyInstance(fromCurrencyCode, fromCurrencyName, toCurrencyCode, toCurrencyName, exchangeRate, lastRefreshed, timezone);
                }
            }
        }
        return null;
    }


}
