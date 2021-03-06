package com.wpy.imagetagviewdemo;

import android.graphics.PointF;

import com.wpy.imagetagview.tag.TagContent;

import java.util.ArrayList;
import java.util.List;

/**
 * test item
 * Created by feiyang on 16/9/1.
 */
public class TestTagContent implements TagContent {

    public PointF mPointF;
    public List<String> mTagContentList;

    public void addTest(float x, float y) {
        mTagContentList = new ArrayList<>();
        mTagContentList.add("这是一条测试的非常长长长长长长长长长长长长长长长长长长长长的标签~~~");
        mTagContentList.add("16768 数字");
        mTagContentList.add("This is a test");

        mPointF = new PointF(x, y);
    }

    public void updateTest() {
        mTagContentList.remove(0);
    }

    @Override
    public List<String> getTagItemContent() {
        return mTagContentList;
    }

    @Override
    public PointF getCenterPointF() {
        return mPointF;
    }
}
