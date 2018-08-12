package com.luo.widgets.utils;

import android.content.Context;

/**
 * Created by Administrator on 2018/8/12.
 */

public class DimenUtils {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getCssPx(Context context, float pxValue) {
        float dpValue = pxValue / context.getResources().getDisplayMetrics().density;
        //96 css-px = ~ 1 inch
        //160 dp = ~ 1 inch
        return (int) dpValue * 96 / 160;
    }

}
