package com.wpy.imagetagview.tag;

import android.graphics.PointF;

import java.util.List;

/**
 * tag model
 * Created by feiyang on 16/9/1.
 */
public interface TagContent {
    List<String> getTagItemContent();

    PointF getCenterPointF();
}
