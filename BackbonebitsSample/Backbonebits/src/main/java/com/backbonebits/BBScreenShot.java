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

package com.backbonebits;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;


/**
 * BBUtility class to take screenshots of activity screen
 */
public class BBScreenShot {
    //region Constants

    private static final String TAG = "BBScreenShot";

    //endregion

    //region Public API

    /**
     * Takes screenshot of provided activity and saves it to provided file.
     * File content will be overwritten if there is already some content.
     *
     * @param activity Activity of which the screenshot will be taken.
     * @param toFile   File where the screenshot will be saved.
     *                 If there is some content it will be overwritten
     * @throws UnableToTakeScreenshotException When there is unexpected error during taking screenshot
     */
    public static void takeScreenshot(Activity activity, final File toFile) {
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity cannot be null.");
        }

        if (toFile == null) {
            throw new IllegalArgumentException("Parameter toFile cannot be null.");
        }

        Bitmap bitmap = null;
        try {
            bitmap = takeBitmapUnchecked(activity);
            writeBitmap(bitmap, toFile);
        } catch (Exception e) {
            String message = "Unable to take screenshot to file " + toFile.getAbsolutePath()
                    + " of activity " + activity.getClass().getName();

            Log.e(TAG, message, e);
            throw new UnableToTakeScreenshotException(message, e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        Log.d(TAG, "Screenshot captured to " + toFile.getAbsolutePath());
    }

    /**
     * Takes screenshot of provided activity and puts it into bitmap.
     *
     * @param activity Activity of which the screenshot will be taken.
     * @return Bitmap of what is displayed in activity.
     * @throws UnableToTakeScreenshotException When there is unexpected error during taking screenshot
     */
    public static Bitmap takeScreenshotBitmap(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity cannot be null.");
        }

        try {
            return takeBitmapUnchecked(activity);
        } catch (Exception e) {
            String message = "Unable to take screenshot to bitmap of activity "
                    + activity.getClass().getName();

            Log.e(TAG, message, e);
            throw new UnableToTakeScreenshotException(message, e);
        }
    }


    //endregion

    //region Methods

    private static Bitmap takeBitmapUnchecked(Activity activity) throws InterruptedException {

            View main = activity.getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        main.setSystemUiVisibility(uiOptions);

        final List<ViewRootData> viewRoots = getRootViews(activity);

        final Bitmap bitmap = Bitmap.createBitmap(main.getWidth(), main.getHeight(), ARGB_8888);

        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                drawRootsToBitmap(viewRoots, bitmap);
            } else {
                final CountDownLatch latch = new CountDownLatch(1);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            drawRootsToBitmap(viewRoots, bitmap);
                        } finally {
                            latch.countDown();
                        }
                    }
                });

                latch.await();
            }
        } catch (Exception ex) {

        }
        return bitmap;
    }

    private static void drawRootsToBitmap(List<ViewRootData> viewRoots, Bitmap bitmap) {
        try {
            for (ViewRootData rootData : viewRoots) {
                drawRootToBitmap(rootData, bitmap);
            }
        } catch (Exception ex) {

        }
    }

    private static void drawRootToBitmap(ViewRootData config, Bitmap bitmap) {
        try {
            // now only dim supported
            if ((config._layoutParams.flags & FLAG_DIM_BEHIND) == FLAG_DIM_BEHIND) {
                Canvas dimCanvas = new Canvas(bitmap);

                int alpha = (int) (255 * config._layoutParams.dimAmount);
                dimCanvas.drawARGB(alpha, 0, 0, 0);
            }

            Canvas canvas = new Canvas(bitmap);
            canvas.translate(config._winFrame.left, config._winFrame.top);
            config._view.draw(canvas);
        } catch (Exception ex) {

        }
    }

    private static void writeBitmap(Bitmap bitmap, File toFile) throws IOException {
        try {
            OutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(toFile));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception ex) {

        }
    }

    @SuppressWarnings("unchecked") // no way to check
    private static List<ViewRootData> getRootViews(Activity activity) {
        List<ViewRootData> rootViews = new ArrayList<>();
        try {
            Object globalWindowManager;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                globalWindowManager = getFieldValue("mWindowManager", activity.getWindowManager());
            } else {
                globalWindowManager = getFieldValue("mGlobal", activity.getWindowManager());
            }
            Object rootObjects = getFieldValue("mRoots", globalWindowManager);
            Object paramsObject = getFieldValue("mParams", globalWindowManager);

            Object[] roots;
            WindowManager.LayoutParams[] params;

            //  There was a change to ArrayList implementation in 4.4
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                roots = ((List) rootObjects).toArray();

                List<WindowManager.LayoutParams> paramsList = (List<WindowManager.LayoutParams>) paramsObject;
                params = paramsList.toArray(new WindowManager.LayoutParams[paramsList.size()]);
            } else {
                roots = (Object[]) rootObjects;
                params = (WindowManager.LayoutParams[]) paramsObject;
            }

            for (int i = 0; i < roots.length; i++) {
                Object root = roots[i];

                Object attachInfo = getFieldValue("mAttachInfo", root);
                int top = (int) getFieldValue("mWindowTop", attachInfo);
                int left = (int) getFieldValue("mWindowLeft", attachInfo);

                Rect winFrame = (Rect) getFieldValue("mWinFrame", root);
                Rect area = new Rect(left, top, left + winFrame.width(), top + winFrame.height());

                View view = (View) getFieldValue("mView", root);
                rootViews.add(new ViewRootData(view, area, params[i]));
            }
        } catch (Exception ex) {

        }
        return rootViews;
    }

    private static Object getFieldValue(String fieldName, Object target) {
        try {
            return getFieldValueUnchecked(fieldName, target);
        } catch (Exception e) {
            throw new UnableToTakeScreenshotException(e);
        }
    }

    private static Object getFieldValueUnchecked(String fieldName, Object target)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(fieldName, target.getClass());

        field.setAccessible(true);
        return field.get(target);
    }

    private static Field findField(String name, Class clazz) throws NoSuchFieldException {
        Class currentClass = clazz;
        while (currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        throw new NoSuchFieldException("Field " + name + " not found for class " + clazz);
    }

    //endregion

    //region Constructors

    // No instances
    private BBScreenShot() {
    }

    //endregion

    //region Nested classes

    /**
     * Custom exception thrown if there is some exception thrown during
     * screenshot capturing to enable better client code exception handling.
     */
    public static class UnableToTakeScreenshotException extends RuntimeException {
        private UnableToTakeScreenshotException(String detailMessage, Exception exception) {
            super(detailMessage, extractException(exception));
        }

        public UnableToTakeScreenshotException(Exception ex) {
            super(extractException(ex));
        }

        /**
         * Method to avoid multiple wrapping. If there is already our exception,
         * just wrap the cause again
         */
        private static Throwable extractException(Exception ex) {
            if (ex instanceof UnableToTakeScreenshotException) {
                return ex.getCause();
            }

            return ex;
        }
    }

    private static class ViewRootData {
        private final View _view;
        private final Rect _winFrame;
        private final WindowManager.LayoutParams _layoutParams;

        public ViewRootData(View view, Rect winFrame, WindowManager.LayoutParams layoutParams) {
            _view = view;
            _winFrame = winFrame;
            _layoutParams = layoutParams;
        }
    }

    //endregion
}
