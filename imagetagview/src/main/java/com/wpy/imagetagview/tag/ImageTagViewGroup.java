package com.wpy.imagetagview.tag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 图片标签  另一种实现方式
 * todo 标签动画
 * Created by feiyang on 16/8/17.
 */
public class ImageTagViewGroup extends ViewGroup {
    private static final String TAG = "ImageTagViewGroup";

    public ImageTagViewGroup(Context context) {
        this(context, null);
    }

    public ImageTagViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTagViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private TextView mTextView;
    private Rect mRect;
    private Rect mTextViewRect;
    private PointF mPoint = new PointF();

    private void init() {
        mTextView = new TextView(getContext());
        mTextView.setText("这个一个测试标签啊啊啊啊啊");
        mTextView.setTextSize(12.0f);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setIncludeFontPadding(false);
        addView(mTextView);

        mRect = new Rect();
        mTextViewRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTextView == null) return;
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        getDrawingRect(mRect);
        Log.d(TAG, "onMeasure: " + mRect.toString());
        mTextViewRect.left = (int) mPoint.x;
        mTextViewRect.top = (int) mPoint.y;
        int measuredWidth = mTextView.getMeasuredWidth();
        int measuredHeight = mTextView.getMeasuredHeight();
        if (measuredWidth > mRect.right - mPoint.x) {
            mTextViewRect.right = mRect.right;
            mTextViewRect.bottom = (int) (mPoint.y + measuredHeight * Math.ceil(measuredWidth / (mRect.right - mPoint.x)));
        } else {
            mTextViewRect.right = (int) (mPoint.x + measuredWidth);
            mTextViewRect.bottom = (int) (mPoint.y + measuredHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: ");
        if (mTextView == null) return;
        Log.d(TAG, "onLayout: mTextViewRect = " + mTextViewRect.toString());
        mTextView.measure(MeasureSpec.makeMeasureSpec(mTextViewRect.width(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mTextViewRect.height(), MeasureSpec.EXACTLY));
        mTextView.layout(mTextViewRect.left, mTextViewRect.top, mTextViewRect.right, mTextViewRect.bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTextView == null) {
            mPoint.set(event.getX(), event.getY());
            init();
        }
        return super.onTouchEvent(event);
    }
}
