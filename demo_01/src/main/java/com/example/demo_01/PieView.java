package com.example.demo_01;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import widget.PieData;

/**
 * @author pikachu
 * @time 2016/11/4 11:10
 * @desc 自定义View Canvas画饼状图
 */

public class PieView extends View {

    private Paint mPaint;
    private RectF mRectF;

    public PieView(Context context) {
        this(context, null);
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View.MeasureSpec.getMode(widthMeasureSpec);
        View.MeasureSpec.getMode(heightMeasureSpec);

        View.MeasureSpec.getSize(widthMeasureSpec);
        View.MeasureSpec.getSize(heightMeasureSpec);

    }

    /**
     * 控件最终的宽 高
     */
    int pieWidth;
    int pieHeight;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.pieHeight = h;
        this.pieWidth = w;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mList == null || mList.size() == 0) {
            return;
        }
        //将画布的坐标原点移至中心位置
        canvas.translate(pieWidth/2,pieHeight/2);

        //区域半径
        float r = (float) (Math.min(pieWidth/2,pieHeight/2)*0.8);


        //定义饼的绘制区域
        mRectF = new RectF(-r, -r, r, r);

        float startAngle = 0;
        for (int i = 0; i < mList.size(); i++) {
            PieData pieData = mList.get(i);
            mPaint.setColor(arr[i]);
            canvas.drawArc(mRectF, startAngle, pieData.sweepAngle, true, mPaint);
            startAngle = startAngle + pieData.sweepAngle;
        }
    }

    int[] arr = {Color.BLACK,Color.RED,Color.GREEN};

    ArrayList<PieData> mList;

    public void setData(ArrayList<PieData> list) {
        this.mList = list;
        if (list == null || list.size() == 0) {
            return;
        }

        float totalNum = 0;
        for (int i = 0; i < list.size(); i++) {
            totalNum += mList.get(i).total;
        }

        for (int i = 0; i < list.size(); i++) {
            PieData pieData = mList.get(i);
            pieData.sweepAngle = pieData.total / totalNum * 360;
        }

        invalidate();
    }


}
