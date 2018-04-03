package com.x.wallet.ui;

import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.x.wallet.AppUtils;

import java.lang.reflect.Field;

/**
 * Created by wuliang on 18-4-3.
 */

public class TabUtils {
    public static void updateTabMargin(TabLayout tabLayout, int leftMargin, int rightMargin){
        try{
            Class<?> tabLayoutClass = TabLayout.class;
            Field tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
            if(tabStrip != null){
                tabStrip.setAccessible(true);
                LinearLayout tabsContainerLl = (LinearLayout) tabStrip.get(tabLayout);
                if(tabsContainerLl != null){
                    int childCount = tabsContainerLl.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = tabsContainerLl.getChildAt(i);
                        child.setPadding(0, 0, 0, 0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        params.leftMargin = leftMargin;
                        params.rightMargin = rightMargin;
                        child.setLayoutParams(params);
                        child.invalidate();
                    }
                }
            }
        } catch (Exception e){
            Log.e(AppUtils.APP_TAG, "TabUtils updateTabMargin", e);
        }
    }
}
