package com.wpy.imagetagview.tag.other;

import android.view.View;

/**
 * 标签容器点击事件
 * Created by feiyang on 16/7/7.
 */
public interface TagClickListener {
    void onClick(float x, float y);

    void onTagEditClick(int index, View view);

    void onLongTagClick(int index, View view);
}
