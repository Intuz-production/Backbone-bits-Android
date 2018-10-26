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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backbonebits.permissionManagerViews.Permission;
import com.backbonebits.permissionManagerViews.PermissionManagerInstance;
import com.backbonebits.permissionManagerViews.PermissionManagerListener;
import com.backbonebits.requestresponse.BBGetRespondDetailRequestResponse;
import com.backbonebits.requestresponse.BBGetRespondRequestResponse;
import com.backbonebits.requestresponse.BBMessageCountRequestResponse;
import com.backbonebits.requestresponse.BBStatusMenuRequestResponse;
import com.backbonebits.service.BBChatHeadService;
import com.backbonebits.utils.BBCommon;
import com.backbonebits.webservices.BBWebServiceCaller;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.squareup.seismic.ShakeDetector;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Backbonebits extends Activity implements ShakeDetector.Listener, ServiceConnection {
    public static final String TAG = Backbonebits.class.getSimpleName();
    public static Context context;
    private boolean test_mode = false;
    private BBChatHeadService mBBChatHeadService;
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 10230;
    private final int ACCESS_PHONE_PERMISSION_REQUEST_CODE = 200;
    public static String from = "";
    private Dialog helpdialog;
    TextView notification_counter;

    private DisplayImageOptions options;
    private boolean isShake = true;
    private boolean mIsBound = false;
    LayoutInflater inflater;
    private static ArrayList<BBGetRespondRequestResponse.ResponderData> responderArray = new ArrayList<BBGetRespondRequestResponse.ResponderData>();
    private static ArrayList<BBGetRespondDetailRequestResponse.ResponderDetailData.ResponderDetailRequestData.TypeData> responderdetailArray = new ArrayList<>();
    private boolean isPermission = false;

    private static final int REQUEST_CODE = 10020;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static MediaProjection mMediaProjection;
    private static VirtualDisplay mVirtualDisplay;
    private static MediaProjectionCallback mMediaProjectionCallback;
    private static MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    private PermissionManagerInstance mPermissionManagerInstance;
    private String[] permissionArray = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO};
    private String[] permissionArray1 = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ShakeDetector shakeDetector = null;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public static BroadcastReceiver stopRecordingReciver;


    public Backbonebits(Context context) {
        Backbonebits.context = context;
        BBForeground.init(Backbonebits.this.getApplication());

        init(context);

    }

    public Backbonebits() {


    }

    BBForeground.Listener myListener = new BBForeground.Listener() {
        public void onBecameForeground() {
            Log.i("inForeground", "inForeground");
        }

        public void onBecameBackground() {
            Log.i("inBackground", "inBackground");
        }

    };

    private void ClearChatHead() {
        try {
            Log.e("isPermission>>", "isPermission>>" + isPermission);
            if (mBBChatHeadService != null) {
                if (mIsBound) {
                    context.unbindService(this);
                    mIsBound = false;
                }
                mBBChatHeadService.stopSelf();
            }

            if (!mIsBound) {
                context.bindService(new Intent(context, BBChatHeadService.class), this, Context.BIND_AUTO_CREATE);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        stopScreenSharing(0);
                    }
                }
            }, 2000);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void init(Context context1) {

        Log.e("init", "init");
        try {
            BBUtils.androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            BBUtils.setSharedPreString(context1, BBUtils.ANDROID_ID, BBUtils.androidID);

            BBForeground.get(context1.getApplicationContext()).addListener(myListener);

//            BBUtils.setBoolean(Backbonebits.context, BBUtils.IS_DIALOG_SHOWN, false);
            ApplicationInfo ai = Backbonebits.context.getPackageManager().getApplicationInfo(context1.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myAPIKey = bundle.getString(context.getResources().getString(R.string.api_key_bb));
            BBUtils.IS_LIVE = bundle.getBoolean("BB_Test_Mode");

            PackageManager manager = Backbonebits.context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context1.getPackageName(), 0);
            BBUtils.version = info.versionName;
            test_mode = bundle.getBoolean(context.getResources().getString(R.string.test_mode_bb));
            if (test_mode) {
            } else {
            }

            if (myAPIKey != null) {
                BBUtils.packageName = context1.getPackageName();
                BBUtils.Key = myAPIKey;
                mPermissionManagerInstance = new PermissionManagerInstance(context1);
            } else {
                Log.e("Backbonebits", "No BBAPIKey found in AndroidManifest.xml.Please re-check it again.");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }


    public void isShakeEnabled(boolean shake) {
        isShake = shake;
    }

    public static void getBBNotification(String message, Context context) {
        try {
            String msg[] = message.split("#");
            Intent intent = new Intent(context, BBPastRequestMessageDetail.class);
            intent.putExtra("request_id", msg[1]);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
            final PackageManager pm = context.getPackageManager();
            final ApplicationInfo applicationInfo = pm.getApplicationInfo(BBUtils.packageName, PackageManager.GET_META_DATA);
            final Resources resources = pm.getResourcesForApplication(applicationInfo);
            final int appIconResId = applicationInfo.icon;

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(appIconResId)
                    .setContentTitle(pm.getApplicationLabel(applicationInfo).toString())
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSound(defaultSoundUri);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // This method is called when this BroadcastReceiver receives an Intent broadcast.
            Log.v(TAG, "Stopping Recording");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopScreenSharing(0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        if (getIntent().getExtras() != null && getIntent().getStringExtra("openHelp") != null) {
            getStatusMenu(Backbonebits.this);
        } else if (getIntent().getExtras() != null && getIntent().getStringExtra("sendScreenshot") != null) {
            alertDialog(null, "Are you sure you want to send screenshot?", "screenshot", Backbonebits.this);
        } else if (getIntent().getExtras() != null && getIntent().getStringExtra("sendVideo") != null) {
            alertDialog(null, "Are you sure want to send video?", "video", Backbonebits.this);
        } else {
            getStatusMenu(Backbonebits.this);
        }

    }

    @Override
    protected void onResume() {
        Log.e("onResume", "resume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.e("onStop", "onStop");
        super.onStop();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        try {
            if (isPermission == true) {
                if (mBBChatHeadService != null) {
                    mBBChatHeadService.removeAllViews();

                }

                ClearChatHead();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setFlagforDialog(false);

    }

    public void openHelpDialog(int isVideo, int isImage, int isFaq) {
        try {

            helpdialog = new Dialog(Backbonebits.this, R.style.BBAppTheme);
            helpdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            helpdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            helpdialog.setCancelable(false);
            helpdialog.setContentView(R.layout.bb_help_dialog);

            LinearLayout lnr_watchVideo = helpdialog.findViewById(R.id.lnr_watchVideo);
            LinearLayout lnr_readFaq = helpdialog.findViewById(R.id.lnr_readFaq);
            LinearLayout lnr_helpScreens = helpdialog.findViewById(R.id.lnr_helpScreens);
            LinearLayout lnr_sendRequest = helpdialog.findViewById(R.id.lnr_sendRequest);
            LinearLayout lnr_sendScreenshots = helpdialog.findViewById(R.id.lnr_sendScreenshots);
            LinearLayout lnr_sendVideo = helpdialog.findViewById(R.id.lnr_sendVideo);
            LinearLayout lnr_pastRequest = helpdialog.findViewById(R.id.lnr_pastRequest);
            ImageView imgCloseBtn = helpdialog.findViewById(R.id.imgCloseBtn);

            // Check if we're running on Android 5.0 or higher
            if (Build.VERSION.SDK_INT >= 21) {
                lnr_sendVideo.setVisibility(View.VISIBLE);
            } else {
                lnr_sendVideo.setVisibility(View.GONE);
            }
            imgCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFlagforDialog(false);
                    if (helpdialog != null && helpdialog.isShowing()) {
                        helpdialog.dismiss();
                    }
                    finish();
                }
            });
            notification_counter = helpdialog.findViewById(R.id.notification_counter);
            if (BBUtils.notificationCount > 0) {
                notification_counter.setVisibility(View.VISIBLE);
                notification_counter.setText(String.valueOf(BBUtils.notificationCount));
            } else {
                notification_counter.setVisibility(View.GONE);

            }
            if (isVideo == 0) {
                lnr_watchVideo.setVisibility(View.GONE);
            }
            if (isImage == 0) {
                lnr_helpScreens.setVisibility(View.GONE);

            }
            if (isFaq == 0) {
                lnr_readFaq.setVisibility(View.GONE);

            }
            final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(TimeZone.getDefault());
            final String d = sdf.format(new Date());

            lnr_watchVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(Backbonebits.this, BBVideoHelperActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                    try {
                        if (BBUtils.getSharedPreString(Backbonebits.this, BBUtils.TODAYDATE).equalsIgnoreCase("") || BBUtils.getSharedPreString(Backbonebits.this, BBUtils.TODAYDATE) == null || sdf.parse(d).after(sdf.parse(BBUtils.getSharedPreString(Backbonebits.this, BBUtils.TODAYDATE)))) {
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            });

            lnr_readFaq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Backbonebits.this, BBFaqHelperActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                }
            });

            lnr_helpScreens.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Backbonebits.this, BBImageHelperActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);
                }
            });

            lnr_sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Backbonebits.this, BBNewRequestDialogActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                    if (helpdialog != null && helpdialog.isShowing()) {
                        helpdialog.dismiss();
                        setFlagforDialog(false);

                    }

                    BBCommon.requestFrom = 0;
                    setFlagforDialog(false);


                }
            });

            lnr_sendScreenshots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog(v, "Are you sure you want to send screenshot?", "screenshot", Backbonebits.this);

                }
            });

            lnr_sendVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog(v, "Are you sure want to send video?", "video", Backbonebits.this);
                }
            });

            lnr_pastRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Backbonebits.this, BBPastRequestActivity.class);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                    finish();

                }
            });

            if (helpdialog != null && !helpdialog.isShowing()) {
                helpdialog.show();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("in onActivityResult", "yes");
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {

                        if (from.equalsIgnoreCase("video")) {
                            mPermissionManagerInstance = new PermissionManagerInstance(this);
                            mPermissionManagerInstance.requestForPermissions(permissionArray, new PermissionManagerListener() {
                                @Override
                                public void permissionCallback(String[] permissions, Permission[] grantResults, boolean allGranted) {
                                    if (allGranted) {
                                        try {
                                            isPermission = false;
                                            onToggleScreenShare();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    } else {
                                        isPermission = true;
                                        Backbonebits.this.finish();
                                    }


                                }
                            });

                        } else {
                            mPermissionManagerInstance = new PermissionManagerInstance(this);
                            mPermissionManagerInstance.requestForPermissions(permissionArray1, new PermissionManagerListener() {
                                @Override
                                public void permissionCallback(String[] permissions, Permission[] grantResults, boolean allGranted) {
                                    if (allGranted) {
                                        try {
                                            isPermission = false;
                                            new startServiceAsync().execute();
                                            Backbonebits.this.finish();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    } else {
                                        isPermission = true;
                                        Backbonebits.this.finish();
                                    }


                                }
                            });

                        }


                    } else {
                        Backbonebits.this.finish();
                    }

                } else {
                    onToggleScreenShare();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Backbonebits.this.finish();
        }

        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {

            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();

            Backbonebits.this.finish();
            return;
        } else {
            isPermission = false;
            new startServiceAsync().execute();
            Backbonebits.this.finish();
        }


        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();

        try {
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alertDialog(final View view, String msg, final String type, final Activity ctx) {

        try {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Backbonebits.this);
            alertDialogBuilder.setMessage(msg);

            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(DialogInterface dialog, int arg1) {

                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    mScreenDensity = metrics.densityDpi;


                    if (type.equalsIgnoreCase("video")) {
                        from = "video";
                        if (helpdialog != null) {
                            helpdialog.dismiss();
                            setFlagforDialog(false);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mMediaRecorder = new MediaRecorder();
                            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                            if (!Settings.canDrawOverlays(context)) {
                                isPermission = true;
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                                startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
                            } else {
                                if (mPermissionManagerInstance == null) {
                                    mPermissionManagerInstance = new PermissionManagerInstance(context);
                                }
                                mPermissionManagerInstance.setShowMessageOnRationale(true);
                                mPermissionManagerInstance.setMessage("Required");
                                mPermissionManagerInstance.requestForPermissions(permissionArray, new PermissionManagerListener() {
                                    @Override
                                    public void permissionCallback(String[] permissions, Permission[] grantResults, boolean allGranted) {
                                        if (allGranted) {
                                            onToggleScreenShare();
                                        }
                                    }
                                });
                            }

                        } else {
                            isPermission = false;
                            onToggleScreenShare();
                        }

                    } else if (type.equalsIgnoreCase("screenshot")) {
                        from = "screenshot";

                        if (helpdialog != null) {
                            helpdialog.dismiss();
                            setFlagforDialog(false);

                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(context)) {
                                if (mPermissionManagerInstance == null) {
                                    mPermissionManagerInstance = new PermissionManagerInstance(context);

                                }
                                mPermissionManagerInstance.setShowMessageOnRationale(true);
                                mPermissionManagerInstance.setMessage("Required");
                                mPermissionManagerInstance.requestForPermissions(permissionArray, new PermissionManagerListener() {
                                    @Override
                                    public void permissionCallback(String[] permissions, Permission[] grantResults, boolean allGranted) {
                                        if (allGranted) {
                                            isPermission = false;
                                            new startServiceAsync().execute();
                                        }


                                    }
                                });

                            } else {
                                isPermission = true;
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                                startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
                            }
                        } else {
                            isPermission = false;
                            new startServiceAsync().execute();
                        }


                        if (isPermission == false) {
                            ctx.finish();
                            Backbonebits.this.finish();
                        }
                    }
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (view == null) {
                        Intent i = new Intent(Backbonebits.this, BBNewRequestDialogActivity.class);
                        startActivity(i);
                        finish();
                        overridePendingTransition(0, 0);
                    }

                }
            });

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.bb_blackcolor));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.bb_blackcolor));
                }
            });
            if (alertDialog != null && !alertDialog.isShowing())
                alertDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void takeScreenshot() {
        try {
            // We are not in Unit test sot teh app
            File screenshotFile = getScreenshotFile();
            BBScreenShot.takeScreenshot((Activity) context, screenshotFile);
            Intent intent = new Intent(context, BBScreenShotEditActivity.class);
            intent.putExtra("path", screenshotFile.getAbsolutePath());
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(0, 0);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected static File getScreenshotFile() {
        File screenshotDirectory;
        String screenshotName;
        try {
            screenshotDirectory = getScreenshotsDirectory(context.getApplicationContext());


            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS", Locale.getDefault());

            screenshotName = dateFormat.format(new Date()) + ".png";
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return new File(screenshotDirectory, screenshotName);
    }

    private static File getScreenshotsDirectory(Context context) throws IllegalAccessException {
        File directory = null;
        try {
            String dirName = "screenshots_" + context.getPackageName();

            File rootDir;

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                rootDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            } else {
                rootDir = context.getDir("screens", MODE_PRIVATE);
            }

            directory = new File(rootDir, dirName);

            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IllegalAccessException("Unable to create screenshot directory " + directory.getAbsolutePath());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return directory;
    }

    public void getStatusMenu(final Context context) {
        try {
            if (!BBUtils.isNetworkAvailable(Backbonebits.this)) {

                BBUtils.showToast(Backbonebits.this, context.getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(Backbonebits.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBStatusMenuRequestResponse> call = service.getStatusMenu(BBUtils.Key, BBUtils.packageName);
                call.enqueue(new Callback<BBStatusMenuRequestResponse>() {
                    @Override
                    public void onResponse(Call<BBStatusMenuRequestResponse> call, Response<BBStatusMenuRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {

                            BBStatusMenuRequestResponse result = response.body();
                            if (result.getStatus() == 2) {
                                Toast.makeText(Backbonebits.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                setFlagforDialog(false);
                                Backbonebits.this.finish();
                            } else if (result.getStatus() == 0 && result.getMsg() != null && !result.getMsg().equalsIgnoreCase("")) {
                                Toast.makeText(Backbonebits.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                Backbonebits.this.finish();
                                setFlagforDialog(false);
                            } else {
                                BBUtils.notificationCount = result.getMessageCount();
                                openHelpDialog(result.getVideo().getStatus(), result.getImage().getStatus(), result.getFaq().getStatus());
                            }
                        } else {
                            BBUtils.hideProgress();
                            setFlagforDialog(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<BBStatusMenuRequestResponse> call, Throwable t) {
                        t.printStackTrace();
                        Log.v("onFailure", "onFailure");
                        Backbonebits.this.finish();
                        BBUtils.showToast(Backbonebits.this, context.getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                        setFlagforDialog(false);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFlagforDialog(boolean isShow) {
        BBUtils.setBoolean(Backbonebits.context, BBUtils.IS_DIALOG_SHOWN, isShow);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            mBBChatHeadService = ((BBChatHeadService.ChatHeadServiceBinder) service).getService();
            if (mBBChatHeadService != null) {
                context.unbindService(this);
                mBBChatHeadService.stopSelf();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBBChatHeadService = null;
    }

    @SuppressLint("NewApi")
    private boolean showChatHead(Context context) {

        Log.e("in showChatHead", "showChatHead");
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                isPermission = true;
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);


            } else {
                if (checkAndRequestPermissions()) {
                    try {
                        isPermission = false;
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            try {
                                if (!from.equalsIgnoreCase("video")) {
                                    isPermission = false;

                                    new startServiceAsync().execute();

                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return true;
                        } else {
                            if (Settings.canDrawOverlays(context)) {
                                if (!from.equalsIgnoreCase("video")) {
                                    isPermission = false;

                                    new startServiceAsync().execute();

                                }
                                return true;
                            }
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    isPermission = true;
                }

            }
        } else {
            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    try {
                        if (!from.equalsIgnoreCase("video")) {
                            isPermission = false;

                            new startServiceAsync().execute();

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }

                if (Settings.canDrawOverlays(context)) {
                    if (!from.equalsIgnoreCase("video")) {
                        isPermission = false;

                        new startServiceAsync().execute();

                    }
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        return false;
    }

    @Override
    public void hearShake() {
        if (isShake) {
            try {
                Log.i(TAG, "handle shake");
                if (!BBUtils.getBoolean(Backbonebits.context, BBUtils.IS_DIALOG_SHOWN) && BBForeground.instance.isForeground()) {
                    Backbonebits.context = context;
                    setFlagforDialog(true);
                    SensorManager sensorManager = (SensorManager) Backbonebits.context.getSystemService(SENSOR_SERVICE);
                    if (shakeDetector == null) {
                        shakeDetector = new ShakeDetector(this);
                        shakeDetector.stop();
                        shakeDetector.start(sensorManager);
                    }
                    init(context);
                    Intent i = new Intent(context, Backbonebits.class);
                    context.startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);
                } else {
                    setFlagforDialog(false);
                }
            } catch (Exception ex) {
                setFlagforDialog(false);
            }
        }
    }

    private class startServiceAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            context.startService(new Intent(context, BBChatHeadService.class));


            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Log.e("onBackPressed", "onBackPressed");
        if (helpdialog != null && helpdialog.isShowing()) {
            helpdialog.dismiss();
            setFlagforDialog(false);
        }
        super.onBackPressed();
    }


    public void initializeBBSDK(final Context context) {
        SensorManager sensorManager = (SensorManager) Backbonebits.context.getSystemService(SENSOR_SERVICE);
        if (shakeDetector == null) {
            shakeDetector = new ShakeDetector(this);
            shakeDetector.stop();
            shakeDetector.start(sensorManager);
        }

        Backbonebits.context = context;
        init(context);

    }

    public void openBBHelper() {
        SensorManager sensorManager = (SensorManager) Backbonebits.context.getSystemService(SENSOR_SERVICE);
        if (shakeDetector == null) {
            shakeDetector = new ShakeDetector(this);
            shakeDetector.stop();
            shakeDetector.start(sensorManager);
        }
        Backbonebits.context = context;
        init(context);

        Intent i = new Intent(context, Backbonebits.class);
        context.startActivity(i);
        finish();
        ((Activity) context).overridePendingTransition(0, 0);
    }

    private boolean checkAndRequestPermissions() {
        int storagePermission = ContextCompat.checkSelfPermission(Backbonebits.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            isPermission = true;
            Log.e("requestPermissions", "requestPermissions");
            ActivityCompat.requestPermissions(Backbonebits.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), ACCESS_PHONE_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.e("onRequestPermsionsrslt", "onRequestPermissionsResult");
        if (requestCode == ACCESS_PHONE_PERMISSION_REQUEST_CODE) {
            Log.e("onRequestPermsionsrslt1", "onRequestPermissionsResult");
            if (checkAndRequestPermissions()) {
                try {
                    try {
                        isPermission = false;
                        new startServiceAsync().execute();
                        Backbonebits.this.finish();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (requestCode == REQUEST_PERMISSIONS) {

                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);

                                }
                            }).show();
                }
                return;
            }
        }
    }


    public static int getUnreadCount() {
        final int[] count = {0};
        try {

            if (BBUtils.isNetworkAvailable(context)) {
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();
                Call<BBMessageCountRequestResponse> call = service.getMessageCount(BBUtils.androidID);
                call.enqueue(new Callback<BBMessageCountRequestResponse>() {
                    @Override
                    public void onResponse(Call<BBMessageCountRequestResponse> call, Response<BBMessageCountRequestResponse> response) {
                        if (response.isSuccessful()) {
                            BBMessageCountRequestResponse result = response.body();
                            if (result.getStatus() == 1) {
                                count[0] = result.getTotal_unread_count();
                            }

                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<BBMessageCountRequestResponse> call, Throwable t) {
                    }
                });
            }
            return count[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count[0];
    }

    public static ArrayList<BBGetRespondDetailRequestResponse.ResponderDetailData.ResponderDetailRequestData.TypeData> openPastReportWithRequestId(String request_id) {
        try {
            if (!BBUtils.isNetworkAvailable(context)) {
                BBUtils.showToast(context, context.getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(context);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBGetRespondDetailRequestResponse> call = service.getRespondDetail(BBUtils.Key, BBUtils.packageName, String.valueOf(request_id), "listdetail");
                call.enqueue(new Callback<BBGetRespondDetailRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBGetRespondDetailRequestResponse> call, Response<BBGetRespondDetailRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBGetRespondDetailRequestResponse result = response.body();
                            if (result.getStatus() == 1) {


                                if (result.getData().getRequest_data().size() > 0) {
                                    responderdetailArray.clear();
                                    for (String key : result.getData().getRequest_data().keySet()) {
                                        System.out.println(key);

                                        for (int j = 0; j < result.getData().getRequest_data().get(key).size(); j++) {

                                            responderdetailArray.add(result.getData().getRequest_data().get(key).get(j));
                                        }


                                    }

                                }


                            } else {

                                Toast.makeText(context, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                            if (result.getStatus() == 1) {


                            } else {
                            }

                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBGetRespondDetailRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(context, context.getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responderdetailArray;
    }

    public static List<BBGetRespondRequestResponse.ResponderData> openPastReports() {
        try {
            if (!BBUtils.isNetworkAvailable(context)) {
                BBUtils.showToast(context, context.getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(context);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBGetRespondRequestResponse> call = service.getRespondData(BBUtils.Key, BBUtils.packageName, "list", BBUtils.androidID);
                call.enqueue(new Callback<BBGetRespondRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBGetRespondRequestResponse> call, Response<BBGetRespondRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBGetRespondRequestResponse result = response.body();
                            if (result.getStatus() == 1) {

                                if (result.getData().size() > 0) {

                                    responderArray.clear();
                                    for (int i = 0; i < result.getData().size(); i++) {
                                        responderArray.add(result.getData().get(i));
                                    }

                                }

                            } else {

                                Toast.makeText(context, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBGetRespondRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(context, context.getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responderArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onToggleScreenShare() {
        initRecorder();
        shareScreen();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        try {

            if (mMediaProjection == null) {
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
                return;
            }
            mVirtualDisplay = createVirtualDisplay();
            BBUtils.setBoolean(context, BBUtils.RECORDING_IS_RINUUNG, true);

            mMediaRecorder.start();


        } catch (Exception ex)

        {
            BBUtils.showToast(this, "Recording will not be saved...");
            ex.printStackTrace();
            if (mMediaProjection != null) {
                mMediaProjection.unregisterCallback(mMediaProjectionCallback);
                mMediaProjection.stop();
                mMediaProjection = null;
                stopScreenSharing(1);
                Backbonebits.this.finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {

        // Get the display size and density.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        int screenDensity = metrics.densityDpi;

//        // Start the video input.
//        mMediaProjection.createVirtualDisplay("Recording Display", screenWidth,
//                screenHeight, screenDensity, 0 /* flags */, mInputSurface,
//                null /* callback */, null /* handler */);

        return mMediaProjection.createVirtualDisplay("MainActivity",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            String state = Environment.getExternalStorageState();
            File file;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                file = new File(Environment.getExternalStorageDirectory().toString() + "/" + "Backbonebits/Video");
            } else {
                file = new File(context.getFilesDir(), "/Backbonebits/Video");
            }
            if (!file.exists()) {
                file.mkdirs();
            }
            File VideoPath = new File(file + "/video.mp4");
            BBCommon.strVideoPath = VideoPath.getAbsolutePath();
            mMediaRecorder.setOutputFile(BBCommon.strVideoPath);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;


            mMediaRecorder.setVideoSize(width, height);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mMediaRecorder.setVideoEncodingBitRate(512 * 10000);//For video quality
            mMediaRecorder.setVideoEncodingBitRate(2500000);//For video quality
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void stopScreenSharing(int from) {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }

            if (mVirtualDisplay == null) {
                return;
            }

            //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
            // be reused again
            BBUtils.setBoolean(context, BBUtils.RECORDING_IS_RINUUNG, false);

            destroyMediaProjection();


            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }

            try {
                if (from == 0) {
                    File dir = new File(BBCommon.strPathForVideos);
                    File VideoPath = new File(dir + "/video.mp4");
                    BBCommon.strVideoPath = VideoPath.getAbsolutePath();
                    BBCommon.requestFrom = 2;
                    BBUtils.hideProgress();
                    Intent i = new Intent(Backbonebits.context, BBNewRequestDialogActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Backbonebits.context.startActivity(i);
                    // overridePendingTransition(0, 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

}

