package com.wpy.imagetagview.tag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.wpy.imagetagview.util.TagLocalDisplay;

/**
 * 标签的相关参数等
 * Created by feiyang on 16/8/31.
 */
public class TagFactor {
    public static final int TYPE_NONE = 0;//如果设为这个类型  会自己检测并设定类型

    //单条标签 左右两个方向
    public static final int TYPE_ONE_LEFT = 1;
    public static final int TYPE_ONE_RIGHT = 2;

    //多条标签 上下左右四个方向
    public static final int TYPE_MORE_LEFT_TOP = 13;
    public static final int TYPE_MORE_LEFT_BOTTOM = 14;
    public static final int TYPE_MORE_RIGHT_TOP = 23;
    public static final int TYPE_MORE_RIGHT_BOTTOM = 24;

    public Rect mViewGroupRect;

    public Paint mCirclePaint;
    public Paint mLinePaint;

    public int mCircleRadius = 4;//圆形的半径
    public int mCircleColor = Color.WHITE;
    public int mOutCircleColor = 0x80000000;
    public int mOutCircleRadius = 8;//外部圆形半径

    public int mLineColor = Color.WHITE;
    public int mLineWidth = 14;//横线的长度
    public int mLineStrokeWidth = 1;//线条的大小
    public int mLineRadiusWidth = 4;//横竖两条线中间弧度的半径
    public int mLineShadowColor = 0xB3000000;

    public float mTextSize = 12f;
    public int mTextColor = Color.WHITE;
    public float mTextLineSpacingExtra = 4f;//TextView 换行间距
    public int mTextShadowColor = 0xB3000000;

    public int mTextLinePadding = 6;//文字和标签图形之间的间距
    public int mTextLineSpacing = 6;//两行文字之间的间距

    private Context mContext;

    public TagFactor(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        initDimension();
        mViewGroupRect = new Rect();

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mLineStrokeWidth);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setShadowLayer(1f, 0, 0, mLineShadowColor);
    }

    private void initDimension() {
        mCircleRadius = TagLocalDisplay.dp2px(mContext, mCircleRadius);
        mOutCircleRadius = TagLocalDisplay.dp2px(mContext, mOutCircleRadius);
        mLineWidth = TagLocalDisplay.dp2px(mContext, mLineWidth);
        mLineStrokeWidth = TagLocalDisplay.dp2px(mContext, mLineStrokeWidth);
        mLineRadiusWidth = TagLocalDisplay.dp2px(mContext, mLineRadiusWidth);
        mTextLinePadding = TagLocalDisplay.dp2px(mContext, mTextLinePadding);
        mTextLineSpacing = TagLocalDisplay.dp2px(mContext, mTextLineSpacing);
    }

    public void checkType(int type) {
        if (TagFactor.TYPE_NONE != type &&
                TagFactor.TYPE_ONE_LEFT != type && TagFactor.TYPE_ONE_RIGHT != type &&
                TagFactor.TYPE_MORE_LEFT_TOP != type && TagFactor.TYPE_MORE_LEFT_BOTTOM != type &&
                TagFactor.TYPE_MORE_RIGHT_TOP != type && TagFactor.TYPE_MORE_RIGHT_BOTTOM != type) {
            throw new IllegalArgumentException("This type is not supported.");
        }
    }
}
