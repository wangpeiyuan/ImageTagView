package com.wpy.imagetagview.tag;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
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

    protected TagFactor mTagFactor;
    protected List<TagItem> mTagItems;

    private GestureDetector mGestureDetector;
    protected TagItem mCurrentClickTagItem = null;
    private boolean mCanTouch = true;
    private TagGroupClickListener mTagGroupClickListener;

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
        mGestureDetector = new GestureDetector(getContext(), new TagGroupGestureListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isEmpty()) return;
        getDrawingRect(mTagFactor.mViewGroupRect);
//        Log.d(TAG, "onMeasure: " + mTagFactor.mViewGroupRect.toString());

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        for (TagItem tagItem : mTagItems) {
            if (tagItem.isEmpty()) continue;
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
        tagItem.addTags(ImageTagViewGroup.this, tagContent.getCenterPointF(), tagContent.getTagItemContent(), type);
        mTagItems.add(tagItem);
    }

    public void updateTag(TagContent tagContent, int index) {
        mTagItems.get(index).updateTag(ImageTagViewGroup.this, tagContent.getTagItemContent());
    }

    public void removeTagChild(int index) {
        if (index < 0 || index >= mTagItems.size()) return;
        mTagItems.get(index).removeTagView(ImageTagViewGroup.this);
        mTagItems.remove(index);
    }

    public void removeAllTag() {
        for (TagItem tagItem : mTagItems) {
            tagItem.removeTagView(ImageTagViewGroup.this);
        }
        mTagItems.clear();
    }

    /**
     * 点击事件处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCanTouch) {
            return mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void setCanTouch(boolean canTouch) {
        this.mCanTouch = canTouch;
    }

    public void setOnTagGroupClickListener(TagGroupClickListener listener) {
        mTagGroupClickListener = listener;
    }

    public class TagGroupGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            mCurrentClickTagItem = null;
            for (TagItem tagItem : mTagItems) {
                if (tagItem.isClickInTag(x, y)) {
                    mCurrentClickTagItem = tagItem;
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            if (mCurrentClickTagItem == null) {
                if (mTagGroupClickListener != null) {
                    mTagGroupClickListener.onClick(x, y);
                }
            } else if (mCurrentClickTagItem.isClickInCircle((int) x, (int) y)) {
                int changeType = mCurrentClickTagItem.getCanChangeType(true);
                mCurrentClickTagItem.changeType(ImageTagViewGroup.this, changeType);
                if (mTagGroupClickListener != null) {
                    mTagGroupClickListener.onTypeChange(mTagItems.indexOf(mCurrentClickTagItem), changeType);
                }
            } else if (mCurrentClickTagItem.isClickInText((int) x, (int) y)) {
                if (mTagGroupClickListener != null) {
                    mTagGroupClickListener.onTagEdit(mTagItems.indexOf(mCurrentClickTagItem));
                }
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mCurrentClickTagItem != null) {
                mCurrentClickTagItem.moveTo(ImageTagViewGroup.this, distanceX, distanceY);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mCurrentClickTagItem != null) {
                if (mTagGroupClickListener != null) {
                    mTagGroupClickListener.onTagLongClick(mTagItems.indexOf(mCurrentClickTagItem));
                }
            }
        }
    }
}
