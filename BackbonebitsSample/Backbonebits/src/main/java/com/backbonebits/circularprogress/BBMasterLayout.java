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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.backbonebits.service.BBChatHeadService;

public class BBMasterLayout extends FrameLayout implements OnClickListener {

    public BBCusImage cusview;
    public int pix = 0;
    public RectF rect;

    private ImageView buttonimage, fillcircle, full_circle_image, arc_image;

    private Path stop, tick, play, download_triangle, download_rectangle;

    private Bitmap third_icon_bmp, second_icon_bmp, first_icon_bmp;

    private Paint stroke_color, fill_color, icon_color, final_icon_color;

    private AnimationSet in, out;

    private RotateAnimation arcRotation;

    private ScaleAnimation new_scale_in, scale_in, scale_out;

    private AlphaAnimation fade_in, fade_out;

    public int flg_frmwrk_mode = 0;
    boolean first_click = false;
    private Context cntx;

    public BBMasterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        cntx = context;
        setOnClickListener(this);
        try {
            initialise();
            setpaint();
            setAnimation();
            displayMetrics();
            iconCreate();
            init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BBMasterLayout(Context context) {
        super(context);
        cntx = context;
        try {
            setOnClickListener(this);
            setBackgroundColor(Color.CYAN);
            initialise();
            setpaint();
            setAnimation();
            displayMetrics();
            iconCreate();
            init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initialise() {
        try {
            cusview = new BBCusImage(getContext(), this);
            buttonimage = new ImageView(getContext());
            full_circle_image = new ImageView(getContext());
            arc_image = new ImageView(getContext());

            fillcircle = new ImageView(getContext());
            cusview.setClickable(false);
            buttonimage.setClickable(false);
            full_circle_image.setClickable(false);
            arc_image.setClickable(false);

            cusview.setClickable(false);
            setClickable(true);
            fillcircle.setClickable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setpaint() {
        try {
            stroke_color = new Paint(Paint.ANTI_ALIAS_FLAG);
            stroke_color.setAntiAlias(true);
            stroke_color.setColor(Color.rgb(154, 154, 154)); // Edit this to change
            // the circle color
            stroke_color.setStrokeWidth(3);
            stroke_color.setStyle(Paint.Style.STROKE);

            icon_color = new Paint(Paint.ANTI_ALIAS_FLAG);
            icon_color.setColor(Color.rgb(154, 154, 154));
            icon_color.setStyle(Paint.Style.FILL_AND_STROKE); // Edit this to change
            // the icon color
            icon_color.setAntiAlias(true);

            final_icon_color = new Paint(Paint.ANTI_ALIAS_FLAG);
            final_icon_color.setColor(Color.WHITE); // Edit this to change the final
            // icon color
            final_icon_color.setStrokeWidth(12);
            final_icon_color.setStyle(Paint.Style.STROKE);
            final_icon_color.setAntiAlias(true);

            fill_color = new Paint(Paint.ANTI_ALIAS_FLAG);
            fill_color.setColor(Color.rgb(154, 154, 154)); // Edit this to change the
            // circle fill color
            fill_color.setStyle(Paint.Style.FILL_AND_STROKE);
            fill_color.setAntiAlias(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setAnimation() {
        try {
            arcRotation = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            arcRotation.setDuration(1000);

            in = new AnimationSet(true);
            out = new AnimationSet(true);

            out.setInterpolator(new AccelerateDecelerateInterpolator());
            in.setInterpolator(new AccelerateDecelerateInterpolator());

            scale_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            scale_out = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            new_scale_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);

            new_scale_in.setDuration(200);

            fade_in = new AlphaAnimation(0.0f, 1.0f);
            fade_out = new AlphaAnimation(1.0f, 0.0f);

            scale_in.setDuration(150);
            scale_out.setDuration(150);
            fade_in.setDuration(150);
            fade_out.setDuration(150);

            in.addAnimation(scale_in);
            in.addAnimation(fade_in);
            out.addAnimation(fade_out);
            out.addAnimation(scale_out);

            arcRotation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    // TODO Auto-generated method stub

                    first_click = false;
                    buttonimage.startAnimation(out);

                }
            });

            out.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub

                    buttonimage.setVisibility(View.GONE);
                    buttonimage.setImageBitmap(second_icon_bmp);
                    buttonimage.setVisibility(View.VISIBLE);
                    buttonimage.startAnimation(in);
                    arc_image.setVisibility(View.GONE);
                    full_circle_image.setVisibility(View.VISIBLE);
                    cusview.setVisibility(View.VISIBLE);

                    flg_frmwrk_mode = 2;

                    System.out.println("flg_frmwrk_mode" + flg_frmwrk_mode);


                }
            });

            new_scale_in.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub


                    cusview.setVisibility(View.GONE);
                    buttonimage.setVisibility(View.VISIBLE);
                    buttonimage.setImageBitmap(third_icon_bmp);
                    flg_frmwrk_mode = 3;
                    buttonimage.startAnimation(in);


                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayMetrics() {

        // Responsible for calculating the size of views and canvas based upon
        // screen resolution.
        try {
            DisplayMetrics metrics = getContext().getResources()
                    .getDisplayMetrics();

            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            float scarea = width * height;
            pix = (int) Math.sqrt(scarea * 0.0217);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void iconCreate() {

        // Creating icons using path
        // Create your own icons or feel free to use these
        try {
            play = new Path();
            play.moveTo(pix * 40 / 100, pix * 36 / 100);
            play.lineTo(pix * 40 / 100, pix * 63 / 100);
            play.lineTo(pix * 69 / 100, pix * 50 / 100);
            play.close();

            stop = new Path();
            stop.moveTo(pix * 38 / 100, pix * 38 / 100);
            stop.lineTo(pix * 62 / 100, pix * 38 / 100);
            stop.lineTo(pix * 62 / 100, pix * 62 / 100);
            stop.lineTo(pix * 38 / 100, pix * 62 / 100);
            stop.close();

            download_triangle = new Path();
            download_triangle.moveTo(pix * 375 / 1000, (pix / 2)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_triangle.lineTo(pix / 2, (pix * 625 / 1000)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_triangle.lineTo(pix * 625 / 1000, (pix / 2)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_triangle.close();

            download_rectangle = new Path();
            download_rectangle.moveTo(pix * 4375 / 10000, (pix / 2)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_rectangle.lineTo(pix * 5625 / 10000, (pix / 2)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_rectangle.lineTo(pix * 5625 / 10000, (pix * 375 / 1000)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_rectangle.lineTo(pix * 4375 / 10000, (pix * 375 / 1000)
                    + (pix * 625 / 10000) - (pix * 3 / 100));
            download_rectangle.close();

            tick = new Path();
            tick.moveTo(pix * 30 / 100, pix * 50 / 100);
            tick.lineTo(pix * 45 / 100, pix * 625 / 1000);
            tick.lineTo(pix * 65 / 100, pix * 350 / 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void init() {

        // Defining and drawing bitmaps and assigning views to the layout
        try {
            LayoutParams lp = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            lp.setMargins(10, 10, 10, 10);

            fillcircle.setVisibility(View.GONE);

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap full_circle_bmp = Bitmap.createBitmap(pix, pix, conf);
            Bitmap arc_bmp = Bitmap.createBitmap(pix, pix, conf);
            Bitmap fill_circle_bmp = Bitmap.createBitmap(pix, pix, conf);

            first_icon_bmp = Bitmap.createBitmap(pix, pix, conf); // Bitmap to draw
            // first icon(
            // Default -
            // Play )

            second_icon_bmp = Bitmap.createBitmap(pix, pix, conf); // Bitmap to draw
            // second icon(
            // Default -
            // Stop )

            third_icon_bmp = Bitmap.createBitmap(pix, pix, conf); // Bitmap to draw
            // third icon(
            // Default -
            // Tick )

            Canvas first_icon_canvas = new Canvas(first_icon_bmp);
            Canvas second_icon_canvas = new Canvas(second_icon_bmp);
            Canvas third_icon_canvas = new Canvas(third_icon_bmp);
            Canvas fill_circle_canvas = new Canvas(fill_circle_bmp);
            Canvas full_circle_canvas = new Canvas(full_circle_bmp);
            Canvas arc_canvas = new Canvas(arc_bmp);

            float startx = (float) (pix * 0.05);
            float endx = (float) (pix * 0.95);
            System.out.println("full circle " + full_circle_canvas.getWidth()
                    + full_circle_canvas.getHeight());
            float starty = (float) (pix * 0.05);
            float endy = (float) (pix * 0.95);
            rect = new RectF(startx, starty, endx, endy);

            first_icon_canvas.drawPath(play, fill_color);   // Draw second icon on canvas( Default - Stop ).
            // *****Set your second icon here****


            second_icon_canvas.drawPath(stop, icon_color);  // Draw second icon on canvas( Default - Stop ).
            // *****Set your second icon here****

            third_icon_canvas.drawPath(tick, final_icon_color); // Draw second icon on canvas( Default - Stop ).
            // *****Set your second icon here****


            full_circle_canvas.drawArc(rect, 0, 360, false, stroke_color);
            fill_circle_canvas.drawArc(rect, 0, 360, false, fill_color);
            arc_canvas.drawArc(rect, -80, 340, false, stroke_color);

            buttonimage.setImageBitmap(first_icon_bmp);
            flg_frmwrk_mode = 1;
            fillcircle.setImageBitmap(fill_circle_bmp);
            full_circle_image.setImageBitmap(full_circle_bmp);
            arc_image.setImageBitmap(arc_bmp);

            cusview.setVisibility(View.GONE);

            addView(full_circle_image, lp);
            addView(arc_image, lp);
            addView(fillcircle, lp);
            addView(buttonimage, lp);
            addView(cusview, lp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void animation() {

        // Starting view animation and setting flag values
        try {
            if (first_click == false) {
                if (flg_frmwrk_mode == 1) {
                    first_click = true;
                    full_circle_image.setVisibility(View.GONE);
                    arc_image.setVisibility(View.VISIBLE);
                    arc_image.startAnimation(arcRotation);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void finalAnimation() {

        // Responsible for final fill up animation
        Intent local = new Intent();
        local.setAction("com.backbonebits.action.stoprecording");

        cntx.sendBroadcast(local);
        buttonimage.setVisibility(View.GONE);
        cusview.reset();
        cusview.setVisibility(View.GONE);
        arc_image.setVisibility(View.GONE);
        full_circle_image.setVisibility(View.GONE);
        fillcircle.setVisibility(View.GONE);
        full_circle_image.setVisibility(View.GONE);
        if (BBChatHeadService.mBBFloatingViewManager != null) {
            BBChatHeadService.mBBFloatingViewManager.removeAllViewToWindow();
            BBChatHeadService.mBBFloatingViewManager = null;
        }
        flg_frmwrk_mode = 2;


    }

    public void reset() {

        // Responsible for resetting the state of view when Stop is clicked

        cusview.reset();
        cusview.setVisibility(View.GONE);
        buttonimage.setImageBitmap(first_icon_bmp);
        flg_frmwrk_mode = 1;

    }

    @Override
    public void onClick(View v) {
        animation();
        Intent local = new Intent();
        local.setAction("com.backbonebits.action.stoprecording");
        cntx.sendBroadcast(local);
    }

}