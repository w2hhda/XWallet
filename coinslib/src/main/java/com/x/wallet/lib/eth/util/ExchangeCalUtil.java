package com.x.wallet.lib.eth.util;

import com.x.wallet.lib.eth.api.EtherscanAPI;
import com.x.wallet.lib.eth.data.CurrencyData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ExchangeCalUtil {

    public static final BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");

    private static ExchangeCalUtil instance;
    private long lastUpdateTimestamp = 0;
    private double rateForChartDisplay = 1;
    private DecimalFormat formatterUsd = new DecimalFormat("#,###,###.##");
    private DecimalFormat formatterCrypt = new DecimalFormat("#,###,###.####");
    private DecimalFormat formatterCryptExact = new DecimalFormat("#,###,###.#######");

    private ExchangeCalUtil() {
    }

    public static ExchangeCalUtil getInstance() {
        if (instance == null)
            instance = new ExchangeCalUtil();
        return instance;
    }

    private CurrencyData[] conversionNames = new CurrencyData[]{
            new CurrencyData("ETH", 1, "Ξ"),
            new CurrencyData("BTC", 0.07, "฿"),
            new CurrencyData("USD", 0, "$")
    };

    private int index = 0;

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public double getRateForChartDisplay() {
        return rateForChartDisplay;
    }

    public CurrencyData next() {
        index = (index + 1) % conversionNames.length;
        return conversionNames[index];
    }

    public CurrencyData getCurrent() {
        return conversionNames[index];
    }

    public CurrencyData previous() {
        index = index > 0 ? index - 1 : conversionNames.length - 1;
        return conversionNames[index];
    }

    public CurrencyData getMainCurreny() {
        return conversionNames[2];
    }

    public CurrencyData getEtherCurrency() {
        return conversionNames[0];
    }

    public String getCurrencyShort() {
        return conversionNames[index].getShorty();
    }

    public String displayBalanceNicely(double d) {
        if (index == 2)
            return displayUsdNicely(d);
        else
            return displayEthNicely(d);
    }

    public String displayUsdNicely(double d) {
        return formatterUsd.format(d);
    }

    public String displayEthNicely(double d) {
        return formatterCrypt.format(d);
    }

    public String displayEthNicelyExact(double d) {
        return formatterCryptExact.format(d);
    }

    public double convertRate(double balance, double rate) {
        if (index == 2) {
            if (balance * rate >= 100000) // dont display cents if bigger than 100k
                return (int) Math.floor(balance * rate);
            return Math.floor(balance * rate * 100) / 100;
        } else {
            if (balance * rate >= 1000)
                return Math.floor(balance * rate * 10) / 10;
            if (balance * rate >= 100)
                return Math.floor(balance * rate * 100) / 100;
            return Math.floor(balance * rate * 1000) / 1000;
        }
    }

    public double weiToEther(long weis) {
        return new BigDecimal(weis).divide(ONE_ETHER, 8, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public String convertRateExact(BigDecimal balance, double rate) {
        if (index == 2) {
            return displayUsdNicely(Math.floor(balance.doubleValue() * rate * 100) / 100) + "";
        } else
            return displayEthNicelyExact(balance.multiply(new BigDecimal(rate)).setScale(7, RoundingMode.CEILING).doubleValue());
    }

    public double convertToUsd(double balance) {
        return Math.floor(balance * getUSDPrice() * 100) / 100;
    }
    

    public double getUSDPrice() {
        return Math.floor(conversionNames[2].getRate() * 100) / 100;
    }

    public double getBTCPrice() {
        return Math.floor(conversionNames[1].getRate() * 10000) / 10000;
    }

    public void updateExchangeRates(final String currency/*, final NetworkUpdateListener update*/) throws IOException {
        if (lastUpdateTimestamp + 40 * 60 * 1000 > System.currentTimeMillis() && currency.equals(conversionNames[2].getName())) { // Dont refresh if not older than 40 min and currency hasnt changed
            return;
        }
        if (!currency.equals(conversionNames[2].getName())) {
            conversionNames[2].setName(currency);
            if (currency.equals("USD"))
                conversionNames[2].setShorty("$");
            else if (currency.equals("EUR"))
                conversionNames[2].setShorty("€");
            else if (currency.equals("GPB"))
                conversionNames[2].setShorty("£");
            else if (currency.equals("AUD"))
                conversionNames[2].setShorty("$");
            else if (currency.equals("RUB"))
                conversionNames[2].setShorty("р");
            else if (currency.equals("CHF"))
                conversionNames[2].setShorty("Fr");
            else if (currency.equals("CAD"))
                conversionNames[2].setShorty("$");
            else if (currency.equals("JPY"))
                conversionNames[2].setShorty("¥");

            else
                conversionNames[2].setShorty(currency);
        }

        //Log.d("updateingn", "Initialize price update");
        EtherscanAPI.getInstance().getEtherPrice(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string()).getJSONObject("result");

                    conversionNames[1].setRate(data.getDouble("ethbtc"));
                    conversionNames[2].setRate(data.getDouble("ethusd"));
                    if (!currency.equals("USD"))
                        convert(currency);
                    else
                        ;
                        //update.onUpdate(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void convert(final String currency/*, final NetworkUpdateListener update*/) throws IOException {
        EtherscanAPI.getInstance().getPriceConversionRates(currency, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                //rateForChartDisplay = ResponseParser.parsePriceConversionRate(response.body().string());
                conversionNames[2].setRate(Math.floor(conversionNames[2].getRate() * rateForChartDisplay * 100) / 100);
                //update.onUpdate(response);
            }
        });
    }

}
