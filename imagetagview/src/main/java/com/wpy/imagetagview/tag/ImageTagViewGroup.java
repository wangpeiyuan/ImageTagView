package com.wpy.imagetagview.tag;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片标签  另一种实现方式
 * todo 标签动画
 * Created by feiyang on 16/8/17.
 */
public class ImageTagViewGroup extends ViewGroup {
    private static final String TAG = "ImageTagViewGroup";

    private TagFactor mTagFactor;

    private List<TagItem> mTagItems;

    public ImageTagViewGroup(Context context) {
        this(context, null);
    }

    public ImageTagViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTagViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);//如果 ViewGroup 没有设置背景的话 需要设置成 false 否则 onDraw() 不会执行
        mTagFactor = new TagFactor(getContext());
        mTagItems = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isEmpty()) return;
        getDrawingRect(mTagFactor.mViewGroupRect);
        Log.d(TAG, "onMeasure: " + mTagFactor.mViewGroupRect.toString());

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        for (TagItem tagItem : mTagItems) {
            tagItem.checkCenterBorder();
            tagItem.checkAndSelectTypeWhenNone();
            tagItem.setTextViewRectAndPath();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isEmpty()) return;
        for (TagItem tagItem : mTagItems) {
            tagItem.onLayout();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEmpty()) return;
        for (TagItem tagItem : mTagItems) {
            tagItem.onDraw(canvas);
        }
    }

    private boolean isEmpty() {
        return mTagItems == null || mTagItems.isEmpty();
    }


    public void addTags(List<TagContent> tagContents) {
        addTags(tagContents, TagFactor.TYPE_NONE);
    }

    public void addTags(List<TagContent> tagContents, int type) {
        if (tagContents == null || tagContents.isEmpty()) return;
        mTagFactor.checkType(type);
        for (TagContent tagContent : tagContents) {
            addTag(tagContent, type);
        }
    }

    public void addTag(TagContent tagContent) {
        addTag(tagContent, TagFactor.TYPE_NONE);
    }

    public void addTag(TagContent tagContent, int type) {
        TagItem tagItem = new TagItem(mTagFactor);
        tagItem.addTags(this, tagContent.getCenterPointF(), tagContent.getTagItemContent(), type);
        mTagItems.add(tagItem);
    }

    public void removeTagChild(int index) {
        mTagItems.remove(index);
        removeViewAt(index);
    }

    public void removeAllTag() {
        mTagItems.clear();
        removeAllViews();
    }

    private void addTest(float x, float y) {
        TestTagContent testTagContent = new TestTagContent();
        testTagContent.addTest(x, y);
        addTag(testTagContent);
    }

    /**
     * todo 点击事件处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        addTest(event.getX(), event.getY());
        return super.onTouchEvent(event);
    }
}
