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

package com.backbonebits.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.backbonebits.BBUtils;
import com.backbonebits.Backbonebits;
import com.backbonebits.R;
import com.backbonebits.circularprogress.BBMasterLayout;
import com.backbonebits.floatinglib.BBFloatingViewListener;
import com.backbonebits.floatinglib.BBFloatingViewManagerBB;
import com.backbonebits.utils.BBCommon;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.backbonebits.utils.BBCommon.strPathForImages;


public class BBChatHeadService extends Service implements BBFloatingViewListener {
    private boolean isTimeCompleted = false;
    private static final String TAG = "BBChatHeadService";
    private IBinder mChatHeadServiceBinder;
    public static BBFloatingViewManagerBB mBBFloatingViewManager;
    public static BBMasterLayout BBMasterLayout;
    Timer timer;
    public int ScreenshotCounter = 0;
    public int ScreenshotTimerCounter = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            if (mBBFloatingViewManager != null) {
                return START_STICKY;
            }
            final DisplayMetrics metrics = new DisplayMetrics();
            final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            mChatHeadServiceBinder = new ChatHeadServiceBinder(this);
            final LayoutInflater inflater = LayoutInflater.from(this);
            Display display = windowManager.getDefaultDisplay();
            int realWidth;
            int realHeight;

            if (Build.VERSION.SDK_INT >= 17) {
                //new pleasant way to get real metrics
                DisplayMetrics realMetrics = new DisplayMetrics();
                display.getRealMetrics(realMetrics);
                realWidth = realMetrics.widthPixels;
                realHeight = realMetrics.heightPixels;

            } else if (Build.VERSION.SDK_INT >= 14) {
                try {
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    realWidth = (Integer) mGetRawW.invoke(display);
                    realHeight = (Integer) mGetRawH.invoke(display);
                } catch (Exception e) {
                    //this may not be 100% accurate, but it's all we've got
                    realWidth = display.getWidth();
                    realHeight = display.getHeight();
                }

            } else {
                realWidth = display.getWidth();
                realHeight = display.getHeight();
            }
            if (Backbonebits.from.equalsIgnoreCase("video")) {
                BBMasterLayout = (BBMasterLayout) inflater.inflate(R.layout.bb_circular_chathead, null, false);

                BBMasterLayout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.e("onClick", "onClick");
                        if (timer != null) {
                            timer.cancel();
                        }
                        ScreenshotCounter = 0;
                        ScreenshotTimerCounter = 0;
                        removeAllViews();
                        isTimeCompleted = true;
                        Intent local = new Intent();
                        Log.e(TAG, "send broadcast");
                        local.setAction("com.backbonebits.action.stoprecording");
                        sendBroadcast(local);
                    }
                });

                BBMasterLayout.animation(); //Need to call this method for animation and progression
                if (BBMasterLayout.flg_frmwrk_mode == 1) {
                    BBCommon.requestFrom = 2;
                    new DownLoadSigTask().execute();
                } else if (BBMasterLayout.flg_frmwrk_mode == 2) {
                    //Running state. Call any method that you want to execute
                    BBMasterLayout.reset();
                    try {

                        if (timer != null) {
                            timer.cancel();
                        }
                        ScreenshotCounter = 0;
                        ScreenshotTimerCounter = 0;
                        stopRecording();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new DownLoadSigTask().cancel(true);
                    ScreenshotCounter = 0;
                    ScreenshotTimerCounter = 0;
                    if (mBBFloatingViewManager != null) {

                        mBBFloatingViewManager.removeAllViewToWindow();
                        mBBFloatingViewManager = null;
                    }
                } else if (BBMasterLayout.flg_frmwrk_mode == 3) {
                    try {
                        if (isTimeCompleted == false) {
                            if (timer != null) {
                                timer.cancel();
                            }
                            ScreenshotCounter = 0;
                            ScreenshotTimerCounter = 0;
                            isTimeCompleted = true;
                            stopRecording();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //}
                    if (mBBFloatingViewManager != null) {


                        mBBFloatingViewManager.removeAllViewToWindow();
                        mBBFloatingViewManager = null;
                    }
                }


                mBBFloatingViewManager = new BBFloatingViewManagerBB(this, this);
                mBBFloatingViewManager.setTrashViewEnabled(false);
                mBBFloatingViewManager.setDisplayMode(BBFloatingViewManagerBB.DISPLAY_MODE_SHOW_ALWAYS);

//            mBBFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed_bb);
//            mBBFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action_bb);
                final BBFloatingViewManagerBB.Options options = new BBFloatingViewManagerBB.Options();
                options.shape = BBFloatingViewManagerBB.SHAPE_RECTANGLE;
                //  options.overMargin = (int) (16 * metrics.density);
                options.floatingViewY = -realHeight;
                options.floatingViewX = (realWidth / 2) - 100;
                mBBFloatingViewManager.addViewToWindow(BBMasterLayout, options);


            } else if (Backbonebits.from.equalsIgnoreCase("screenshot")) {
                final ImageView iconView = (ImageView) inflater.inflate(R.layout.bb_widget_chathead, null, false);
                iconView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBBFloatingViewManager != null) {
                            mBBFloatingViewManager.removeAllViewToWindow();
                            mBBFloatingViewManager = null;
                        }
                        if (Backbonebits.from.equalsIgnoreCase("screenshot")) {
                            if(getFrontAppPackageName(Backbonebits.context))
                            {
                                Backbonebits.takeScreenshot();

                            }
                            else
                            {

                                BBUtils.showToast(Backbonebits.context,"You are not authorized to take screenshots of other apps.");
                            }
                        } else if (Backbonebits.from.equalsIgnoreCase("video")) {


                        }
                    }
                });

                mBBFloatingViewManager = new BBFloatingViewManagerBB(this, this);
                mBBFloatingViewManager.setTrashViewEnabled(false);

                mBBFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed_bb);
                mBBFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action_bb);
                final BBFloatingViewManagerBB.Options options = new BBFloatingViewManagerBB.Options();
                options.shape = BBFloatingViewManagerBB.SHAPE_CIRCLE;
                //options.overMargin = (int) (16 * metrics.density);
                options.floatingViewY = -realHeight;
                options.floatingViewX = (realWidth / 2) - 100;
                mBBFloatingViewManager.addViewToWindow(iconView, options);
            }


            //startForeground(NOTIFICATION_ID, createNotification());

        } catch (Exception ex) {

        }
        return START_REDELIVER_INTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mChatHeadServiceBinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }


    private void destroy() {
        if (mBBFloatingViewManager != null) {
            mBBFloatingViewManager.removeAllViewToWindow();
            mBBFloatingViewManager = null;
        }
    }


    public static class ChatHeadServiceBinder extends Binder {

        /*
         FloatingRateService
         */
        private final WeakReference<BBChatHeadService> mService;

        /*
          @param service BBChatHeadService
         */
        ChatHeadServiceBinder(BBChatHeadService service) {
            mService = new WeakReference<>(service);
        }

        /**
         * @return BBChatHeadService
         */
        public BBChatHeadService getService() {
            return mService.get();
        }
    }

    class DownLoadSigTask extends AsyncTask<String, Integer, String> {


        @Override
        protected void onPreExecute() {
            File dir = new File(strPathForImages);
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }

        }


        @Override
        protected String doInBackground(final String... args) {

            try {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (ScreenshotTimerCounter > 10000) {

                            stopRecording();
                        }
                        ScreenshotTimerCounter = ScreenshotTimerCounter + 500;
                        ScreenshotCounter++;
                        publishProgress(ScreenshotTimerCounter / 100);
                    }
                };

                timer = new Timer();
                timer.schedule(timerTask, 0, 500);


            } catch (Exception ex) {

            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... progress) {

            //publishing progress to progress arc
            BBMasterLayout.cusview.setupprogress(progress[0]);
        }


    }

    public void removeAllViews() {
        if (mBBFloatingViewManager != null) {
            mBBFloatingViewManager.removeAllViewToWindow();
            mBBFloatingViewManager = null;
        }
    }

    private void stopRecording() {
        if (timer != null) {
            timer.cancel();
        }
        ScreenshotCounter = 0;
        ScreenshotTimerCounter = 0;
        removeAllViews();
        isTimeCompleted = true;
        Intent local = new Intent();
        local.setAction("com.backbonebits.action.stoprecording");
        sendBroadcast(local);
    }

    public  boolean getFrontAppPackageName(Context mContext) {
        String appname = mContext.getPackageName();

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = "";
        if (am != null) {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

                packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();

        }
        return appname.equalsIgnoreCase(packageName);
    }
}
