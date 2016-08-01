package com.wpy.imagetagview.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by feiyang on 16/8/1.
 */
public class TagLocalDisplay {
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static float sp2px(Context context, float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
}
