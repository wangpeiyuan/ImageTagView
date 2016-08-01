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
import android.view.View;

import com.wpy.imagetagview.util.TagLocalDisplay;

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

    private StaticLayout[] mStaticLayouts;
    private int[] mTextHeights;
    private int[] mTextWidths;

    private List<String> mTagTexts = new ArrayList<>();

    private int mCurrentType = TYPE_NONE;//标签的类型

    private Rect mTagTextRect;//文字的区间
    private Rect mCircleRect;//圆点的区间
    private Rect mContentRect;//整个标签的区域 包含文字和圆点

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
        initDimension();
        mCurrentType = TYPE_NONE;
        mTagTextRect = new Rect();
        mCircleRect = new Rect();
        mTagViewRect = new Rect();
        mContentRect = new Rect();

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(lineStrokeWidth);
        mLinePaint.setColor(lineColor);


        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
    }

    private void initDimension() {
        mCircleRadius = TagLocalDisplay.dp2px(getContext(), mCircleRadius);
        lineWidth = TagLocalDisplay.dp2px(getContext(), lineWidth);
        lineStrokeWidth = TagLocalDisplay.dp2px(getContext(), lineStrokeWidth);
        lineRadiusWidth = TagLocalDisplay.dp2px(getContext(), lineRadiusWidth);
        textSize = TagLocalDisplay.sp2px(getContext(), textSize);
        textLinePadding = TagLocalDisplay.dp2px(getContext(), textLinePadding);
        textLineSpacing = TagLocalDisplay.dp2px(getContext(), textLineSpacing);
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
        checkAndReviseCenterY();
    }

    /**
     * 文字最大能设置的宽度  判断最大的宽度是否足够绘制文字  不能的话 移动 X 值
     */
    private int getTextMaxWidth() {
        int maxWidth;
        int maxDesiredWidth = getMaxDesiredWidth();
        if (!isDirectionLeft()) {
            maxWidth = getTextMaxWidthDirectionRight();
            if (maxWidth < maxDesiredWidth) {
                Log.d(TAG, "getTextMaxWidth: DirectionRight ");
                float reviseWidth = maxDesiredWidth - maxWidth;//需要调整的宽度
                Log.d(TAG, "getTextMaxWidth: reviseWidth = " + reviseWidth);
                mCenterX = (mCenterX - reviseWidth) <= mTagViewRect.left ?
                        (mCircleRadius * 2) : (mCenterX - reviseWidth);
                Log.d(TAG, "getTextMaxWidth: revise mCenterX = " + mCenterX);
                maxWidth = getTextMaxWidthDirectionRight();
            }
        } else {
            maxWidth = getTextMaxWidthDirectionLeft();
            if (maxWidth < maxDesiredWidth) {
                Log.d(TAG, "getTextMaxWidth: DirectionLeft ");
                float reviseWidth = maxDesiredWidth - maxWidth;//需要调整的宽度
                Log.d(TAG, "getTextMaxWidth: reviseWidth = " + reviseWidth);
                mCenterX = (mCenterX + reviseWidth) >= mTagViewRect.right ?
                        (mTagViewRect.right - mCircleRadius * 2) : (mCenterX + reviseWidth);
                Log.d(TAG, "getTextMaxWidth: revise mCenterX = " + mCenterX);
                maxWidth = getTextMaxWidthDirectionLeft();
            }
        }
        return maxWidth;
    }

    /**
     * 获取要绘制文字的最大宽度
     */
    private int getMaxDesiredWidth() {
        int maxDesiredWidth = 0;
        for (String tag : mTagTexts) {
            int desiredWidth = (int) Layout.getDesiredWidth(tag, mTextPaint);
            Log.d(TAG, "getMaxDesiredWidth: StringTag = " + tag + " desiredWidth = " + desiredWidth);
            if (desiredWidth > maxDesiredWidth) {
                maxDesiredWidth = desiredWidth;
            }
        }
        Log.d(TAG, "getMaxDesiredWidth: maxDesiredWidth = " + maxDesiredWidth);
        return maxDesiredWidth;
    }

    private int getTextMaxWidthDirectionRight() {
        return (int) (getMeasuredWidth() - mCenterX - lineWidth - lineRadiusWidth - textLinePadding);
    }

    private int getTextMaxWidthDirectionLeft() {
        return (int) (mCenterX - lineWidth - lineRadiusWidth - textLinePadding);
    }

    /**
     * 检测上下是否超出边界  超出移动 Y 值
     */
    private void checkAndReviseCenterY() {
        if (isDirectionTop() && ((mCenterY - getTotalTextHeight()) < mTagViewRect.top)) {
            Log.d(TAG, "checkAndReviseCenterY: DirectionTop ");
            float reviseHeight = mTagViewRect.top - mCenterY - getTotalTextHeight();
            Log.d(TAG, "checkAndReviseCenterY: reviseHeight = " + reviseHeight);
            mCenterY = mCenterY + Math.abs(reviseHeight) + mCircleRadius * 2 + lineRadiusWidth;
            Log.d(TAG, "checkAndReviseCenterY: revise mCenterY = " + mCenterY);
        } else if (!isDirectionTop() && (mCenterY + getTotalTextHeight()) > mTagViewRect.bottom) {
            Log.d(TAG, "checkAndReviseCenterY: DirectionBottom ");
            float reviseHeight = mCenterY + getTotalTextHeight() - mTagViewRect.bottom;
            Log.d(TAG, "checkAndReviseCenterY: reviseHeight = " + reviseHeight);
            mCenterY = mCenterY - Math.abs(reviseHeight) - mCircleRadius * 2 - lineRadiusWidth;
            Log.d(TAG, "checkAndReviseCenterY: revise mCenterY = " + mCenterY);
        }
    }

    /**
     * 添加文字绘制
     */
    private void addStaticLayout() {
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

    public boolean isDirectionLeft() {
        return mCurrentType == TYPE_ONE_LEFT ||
                mCurrentType == TYPE_MORE_LEFT_TOP ||
                mCurrentType == TYPE_MORE_LEFT_BOTTOM;
    }

    public boolean isDirectionTop() {
        return mCurrentType == TYPE_MORE_LEFT_TOP ||
                mCurrentType == TYPE_MORE_RIGHT_TOP;
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
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);
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
            canvas.drawPath(getLinePath(), mLinePaint);

            drawText(canvas);

            mContentRect.left = mCircleRect.left < mTagTextRect.left ? mCircleRect.left : mTagTextRect.left;
            mContentRect.top = mCircleRect.top < mTagTextRect.top ? mCircleRect.top : mTagTextRect.top;
            mContentRect.right = mCircleRect.right > mTagTextRect.right ? mCircleRect.right : mTagTextRect.right;
            mContentRect.bottom = mCircleRect.bottom > mTagTextRect.bottom ? mCircleRect.bottom : mTagTextRect.bottom;

            Log.d(TAG, "dispatchDraw: mCircleRect = " + mCircleRect.toString());
            Log.d(TAG, "dispatchDraw: mTagTextRect = " + mTagTextRect.toString());
            Log.d(TAG, "dispatchDraw: mContentRect = " + mContentRect.toString());
        }

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

    private Rect getMoveContentRect(float centerX, float centerY) {
        float distanceX = centerX - mCenterX;
        float distanceY = centerY - mCenterY;

        Rect ContentRectTemp = copyRect(mContentRect);

        ContentRectTemp.left = (int) (ContentRectTemp.left + distanceX);
        ContentRectTemp.top = (int) (ContentRectTemp.top + distanceY);
        ContentRectTemp.right = (int) (ContentRectTemp.right + distanceX);
        ContentRectTemp.bottom = (int) (ContentRectTemp.bottom + distanceY);

        return ContentRectTemp;
    }

    public void moveTo(float centerX, float centerY) {
        Rect moveContentRect = getMoveContentRect(centerX, centerY);

        if (mTagViewRect.left <= moveContentRect.left && mTagViewRect.right >= moveContentRect.right) {
            Log.d(TAG, "moveTo: " + mCenterX + " -> " + centerX + " ");
            this.mCenterX = centerX;
        }
        if (mTagViewRect.top <= moveContentRect.top && mTagViewRect.bottom >= moveContentRect.bottom) {
            Log.d(TAG, "moveTo: " + mCenterY + " -> " + centerY);
            this.mCenterY = centerY;
        }
        postInvalidate();
    }

    /**
     * 获取可以变换的类型 顺时针变换
     *
     * @param forceChangeType 强制变换类型 centerX、centerY 可能会变化
     */
    public int getCanChangeType(boolean forceChangeType) {
        int type = mCurrentType;

        switch (mCurrentType) {
            case TYPE_ONE_LEFT:
                if ((mTagViewRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTagTextRect.left) || forceChangeType) {
                    type = TYPE_ONE_RIGHT;
                }
                break;
            case TYPE_ONE_RIGHT:
                if ((mCircleRect.left - mTagViewRect.left) >=
                        (mTagTextRect.right - mCircleRect.right) || forceChangeType) {
                    type = TYPE_ONE_LEFT;
                }
                break;
            case TYPE_MORE_LEFT_TOP:
                if ((mTagViewRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTagTextRect.left) || forceChangeType) {
                    type = TYPE_MORE_RIGHT_TOP;
                } else if ((mTagViewRect.bottom - mTagTextRect.bottom) >=
                        mTagTextRect.height()) {
                    type = TYPE_MORE_LEFT_BOTTOM;
                }
                break;
            case TYPE_MORE_LEFT_BOTTOM:
                if ((mTagTextRect.top - mTagViewRect.top) >=
                        mTagTextRect.height() || forceChangeType) {
                    type = TYPE_MORE_LEFT_TOP;
                } else if ((mTagViewRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTagTextRect.left)) {
                    type = TYPE_MORE_RIGHT_BOTTOM;
                }
                break;
            case TYPE_MORE_RIGHT_TOP:
                if ((mTagViewRect.bottom - mTagTextRect.bottom) >=
                        mTagTextRect.height() || forceChangeType) {
                    type = TYPE_MORE_RIGHT_BOTTOM;
                } else if ((mCircleRect.left - mTagViewRect.left) >=
                        (mTagTextRect.right - mCircleRect.right)) {
                    type = TYPE_MORE_LEFT_TOP;
                }
                break;
            case TYPE_MORE_RIGHT_BOTTOM:
                if ((mCircleRect.left - mTagViewRect.left) >=
                        (mTagTextRect.right - mCircleRect.right) || forceChangeType) {
                    type = TYPE_MORE_LEFT_BOTTOM;
                } else if ((mTagTextRect.top - mTagViewRect.top) >=
                        mTagTextRect.height()) {
                    type = TYPE_MORE_RIGHT_TOP;
                }
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
}
