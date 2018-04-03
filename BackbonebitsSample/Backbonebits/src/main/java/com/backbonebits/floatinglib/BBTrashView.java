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

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


class BBTrashView extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {


    private static final int BACKGROUND_HEIGHT = 164;


    private static final float TARGET_CAPTURE_HORIZONTAL_REGION = 30.0f;


    private static final float TARGET_CAPTURE_VERTICAL_REGION = 4.0f;

    private static final long TRASH_ICON_SCALE_DURATION_MILLIS = 200L;


    static final int ANIMATION_NONE = 0;

    static final int ANIMATION_OPEN = 1;

    static final int ANIMATION_CLOSE = 2;

    static final int ANIMATION_FORCE_CLOSE = 3;


    private static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();


    private final WindowManager mWindowManager;


    private final WindowManager.LayoutParams mParams;


    private final DisplayMetrics mMetrics;


    private final ViewGroup mRootView;

    private final FrameLayout mTrashIconRootView;


    private final ImageView mFixedTrashIconView;


    private final ImageView mActionTrashIconView;


    private int mActionTrashIconBaseWidth;


    private int mActionTrashIconBaseHeight;


    private float mActionTrashIconMaxScale;


    private final FrameLayout mBackgroundView;


    private ObjectAnimator mEnterScaleAnimator;

    private ObjectAnimator mExitScaleAnimator;


    private final AnimationHandler mAnimationHandler;


    private BBTrashViewListener mBBTrashViewListener;


    private boolean mIsEnabled;


    BBTrashView(Context context) {
        super(context);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mAnimationHandler = new AnimationHandler(this);
        mIsEnabled = true;

        mParams = new WindowManager.LayoutParams();
        mParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;


        mRootView = new FrameLayout(context);
        mTrashIconRootView = new FrameLayout(context);
        mFixedTrashIconView = new ImageView(context);
        mActionTrashIconView = new ImageView(context);
        mBackgroundView = new FrameLayout(context);
        mBackgroundView.setAlpha(0.0f);
        final GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x00000000, 0x50000000});
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            mBackgroundView.setBackgroundDrawable(gradientDrawable);
        } else {
            mBackgroundView.setBackground(gradientDrawable);
        }

        final LayoutParams backgroundParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (BACKGROUND_HEIGHT * mMetrics.density));
        mRootView.addView(mBackgroundView, backgroundParams);
        final LayoutParams actionTrashIconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionTrashIconParams.gravity = Gravity.CENTER;
        mTrashIconRootView.addView(mActionTrashIconView, actionTrashIconParams);
        final LayoutParams fixedTrashIconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fixedTrashIconParams.gravity = Gravity.CENTER;
        mTrashIconRootView.addView(mFixedTrashIconView, fixedTrashIconParams);
        final LayoutParams trashIconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trashIconParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mRootView.addView(mTrashIconRootView, trashIconParams);

        addView(mRootView);

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
        getViewTreeObserver().removeOnPreDrawListener(this);
        mTrashIconRootView.setTranslationY(mTrashIconRootView.getMeasuredHeight());
        return true;
    }


    private void updateViewLayout() {
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mParams.x = (mMetrics.widthPixels - getWidth()) / 2;
        mParams.y = 0;


        mAnimationHandler.onUpdateViewLayout();

        mWindowManager.updateViewLayout(this, mParams);
    }


    void dismiss() {
        mAnimationHandler.removeMessages(ANIMATION_OPEN);
        mAnimationHandler.removeMessages(ANIMATION_CLOSE);
        mAnimationHandler.sendAnimationMessage(ANIMATION_FORCE_CLOSE);
        setScaleTrashIconImmediately(false);
    }


    void getWindowDrawingRect(Rect outRect) {
       try
       {
        final ImageView iconView = hasActionTrashIcon() ? mActionTrashIconView : mFixedTrashIconView;
        final float iconPaddingLeft = iconView.getPaddingLeft();
        final float iconPaddingTop = iconView.getPaddingTop();
        final float iconWidth = iconView.getWidth() - iconPaddingLeft - iconView.getPaddingRight();
        final float iconHeight = iconView.getHeight() - iconPaddingTop - iconView.getPaddingBottom();
        final float x = mTrashIconRootView.getX() + iconPaddingLeft;
        final float y = mRootView.getHeight() - mTrashIconRootView.getY() - iconPaddingTop - iconHeight;
        final int left = (int) (x - TARGET_CAPTURE_HORIZONTAL_REGION * mMetrics.density);
        final int top = -mRootView.getHeight();
        final int right = (int) (x + iconWidth + TARGET_CAPTURE_HORIZONTAL_REGION * mMetrics.density);
        final int bottom = (int) (y + iconHeight + TARGET_CAPTURE_VERTICAL_REGION * mMetrics.density);
        outRect.set(left, top, right, bottom);
       }catch(Exception ex)
       {

       }
    }

    void calcActionTrashIconPadding(float width, float height, float shape) {
      try{
        if (!hasActionTrashIcon()) {
            return;
        }
        mAnimationHandler.mTargetWidth = width;
        mAnimationHandler.mTargetHeight = height;
        final float newWidthScale = width / mActionTrashIconBaseWidth * shape;
        final float newHeightScale = height / mActionTrashIconBaseHeight * shape;
        mActionTrashIconMaxScale = Math.max(newWidthScale, newHeightScale);
        mEnterScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionTrashIconView, PropertyValuesHolder.ofFloat(ImageView.SCALE_X, mActionTrashIconMaxScale), PropertyValuesHolder.ofFloat(ImageView.SCALE_Y, mActionTrashIconMaxScale));
        mEnterScaleAnimator.setInterpolator(new OvershootInterpolator());
        mEnterScaleAnimator.setDuration(TRASH_ICON_SCALE_DURATION_MILLIS);
        mExitScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionTrashIconView, PropertyValuesHolder.ofFloat(ImageView.SCALE_X, 1.0f), PropertyValuesHolder.ofFloat(ImageView.SCALE_Y, 1.0f));
        mExitScaleAnimator.setInterpolator(new OvershootInterpolator());
        mExitScaleAnimator.setDuration(TRASH_ICON_SCALE_DURATION_MILLIS);

        final int horizontalPadding = Math.max((int) ((mActionTrashIconMaxScale - 1.0f) * mActionTrashIconBaseWidth / 2 + 0.5f), 0);
        final int verticalPadding = Math.max((int) ((mActionTrashIconMaxScale - 1.0f) * mActionTrashIconBaseHeight / 2 + 0.5f), 0);
        mActionTrashIconView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
      }catch(Exception ex)
      {

      }
      }


    float getTrashIconCenterX() {
        final ImageView iconView = hasActionTrashIcon() ? mActionTrashIconView : mFixedTrashIconView;
        final float iconViewPaddingLeft = iconView.getPaddingLeft();
        final float iconWidth = iconView.getWidth() - iconViewPaddingLeft - iconView.getPaddingRight();
        final float x = mTrashIconRootView.getX() + iconViewPaddingLeft;
        return x + iconWidth / 2;
    }


    float getTrashIconCenterY() {
        final ImageView iconView = hasActionTrashIcon() ? mActionTrashIconView : mFixedTrashIconView;
        final float iconViewHeight = iconView.getHeight();
        final float iconViewPaddingBottom = iconView.getPaddingBottom();
        final float iconHeight = iconViewHeight - iconView.getPaddingTop() - iconViewPaddingBottom;
        final float y = mRootView.getHeight() - mTrashIconRootView.getY() - iconViewHeight + iconViewPaddingBottom;
        return y + iconHeight / 2;
    }



    private boolean hasActionTrashIcon() {
        return mActionTrashIconBaseWidth != 0 && mActionTrashIconBaseHeight != 0;
    }


    void setFixedTrashIconImage(int resId) {
        mFixedTrashIconView.setImageResource(resId);
    }


    void setActionTrashIconImage(int resId) {
        mActionTrashIconView.setImageResource(resId);
        final Drawable drawable = mActionTrashIconView.getDrawable();
        if (drawable != null) {
            mActionTrashIconBaseWidth = drawable.getIntrinsicWidth();
            mActionTrashIconBaseHeight = drawable.getIntrinsicHeight();
        }
    }


    void setFixedTrashIconImage(Drawable drawable) {
        mFixedTrashIconView.setImageDrawable(drawable);
    }


    void setActionTrashIconImage(Drawable drawable) {
        mActionTrashIconView.setImageDrawable(drawable);
        if (drawable != null) {
            mActionTrashIconBaseWidth = drawable.getIntrinsicWidth();
            mActionTrashIconBaseHeight = drawable.getIntrinsicHeight();
        }
    }


    private void setScaleTrashIconImmediately(boolean isEnter) {
        cancelScaleTrashAnimation();

        mActionTrashIconView.setScaleX(isEnter ? mActionTrashIconMaxScale : 1.0f);
        mActionTrashIconView.setScaleY(isEnter ? mActionTrashIconMaxScale : 1.0f);
    }


    void setScaleTrashIcon(boolean isEnter) {
        if (!hasActionTrashIcon()) {
            return;
        }

        cancelScaleTrashAnimation();

        if (isEnter) {
            mEnterScaleAnimator.start();
        } else {
            mExitScaleAnimator.start();
        }
    }


    void setTrashEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }

        mIsEnabled = enabled;
        if (!mIsEnabled) {
            dismiss();
        }
    }


    boolean isTrashEnabled() {
        return mIsEnabled;
    }


    private void cancelScaleTrashAnimation() {
        if (mEnterScaleAnimator != null && mEnterScaleAnimator.isStarted()) {
            mEnterScaleAnimator.cancel();
        }

        if (mExitScaleAnimator != null && mExitScaleAnimator.isStarted()) {
            mExitScaleAnimator.cancel();
        }
    }


    void setTrashViewListener(BBTrashViewListener listener) {
        mBBTrashViewListener = listener;
    }


    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }


    void onTouchFloatingView(MotionEvent event, float x, float y) {
        try{
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mAnimationHandler.updateTargetPosition(x, y);
            mAnimationHandler.removeMessages(ANIMATION_CLOSE);
            mAnimationHandler.sendAnimationMessageDelayed(ANIMATION_OPEN, LONG_PRESS_TIMEOUT);
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            mAnimationHandler.updateTargetPosition(x, y);
            if (!mAnimationHandler.isAnimationStarted(ANIMATION_OPEN)) {
                mAnimationHandler.removeMessages(ANIMATION_OPEN);
                mAnimationHandler.sendAnimationMessage(ANIMATION_OPEN);
            }
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mAnimationHandler.removeMessages(ANIMATION_OPEN);
            mAnimationHandler.sendAnimationMessage(ANIMATION_CLOSE);
        }
        }catch(Exception ex)
        {

        }
    }


    static class AnimationHandler extends Handler {


        private static final long ANIMATION_REFRESH_TIME_MILLIS = 17L;


        private static final long BACKGROUND_DURATION_MILLIS = 200L;


        private static final long TRASH_OPEN_START_DELAY_MILLIS = 200L;


        private static final long TRASH_OPEN_DURATION_MILLIS = 400L;


        private static final long TRASH_CLOSE_DURATION_MILLIS = 200L;


        private static final float OVERSHOOT_TENSION = 1.0f;


        private static final int TRASH_MOVE_LIMIT_OFFSET_X = 22;


        private static final int TRASH_MOVE_LIMIT_TOP_OFFSET = -4;


        private static final int TYPE_FIRST = 1;

        private static final int TYPE_UPDATE = 2;


        private static final float MAX_ALPHA = 1.0f;


        private static final float MIN_ALPHA = 0.0f;


        private long mStartTime;


        private float mStartAlpha;


        private float mStartTransitionY;


        private int mStartedCode;


        private float mTargetPositionX;


        private float mTargetPositionY;


        private float mTargetWidth;


        private float mTargetHeight;


        private final Rect mTrashIconLimitPosition;


        private float mMoveStickyYRange;


        private final OvershootInterpolator mOvershootInterpolator;


        private final WeakReference<BBTrashView> mTrashView;


        AnimationHandler(BBTrashView trashView) {
            mTrashView = new WeakReference<>(trashView);
            mStartedCode = ANIMATION_NONE;
            mTrashIconLimitPosition = new Rect();
            mOvershootInterpolator = new OvershootInterpolator(OVERSHOOT_TENSION);
        }


        @Override
        public void handleMessage(Message msg) {
            try{
            final BBTrashView trashView = mTrashView.get();
            if (trashView == null) {
                removeMessages(ANIMATION_OPEN);
                removeMessages(ANIMATION_CLOSE);
                removeMessages(ANIMATION_FORCE_CLOSE);
                return;
            }

            if (!trashView.isTrashEnabled()) {
                return;
            }

            final int animationCode = msg.what;
            final int animationType = msg.arg1;
            final FrameLayout backgroundView = trashView.mBackgroundView;
            final FrameLayout trashIconRootView = trashView.mTrashIconRootView;
            final BBTrashViewListener listener = trashView.mBBTrashViewListener;
            final float screenWidth = trashView.mMetrics.widthPixels;
            final float trashViewX = trashView.mParams.x;

            if (animationType == TYPE_FIRST) {
                mStartTime = SystemClock.uptimeMillis();
                mStartAlpha = backgroundView.getAlpha();
                mStartTransitionY = trashIconRootView.getTranslationY();
                mStartedCode = animationCode;
                if (listener != null) {
                    listener.onTrashAnimationStarted(mStartedCode);
                }
            }
            final float elapsedTime = SystemClock.uptimeMillis() - mStartTime;

            if (animationCode == ANIMATION_OPEN) {
                final float currentAlpha = backgroundView.getAlpha();
                if (currentAlpha < MAX_ALPHA) {
                    final float alphaTimeRate = Math.min(elapsedTime / BACKGROUND_DURATION_MILLIS, 1.0f);
                    final float alpha = Math.min(mStartAlpha + alphaTimeRate, MAX_ALPHA);
                    backgroundView.setAlpha(alpha);
                }

                if (elapsedTime >= TRASH_OPEN_START_DELAY_MILLIS) {
                    final float screenHeight = trashView.mMetrics.heightPixels;
                    final float positionX = trashViewX + (mTargetPositionX + mTargetWidth) / (screenWidth + mTargetWidth) * mTrashIconLimitPosition.width() + mTrashIconLimitPosition.left;

                    final float targetPositionYRate = Math.min(2 * (mTargetPositionY + mTargetHeight) / (screenHeight + mTargetHeight), 1.0f);
                    final float stickyPositionY = mMoveStickyYRange * targetPositionYRate + mTrashIconLimitPosition.height() - mMoveStickyYRange;
                    final float translationYTimeRate = Math.min((elapsedTime - TRASH_OPEN_START_DELAY_MILLIS) / TRASH_OPEN_DURATION_MILLIS, 1.0f);
                    final float positionY = mTrashIconLimitPosition.bottom - stickyPositionY * mOvershootInterpolator.getInterpolation(translationYTimeRate);
                    trashIconRootView.setTranslationX(positionX);
                    trashIconRootView.setTranslationY(positionY);
                }

                sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
            }
            else if (animationCode == ANIMATION_CLOSE) {
                final float alphaElapseTimeRate = Math.min(elapsedTime / BACKGROUND_DURATION_MILLIS, 1.0f);
                final float alpha = Math.max(mStartAlpha - alphaElapseTimeRate, MIN_ALPHA);
                backgroundView.setAlpha(alpha);

                final float translationYTimeRate = Math.min(elapsedTime / TRASH_CLOSE_DURATION_MILLIS, 1.0f);
                if (alphaElapseTimeRate < 1.0f || translationYTimeRate < 1.0f) {
                    final float position = mStartTransitionY + mTrashIconLimitPosition.height() * translationYTimeRate;
                    trashIconRootView.setTranslationY(position);
                    sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
                } else {
                    trashIconRootView.setTranslationY(mTrashIconLimitPosition.bottom);
                    mStartedCode = ANIMATION_NONE;
                    if (listener != null) {
                        listener.onTrashAnimationEnd(ANIMATION_CLOSE);
                    }
                }
            }
            else if (animationCode == ANIMATION_FORCE_CLOSE) {
                backgroundView.setAlpha(0.0f);
                trashIconRootView.setTranslationY(mTrashIconLimitPosition.bottom);
                mStartedCode = ANIMATION_NONE;
                if (listener != null) {
                    listener.onTrashAnimationEnd(ANIMATION_FORCE_CLOSE);
                }
            }
        }catch(Exception ex)
        {

        }
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

        boolean isAnimationStarted(int animationCode) {
            return mStartedCode == animationCode;
        }


        void updateTargetPosition(float x, float y) {
            mTargetPositionX = x;
            mTargetPositionY = y;
        }

        void onUpdateViewLayout() {
            try{
            final BBTrashView trashView = mTrashView.get();
            if (trashView == null) {
                return;
            }
            final float density = trashView.mMetrics.density;
            final float backgroundHeight = trashView.mBackgroundView.getMeasuredHeight();
            final float offsetX = TRASH_MOVE_LIMIT_OFFSET_X * density;
            final int trashIconHeight = trashView.mTrashIconRootView.getMeasuredHeight();
            final int left = (int) -offsetX;
            final int top = (int) ((trashIconHeight - backgroundHeight) / 2 - TRASH_MOVE_LIMIT_TOP_OFFSET * density);
            final int right = (int) offsetX;
            final int bottom = trashIconHeight;
            mTrashIconLimitPosition.set(left, top, right, bottom);

            mMoveStickyYRange = backgroundHeight * 0.20f;
            }catch(Exception ex)
            {

            }
        }
    }
}
