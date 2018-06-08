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
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;


class BBFullscreenObserverView extends View implements ViewTreeObserver.OnGlobalLayoutListener, View.OnSystemUiVisibilityChangeListener {


    private final WindowManager.LayoutParams mParams;


    private final BBScreenChangedListener mBBScreenChangedListener;


    private int mLastUiVisibility;


    private final Rect mWindowRect;



    BBFullscreenObserverView(Context context, BBScreenChangedListener listener) {
        super(context);

        mBBScreenChangedListener = listener;

        mParams = new WindowManager.LayoutParams();
        mParams.width = 1;
        mParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;

        mWindowRect = new Rect();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        setOnSystemUiVisibilityChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        try{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
        setOnSystemUiVisibilityChangeListener(null);
        }catch(Exception ex)
        {

        }
        super.onDetachedFromWindow();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGlobalLayout() {
        try{
        if (mBBScreenChangedListener != null) {
            //getWindowVisibleDisplayFrame(mWindowRect);
            //mBBScreenChangedListener.onScreenChanged(mLastUiVisibility != View.SYSTEM_UI_FLAG_VISIBLE || mWindowRect.top == 0);
        }
        }catch(Exception ex)
        {

        }
    }


    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        try{
        mLastUiVisibility = visibility;
        if (mBBScreenChangedListener != null) {
            getWindowVisibleDisplayFrame(mWindowRect);
            mBBScreenChangedListener.onScreenChanged(mLastUiVisibility != View.SYSTEM_UI_FLAG_VISIBLE || mWindowRect.top == 0);
        }
        }catch(Exception ex)
        {

        }
    }


    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }
}