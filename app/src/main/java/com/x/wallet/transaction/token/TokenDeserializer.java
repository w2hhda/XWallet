package com.x.wallet.transaction.token;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.x.wallet.transaction.balance.TokenListBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 18-4-9.
 */

public class TokenDeserializer implements JsonDeserializer<TokenListBean> {

    @Override
    public TokenListBean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        //Log.i("TokenDeserializer", "TokenDeserializer deserialize json = " + json);
        JsonObject jsonObject = json.getAsJsonObject();
        String address = "";
        final JsonElement jsonAddress = jsonObject.get("address");
        if(jsonAddress != null){
            address = jsonAddress.getAsString();
        }
        List<TokenListBean.TokenBean> tokenBeans = new ArrayList<>();
        JsonElement jsonObjectTokens = jsonObject.get("tokens");
        if(jsonObjectTokens != null){
            JsonArray jsonArray = jsonObjectTokens.getAsJsonArray();
            int length = jsonArray.size();
            for(int i = 0; i < length; i++){
                TokenListBean.TokenBean tokenBean = new TokenListBean.TokenBean();

                JsonElement element = jsonArray.get(i);
                JsonObject elementObject = element.getAsJsonObject();

                JsonElement tokenInfoElement = elementObject.get("tokenInfo");
                TokenListBean.TokenInfo tokenInfo = handleTokenInfo(tokenInfoElement);
                tokenBean.setTokenInfo(tokenInfo);

                String balance = elementObject.get("balance").getAsString();
                tokenBean.setBalance(balance);
                tokenBeans.add(tokenBean);
            }
        }

        final TokenListBean tokenListBean = new TokenListBean();
        tokenListBean.setAddress(address);
        tokenListBean.setTokens(tokenBeans);
        return tokenListBean;
    }

    private TokenListBean.TokenInfo handleTokenInfo(JsonElement tokenInfoElement){
        TokenListBean.TokenInfo tokenInfo = new TokenListBean.TokenInfo();
        if(tokenInfoElement != null){
            JsonObject object = tokenInfoElement.getAsJsonObject();
            String address = object.get("address").getAsString();
            String name = object.get("name").getAsString();
            int decimals = object.get("decimals").getAsInt();
            String symbol = object.get("symbol").getAsString();
            tokenInfo.setAddress(address);
            tokenInfo.setName(name);
            tokenInfo.setDecimals(decimals);
            tokenInfo.setSymbol(symbol);

            JsonElement priceElement = object.get("price");
            if(priceElement != null){
                if(priceElement instanceof JsonObject){
                    JsonObject priceObject = priceElement.getAsJsonObject();
                    if(priceObject.has("rate")){
                        double rate = priceObject.get("rate").getAsDouble();
                        TokenListBean.Price price = new TokenListBean.Price();
                        price.setRate(rate);
                        tokenInfo.setPrice(price);
                    }
                }
            }
        }
        return tokenInfo;
    }
}
