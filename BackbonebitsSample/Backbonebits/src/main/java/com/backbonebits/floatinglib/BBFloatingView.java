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
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;


class BBFloatingView extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {
    private static final float MOVE_THRESHOLD_DP = 8.0f;
    private static final float SCALE_PRESSED = 0.9f;
    private static final float SCALE_NORMAL = 1.0f;
    private static final long MOVE_TO_EDGE_DURATION = 450L;
    private static final float MOVE_TO_EDGE_OVERSHOOT_TENSION = 1.25f;
    static final int STATE_NORMAL = 0;
    static final int STATE_INTERSECTING = 1;
    static final int STATE_FINISHING = 2;
    private static final int LONG_PRESS_TIMEOUT = (int) (1.5f * ViewConfiguration.getLongPressTimeout());
    static final int DEFAULT_X = Integer.MIN_VALUE;
    static final int DEFAULT_Y = Integer.MIN_VALUE;
    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mParams;
    private final DisplayMetrics mMetrics;
    private long mTouchDownTime;
    private float mScreenTouchDownX;
    private float mScreenTouchDownY;
    private boolean mIsMoveAccept;
    private float mScreenTouchX;
    private float mScreenTouchY;
    private float mLocalTouchX;
    private float mLocalTouchY;
    private int mInitX;
    private int mInitY;
    private final int mStatusBarHeight;
    private ValueAnimator mMoveEdgeAnimator;
    private final TimeInterpolator mMoveEdgeInterpolator;
    private final Rect mMoveLimitRect;
    private final Rect mPositionLimitRect;
    private boolean mIsDraggable;
    private float mShape;
    private final FloatingAnimationHandler mAnimationHandler;
    private final LongPressHandler mLongPressHandler;
    private int mOverMargin;
    private OnTouchListener mOnTouchListener;
    private boolean mIsLongPressed;
    private int mMoveDirection;
    BBFloatingView(final Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        mAnimationHandler = new FloatingAnimationHandler(this);
        mLongPressHandler = new LongPressHandler(this);
        mMoveEdgeInterpolator = new OvershootInterpolator(MOVE_TO_EDGE_OVERSHOOT_TENSION);
        mMoveDirection = BBFloatingViewManagerBB.MOVE_DIRECTION_DEFAULT;
        mMoveLimitRect = new Rect();
        mPositionLimitRect = new Rect();

        final Resources resources = context.getResources();
        final int statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeightId > 0) {
            mStatusBarHeight = resources.getDimensionPixelSize(statusBarHeightId);
        } else {
            mStatusBarHeight = 0;
        }
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateViewLayout();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewLayout();
    }

    @Override
    public boolean onPreDraw() {
        try{
        getViewTreeObserver().removeOnPreDrawListener(this);
        if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_NONE) {
            mParams.x = mInitX;
            mParams.y = mInitY;
            moveTo(mInitX, mInitY, mInitX, mInitY, false);
        } else {
            mParams.x = 0;
            mParams.y = mMetrics.heightPixels - mStatusBarHeight - getMeasuredHeight();
            moveToEdge(false);
        }
        mIsDraggable = true;
        mWindowManager.updateViewLayout(this, mParams);
    }catch(Exception ex)
    {

    }
        return true;
    }

    private void updateViewLayout() {
        try{
        cancelAnimation();

        final int oldScreenHeight = mMetrics.heightPixels;
        final int oldScreenWidth = mMetrics.widthPixels;
        final int oldPositionLimitWidth = mPositionLimitRect.width();
        final int oldPositionLimitHeight = mPositionLimitRect.height();

        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int newScreenWidth = mMetrics.widthPixels;
        final int newScreenHeight = mMetrics.heightPixels;

        mMoveLimitRect.set(-width, -height * 2, newScreenWidth + width, newScreenHeight + height);
        mPositionLimitRect.set(-mOverMargin, 0, newScreenWidth - width + mOverMargin, newScreenHeight - mStatusBarHeight - height);

        if (oldScreenWidth != newScreenWidth || oldScreenHeight != newScreenHeight) {
            if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_DEFAULT) {
                if (mParams.x > (newScreenWidth - width) / 2) {
                    mParams.x = mPositionLimitRect.right;
                }
                else {
                    mParams.x = mPositionLimitRect.left;
                }
            }
            else if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_LEFT) {
                mParams.x = mPositionLimitRect.left;
            }
            else if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_RIGHT) {
                mParams.x = mPositionLimitRect.right;
            }
            else {
                final int newX = (int) (mParams.x * mPositionLimitRect.width() / (float) oldPositionLimitWidth + 0.5f);
                mParams.x = Math.min(Math.max(mPositionLimitRect.left, newX), mPositionLimitRect.right);
            }

            final int newY = (int) (mParams.y * mPositionLimitRect.height() / (float) oldPositionLimitHeight + 0.5f);
            mParams.y = Math.min(Math.max(mPositionLimitRect.top, newY), mPositionLimitRect.bottom);
            mWindowManager.updateViewLayout(this, mParams);
        }
        }catch(Exception ex)
        {

        }
    }


    @Override
    protected void onDetachedFromWindow() {
        if (mMoveEdgeAnimator != null) {
            mMoveEdgeAnimator.removeAllUpdateListeners();
        }
        super.onDetachedFromWindow();
    }


    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
       try{
        if (getVisibility() != View.VISIBLE) {
            return true;
        }

        if (!mIsDraggable) {
            return true;
        }

        mScreenTouchX = event.getRawX();
        mScreenTouchY = event.getRawY();
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            cancelAnimation();
            mScreenTouchDownX = mScreenTouchX;
            mScreenTouchDownY = mScreenTouchY;
            mLocalTouchX = event.getX();
            mLocalTouchY = event.getY();
            mIsMoveAccept = false;
            setScale(SCALE_PRESSED);
            mAnimationHandler.updateTouchPosition(getXByTouch(), getYByTouch());
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            mAnimationHandler.sendAnimationMessage(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
            mLongPressHandler.sendEmptyMessageDelayed(LongPressHandler.LONG_PRESSED, LONG_PRESS_TIMEOUT);
            mTouchDownTime = event.getDownTime();
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            if (mIsMoveAccept) {
                mIsLongPressed = false;
                mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
            }
            if (mTouchDownTime != event.getDownTime()) {
                return true;
            }
            final float moveThreshold = MOVE_THRESHOLD_DP * mMetrics.density;
            if (!mIsMoveAccept && Math.abs(mScreenTouchX - mScreenTouchDownX) < moveThreshold && Math.abs(mScreenTouchY - mScreenTouchDownY) < moveThreshold) {
                return true;
            }
            mIsMoveAccept = true;
            mAnimationHandler.updateTouchPosition(getXByTouch(), getYByTouch());
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            final boolean tmpIsLongPressed = mIsLongPressed;
            mIsLongPressed = false;
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
            if (mTouchDownTime != event.getDownTime()) {
                return true;
            }
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            setScale(SCALE_NORMAL);

            if (mIsMoveAccept) {
                moveToEdge(true);
            }
            else {
                if (!tmpIsLongPressed) {
                    final int size = getChildCount();
                    for (int i = 0; i < size; i++) {
                        getChildAt(i).performClick();
                    }
                }
            }
        }

        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(this, event);
        }
       }catch(Exception ex)
       {

       }
        return true;
    }

    private void onLongClick() {
        try{
        mIsLongPressed = true;
        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            getChildAt(i).performLongClick();
        }
        }catch(Exception ex)
        {

        }
    }

    @Override
    public void setVisibility(int visibility) {
       try{
        if (visibility != View.VISIBLE) {
            cancelLongPress();
            setScale(SCALE_NORMAL);
            if (mIsMoveAccept) {
                moveToEdge(false);
            }
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
        }
       }catch(Exception ex)
       {

       }
        super.setVisibility(visibility);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }


    private void moveToEdge(boolean withAnimation) {
        try{
        final int currentX = getXByTouch();
        final int currentY = getYByTouch();
        final int goalPositionX;
        if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_DEFAULT) {
            final boolean isMoveRightEdge = currentX > (mMetrics.widthPixels - getWidth()) / 2;
            goalPositionX = isMoveRightEdge ? mPositionLimitRect.right : mPositionLimitRect.left;
        }
        else if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_LEFT) {
            goalPositionX = mPositionLimitRect.left;
        }
        else if (mMoveDirection == BBFloatingViewManagerBB.MOVE_DIRECTION_RIGHT) {
            goalPositionX = mPositionLimitRect.right;
        }
        else {
            goalPositionX = currentX;
        }
        final int goalPositionY = currentY;
        moveTo(currentX, currentY, goalPositionX, goalPositionY, withAnimation);
        }catch(Exception ex)
        {

        }
    }


    private void moveTo(int currentX, int currentY, int goalPositionX, int goalPositionY, boolean withAnimation) {
      try{
        goalPositionX = Math.min(Math.max(mPositionLimitRect.left, goalPositionX), mPositionLimitRect.right);
        goalPositionY = Math.min(Math.max(mPositionLimitRect.top, goalPositionY), mPositionLimitRect.bottom);
        if (withAnimation) {
            mParams.y = goalPositionY;

            mMoveEdgeAnimator = ValueAnimator.ofInt(currentX, goalPositionX);
            mMoveEdgeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mParams.x = (Integer) animation.getAnimatedValue();
                    mWindowManager.updateViewLayout(BBFloatingView.this, mParams);
                }
            });
            mMoveEdgeAnimator.setDuration(MOVE_TO_EDGE_DURATION);
            mMoveEdgeAnimator.setInterpolator(mMoveEdgeInterpolator);
            mMoveEdgeAnimator.start();
        } else {
            if (mParams.x != goalPositionX || mParams.y != goalPositionY) {
                mParams.x = goalPositionX;
                mParams.y = goalPositionY;
                mWindowManager.updateViewLayout(BBFloatingView.this, mParams);
            }
        }
        mLocalTouchX = 0;
        mLocalTouchY = 0;
        mScreenTouchDownX = 0;
        mScreenTouchDownY = 0;
        mIsMoveAccept = false;
      }catch(Exception ex)
      {

      }
    }


    private void cancelAnimation() {
        if (mMoveEdgeAnimator != null && mMoveEdgeAnimator.isStarted()) {
            mMoveEdgeAnimator.cancel();
            mMoveEdgeAnimator = null;
        }
    }

    private void setScale(float newScale) {
        try{
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View targetView = getChildAt(i);
                targetView.setScaleX(newScale);
                targetView.setScaleY(newScale);
            }
        } else {
            setScaleX(newScale);
            setScaleY(newScale);
        }
        }catch(Exception ex)
        {

        }
    }

    void setDraggable(boolean isDraggable) {
        mIsDraggable = isDraggable;
    }


    void setShape(float shape) {
        mShape = shape;
    }


    float getShape() {
        return mShape;
    }


    void setOverMargin(int margin) {
        mOverMargin = margin;
    }


    void setMoveDirection(int moveDirection) {
        // デフォルトから変更されていたら画面端に移動しない
        if (mInitX != DEFAULT_X || mInitY != DEFAULT_Y) {
            mMoveDirection = BBFloatingViewManagerBB.MOVE_DIRECTION_NONE;
        } else {
            mMoveDirection = moveDirection;
        }
    }


    void setInitCoords(int x, int y) {
        mInitX = x;
        mInitY = y;
    }


    void getWindowDrawingRect(Rect outRect) {
        final int currentX = getXByTouch();
        final int currentY = getYByTouch();
        outRect.set(currentX, currentY, currentX + getWidth(), currentY + getHeight());
    }


    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }


    private int getXByTouch() {
        return (int) (mScreenTouchX - mLocalTouchX);
    }


    private int getYByTouch() {
        return (int) (mMetrics.heightPixels - (mScreenTouchY - mLocalTouchY + getHeight()));
    }


    void setNormal() {
        mAnimationHandler.setState(STATE_NORMAL);
        mAnimationHandler.updateTouchPosition(getXByTouch(), getYByTouch());
    }


    void setIntersecting(int centerX, int centerY) {
        mAnimationHandler.setState(STATE_INTERSECTING);
        mAnimationHandler.updateTargetPosition(centerX, centerY);
    }


    void setFinishing() {
        mAnimationHandler.setState(STATE_FINISHING);
        setVisibility(View.GONE);
    }

    int getState() {
        return mAnimationHandler.getState();
    }


    static class FloatingAnimationHandler extends Handler {


        private static final long ANIMATION_REFRESH_TIME_MILLIS = 17L;

        private static final long CAPTURE_DURATION_MILLIS = 300L;


        private static final int ANIMATION_NONE = 0;


        private static final int ANIMATION_IN_TOUCH = 1;


        private static final int TYPE_FIRST = 1;

        private static final int TYPE_UPDATE = 2;


        private long mStartTime;


        private float mStartX;


        private float mStartY;


        private int mStartedCode;


        private int mState;


        private boolean mIsChangeState;


        private float mTouchPositionX;


        private float mTouchPositionY;


        private float mTargetPositionX;

        private float mTargetPositionY;


        private final WeakReference<BBFloatingView> mFloatingView;


        FloatingAnimationHandler(BBFloatingView BBFloatingView) {
            mFloatingView = new WeakReference<>(BBFloatingView);
            mStartedCode = ANIMATION_NONE;
            mState = STATE_NORMAL;
        }


        @Override
        public void handleMessage(Message msg) {
            try{
            final BBFloatingView BBFloatingView = mFloatingView.get();
            if (BBFloatingView == null) {
                removeMessages(ANIMATION_IN_TOUCH);
                return;
            }

            final int animationCode = msg.what;
            final int animationType = msg.arg1;
            final WindowManager.LayoutParams params = BBFloatingView.mParams;
            final WindowManager windowManager = BBFloatingView.mWindowManager;

            if (mIsChangeState || animationType == TYPE_FIRST) {
                mStartTime = mIsChangeState ? SystemClock.uptimeMillis() : 0;
                mStartX = params.x;
                mStartY = params.y;
                mStartedCode = animationCode;
                mIsChangeState = false;
            }
            final float elapsedTime = SystemClock.uptimeMillis() - mStartTime;
            final float trackingTargetTimeRate = Math.min(elapsedTime / CAPTURE_DURATION_MILLIS, 1.0f);

            if (mState == BBFloatingView.STATE_NORMAL) {
                final float basePosition = calcAnimationPosition(trackingTargetTimeRate);
                final Rect moveLimitRect = BBFloatingView.mMoveLimitRect;
                final float targetPositionX = Math.min(Math.max(moveLimitRect.left, (int) mTouchPositionX), moveLimitRect.right);
                final float targetPositionY = Math.min(Math.max(moveLimitRect.top, (int) mTouchPositionY), moveLimitRect.bottom);
                params.x = (int) (mStartX + (targetPositionX - mStartX) * basePosition);
                params.y = (int) (mStartY + (targetPositionY - mStartY) * basePosition);
                windowManager.updateViewLayout(BBFloatingView, params);
                sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
            }
            else if (mState == BBFloatingView.STATE_INTERSECTING) {
                final float basePosition = calcAnimationPosition(trackingTargetTimeRate);
                final float targetPositionX = mTargetPositionX - BBFloatingView.getWidth() / 2;
                final float targetPositionY = mTargetPositionY - BBFloatingView.getHeight() / 2;
                params.x = (int) (mStartX + (targetPositionX - mStartX) * basePosition);
                params.y = (int) (mStartY + (targetPositionY - mStartY) * basePosition);
                windowManager.updateViewLayout(BBFloatingView, params);
                sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
            }
        }catch(Exception ex)
        {

        }
        }


        private static float calcAnimationPosition(float timeRate) {
             float position = 0;
            try{

            // y=0.55sin(8.0564x-π/2)+0.55
            if (timeRate <= 0.4) {
                position = (float) (0.55 * Math.sin(8.0564 * timeRate - Math.PI / 2) + 0.55);
            }
            // y=4(0.417x-0.341)^2-4(0.417-0.341)^2+1
            else {
                position = (float) (4 * Math.pow(0.417 * timeRate - 0.341, 2) - 4 * Math.pow(0.417 - 0.341, 2) + 1);
            }
            }catch(Exception ex)
            {

            }
            return position;
        }


        void sendAnimationMessageDelayed(int animation, long delayMillis) {
            sendMessageAtTime(newMessage(animation, TYPE_FIRST), SystemClock.uptimeMillis() + delayMillis);
        }


        void sendAnimationMessage(int animation) {
            sendMessage(newMessage(animation, TYPE_FIRST));
        }


        private static Message newMessage(int animation, int type) {
            final Message message = Message.obtain();
            message.what = animation;
            message.arg1 = type;
            return message;
        }


        void updateTouchPosition(float positionX, float positionY) {
            mTouchPositionX = positionX;
            mTouchPositionY = positionY;
        }


        void updateTargetPosition(float centerX, float centerY) {
            mTargetPositionX = centerX;
            mTargetPositionY = centerY;
        }


        void setState(int newState) {
            if (mState != newState) {
                mIsChangeState = true;
            }
            mState = newState;
        }


        int getState() {
            return mState;
        }
    }


    static class LongPressHandler extends Handler {


        private WeakReference<BBFloatingView> mFloatingView;


        private static final int LONG_PRESSED = 0;


        LongPressHandler(BBFloatingView view) {
            mFloatingView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            BBFloatingView view = mFloatingView.get();
            if (view == null) {
                removeMessages(LONG_PRESSED);
                return;
            }

            view.onLongClick();
        }
    }
}
