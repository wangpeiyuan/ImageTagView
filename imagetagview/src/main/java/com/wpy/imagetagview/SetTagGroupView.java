package com.wpy.imagetagview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feiyang on 16/7/4.
 */
public class SetTagGroupView extends FrameLayout {

    private static final String TAG = "SetTagGroupView";

    private GestureDetector mGestureDetector;

    private List<ImageTagView> mTagSetViews = new ArrayList<>();

    private ImageTagView mCurrentTagView = null;

    private boolean canTouch = true;

    private TagClickListener mTagClickListener;

    public SetTagGroupView(Context context) {
        this(context, null);
    }

    public SetTagGroupView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SetTagGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(getContext(), new TagGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (canTouch) {
            return mGestureDetector.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    public void setCanTouch(boolean canTouch) {
        this.canTouch = canTouch;
    }

    public void setTagClickListener(TagClickListener clickListener) {
        this.mTagClickListener = clickListener;
    }

    private void addTagTest(float x, float y) {
        List<String> strings = new ArrayList<>();
//        strings.add("之前做过一个图上标签但是动画样式不太好看");
        strings.add("经过查找资料发现了一种");
//        strings.add("PathMeasure");
        final ImageTagView tagSetView = new ImageTagView(getContext());
        addView(tagSetView);
        tagSetView.addTags(x, y, strings);
        mTagSetViews.add(tagSetView);
    }

    public class TagGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            Log.d(TAG, "onDown: e.getX() = " + x + " e.getY() = " + y);
            //这边判断点击到那个 view 没有选中的为 null
            ImageTagView view = null;
            for (ImageTagView tagSetView : mTagSetViews) {
                if (tagSetView.isCenterClick(x, y) ||
                        tagSetView.isContentTextClick(x, y)) {
                    view = tagSetView;
                }
            }
            mCurrentTagView = view;
            Log.d(TAG, "onDown: mCurrentTagView " + (mCurrentTagView == null));
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            Log.d(TAG, "onSingleTapUp: e.getX() = " + x + " e.getY() = " + y);
            if (mCurrentTagView == null && mTagClickListener != null) {
                //添加
                mTagClickListener.onClick(x, y);
            } else if (mCurrentTagView != null &&
                    mCurrentTagView.isCenterClick((int) x, (int) y)) {
                mCurrentTagView.changeType(mCurrentTagView.getCanChangeType());
            } else if (mCurrentTagView != null &&
                    mCurrentTagView.isContentTextClick((int) x, (int) y) &&
                    mTagClickListener != null) {
                //编辑
                mTagClickListener.onTagEditClick(mTagSetViews.indexOf(mCurrentTagView), mCurrentTagView);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //移动选中的 view
            if (mCurrentTagView != null) {//不能直接传坐标  会导致跳跃问题
                Log.d(TAG, "onScroll: distanceX = " + distanceX + " distanceY = " + distanceY);
                mCurrentTagView.moveTo(mCurrentTagView.getCenterX() - distanceX,
                        mCurrentTagView.getCenterY() - distanceY);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //删除选中的 view
            if (mCurrentTagView != null && mTagClickListener != null) {
                Log.d(TAG, "onLongPress: ");
                mTagClickListener.onLongTagClick(mTagSetViews.indexOf(mCurrentTagView), mCurrentTagView);
            }
        }
    }
}
