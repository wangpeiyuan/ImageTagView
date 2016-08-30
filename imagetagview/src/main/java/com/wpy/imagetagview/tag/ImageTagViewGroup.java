package com.wpy.imagetagview.tag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wpy.imagetagview.util.TagLocalDisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片标签  另一种实现方式
 * todo 标签动画
 * Created by feiyang on 16/8/17.
 */
public class ImageTagViewGroup extends ViewGroup {
    private static final String TAG = "ImageTagViewGroup";

    public static final int TYPE_NONE = 0;//如果设为这个类型  会自己检测并设定类型

    //单条标签 左右两个方向
    public static final int TYPE_ONE_LEFT = 1;
    public static final int TYPE_ONE_RIGHT = 2;

    //多条标签 上下左右四个方向
    public static final int TYPE_MORE_LEFT_TOP = 13;
    public static final int TYPE_MORE_LEFT_BOTTOM = 14;
    public static final int TYPE_MORE_RIGHT_TOP = 23;
    public static final int TYPE_MORE_RIGHT_BOTTOM = 24;

    private int mCurrentType = TYPE_NONE;//标签的类型

    private TextView[] mTextViews;
    private Rect[] mTextViewRects;

    private Rect mViewGroupRect;
    private PointF mCenterPointF = new PointF();

    private Paint mCirclePaint;
    private Paint mLinePaint;

    private Path mLinePath;

    private int mCircleRadius = 4;//圆形的半径
    private int mCircleColor = Color.WHITE;
    private int mOutCircleColor = 0x80000000;
    private int mOutCircleRadius = 8;//外部圆形半径

    private int lineColor = Color.WHITE;
    private int lineWidth = 14;//横线的长度
    private int lineStrokeWidth = 1;//线条的大小
    private int lineRadiusWidth = 4;//横竖两条线中间弧度的半径

    private float textSize = 12f;
    private int textColor = Color.WHITE;

    private int textLinePadding = 6;//文字和标签图形之间的间距
    private int textLineSpacing = 6;//两行文字之间的间距
    private float textSpacingAdd = 9f;//换行间距


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
        initDimension();
        mCurrentType = TYPE_NONE;
        mViewGroupRect = new Rect();

        mLinePath = new Path();

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(lineStrokeWidth);
        mLinePaint.setColor(lineColor);
    }

    private void initDimension() {
        mCircleRadius = TagLocalDisplay.dp2px(getContext(), mCircleRadius);
        mOutCircleRadius = TagLocalDisplay.dp2px(getContext(), mOutCircleRadius);
        lineWidth = TagLocalDisplay.dp2px(getContext(), lineWidth);
        lineStrokeWidth = TagLocalDisplay.dp2px(getContext(), lineStrokeWidth);
        lineRadiusWidth = TagLocalDisplay.dp2px(getContext(), lineRadiusWidth);
        textLinePadding = TagLocalDisplay.dp2px(getContext(), textLinePadding);
        textLineSpacing = TagLocalDisplay.dp2px(getContext(), textLineSpacing);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isEmpty()) return;
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        getDrawingRect(mViewGroupRect);
        Log.d(TAG, "onMeasure: " + mViewGroupRect.toString());

        checkCenterBorder();
        checkAndSelectTypeWhenNone();

        setTextViewRectAndPath();
    }

    /**
     * 对中心点进行边界检测
     */
    private void checkCenterBorder() {
        if (mCenterPointF.x - mOutCircleRadius <= mViewGroupRect.left) {
            mCenterPointF.x = mCenterPointF.x + mOutCircleRadius;
        } else if (mCenterPointF.x + mOutCircleRadius >= mViewGroupRect.right) {
            mCenterPointF.x = mCenterPointF.x - mOutCircleRadius;
        }

        if (mCenterPointF.y - mOutCircleRadius <= mViewGroupRect.top) {
            mCenterPointF.y = mCenterPointF.y + mOutCircleRadius;
        } else if (mCenterPointF.y + mOutCircleRadius >= mViewGroupRect.bottom) {
            mCenterPointF.y = mCenterPointF.y - mOutCircleRadius;
        }
    }

    /**
     * 未设置方向时，检测并设置一个方向
     */
    private void checkAndSelectTypeWhenNone() {
        if (mCurrentType != TYPE_NONE) return;
        if (mTextViews.length == 1) {//单条标签
            if (mCenterPointF.x <= mViewGroupRect.exactCenterX()) {
                mCurrentType = TYPE_ONE_RIGHT;
            } else {
                mCurrentType = TYPE_ONE_LEFT;
            }
        } else if (mTextViews.length > 1) {//多条标签
            //将 view 分为均匀的 4块 区域
            if (mCenterPointF.x <= mViewGroupRect.exactCenterX()) {
                if (mCenterPointF.y <= mViewGroupRect.exactCenterY()) {
                    mCurrentType = TYPE_MORE_RIGHT_BOTTOM;
                } else {
                    mCurrentType = TYPE_MORE_RIGHT_TOP;
                }
            } else if (mCenterPointF.x > mViewGroupRect.exactCenterX()) {
                if (mCenterPointF.y <= mViewGroupRect.exactCenterY()) {
                    mCurrentType = TYPE_MORE_LEFT_BOTTOM;
                } else {
                    mCurrentType = TYPE_MORE_LEFT_TOP;
                }
            }
        }
        Log.d(TAG, "checkAndSelectType: mCurrentType = " + mCurrentType);
    }

    private void setTextViewRectAndPath() {
        mLinePath.reset();
        RectF rectF;
        switch (mCurrentType) {
            case TYPE_ONE_LEFT:
                /**
                 * text ————
                 */
                setTypeOneLeftTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x - lineWidth, mCenterPointF.y);
                break;
            case TYPE_ONE_RIGHT:
                /**
                 * ———— text
                 */
                setTypeOneRightTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x + lineWidth, mCenterPointF.y);
                break;
            case TYPE_MORE_LEFT_TOP:
                /**
                 * |
                 * |
                 * |_____
                 */
                setTypeMoreLeftTopTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x - lineWidth, mCenterPointF.y);

                rectF = new RectF(mCenterPointF.x - lineWidth - lineRadiusWidth,
                        mCenterPointF.y - lineRadiusWidth * 2,
                        mCenterPointF.x - lineWidth + lineRadiusWidth, mCenterPointF.y);
                mLinePath.addArc(rectF, 90, 90);

                mLinePath.moveTo(mCenterPointF.x - lineWidth - lineRadiusWidth,
                        mCenterPointF.y - lineRadiusWidth);

                mLinePath.lineTo(mCenterPointF.x - lineWidth - lineRadiusWidth,
                        mCenterPointF.y - lineRadiusWidth - (mTextViewRects[0].top - mCenterPointF.y));

                break;
            case TYPE_MORE_LEFT_BOTTOM:
                /**
                 * |------
                 * |
                 * |
                 */
                break;
            case TYPE_MORE_RIGHT_TOP:
                /**
                 *      |
                 *      |
                 * _____|
                 */
                break;
            case TYPE_MORE_RIGHT_BOTTOM:
                /**
                 * ------|
                 *       |
                 *       |
                 */
                break;
        }
    }

    private void setTypeOneLeftTextViewRect() {
        mTextViews[0].setGravity(Gravity.RIGHT);
        int measuredWidth = mTextViews[0].getMeasuredWidth();
        int measuredHeight = mTextViews[0].getMeasuredHeight() / mTextViews[0].getLineCount();

        float reviseWidth = measuredWidth - getTextMaxWidthDirectionLeft();

        if (reviseWidth > 0) {
            mCenterPointF.x = (mCenterPointF.x + reviseWidth) >= mViewGroupRect.right ?
                    (mViewGroupRect.right - mOutCircleRadius) : (mCenterPointF.x + reviseWidth);
            mTextViewRects[0].left = mViewGroupRect.left;

            float maxWidth = getTextMaxWidthDirectionLeft();
            mTextViewRects[0].right = (int) maxWidth;

            mTextViewRects[0].bottom = (int) (mCenterPointF.y +
                    measuredHeight * Math.ceil(measuredWidth / maxWidth));

            if (mTextViewRects[0].bottom > mViewGroupRect.bottom) {
                int reviseHeight = mTextViewRects[0].bottom - mViewGroupRect.bottom;
                mTextViewRects[0].bottom = mViewGroupRect.bottom;
                mCenterPointF.y = (mCenterPointF.y - reviseHeight) <= mViewGroupRect.top ?
                        (mViewGroupRect.top - mOutCircleRadius) : (mCenterPointF.y - reviseHeight);
            }

            mTextViewRects[0].top = (int) mCenterPointF.y;
        } else {
            float maxWidth = getTextMaxWidthDirectionLeft();
            mTextViewRects[0].left = (int) (maxWidth - measuredWidth);
            mTextViewRects[0].right = (int) maxWidth;
            mTextViewRects[0].top = (int) mCenterPointF.y;
            mTextViewRects[0].bottom = (int) (mCenterPointF.y + measuredHeight);
        }
    }

    private void setTypeOneRightTextViewRect() {
        mTextViews[0].setGravity(Gravity.LEFT);
        //如果超出屏幕 此时会自动换行
        int measuredWidth = mTextViews[0].getMeasuredWidth();
        int measuredHeight = mTextViews[0].getMeasuredHeight() / mTextViews[0].getLineCount();
        float reviseWidth = measuredWidth - getTextMaxWidthDirectionRight();

        if (reviseWidth > 0) {
            mCenterPointF.x = (mCenterPointF.x - reviseWidth) <= mViewGroupRect.left ?
                    (mViewGroupRect.left + mOutCircleRadius) : (mCenterPointF.x - reviseWidth);
            mTextViewRects[0].left = (int) (mCenterPointF.x + lineWidth + lineRadiusWidth + textLinePadding);
            mTextViewRects[0].right = (mTextViewRects[0].left + measuredWidth) > mViewGroupRect.right ?
                    mViewGroupRect.right : (mTextViewRects[0].left + measuredWidth);

            mTextViewRects[0].bottom = (int) (mCenterPointF.y +
                    measuredHeight * Math.ceil(measuredWidth / getTextMaxWidthDirectionRight()));
            if (mTextViewRects[0].bottom > mViewGroupRect.bottom) {
                int reviseHeight = mTextViewRects[0].bottom - mViewGroupRect.bottom;
                mTextViewRects[0].bottom = mViewGroupRect.bottom;
                mCenterPointF.y = (mCenterPointF.y - reviseHeight) <= mViewGroupRect.top ?
                        (mViewGroupRect.top - mOutCircleRadius) : (mCenterPointF.y - reviseHeight);
            }
            mTextViewRects[0].top = (int) mCenterPointF.y;
        } else {
            mTextViewRects[0].left = (int) (mCenterPointF.x + lineWidth + lineRadiusWidth + textLinePadding);
            mTextViewRects[0].right = mTextViewRects[0].left + measuredWidth;
            mTextViewRects[0].top = (int) mCenterPointF.y;
            mTextViewRects[0].bottom = (int) (mCenterPointF.y + measuredHeight);
        }
    }

    private void setTypeMoreLeftTopTextViewRect() {
        // TODO: 16/8/30 先进行单个的左右边界判断移动，以及单个的上下边界判断移动
        for (int i = mTextViews.length - 1; i >= 0; i--) {
            TextView textView = mTextViews[i];
            textView.setGravity(Gravity.RIGHT);
            int measuredWidth = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

            float reviseWidth = measuredWidth - getTextMaxWidthDirectionLeft();

            if (reviseWidth > 0) {

            } else {

            }
        }
    }

    private float getTextMaxWidthDirectionRight() {
        return mViewGroupRect.right - mCenterPointF.x - lineWidth - lineRadiusWidth - textLinePadding;
    }

    private float getTextMaxWidthDirectionLeft() {
        return mCenterPointF.x - lineWidth - lineRadiusWidth - textLinePadding;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isEmpty()) return;
        for (int i = 0; i < mTextViews.length; i++) {
            Log.d(TAG, "onLayout: mTextViewRect[" + i + "] = " + mTextViewRects[i].toString());
            mTextViews[i].measure(MeasureSpec.makeMeasureSpec(mTextViewRects[i].width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mTextViewRects[i].height(), MeasureSpec.EXACTLY));
            mTextViews[i].layout(mTextViewRects[i].left, mTextViewRects[i].top,
                    mTextViewRects[i].right, mTextViewRects[i].bottom);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEmpty()) return;

        //绘制外圆
        mCirclePaint.setColor(mOutCircleColor);
        canvas.drawCircle(mCenterPointF.x, mCenterPointF.y, mOutCircleRadius, mCirclePaint);

        //绘制线条
        canvas.drawPath(mLinePath, mLinePaint);

        //绘制内圆
        mCirclePaint.setColor(mCircleColor);
        canvas.drawCircle(mCenterPointF.x, mCenterPointF.y, mCircleRadius, mCirclePaint);

    }

    private boolean isEmpty() {
        return mTextViews == null || mTextViews.length < 1;
    }

    public void addTags(PointF centerPointF, List<String> tagContents) {
        addTags(centerPointF, tagContents, TYPE_NONE);
    }

    public void addTags(PointF centerPointF, List<String> tagContents, int type) {
        if (centerPointF == null || tagContents == null || tagContents.isEmpty()) return;
        mCenterPointF.set(centerPointF);
        mCurrentType = type;

        mTextViews = new TextView[tagContents.size()];
        mTextViewRects = new Rect[tagContents.size()];

        for (int i = 0; i < tagContents.size(); i++) {
            mTextViewRects[i] = new Rect();

            mTextViews[i] = new TextView(getContext());
            mTextViews[i].setTextSize(textSize);
            mTextViews[i].setTextColor(textColor);
            mTextViews[i].setIncludeFontPadding(false);

            mTextViews[i].setText(tagContents.get(i));
            addView(mTextViews[i]);
        }
    }

    public void clear() {
        mTextViews = null;
        mTextViewRects = null;
        mCurrentType = TYPE_NONE;
        mLinePath.reset();
        removeAllViews();
    }

    private void addTest(float x, float y) {
        List<String> strings = new ArrayList<>();
        strings.add("这是一条测试的非常长长长长长长长长长长长长长长长长长长长长的标签~~~");
//        strings.add("16768 数字");
//        strings.add("This is a test");

        addTags(new PointF(x, y), strings, TYPE_ONE_RIGHT);
    }

    /**
     * todo 点击事件处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEmpty()) {
            addTest(event.getX(), event.getY());
        }
        return super.onTouchEvent(event);
    }
}
