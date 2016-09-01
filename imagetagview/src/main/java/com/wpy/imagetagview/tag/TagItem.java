package com.wpy.imagetagview.tag;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * 具体标签 Item
 * Created by feiyang on 16/8/31.
 */
public class TagItem {
    private static final String TAG = "TagItem";

    private int mCurrentType = TagFactor.TYPE_NONE;//标签的类型

    private TextView[] mTextViews;
    private Rect[] mTextViewRects;

    private PointF mCenterPointF = new PointF();

    private Path mLinePath;

    private TagFactor mFactor;

    //用来判断点击区间
    private Rect mTextRect;//文字的区间
    private Rect mCircleRect;//圆点的区间  有一定的扩大
    private Rect mTagRect;//整个标签的区域 包含文字和圆点

    public TagItem(TagFactor factor) {
        mFactor = factor;
        mLinePath = new Path();

        mTextRect = new Rect();
        mCircleRect = new Rect();
        mTagRect = new Rect();
    }

    /**
     * 对中心点进行边界检测
     */
    public void checkCenterBorder() {
        if (mCenterPointF.x - mFactor.mOutCircleRadius <= mFactor.mViewGroupRect.left) {
            mCenterPointF.x = mCenterPointF.x + mFactor.mOutCircleRadius;
        } else if (mCenterPointF.x + mFactor.mOutCircleRadius >= mFactor.mViewGroupRect.right) {
            mCenterPointF.x = mCenterPointF.x - mFactor.mOutCircleRadius;
        }

        if (mCenterPointF.y - mFactor.mOutCircleRadius <= mFactor.mViewGroupRect.top) {
            mCenterPointF.y = mCenterPointF.y + mFactor.mOutCircleRadius;
        } else if (mCenterPointF.y + mFactor.mOutCircleRadius >= mFactor.mViewGroupRect.bottom) {
            mCenterPointF.y = mCenterPointF.y - mFactor.mOutCircleRadius;
        }
    }

    /**
     * 未设置方向时，检测并设置一个方向
     */
    public void checkAndSelectTypeWhenNone() {
        if (mCurrentType != TagFactor.TYPE_NONE) return;
        if (mTextViews.length == 1) {//单条标签
            if (mCenterPointF.x <= mFactor.mViewGroupRect.exactCenterX()) {
                mCurrentType = TagFactor.TYPE_ONE_RIGHT;
            } else {
                mCurrentType = TagFactor.TYPE_ONE_LEFT;
            }
        } else if (mTextViews.length > 1) {//多条标签
            //将 view 分为均匀的 4块 区域
            if (mCenterPointF.x <= mFactor.mViewGroupRect.exactCenterX()) {
                if (mCenterPointF.y <= mFactor.mViewGroupRect.exactCenterY()) {
                    mCurrentType = TagFactor.TYPE_MORE_RIGHT_BOTTOM;
                } else {
                    mCurrentType = TagFactor.TYPE_MORE_RIGHT_TOP;
                }
            } else if (mCenterPointF.x > mFactor.mViewGroupRect.exactCenterX()) {
                if (mCenterPointF.y <= mFactor.mViewGroupRect.exactCenterY()) {
                    mCurrentType = TagFactor.TYPE_MORE_LEFT_BOTTOM;
                } else {
                    mCurrentType = TagFactor.TYPE_MORE_LEFT_TOP;
                }
            }
        }
        Log.d(TAG, "checkAndSelectType: mCurrentType = " + mCurrentType);
    }

    public void setTextViewRectAndPath() {
        mLinePath.reset();
        RectF rectF;
        switch (mCurrentType) {
            case TagFactor.TYPE_ONE_LEFT:
                /**
                 * text ————
                 */
                setTypeOneLeftTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x - mFactor.mLineWidth, mCenterPointF.y);
                break;
            case TagFactor.TYPE_ONE_RIGHT:
                /**
                 * ———— text
                 */
                setTypeOneRightTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x + mFactor.mLineWidth, mCenterPointF.y);
                break;
            case TagFactor.TYPE_MORE_LEFT_TOP:
                /**
                 * |
                 * |
                 * |_____
                 */
                setTypeMoreLeftTopTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x - mFactor.mLineWidth, mCenterPointF.y);

                rectF = new RectF(mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth,
                        mCenterPointF.y - mFactor.mLineRadiusWidth * 2,
                        mCenterPointF.x - mFactor.mLineWidth + mFactor.mLineRadiusWidth, mCenterPointF.y);
                mLinePath.addArc(rectF, 90, 90);

                mLinePath.moveTo(mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth,
                        mCenterPointF.y - mFactor.mLineRadiusWidth);

                mLinePath.lineTo(mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth,
                        mCenterPointF.y - mFactor.mLineRadiusWidth - (mCenterPointF.y - mTextViewRects[0].top));
                break;
            case TagFactor.TYPE_MORE_LEFT_BOTTOM:
                /**
                 * |------
                 * |
                 * |
                 */
                setTypeMoreLeftBottomTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x - mFactor.mLineWidth, mCenterPointF.y);

                rectF = new RectF(mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth, mCenterPointF.y,
                        mCenterPointF.x - mFactor.mLineWidth + mFactor.mLineRadiusWidth, mCenterPointF.y + mFactor.mLineRadiusWidth * 2);
                mLinePath.addArc(rectF, 180, 90);

                mLinePath.moveTo(mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth, mCenterPointF.y + mFactor.mLineRadiusWidth);

                mLinePath.lineTo(mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth,
                        mCenterPointF.y + mFactor.mLineRadiusWidth +
                                (mTextViewRects[mTextViewRects.length - 1].bottom - mCenterPointF.y));
                break;
            case TagFactor.TYPE_MORE_RIGHT_TOP:
                /**
                 *      |
                 *      |
                 * _____|
                 */
                setTypeMoreRightTopTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x + mFactor.mLineWidth, mCenterPointF.y);

                rectF = new RectF(mCenterPointF.x + mFactor.mLineWidth - mFactor.mLineRadiusWidth, mCenterPointF.y - mFactor.mLineRadiusWidth * 2,
                        mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth, mCenterPointF.y);
                mLinePath.addArc(rectF, 0, 90);

                mLinePath.moveTo(mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth, mCenterPointF.y - mFactor.mLineRadiusWidth);

                mLinePath.lineTo(mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth,
                        mCenterPointF.y - mFactor.mLineRadiusWidth - (mCenterPointF.y - mTextViewRects[0].top));
                break;
            case TagFactor.TYPE_MORE_RIGHT_BOTTOM:
                /**
                 * ------|
                 *       |
                 *       |
                 */
                setTypeMoreRightBottomTextViewRect();

                mLinePath.moveTo(mCenterPointF.x, mCenterPointF.y);
                mLinePath.lineTo(mCenterPointF.x + mFactor.mLineWidth, mCenterPointF.y);

                rectF = new RectF(mCenterPointF.x + mFactor.mLineWidth - mFactor.mLineRadiusWidth, mCenterPointF.y,
                        mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth, mCenterPointF.y + mFactor.mLineRadiusWidth * 2);
                mLinePath.addArc(rectF, 270, 90);

                mLinePath.moveTo(mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth, mCenterPointF.y + mFactor.mLineRadiusWidth);

                mLinePath.lineTo(mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth,
                        mCenterPointF.y + mFactor.mLineRadiusWidth +
                                (mTextViewRects[mTextViewRects.length - 1].bottom - mCenterPointF.y));
                break;
        }
    }

    private void setTypeOneLeftTextViewRect() {
        TextView textView = mTextViews[0];
        textView.setGravity(Gravity.END);
        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

        Rect textViewRect = mTextViewRects[0];

        // TODO: 16/8/30 可以采用这种方式的边界判断  可能会减少部分代码
//        mTextViewRects[0].set(0, 0, measuredWidth, measuredHeight);

        float reviseWidth = measuredWidth - getTextMaxWidthDirectionLeft();

        if (reviseWidth > 0) {
            mCenterPointF.x = (mCenterPointF.x + reviseWidth) >= mFactor.mViewGroupRect.right ?
                    (mFactor.mViewGroupRect.right - mFactor.mOutCircleRadius) : (mCenterPointF.x + reviseWidth);
        }
        float maxWidth = getTextMaxWidthDirectionLeft();
        textViewRect.left = reviseWidth > 0 ? mFactor.mViewGroupRect.left : (int) (maxWidth - measuredWidth);
        textViewRect.right = (int) maxWidth;

        double contentHeight = measuredHeight * (reviseWidth > 0 ? Math.ceil(measuredWidth / maxWidth) : 1);

        textViewRect.bottom = (int) (mCenterPointF.y + contentHeight / 2);

        if (textViewRect.bottom > mFactor.mViewGroupRect.bottom) {
            int reviseHeight = textViewRect.bottom - mFactor.mViewGroupRect.bottom;
            textViewRect.bottom = mFactor.mViewGroupRect.bottom;
            mCenterPointF.y = (mCenterPointF.y - reviseHeight) <= mFactor.mViewGroupRect.top ?
                    (mFactor.mViewGroupRect.top + mFactor.mOutCircleRadius) : (mCenterPointF.y - reviseHeight);
        }
        textViewRect.top = (int) (mCenterPointF.y - contentHeight / 2);

        if (textViewRect.top < mFactor.mViewGroupRect.top) {
            int reviseH = mFactor.mViewGroupRect.top - textViewRect.top;
            mCenterPointF.y = mCenterPointF.y + reviseH;
            textViewRect.top = textViewRect.top + reviseH;
            textViewRect.bottom = textViewRect.bottom + reviseH;
        }
    }

    private void setTypeOneRightTextViewRect() {
        TextView textView = mTextViews[0];
        textView.setGravity(Gravity.START);
        //如果超出屏幕 此时会自动换行
        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

        Rect textViewRect = mTextViewRects[0];

        float reviseWidth = measuredWidth - getTextMaxWidthDirectionRight();

        if (reviseWidth > 0) {
            mCenterPointF.x = (mCenterPointF.x - reviseWidth) <= mFactor.mViewGroupRect.left ?
                    (mFactor.mViewGroupRect.left + mFactor.mOutCircleRadius) : (mCenterPointF.x - reviseWidth);
        }

        textViewRect.left = (int) (mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth + mFactor.mTextLinePadding);
        textViewRect.right = (textViewRect.left + measuredWidth) > mFactor.mViewGroupRect.right ?
                mFactor.mViewGroupRect.right : (textViewRect.left + measuredWidth);

        double contentHeight = measuredHeight * (reviseWidth > 0 ?
                Math.ceil(measuredWidth / getTextMaxWidthDirectionRight()) : 1);

        textViewRect.bottom = (int) (mCenterPointF.y + contentHeight / 2);

        if (textViewRect.bottom > mFactor.mViewGroupRect.bottom) {
            int reviseHeight = textViewRect.bottom - mFactor.mViewGroupRect.bottom;
            textViewRect.bottom = mFactor.mViewGroupRect.bottom;
            mCenterPointF.y = (mCenterPointF.y - reviseHeight) <= mFactor.mViewGroupRect.top ?
                    (mFactor.mViewGroupRect.top + mFactor.mOutCircleRadius) : (mCenterPointF.y - reviseHeight);
        }
        textViewRect.top = (int) (mCenterPointF.y - contentHeight / 2);

        if (textViewRect.top < mFactor.mViewGroupRect.top) {
            int reviseH = mFactor.mViewGroupRect.top - textViewRect.top;
            mCenterPointF.y = mCenterPointF.y + reviseH;
            textViewRect.top = textViewRect.top + reviseH;
            textViewRect.bottom = textViewRect.bottom + reviseH;
        }
    }

    private void setTypeMoreLeftTopTextViewRect() {
        for (int i = mTextViews.length - 1; i >= 0; i--) {
            TextView textView = mTextViews[i];
            textView.setGravity(Gravity.END);
            int measuredWidth = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

            Rect textViewRect = mTextViewRects[i];

            float reviseWidth = measuredWidth - getTextMaxWidthDirectionLeft();

            float oldX = mCenterPointF.x;
            float oldY = mCenterPointF.y;

            if (reviseWidth > 0) {
                mCenterPointF.x = (mCenterPointF.x + reviseWidth) >= mFactor.mViewGroupRect.right ?
                        (mFactor.mViewGroupRect.right - mFactor.mOutCircleRadius) : (mCenterPointF.x + reviseWidth);
            }

            float maxWidth = getTextMaxWidthDirectionLeft();
            textViewRect.left = reviseWidth > 0 ? mFactor.mViewGroupRect.left : (int) (maxWidth - measuredWidth);
            textViewRect.right = (int) maxWidth;

            if (mCenterPointF.x > oldX) {//移动其他的左右边界
                for (int j = i + 1; j < mTextViews.length; j++) {
                    int width = mTextViewRects[j].width();
                    mTextViewRects[j].right = textViewRect.right;
                    mTextViewRects[j].left = textViewRect.right - width;
                }
            }
            int textViewRectBottom = getTextViewRectBottomDirectionTop(i);
            textViewRect.top = (int) ((mCenterPointF.y - textViewRectBottom) -
                    measuredHeight * (reviseWidth > 0 ? Math.ceil(measuredWidth / maxWidth) : 1));

            if (textViewRect.top < mFactor.mViewGroupRect.top) {
                int reviseHeight = Math.abs(mFactor.mViewGroupRect.top - textViewRect.top);
                textViewRect.top = mFactor.mViewGroupRect.top;
                mCenterPointF.y = (mCenterPointF.y + reviseHeight) >= mFactor.mViewGroupRect.bottom ?
                        (mFactor.mViewGroupRect.bottom - mFactor.mOutCircleRadius) : (mCenterPointF.y + reviseHeight);
            }
            textViewRect.bottom = (int) (mCenterPointF.y - textViewRectBottom);

            if (mCenterPointF.y > oldY) {
                float reviseHeight = mCenterPointF.y - oldY;
                for (int y = i + 1; y < mTextViews.length; y++) {
                    mTextViewRects[y].top = (int) (mTextViewRects[y].top + reviseHeight);
                    mTextViewRects[y].bottom = (int) (mTextViewRects[y].bottom + reviseHeight);
                }
            }
        }
    }

    private void setTypeMoreLeftBottomTextViewRect() {
        for (int i = 0; i < mTextViews.length; i++) {
            TextView textView = mTextViews[i];
            textView.setGravity(Gravity.END);
            int measuredWidth = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

            Rect textViewRect = mTextViewRects[i];

            float reviseWidth = measuredWidth - getTextMaxWidthDirectionLeft();

            float oldX = mCenterPointF.x;
            float oldY = mCenterPointF.y;

            if (reviseWidth > 0) {
                mCenterPointF.x = (mCenterPointF.x + reviseWidth) >= mFactor.mViewGroupRect.right ?
                        (mFactor.mViewGroupRect.right - mFactor.mOutCircleRadius) : (mCenterPointF.x + reviseWidth);
            }

            float maxWidth = getTextMaxWidthDirectionLeft();
            textViewRect.left = reviseWidth > 0 ? mFactor.mViewGroupRect.left : (int) (maxWidth - measuredWidth);
            textViewRect.right = (int) maxWidth;

            if (mCenterPointF.x > oldX) {//移动其他的左右边界
                for (int j = i - 1; j >= 0; j--) {
                    int width = mTextViewRects[j].width();
                    mTextViewRects[j].right = textViewRect.right;
                    mTextViewRects[j].left = textViewRect.right - width;
                }
            }

            int textViewRectTop = getTextViewRectTopDirectionBottom(i);
            textViewRect.bottom = (int) ((mCenterPointF.y + textViewRectTop) +
                    measuredHeight * (reviseWidth > 0 ? Math.ceil(measuredWidth / maxWidth) : 1));

            if (textViewRect.bottom > mFactor.mViewGroupRect.bottom) {
                int reviseHeight = textViewRect.bottom - mFactor.mViewGroupRect.bottom;
                textViewRect.bottom = mFactor.mViewGroupRect.bottom;
                mCenterPointF.y = (mCenterPointF.y - reviseHeight) <= mFactor.mViewGroupRect.top ?
                        (mFactor.mViewGroupRect.top + mFactor.mOutCircleRadius) : (mCenterPointF.y - reviseHeight);
            }
            textViewRect.top = (int) (mCenterPointF.y + textViewRectTop);

            if (mCenterPointF.y < oldY) {
                float reviseHeight = oldY - mCenterPointF.y;
                for (int y = i - 1; y >= 0; y--) {
                    mTextViewRects[y].top = (int) (mTextViewRects[y].top - reviseHeight);
                    mTextViewRects[y].bottom = (int) (mTextViewRects[y].bottom - reviseHeight);
                }
            }
        }
    }

    private void setTypeMoreRightTopTextViewRect() {
        for (int i = mTextViews.length - 1; i >= 0; i--) {
            TextView textView = mTextViews[i];
            textView.setGravity(Gravity.START);
            int measuredWidth = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

            Rect textViewRect = mTextViewRects[i];

            float reviseWidth = measuredWidth - getTextMaxWidthDirectionRight();

            float oldX = mCenterPointF.x;
            float oldY = mCenterPointF.y;

            if (reviseWidth > 0) {
                mCenterPointF.x = (mCenterPointF.x - reviseWidth) <= mFactor.mViewGroupRect.left ?
                        (mFactor.mViewGroupRect.left + mFactor.mOutCircleRadius) : (mCenterPointF.x - reviseWidth);
            }

            textViewRect.left = (int) (mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth + mFactor.mTextLinePadding);
            textViewRect.right = (textViewRect.left + measuredWidth) > mFactor.mViewGroupRect.right ?
                    mFactor.mViewGroupRect.right : (textViewRect.left + measuredWidth);

            if (mCenterPointF.x < oldX) {//移动其他的左右边界
                for (int j = i + 1; j < mTextViews.length; j++) {
                    int width = mTextViewRects[j].width();
                    mTextViewRects[j].left = textViewRect.left;
                    mTextViewRects[j].right = textViewRect.left + width;
                }
            }

            int textViewRectBottom = getTextViewRectBottomDirectionTop(i);
            textViewRect.top = (int) ((mCenterPointF.y - textViewRectBottom) -
                    measuredHeight * (reviseWidth > 0 ? Math.ceil(measuredWidth / getTextMaxWidthDirectionRight()) : 1));

            if (textViewRect.top < mFactor.mViewGroupRect.top) {
                int reviseHeight = Math.abs(mFactor.mViewGroupRect.top - textViewRect.top);
                textViewRect.top = mFactor.mViewGroupRect.top;
                mCenterPointF.y = (mCenterPointF.y + reviseHeight) >= mFactor.mViewGroupRect.bottom ?
                        (mFactor.mViewGroupRect.bottom - mFactor.mOutCircleRadius) : (mCenterPointF.y + reviseHeight);
            }
            textViewRect.bottom = (int) (mCenterPointF.y - textViewRectBottom);

            if (mCenterPointF.y > oldY) {
                float reviseHeight = mCenterPointF.y - oldY;
                for (int y = i + 1; y < mTextViews.length; y++) {
                    mTextViewRects[y].top = (int) (mTextViewRects[y].top + reviseHeight);
                    mTextViewRects[y].bottom = (int) (mTextViewRects[y].bottom + reviseHeight);
                }
            }
        }
    }

    private void setTypeMoreRightBottomTextViewRect() {
        for (int i = 0; i < mTextViews.length; i++) {
            TextView textView = mTextViews[i];
            textView.setGravity(Gravity.START);
            int measuredWidth = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight() / textView.getLineCount();

            Rect textViewRect = mTextViewRects[i];

            float reviseWidth = measuredWidth - getTextMaxWidthDirectionRight();

            float oldX = mCenterPointF.x;
            float oldY = mCenterPointF.y;

            if (reviseWidth > 0) {
                mCenterPointF.x = (mCenterPointF.x - reviseWidth) <= mFactor.mViewGroupRect.left ?
                        (mFactor.mViewGroupRect.left + mFactor.mOutCircleRadius) : (mCenterPointF.x - reviseWidth);
            }

            textViewRect.left = (int) (mCenterPointF.x + mFactor.mLineWidth + mFactor.mLineRadiusWidth + mFactor.mTextLinePadding);
            textViewRect.right = (textViewRect.left + measuredWidth) > mFactor.mViewGroupRect.right ?
                    mFactor.mViewGroupRect.right : (textViewRect.left + measuredWidth);

            if (mCenterPointF.x < oldX) {//移动其他的左右边界
                for (int j = i + 1; j < mTextViews.length; j++) {
                    int width = mTextViewRects[j].width();
                    mTextViewRects[j].left = textViewRect.left;
                    mTextViewRects[j].right = textViewRect.left + width;
                }
            }

            int textViewRectTop = getTextViewRectTopDirectionBottom(i);
            textViewRect.bottom = (int) ((mCenterPointF.y + textViewRectTop) +
                    measuredHeight * (reviseWidth > 0 ? Math.ceil(measuredWidth / getTextMaxWidthDirectionRight()) : 1));

            if (textViewRect.bottom > mFactor.mViewGroupRect.bottom) {
                int reviseHeight = textViewRect.bottom - mFactor.mViewGroupRect.bottom;
                textViewRect.bottom = mFactor.mViewGroupRect.bottom;
                mCenterPointF.y = (mCenterPointF.y - reviseHeight) <= mFactor.mViewGroupRect.top ?
                        (mFactor.mViewGroupRect.top + mFactor.mOutCircleRadius) : (mCenterPointF.y - reviseHeight);
            }
            textViewRect.top = (int) (mCenterPointF.y + textViewRectTop);

            if (mCenterPointF.y < oldY) {
                float reviseHeight = oldY - mCenterPointF.y;
                for (int y = i - 1; y >= 0; y--) {
                    mTextViewRects[y].top = (int) (mTextViewRects[y].top - reviseHeight);
                    mTextViewRects[y].bottom = (int) (mTextViewRects[y].bottom - reviseHeight);
                }
            }
        }
    }

    private float getTextMaxWidthDirectionRight() {
        return mFactor.mViewGroupRect.right - mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth - mFactor.mTextLinePadding;
    }

    private float getTextMaxWidthDirectionLeft() {
        return mCenterPointF.x - mFactor.mLineWidth - mFactor.mLineRadiusWidth - mFactor.mTextLinePadding;
    }

    private int getTextViewRectBottomDirectionTop(int index) {
        int height = mFactor.mLineRadiusWidth;
        if (index < mTextViewRects.length) {
            for (int i = mTextViewRects.length - 1; i > index; i--) {
                height = height + mTextViewRects[i].height();
            }
            height = height + mFactor.mTextLinePadding * (mTextViewRects.length - index);
        }
        return height;
    }

    private int getTextViewRectTopDirectionBottom(int index) {
        int height = mFactor.mLineRadiusWidth;
        if (index < mTextViewRects.length) {
            for (int i = 0; i < index; i++) {
                height = height + mTextViewRects[i].height();
            }
            height = height + mFactor.mTextLinePadding * index;
        }
        return height;
    }

    private boolean isEmpty() {
        return mTextViews == null || mTextViews.length < 1;
    }

    public void onLayout() {
        if (isEmpty()) return;
        mTextRect.setEmpty();
        mCircleRect.setEmpty();
        mTagRect.setEmpty();
        for (int i = 0; i < mTextViews.length; i++) {
            Rect textViewRect = mTextViewRects[i];
            Log.d(TAG, "onLayout: mTextViewRect[" + i + "] = " + textViewRect.toString());
            mTextViews[i].measure(View.MeasureSpec.makeMeasureSpec(textViewRect.width(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(textViewRect.height(), View.MeasureSpec.EXACTLY));
            mTextViews[i].layout(textViewRect.left, textViewRect.top,
                    textViewRect.right, textViewRect.bottom);

            if (mTextRect.isEmpty()) {
                mTextRect.set(textViewRect);
            } else {
                mTextRect.left = textViewRect.left < mTextRect.left ? textViewRect.left : mTextRect.left;
                mTextRect.top = textViewRect.top < mTextRect.top ? textViewRect.top : mTextRect.top;
                mTextRect.right = textViewRect.right > mTextRect.right ? textViewRect.right : mTextRect.right;
                mTextRect.bottom = textViewRect.bottom > mTextRect.bottom ? textViewRect.bottom : mTextRect.bottom;
            }
        }

        int add = mFactor.mOutCircleRadius * 2;
        mCircleRect.left = (int) (mCenterPointF.x - add);
        mCircleRect.top = (int) (mCenterPointF.y - add);
        mCircleRect.right = (int) (mCenterPointF.x + add);
        mCircleRect.bottom = (int) (mCenterPointF.y + add);

        mTagRect.left = mCircleRect.left < mTextRect.left ? mCircleRect.left : mTextRect.left;
        mTagRect.top = mCircleRect.top < mTextRect.top ? mCircleRect.top : mTextRect.top;
        mTagRect.right = mCircleRect.right > mTextRect.right ? mCircleRect.right : mTextRect.right;
        mTagRect.bottom = mCircleRect.bottom > mTextRect.bottom ? mCircleRect.bottom : mTextRect.bottom;

        Log.d(TAG, "onLayout: mCircleRect = " + mCircleRect.toString());
        Log.d(TAG, "onLayout: mTextRect = " + mTextRect.toString());
        Log.d(TAG, "onLayout: mTagRect = " + mTagRect.toString());
    }

    public void onDraw(Canvas canvas) {
        if (isEmpty()) return;
        //绘制外圆
        mFactor.mCirclePaint.setColor(mFactor.mOutCircleColor);
        canvas.drawCircle(mCenterPointF.x, mCenterPointF.y, mFactor.mOutCircleRadius, mFactor.mCirclePaint);

        //绘制线条
        canvas.drawPath(mLinePath, mFactor.mLinePaint);

        //绘制内圆
        mFactor.mCirclePaint.setColor(mFactor.mCircleColor);
        canvas.drawCircle(mCenterPointF.x, mCenterPointF.y, mFactor.mCircleRadius, mFactor.mCirclePaint);
    }


    public void addTags(ViewGroup viewGroup, PointF centerPointF, List<String> tagContents) {
        addTags(viewGroup, centerPointF, tagContents, TagFactor.TYPE_NONE);
    }

    public void addTags(ViewGroup viewGroup, PointF centerPointF, List<String> tagContents, int type) {
        if (centerPointF == null || tagContents == null || tagContents.isEmpty()) return;
        mCenterPointF.set(centerPointF);
        mCurrentType = type;

        mTextViews = new TextView[tagContents.size()];
        mTextViewRects = new Rect[tagContents.size()];

        for (int i = 0; i < tagContents.size(); i++) {
            mTextViewRects[i] = new Rect();

            mTextViews[i] = new TextView(viewGroup.getContext());
            mTextViews[i].setTextSize(mFactor.mTextSize);
            mTextViews[i].setTextColor(mFactor.mTextColor);
            mTextViews[i].setIncludeFontPadding(false);
            mTextViews[i].setLineSpacing(mFactor.mTextLineSpacingExtra, 1.0f);
            mTextViews[i].setShadowLayer(1f, 0, 0, mFactor.mTextShadowColor);

            mTextViews[i].setText(tagContents.get(i));
            viewGroup.addView(mTextViews[i]);
        }
    }

    public void updateTag(ViewGroup viewGroup, List<String> tagContents) {
        removeTagView(viewGroup);
        mTextViews = null;
        mTextViewRects = null;
        addTags(viewGroup, mCenterPointF, tagContents);
    }

    public void removeTagView(ViewGroup viewGroup) {
        for (TextView textView : mTextViews) {
            viewGroup.removeView(textView);
        }
    }

    public boolean isClickInTag(int x, int y) {
        return mTextRect.contains(x, y) || mCircleRect.contains(x, y);
    }

    public boolean isClickInCircle(int x, int y) {
        return mCircleRect.contains(x, y);
    }

    public boolean isClickInText(int x, int y) {
        return mTextRect.contains(x, y);
    }

    // FIXME: 16/9/1 圆点移到边界会有闪动
    public void moveTo(ViewGroup viewGroup, float distanceX, float distanceY) {
        if (distanceX == 0 && distanceY == 0) return;
        float targetX = mCenterPointF.x - distanceX;
        float targetY = mCenterPointF.y - distanceY;
        if (targetX + mFactor.mOutCircleRadius <= mFactor.mViewGroupRect.right &&
                targetX - mFactor.mOutCircleRadius >= mFactor.mViewGroupRect.left) {
            Log.d(TAG, "moveTo: X " + mCenterPointF.x + " -> " + targetX + " ");
            mCenterPointF.x = targetX;
        }
        if (targetY + mFactor.mOutCircleRadius <= mFactor.mViewGroupRect.bottom &&
                targetY - mFactor.mOutCircleRadius >= mFactor.mViewGroupRect.top) {
            Log.d(TAG, "moveTo: Y " + mCenterPointF.y + " -> " + targetY + " ");
            mCenterPointF.y = targetY;
        }
        viewGroup.requestLayout();
        viewGroup.postInvalidate();
    }

    public void changeType(ViewGroup viewGroup, int type) {
        if (type == mCurrentType) return;
        mCurrentType = type;
        viewGroup.requestLayout();
        viewGroup.postInvalidate();
    }

    /**
     * 获取可以变换的类型 顺时针变换
     *
     * @param forceChange 强制转换 使用这个会使得圆点坐标变换
     */
    public int getCanChangeType(boolean forceChange) {
        int type = mCurrentType;

        switch (mCurrentType) {
            case TagFactor.TYPE_ONE_LEFT:
                if ((mFactor.mViewGroupRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTextRect.left) || forceChange) {
                    type = TagFactor.TYPE_ONE_RIGHT;
                }
                break;
            case TagFactor.TYPE_ONE_RIGHT:
                if ((mCircleRect.left - mFactor.mViewGroupRect.left) >=
                        (mTextRect.right - mCircleRect.right) || forceChange) {
                    type = TagFactor.TYPE_ONE_LEFT;
                }
                break;
            case TagFactor.TYPE_MORE_LEFT_TOP:
                if ((mFactor.mViewGroupRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTextRect.left) || forceChange) {
                    type = TagFactor.TYPE_MORE_RIGHT_TOP;
                } else if ((mFactor.mViewGroupRect.bottom - mTextRect.bottom) >=
                        mTextRect.height()) {
                    type = TagFactor.TYPE_MORE_LEFT_BOTTOM;
                }
                break;
            case TagFactor.TYPE_MORE_LEFT_BOTTOM:
                if ((mTextRect.top - mFactor.mViewGroupRect.top) >=
                        mTextRect.height() || forceChange) {
                    type = TagFactor.TYPE_MORE_LEFT_TOP;
                } else if ((mFactor.mViewGroupRect.right - mCircleRect.right) >=
                        (mCircleRect.left - mTextRect.left)) {
                    type = TagFactor.TYPE_MORE_RIGHT_BOTTOM;
                }
                break;
            case TagFactor.TYPE_MORE_RIGHT_TOP:
                if ((mFactor.mViewGroupRect.bottom - mTextRect.bottom) >=
                        mTextRect.height() || forceChange) {
                    type = TagFactor.TYPE_MORE_RIGHT_BOTTOM;
                } else if ((mCircleRect.left - mFactor.mViewGroupRect.left) >=
                        (mTextRect.right - mCircleRect.right)) {
                    type = TagFactor.TYPE_MORE_LEFT_TOP;
                }
                break;
            case TagFactor.TYPE_MORE_RIGHT_BOTTOM:
                if ((mCircleRect.left - mFactor.mViewGroupRect.left) >=
                        (mTextRect.right - mCircleRect.right) || forceChange) {
                    type = TagFactor.TYPE_MORE_LEFT_BOTTOM;
                } else if ((mTextRect.top - mFactor.mViewGroupRect.top) >=
                        mTextRect.height()) {
                    type = TagFactor.TYPE_MORE_RIGHT_TOP;
                }
                break;
        }
        return type;
    }
}
