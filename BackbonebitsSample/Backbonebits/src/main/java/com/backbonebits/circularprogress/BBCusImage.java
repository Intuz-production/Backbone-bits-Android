//  The MIT License (MIT)

//  Copyright (c) 2018 Intuz Solutions Pvt Ltd.

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
//  (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
//  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.backbonebits.circularprogress;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

public class BBCusImage extends View {

    private Paint myPaint;
    private Paint myFramePaint;
    public TextView value;
    private float startAngle;
    public float temp;
    float sweepAngle;
    private int flag = 0;
    RectF rect;
    private BBMasterLayout m;
    int pix = 0;

    public BBCusImage(Context context, AttributeSet attrs, BBMasterLayout m) {
        super(context, attrs);
        this.m = m;
        init();
    }

    public BBCusImage(Context context, BBMasterLayout m) {
        super(context);
        this.m = m;
        init();
    }

    private void init() {
        try {
            myPaint = new Paint();
            DisplayMetrics metrics = getContext().getResources()
                    .getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            float scarea = width * height;
            pix = (int) Math.sqrt(scarea * 0.0217);

            myPaint.setAntiAlias(true);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setColor(Color.rgb(0, 161, 234));  //Edit this to change progress arc color.
            myPaint.setStrokeWidth(7);

            myFramePaint = new Paint();
            myFramePaint.setAntiAlias(true);
            myFramePaint.setColor(Color.TRANSPARENT);

            float startx = (float) (pix * 0.05);
            float endx = (float) (pix * 0.95);
            float starty = (float) (pix * 0.05);
            float endy = (float) (pix * 0.95);
            rect = new RectF(startx, starty, endx, endy);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setupprogress(int progress) {
        try {
            sweepAngle = (float) (progress * 3.6);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void reset() {

        //Resetting progress arc

        sweepAngle = 0;
        startAngle = -90;
        flag = 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int desiredWidth = pix;
            int desiredHeight = pix;
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width;
            int height;


            if (widthMode == MeasureSpec.EXACTLY) {

                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {

                width = Math.min(desiredWidth, widthSize);
            } else {

                width = desiredWidth;
            }


            if (heightMode == MeasureSpec.EXACTLY) {

                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {

                height = Math.min(desiredHeight, heightSize);
            } else {

                height = desiredHeight;
            }


            setMeasuredDimension(width, height);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            canvas.drawArc(rect, startAngle, sweepAngle, false, myPaint);
            startAngle = -90;

            if (sweepAngle < 360 && flag == 0) {

                invalidate();

            } else if (flag == 1) {

                sweepAngle = 0;
                startAngle = -90;
                flag = 0;
                invalidate();
            } else {

                sweepAngle = 0;
                startAngle = -90;
                m.finalAnimation();

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}