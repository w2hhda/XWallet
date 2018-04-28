package com.x.wallet.ui.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.x.wallet.R;
import com.x.wallet.transaction.FavoriteAddressDbAsycTask;
import com.x.wallet.ui.data.AddressItem;
import com.x.wallet.ui.dialog.ContentShowDialogHelper;

public class FavoriteAddressHelper {
    public static void deleteFavoriteAddress(final Context context, final AddressItem item, final FavoriteAddressDbAsycTask.OnDataActionFinishedListener listener){
        ContentShowDialogHelper.showConfirmDialog(context, R.string.del_favorite_address, context.getResources().getString(R.string.confirm_delete_address), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleAddressAction(context, FavoriteAddressDbAsycTask.DEL_ACTION, item, listener);
            }
        });
    }

    public static void handleAddressAction(final Context context, int action, AddressItem item, FavoriteAddressDbAsycTask.OnDataActionFinishedListener listener){
        new FavoriteAddressDbAsycTask(context, item, action, listener).execute();
    }

    public static void alertMsg(final Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
