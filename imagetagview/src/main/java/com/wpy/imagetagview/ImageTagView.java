package com.wpy.imagetagview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * 图片标签
 * Created by feiyang on 16/6/29.
 */
public class ImageTagView extends View {

    private static final String TAG = "ImageTagView";

    public static final int TYPE_NONE = 0;//如果设为这个类型  会自己检测并设定类型

    //单条标签 左右两个方向
    public static final int TYPE_ONE_LEFT = 1;
    public static final int TYPE_ONE_RIGHT = 2;

    //多条标签 上下左右四个方向
    public static final int TYPE_MORE_LEFT_TOP = 11;
    public static final int TYPE_MORE_LEFT_BOTTOM = 12;
    public static final int TYPE_MORE_RIGHT_TOP = 21;
    public static final int TYPE_MORE_RIGHT_BOTTOM = 22;

    private Paint mCirclePaint;
    private Paint mLinePaint;
    private TextPaint mTextPaint;

    private int mCircleRadius = 4;//圆形的半径
    private int mCircleColor = Color.WHITE;
    private int mOutCircleColorAlpha = 60;
    private int mOutCircleRadiusFactor = 2;//外部圆形半径是内部的几倍

    private int lineColor = Color.WHITE;
    private int lineWidth = 24;//横线的长度
    private int lineStrokeWidth = 1;//线条的大小
    private int lineRadiusWidth = 4;//横竖两条线中间弧度的半径

    private float textSize = 12;
    private int textColor = Color.WHITE;

    private int textLinePadding = 6;//文字和标签图形之间的间距
    private int textLineSpacing = 6;//两行文字之间的间距
    private float textSpacingAdd = 9f;//换行间距

    private float mCenterX, mCenterY;//圆点的中心位置

    private int mTextMaxWidth;//文字最大能设置宽度
    private int mMinWidth = 24;

    private StaticLayout[] mStaticLayouts;
    private int[] mTextHeights;
    private int[] mTextWidths;

    private List<String> mTagTexts = new ArrayList<>();

    private int mCurrentType = TYPE_NONE;//标签的类型

    private Rect mTagTextRect;//文字的区间
    private Rect mCircleRect;//圆点的区间

    private Rect mTagViewRect;

    public ImageTagView(Context context) {
        this(context, null);
    }

    public ImageTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCurrentType = TYPE_NONE;
        mTagTextRect = new Rect();
        mCircleRect = new Rect();
        mTagViewRect = new Rect();

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);

        mCircleRadius = dp2px(getContext(), mCircleRadius);
        lineWidth = dp2px(getContext(), lineWidth);
        lineStrokeWidth = dp2px(getContext(), lineStrokeWidth);
        lineRadiusWidth = dp2px(getContext(), lineRadiusWidth);
        textSize = sp2px(getContext(), textSize);
        textLinePadding = dp2px(getContext(), textLinePadding);
        textLineSpacing = dp2px(getContext(), textLineSpacing);
        mMinWidth = dp2px(getContext(), mMinWidth);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isTagsEmpty()) return;
//        getLocalVisibleRect(mTagViewRect);//这个获取的是可见区域的大小，在 listview 中会导致大小不对问题
        getDrawingRect(mTagViewRect);
        Log.d(TAG, "onLayout: mTagViewRect " + mTagViewRect.toString());

        checkAndSelectType();
        checkCenterBorder();

        mTextMaxWidth = getTextMaxWidth();
        Log.d(TAG, "onLayout: mTextMaxWidth = " + mTextMaxWidth);

        addStaticLayout();
        checkBorderAndChangeType();
    }

    /**
     * 文字最大能设置的宽度
     */
    private int getTextMaxWidth() {
        int maxWidth;//判断<=0的情况，此时应该调整类型
        if (!isDirectionLeft()) {
            maxWidth = getTextMaxWidthDirectionRight();
            if (maxWidth <= mMinWidth) {//改变类型 防止显示时超出界限
                maxWidth = getTextMaxWidthDirectionLeft();
                autoChangeType = CHANGE_LEFT;

            }
        } else {
            maxWidth = getTextMaxWidthDirectionLeft();
            if (maxWidth <= mMinWidth) {//改变类型 防止显示时超出界限
                maxWidth = getTextMaxWidthDirectionRight();
                autoChangeType = CHANGE_RIGHT;
            }
        }
        return maxWidth;
    }

    private int getTextMaxWidthDirectionRight() {
        return (int) (getMeasuredWidth() - mCenterX - lineWidth - lineRadiusWidth - textLinePadding);
    }

    private int getTextMaxWidthDirectionLeft() {
        return (int) (mCenterX - lineWidth - lineRadiusWidth - textLinePadding);
    }

    /**
     * 添加文字绘制
     */
    private void addStaticLayout() {
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        for (int i = 0; i < mTagTexts.size(); i++) {
            mStaticLayouts[i] = new StaticLayout(mTagTexts.get(i), mTextPaint, mTextMaxWidth,
                    isDirectionLeft() ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL,
                    1.0f, textSpacingAdd, false);
            mTextHeights[i] = mStaticLayouts[i].getHeight();
//            mTextWidths[i] = mStaticLayouts[i].getWidth();//注：这个返回的是设置的最大宽度
            Log.d(TAG, "addStaticLayout: mTextHeights[" + i + "] = " + mTextHeights[i]);

//            Log.d(TAG, "addStaticLayout: mStaticLayouts[" + i + "] ");
            int lineCount = 0;
            for (int line = 0; line < mStaticLayouts[i].getLineCount(); line++) {
//                Log.d(TAG, "addStaticLayout: getLineMax = " + mStaticLayouts[i].getLineMax(line));
                int lineMax = (int) mStaticLayouts[i].getLineMax(line);
                if (lineMax > lineCount) {
                    lineCount = lineMax;
                    mTextWidths[i] = lineCount;
                }
            }
            Log.d(TAG, "addStaticLayout: mTextWidths[" + i + "] = " + mTextWidths[i]);
        }
    }

    private int autoChangeType = -3;
    private final int CHANGE_LEFT = -1;
    private final int CHANGE_RIGHT = -2;

    //做一次边界检测  防止超出界限 兼容旧数据
    private void checkBorderAndChangeType() {
        switch (mCurrentType) {
            case TYPE_ONE_LEFT:
                if (autoChangeType == CHANGE_RIGHT) {
                    mCurrentType = TYPE_ONE_RIGHT;
                }
                break;
            case TYPE_ONE_RIGHT:
                if (autoChangeType == CHANGE_LEFT) {
                    mCurrentType = TYPE_ONE_LEFT;
                }
                break;
            case TYPE_MORE_LEFT_TOP:
                boolean leftChangeTop = (mCenterY - getTotalTextHeight()) < mTagViewRect.top;
                if (autoChangeType == CHANGE_RIGHT) {
                    mCurrentType = leftChangeTop ? TYPE_MORE_RIGHT_BOTTOM : TYPE_MORE_RIGHT_TOP;
                } else {
                    if (leftChangeTop) {
                        mCurrentType = TYPE_MORE_LEFT_BOTTOM;
                    }
                }
                break;
            case TYPE_MORE_LEFT_BOTTOM:
                boolean leftChangeBottom = (mCenterY + getTotalTextHeight()) > mTagViewRect.bottom;
                if (autoChangeType == CHANGE_RIGHT) {
                    mCurrentType = leftChangeBottom ? TYPE_MORE_RIGHT_TOP : TYPE_MORE_RIGHT_BOTTOM;
                } else {
                    if (leftChangeBottom) {
                        mCurrentType = TYPE_MORE_LEFT_TOP;
                    }
                }
                break;
            case TYPE_MORE_RIGHT_TOP:
                boolean rightChangeTop = (mCenterY - getTotalTextHeight()) < mTagViewRect.top;
                if (autoChangeType == CHANGE_LEFT) {
                    mCurrentType = rightChangeTop ? TYPE_MORE_LEFT_BOTTOM : TYPE_MORE_LEFT_TOP;
                } else {
                    if (rightChangeTop) {
                        mCurrentType = TYPE_MORE_RIGHT_BOTTOM;
                    }
                }
                break;
            case TYPE_MORE_RIGHT_BOTTOM:
                boolean rightChangeBottom = (mCenterY + getTotalTextHeight()) > mTagViewRect.bottom;
                if (autoChangeType == CHANGE_LEFT) {
                    mCurrentType = rightChangeBottom ? TYPE_MORE_LEFT_TOP : TYPE_MORE_LEFT_BOTTOM;
                } else {
                    if (rightChangeBottom) {
                        mCurrentType = TYPE_MORE_RIGHT_TOP;
                    }
                }
                break;
        }
        autoChangeType = -3;
    }

    private boolean isDirectionLeft() {
        return mCurrentType == TYPE_ONE_LEFT ||
                mCurrentType == TYPE_MORE_LEFT_TOP ||
                mCurrentType == TYPE_MORE_LEFT_BOTTOM;
    }

    /**
     * 对中心点进行边界检测
     */
    private void checkCenterBorder() {
        int radius = mCircleRadius * mOutCircleRadiusFactor;
        if (mCenterX - radius < mTagViewRect.left) {
            mCenterX = mCenterX + radius;
        } else if (mCenterX + radius > mTagViewRect.right) {
            mCenterX = mCenterX - radius;
        }

        if (mCenterY - radius < mTagViewRect.top) {
            mCenterY = mCenterY + radius;
        } else if (mCenterY + radius > mTagViewRect.bottom) {
            mCenterY = mCenterY - radius;
        }
    }

    /**
     * 未设置方向时，检测并设置一个方向
     */
    private void checkAndSelectType() {
        if (mCurrentType != TYPE_NONE) return;
        if (mTagTexts.size() == 1) {//单条标签
            if (mCenterX <= mTagViewRect.exactCenterX()) {
                mCurrentType = TYPE_ONE_RIGHT;
            } else {
                mCurrentType = TYPE_ONE_LEFT;
            }
        } else if (mTagTexts.size() > 1) {//多条标签
            //将 view 分为均匀的 4块 区域
            if (mCenterX <= mTagViewRect.exactCenterX()) {
                if (mCenterY <= mTagViewRect.exactCenterY()) {
                    mCurrentType = TYPE_MORE_RIGHT_BOTTOM;
                } else {
                    mCurrentType = TYPE_MORE_RIGHT_TOP;
                }
            } else if (mCenterX > mTagViewRect.exactCenterX()) {
                if (mCenterY <= mTagViewRect.exactCenterY()) {
                    mCurrentType = TYPE_MORE_LEFT_BOTTOM;
                } else {
                    mCurrentType = TYPE_MORE_LEFT_TOP;
                }
            }
        }
        Log.d(TAG, "checkAndSelectType: mCurrentType = " + mCurrentType);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.d(TAG, "dispatchDraw: ");
        if (!isTagsEmpty()) {
            //绘制圆形
            mCirclePaint.setColor(mCircleColor);
            canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mCirclePaint);
            mCirclePaint.setAlpha(mOutCircleColorAlpha);
            canvas.drawCircle(mCenterX, mCenterY, mCircleRadius * mOutCircleRadiusFactor, mCirclePaint);

            //圆点范围坐标
            mCircleRect.left = (int) (mCenterX - mCircleRadius * mOutCircleRadiusFactor);
            mCircleRect.top = (int) (mCenterY - mCircleRadius * mOutCircleRadiusFactor);
            mCircleRect.right = (int) (mCenterX + mCircleRadius * mOutCircleRadiusFactor);
            mCircleRect.bottom = (int) (mCenterY + mCircleRadius * mOutCircleRadiusFactor);

            //绘制线条
            mLinePaint.setStrokeWidth(lineStrokeWidth);
            mLinePaint.setColor(lineColor);
            canvas.drawPath(getLinePath(), mLinePaint);

            drawText(canvas);

            Log.d(TAG, "dispatchDraw: mCircleRect = " + mCircleRect.toString());
            Log.d(TAG, "dispatchDraw: mTagTextRect = " + mTagTextRect.toString());
        }

        super.dispatchDraw(canvas);
    }

    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas) {
        switch (mCurrentType) {
            case TYPE_ONE_LEFT:
                canvas.save();
                canvas.translate(mCenterX - lineWidth - mTextMaxWidth - textLinePadding, mCenterY - mCircleRadius);
                mStaticLayouts[0].draw(canvas);
                canvas.restore();

                mTagTextRect.left = (int) (mCenterX - lineWidth - mTextWidths[0] - textLinePadding);
                mTagTextRect.top = (int) (mCenterY - mCircleRadius);
                mTagTextRect.right = (int) (mCenterX - lineWidth - textLinePadding);
                mTagTextRect.bottom = (int) (mCenterY - mCircleRadius + mTextHeights[0]);
                break;
            case TYPE_ONE_RIGHT:
                canvas.save();
                canvas.translate(mCenterX + lineWidth + textLinePadding, mCenterY - mCircleRadius);
                mStaticLayouts[0].draw(canvas);
                canvas.restore();

                mTagTextRect.left = (int) (mCenterX + lineWidth + textLinePadding);
                mTagTextRect.top = (int) (mCenterY - mCircleRadius);
                mTagTextRect.right = (int) (mCenterX + lineWidth + textLinePadding + mTextWidths[0]);
                mTagTextRect.bottom = (int) (mCenterY - mCircleRadius + mTextHeights[0]);
                break;
            case TYPE_MORE_LEFT_TOP:
                for (int i = 0; i < mStaticLayouts.length; i++) {
                    canvas.save();
                    canvas.translate(mCenterX - lineWidth - lineRadiusWidth - mTextMaxWidth - textLinePadding,
                            mCenterY - lineRadiusWidth - getTotalTextHeight() + getTextY(i));
                    mStaticLayouts[i].draw(canvas);
                    canvas.restore();
                }

                mTagTextRect.left = (int) (mCenterX - lineWidth - lineRadiusWidth - textLinePadding - getMaxLineWidth());
                mTagTextRect.top = (int) (mCenterY - lineRadiusWidth - getTotalTextHeight());
                mTagTextRect.right = (int) (mCenterX - lineWidth - lineRadiusWidth - textLinePadding);
                mTagTextRect.bottom = (int) mCenterY;
                break;
            case TYPE_MORE_LEFT_BOTTOM:
                for (int i = 0; i < mStaticLayouts.length; i++) {
                    canvas.save();
                    canvas.translate(mCenterX - lineWidth - lineRadiusWidth - textLinePadding - mTextMaxWidth,
                            mCenterY + getTextY(i));
                    mStaticLayouts[i].draw(canvas);
                    canvas.restore();
                }

                mTagTextRect.left = (int) (mCenterX - lineWidth - lineRadiusWidth - textLinePadding - getMaxLineWidth());
                mTagTextRect.top = (int) mCenterY;
                mTagTextRect.right = (int) (mCenterX - lineWidth - lineRadiusWidth - textLinePadding);
                mTagTextRect.bottom = (int) (mCenterY + lineRadiusWidth + getTotalTextHeight());
                break;
            case TYPE_MORE_RIGHT_TOP:
                for (int i = 0; i < mStaticLayouts.length; i++) {
                    canvas.save();
                    canvas.translate(mCenterX + lineWidth + lineRadiusWidth + textLinePadding,
                            mCenterY - lineRadiusWidth - getTotalTextHeight() + getTextY(i));
                    mStaticLayouts[i].draw(canvas);
                    canvas.restore();
                }

                mTagTextRect.left = (int) (mCenterX + lineWidth + lineRadiusWidth + textLinePadding);
                mTagTextRect.top = (int) (mCenterY - lineRadiusWidth - getTotalTextHeight());
                mTagTextRect.right = (int) (mCenterX + lineWidth + lineRadiusWidth + textLinePadding + getMaxLineWidth());
                mTagTextRect.bottom = (int) mCenterY;
                break;
            case TYPE_MORE_RIGHT_BOTTOM:
                for (int i = 0; i < mStaticLayouts.length; i++) {
                    canvas.save();
                    canvas.translate(mCenterX + lineWidth + lineRadiusWidth + textLinePadding,
                            mCenterY + lineRadiusWidth + getTextY(i));
                    mStaticLayouts[i].draw(canvas);
                    canvas.restore();
                }

                mTagTextRect.left = (int) (mCenterX + lineWidth + lineRadiusWidth + textLinePadding);
                mTagTextRect.top = (int) mCenterY;
                mTagTextRect.right = (int) (mCenterX + lineWidth + lineRadiusWidth + textLinePadding + getMaxLineWidth());
                mTagTextRect.bottom = (int) (mCenterY + lineRadiusWidth + getTotalTextHeight());
                break;
            default:
                break;
        }
    }

    /**
     * 多行文字，获取最大的宽度
     */
    private int getMaxLineWidth() {
        int maxLineWidth = 0;
        for (int mTextWidth : mTextWidths) {
            if (mTextWidth > maxLineWidth) {
                maxLineWidth = mTextWidth;
            }
        }
        return maxLineWidth;
    }

    /**
     * 线条路径
     */
    private Path getLinePath() {
        Path path = new Path();
        path.moveTo(mCenterX, mCenterY);
        RectF rectF;
        switch (mCurrentType) {
            case TYPE_ONE_LEFT:
                path.lineTo(mCenterX - lineWidth, mCenterY);
                break;
            case TYPE_ONE_RIGHT:
                path.lineTo(mCenterX + lineWidth, mCenterY);
                break;
            case TYPE_MORE_LEFT_TOP:
                /**
                 * |
                 * |
                 * |_____
                 */
                path.lineTo(mCenterX - lineWidth, mCenterY);

                rectF = new RectF(mCenterX - lineWidth - lineRadiusWidth, mCenterY - lineRadiusWidth * 2,
                        mCenterX - lineWidth + lineRadiusWidth, mCenterY);
                path.addArc(rectF, 90, 90);

                path.moveTo(mCenterX - lineWidth - lineRadiusWidth, mCenterY - lineRadiusWidth);

                path.lineTo(mCenterX - lineWidth - lineRadiusWidth, mCenterY - lineRadiusWidth - getTotalTextHeight());
                break;
            case TYPE_MORE_LEFT_BOTTOM:
                /**
                 * |------
                 * |
                 * |
                 */
                path.lineTo(mCenterX - lineWidth, mCenterY);

                rectF = new RectF(mCenterX - lineWidth - lineRadiusWidth, mCenterY,
                        mCenterX - lineWidth + lineRadiusWidth, mCenterY + lineRadiusWidth * 2);
                path.addArc(rectF, 180, 90);

                path.moveTo(mCenterX - lineWidth - lineRadiusWidth, mCenterY + lineRadiusWidth);

                path.lineTo(mCenterX - lineWidth - lineRadiusWidth, mCenterY + lineRadiusWidth + getTotalTextHeight());
                break;
            case TYPE_MORE_RIGHT_TOP:
                /**
                 *      |
                 *      |
                 * _____|
                 */
                path.lineTo(mCenterX + lineWidth, mCenterY);

                rectF = new RectF(mCenterX + lineWidth - lineRadiusWidth, mCenterY - lineRadiusWidth * 2,
                        mCenterX + lineWidth + lineRadiusWidth, mCenterY);
                path.addArc(rectF, 0, 90);

                path.moveTo(mCenterX + lineWidth + lineRadiusWidth, mCenterY - lineRadiusWidth);

                path.lineTo(mCenterX + lineWidth + lineRadiusWidth, mCenterY - lineRadiusWidth - getTotalTextHeight());
                break;
            case TYPE_MORE_RIGHT_BOTTOM:
                /**
                 * ------|
                 *       |
                 *       |
                 */
                path.lineTo(mCenterX + lineWidth, mCenterY);

                rectF = new RectF(mCenterX + lineWidth - lineRadiusWidth, mCenterY,
                        mCenterX + lineWidth + lineRadiusWidth, mCenterY + lineRadiusWidth * 2);
                path.addArc(rectF, 270, 90);

                path.moveTo(mCenterX + lineWidth + lineRadiusWidth, mCenterY + lineRadiusWidth);

                path.lineTo(mCenterX + lineWidth + lineRadiusWidth, mCenterY + lineRadiusWidth + getTotalTextHeight());
                break;
            default:
                break;
        }
        return path;
    }

    /**
     * 文字的总高度
     */
    private int getTotalTextHeight() {
        int mTotalTextHeight = 0;
        for (int mTextHeight : mTextHeights) {
            mTotalTextHeight = mTotalTextHeight + mTextHeight;
        }
        mTotalTextHeight = mTotalTextHeight + (mTagTexts.size() - 1) * textLineSpacing;
        return mTotalTextHeight;
    }

    /**
     * 文字绘制的 Y 轴的值
     *
     * @param currentText 当前第几条文字
     */
    private int getTextY(int currentText) {
        int y = 0;
        if (currentText > 0) {
            for (int i = 0; i < currentText; i++) {
                y = y + mTextHeights[i] + textLineSpacing;
            }
        }
        return y;
    }

    private boolean isTagsEmpty() {
        return mTagTexts == null || mTagTexts.isEmpty();
    }

    private void setTagTexts(List<String> tagTexts) {
        this.mTagTexts = tagTexts;
        Log.d(TAG, "setTagTexts: mTagTexts.size = " + mTagTexts.size());
        if (isTagsEmpty()) return;
        mStaticLayouts = new StaticLayout[tagTexts.size()];
        mTextHeights = new int[tagTexts.size()];
        mTextWidths = new int[tagTexts.size()];
    }

    private Rect copyRect(Rect rect) {
        Rect newRect = new Rect();
        newRect.left = rect.left;
        newRect.top = rect.top;
        newRect.right = rect.right;
        newRect.bottom = rect.bottom;
        return newRect;
    }

    /**
     * 检测边界 防止标签超出范围
     */
    private boolean canMove(float centerX, float centerY) {
        float distanceX = centerX - mCenterX;
        float distanceY = centerY - mCenterY;

        Rect circleRectTemp = copyRect(mCircleRect);

        circleRectTemp.left = (int) (circleRectTemp.left + distanceX);
        circleRectTemp.top = (int) (circleRectTemp.top + distanceY);
        circleRectTemp.right = (int) (circleRectTemp.right + distanceX);
        circleRectTemp.bottom = (int) (circleRectTemp.bottom + distanceY);

        Rect textRectTemp = copyRect(mTagTextRect);

        textRectTemp.left = (int) (textRectTemp.left + distanceX);
        textRectTemp.top = (int) (textRectTemp.top + distanceY);
        textRectTemp.right = (int) (textRectTemp.right + distanceX);
        textRectTemp.bottom = (int) (textRectTemp.bottom + distanceY);

        return mTagViewRect.contains(circleRectTemp) &&
                mTagViewRect.contains(textRectTemp);
    }

    public void moveTo(float centerX, float centerY) {
        if (!canMove(centerX, centerY)) return;
        Log.d(TAG, "moveTo: " + mCenterX + " -> " + centerX + " "
                + mCenterY + " -> " + centerY);
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        postInvalidate();
    }

    /**
     * 获取可以变换的类型 顺时针变换
     */
    public int getCanChangeType() {
        int type = mCurrentType;

        switch (mCurrentType) {
            case TYPE_ONE_LEFT:
                if ((mTagViewRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTagTextRect.left)) {
                    type = TYPE_ONE_RIGHT;
                }
                break;
            case TYPE_ONE_RIGHT:
                if ((mCircleRect.left - mTagViewRect.left) >=
                        (mTagTextRect.right - mCircleRect.right)) {
                    type = TYPE_ONE_LEFT;
                }
                break;
            case TYPE_MORE_LEFT_TOP:
                if ((mTagViewRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTagTextRect.left)) {
                    type = TYPE_MORE_RIGHT_TOP;
                } else if ((mTagViewRect.bottom - mTagTextRect.bottom) >=
                        mTagTextRect.height()) {
                    type = TYPE_MORE_LEFT_BOTTOM;
                }
                break;
            case TYPE_MORE_LEFT_BOTTOM:
                if ((mTagTextRect.top - mTagViewRect.top) >=
                        mTagTextRect.height()) {
                    type = TYPE_MORE_LEFT_TOP;
                } else if ((mTagViewRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTagTextRect.left)) {
                    type = TYPE_MORE_RIGHT_BOTTOM;
                }
                break;
            case TYPE_MORE_RIGHT_TOP:
                if ((mTagViewRect.bottom - mTagTextRect.bottom) >=
                        mTagTextRect.height()) {
                    type = TYPE_MORE_RIGHT_BOTTOM;
                } else if ((mCircleRect.left - mTagViewRect.left) >=
                        (mTagTextRect.right - mCircleRect.right)) {
                    type = TYPE_MORE_LEFT_TOP;
                }
                break;
            case TYPE_MORE_RIGHT_BOTTOM:
                if ((mCircleRect.left - mTagViewRect.left) >=
                        (mTagTextRect.right - mCircleRect.right)) {
                    type = TYPE_MORE_LEFT_BOTTOM;
                } else if ((mTagTextRect.top - mTagViewRect.top) >=
                        mTagTextRect.height()) {
                    type = TYPE_MORE_RIGHT_TOP;
                }
                break;
            default:
                break;
        }
        return type;
    }

    public void changeType(int type) {
        if (type == mCurrentType) return;
        mCurrentType = type;
        requestLayout();
        postInvalidate();
    }

    public boolean isCenterClick(int x, int y) {
        return mCircleRect.contains(x, y);
    }

    public boolean isContentTextClick(int x, int y) {
        return mTagTextRect.contains(x, y);
    }

    public void addTags(float centerX, float centerY, List<String> tagTexts) {
        addTags(centerX, centerY, tagTexts, TYPE_NONE);
    }

    public void addTags(float centerX, float centerY, List<String> tagTexts, int type) {
        mCurrentType = type;
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        Log.d(TAG, "addTags: mCenterX = " + mCenterX + " mCenterY = " + mCenterY);
        setTagTexts(tagTexts);
        postInvalidate();
    }

    public void updateTag(List<String> tagTexts) {
        mCurrentType = TYPE_NONE;
        setTagTexts(tagTexts);
        requestLayout();
        postInvalidate();
    }

    public float getCenterX() {
        return mCenterX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public int getType() {
        return mCurrentType;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static float sp2px(Context context, float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
}
