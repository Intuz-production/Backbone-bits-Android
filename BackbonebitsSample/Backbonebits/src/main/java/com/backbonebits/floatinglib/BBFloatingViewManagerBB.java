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

package com.backbonebits.floatinglib;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.util.ArrayList;

public class BBFloatingViewManagerBB implements BBScreenChangedListener, View.OnTouchListener, BBTrashViewListener {


    public static final int DISPLAY_MODE_SHOW_ALWAYS = 1;


    public static final int DISPLAY_MODE_HIDE_ALWAYS = 2;


    public static final int DISPLAY_MODE_HIDE_FULLSCREEN = 3;


    public static final int MOVE_DIRECTION_DEFAULT = 0;

    public static final int MOVE_DIRECTION_LEFT = 1;

    public static final int MOVE_DIRECTION_RIGHT = 2;

    public static final int MOVE_DIRECTION_NONE = 3;


    private static final long VIBRATE_INTERSECTS_MILLIS = 15;


    public static final float SHAPE_CIRCLE = 1.0f;


    public static final float SHAPE_RECTANGLE = 1.4142f;


    private final Context mContext;


    private final WindowManager mWindowManager;


    private BBFloatingView mTargetBBFloatingView;


    private final BBFullscreenObserverView mBBFullscreenObserverView;


    private final BBTrashView mTrashView;


    private final BBFloatingViewListener mBBFloatingViewListener;


    private final Rect mFloatingViewRect;


    private final Rect mTrashViewRect;


    private final Vibrator mVibrator;


    private boolean mIsMoveAccept;


    private int mDisplayMode;


    private final ArrayList<BBFloatingView> mBBFloatingViewList;

    public BBFloatingViewManagerBB(Context context, BBFloatingViewListener listener) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mBBFloatingViewListener = listener;
        mFloatingViewRect = new Rect();
        mTrashViewRect = new Rect();
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mIsMoveAccept = false;
        mDisplayMode = DISPLAY_MODE_HIDE_FULLSCREEN;

        mBBFloatingViewList = new ArrayList<>();
        mBBFullscreenObserverView = new BBFullscreenObserverView(context, this);
        mTrashView = new BBTrashView(context);
    }


    private boolean isIntersectWithTrash() {
        if (!mTrashView.isTrashEnabled()) {
            return false;
        }
        mTrashView.getWindowDrawingRect(mTrashViewRect);
        mTargetBBFloatingView.getWindowDrawingRect(mFloatingViewRect);
        return Rect.intersects(mTrashViewRect, mFloatingViewRect);
    }


    @Override
    public void onScreenChanged(boolean isFullscreen) {
       try{
        if (mDisplayMode != DISPLAY_MODE_HIDE_FULLSCREEN) {
            return;
        }

        mIsMoveAccept = false;
        final int state = mTargetBBFloatingView.getState();
        if (state == BBFloatingView.STATE_NORMAL) {
            final int size = mBBFloatingViewList.size();
            for (int i = 0; i < size; i++) {
                final BBFloatingView BBFloatingView = mBBFloatingViewList.get(i);
                BBFloatingView.setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
            }
            mTrashView.dismiss();
        }

        else if (state == BBFloatingView.STATE_INTERSECTING) {
            mTargetBBFloatingView.setFinishing();
            mTrashView.dismiss();
        }
       }catch(Exception ex)
       {

       }
    }


    @Override
    public void onTrashAnimationStarted(int animationCode) {
      try{
        if (animationCode == BBTrashView.ANIMATION_CLOSE || animationCode == BBTrashView.ANIMATION_FORCE_CLOSE) {
            final int size = mBBFloatingViewList.size();
            for (int i = 0; i < size; i++) {
                final BBFloatingView BBFloatingView = mBBFloatingViewList.get(i);
                BBFloatingView.setDraggable(false);
            }
        }
      }catch(Exception ex)
      {

      }
    }


    @Override
    public void onTrashAnimationEnd(int animationCode) {
        try{

        final int state = mTargetBBFloatingView.getState();
        if (state == BBFloatingView.STATE_FINISHING) {
            removeViewToWindow(mTargetBBFloatingView);
        }

        final int size = mBBFloatingViewList.size();
        for (int i = 0; i < size; i++) {
            final BBFloatingView BBFloatingView = mBBFloatingViewList.get(i);
            BBFloatingView.setDraggable(true);
        }
        }catch(Exception ex)
        {

        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try{
        final int action = event.getAction();

        if (action != MotionEvent.ACTION_DOWN && !mIsMoveAccept) {
            return false;
        }

        final int state = mTargetBBFloatingView.getState();
        mTargetBBFloatingView = (BBFloatingView) v;


        if (action == MotionEvent.ACTION_DOWN) {

            mIsMoveAccept = true;
        }

        else if (action == MotionEvent.ACTION_MOVE) {

            final boolean isIntersecting = isIntersectWithTrash();

            final boolean isIntersect = state == BBFloatingView.STATE_INTERSECTING;

            if (isIntersecting) {
                mTargetBBFloatingView.setIntersecting((int) mTrashView.getTrashIconCenterX(), (int) mTrashView.getTrashIconCenterY());
            }
            if (isIntersecting && !isIntersect) {
                mVibrator.vibrate(VIBRATE_INTERSECTS_MILLIS);
                mTrashView.setScaleTrashIcon(true);
            }
            else if (!isIntersecting && isIntersect) {
                mTargetBBFloatingView.setNormal();
                mTrashView.setScaleTrashIcon(false);
            }

        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (state == BBFloatingView.STATE_INTERSECTING) {
                mTargetBBFloatingView.setFinishing();
                mTrashView.setScaleTrashIcon(false);
            }
            mIsMoveAccept = false;
        }


        if (state == BBFloatingView.STATE_INTERSECTING) {
            mTrashView.onTouchFloatingView(event, mFloatingViewRect.left, mFloatingViewRect.top);
        } else {
            final WindowManager.LayoutParams params = mTargetBBFloatingView.getWindowLayoutParams();
            mTrashView.onTouchFloatingView(event, params.x, params.y);
        }
        }catch(Exception ex)
        {

        }

        return false;
    }


    public void setFixedTrashIconImage(int resId) {
        mTrashView.setFixedTrashIconImage(resId);
    }


    public void setActionTrashIconImage(int resId) {
        mTrashView.setActionTrashIconImage(resId);
    }


    public void setFixedTrashIconImage(Drawable drawable) {
        mTrashView.setFixedTrashIconImage(drawable);
    }


    public void setActionTrashIconImage(Drawable drawable) {
        mTrashView.setActionTrashIconImage(drawable);
    }


    public void setDisplayMode(int displayMode) {
        mDisplayMode = displayMode;
        if (mDisplayMode == DISPLAY_MODE_SHOW_ALWAYS || mDisplayMode == DISPLAY_MODE_HIDE_FULLSCREEN) {
            for (BBFloatingView BBFloatingView : mBBFloatingViewList) {
                BBFloatingView.setVisibility(View.VISIBLE);
            }
        }
        else if (mDisplayMode == DISPLAY_MODE_HIDE_ALWAYS) {
            for (BBFloatingView BBFloatingView : mBBFloatingViewList) {
                BBFloatingView.setVisibility(View.GONE);
            }
            mTrashView.dismiss();
        }
    }


    public void setTrashViewEnabled(boolean enabled) {
        mTrashView.setTrashEnabled(enabled);
    }


    public boolean isTrashViewEnabled() {
        return mTrashView.isTrashEnabled();
    }


    @Deprecated
    public void addViewToWindow(View view, float shape, int overMargin) {
        final Options options = new Options();
        options.shape = shape;
        options.overMargin = overMargin;
        addViewToWindow(view, options);
    }


    public void addViewToWindow(View view, Options options) {
        try{
        final boolean isFirstAttach = mBBFloatingViewList.isEmpty();
        final BBFloatingView BBFloatingView = new BBFloatingView(mContext);
        BBFloatingView.setInitCoords(options.floatingViewX, options.floatingViewY);
        BBFloatingView.setOnTouchListener(this);
        BBFloatingView.setShape(options.shape);
        BBFloatingView.setOverMargin(options.overMargin);
        BBFloatingView.setMoveDirection(options.moveDirection);
        BBFloatingView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                BBFloatingView.getViewTreeObserver().removeOnPreDrawListener(this);
                mTrashView.calcActionTrashIconPadding(BBFloatingView.getMeasuredWidth(), BBFloatingView.getMeasuredHeight(), BBFloatingView.getShape());
                return false;
            }
        });
        BBFloatingView.addView(view);
        if (mDisplayMode == DISPLAY_MODE_HIDE_ALWAYS) {
            BBFloatingView.setVisibility(View.GONE);
        }
        mBBFloatingViewList.add(BBFloatingView);
        mTrashView.setTrashViewListener(this);

        mWindowManager.addView(BBFloatingView, BBFloatingView.getWindowLayoutParams());

        if (isFirstAttach) {
            mWindowManager.addView(mBBFullscreenObserverView, mBBFullscreenObserverView.getWindowLayoutParams());
            mTargetBBFloatingView = BBFloatingView;
        } else {
            mWindowManager.removeViewImmediate(mTrashView);
        }
        mWindowManager.addView(mTrashView, mTrashView.getWindowLayoutParams());
        }catch(Exception ex)
        {

        }
    }


    private void removeViewToWindow(BBFloatingView BBFloatingView) {
        try{
        final int matchIndex = mBBFloatingViewList.indexOf(BBFloatingView);
        if (matchIndex != -1) {
            mWindowManager.removeViewImmediate(BBFloatingView);
            mBBFloatingViewList.remove(matchIndex);
        }

        if (mBBFloatingViewList.isEmpty()) {
            if (mBBFloatingViewListener != null) {
                mBBFloatingViewListener.onFinishFloatingView();
            }
        }
        }catch(Exception ex)
        {

        }
    }


    public void removeAllViewToWindow() {
        try {
            mWindowManager.removeViewImmediate(mBBFullscreenObserverView);
            mWindowManager.removeViewImmediate(mTrashView);
            final int size = mBBFloatingViewList.size();
            for (int i = 0; i < size; i++) {
                final BBFloatingView BBFloatingView = mBBFloatingViewList.get(i);
                mWindowManager.removeViewImmediate(BBFloatingView);
            }
            mBBFloatingViewList.clear();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public static class Options {


        public float shape;

        public int overMargin;

        public int floatingViewX;

        public int floatingViewY;

        public int moveDirection;


        public Options() {
            shape = SHAPE_CIRCLE;
            overMargin = 0;
            floatingViewX = BBFloatingView.DEFAULT_X;
            floatingViewY = BBFloatingView.DEFAULT_Y;
            moveDirection = MOVE_DIRECTION_DEFAULT;
        }

    }

}
