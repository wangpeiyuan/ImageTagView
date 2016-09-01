package com.wpy.imagetagview.tag;

/**
 * 点击事件
 * Created by feiyang on 16/9/1.
 */
public interface TagGroupClickListener {
    void onClick(float x, float y);

    void onTypeChange(int index, int type);

    void onTagLongClick(int index);
}
