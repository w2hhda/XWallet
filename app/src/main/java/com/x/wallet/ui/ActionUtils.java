package com.x.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.x.wallet.AppUtils;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.data.TransactionItem;

/**
 * Created by wuliang on 18-4-16.
 */

public class ActionUtils {
    public static void handleAddAccountAction(Activity activity, int actionType, boolean isFromMainPage, int requestCode){
        if(isFromMainPage) {
            Intent intent = new Intent("com.x.wallet.action.COINTYPE_CHOOSE_ACTION");
            intent.putExtra(AppUtils.ACTION_TYPE, actionType);
            activity.startActivity(intent);
        } else {
            Intent intent = new Intent("com.x.wallet.action.COINTYPE_CHOOSE_ACTION");
            intent.putExtra(AppUtils.ACTION_TYPE, actionType);
            activity.startActivityForResult(intent, requestCode);
        }
        /*if(actionType == AppUtils.ACCOUNT_ACTION_TYPE_NEW){
            Intent intent = new Intent("com.x.wallet.action.CREATE_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.COIN_TYPE, LibUtils.COINTYPE.COIN_ETH);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent("com.x.wallet.action.IMPORT_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.COIN_TYPE, LibUtils.COINTYPE.COIN_ETH);
            context.startActivity(intent);
        }*/
    }

    public static void openTransactionDetail(Context context, TransactionItem item,
                                             boolean isTokenAccount, int coinType){
        Intent intent = new Intent("com.x.wallet.action_SEE_TRANSACTION_DETAIL");
        intent.putExtra(AppUtils.TRANSACTION_ITEM, item);
        intent.putExtra(AppUtils.ACCOUNT_TYPE, isTokenAccount);
        intent.putExtra(AppUtils.COIN_TYPE, coinType);
        context.startActivity(intent);
    }

    public static void createAccount(Activity activity, int actionType, int coinType, int requestCode) {
        if (actionType == AppUtils.ACCOUNT_ACTION_TYPE_NEW) {
            Intent intent = new Intent("com.x.wallet.action.CREATE_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.COIN_TYPE, coinType);
            activity.startActivityForResult(intent, requestCode);
        } else {
            Intent intent = new Intent("com.x.wallet.action.IMPORT_ACCOUNT_ACTION");
            intent.putExtra(AppUtils.COIN_TYPE, coinType);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void editFavoriteAddress(Context context, AddressItem item){
        Intent intent = new Intent("com.x.wallet.action.EDIT_FAVORITE_ADDRESS_ACTION");
        if (item != null){
            intent.putExtra(AppUtils.ADDRESS_ITEM, item);
        }
        context.startActivity(intent);

    }

    public static void addFavoriteAddress(Context context, String address, String addressType){
        Intent intent = new Intent("com.x.wallet.action.EDIT_FAVORITE_ADDRESS_ACTION");
        intent.putExtra(AppUtils.EXTRA_ADDRESS, address);
        intent.putExtra(AppUtils.EXTRA_ADDRESS_TYPE, addressType);
        context.startActivity(intent);

    }
}
