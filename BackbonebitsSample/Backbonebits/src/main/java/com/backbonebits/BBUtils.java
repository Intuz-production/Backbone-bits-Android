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
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;


public class BBUtils {
    //public static final String BASE_URL = "Your_Server_Url"; // Please put your server url here.
    public static final String BASE_URL = "http://clientveb.net/inhouse/backbone/backbonebits_web/services/";

    public static String Key;
    public static String packageName;
    public static String version;
    public static int notificationCount;
    public static final String PREFS_NAME = "MyPrefs";
    private static Dialog popupWindow;
    public static String androidID = "";
    public static String ID = "id";
    public static double lat = 0, lon = 0;
    public static String NAME = "name";
    public static String EMAIL = "email";
    public static String AppVersion = "appversion";
    public static String TODAYDATE = "todaydate";
    public static String ANDROID_ID = "android_id";
    public static String GCM_ID = "gcm_id";
    public static String IS_DIALOG_SHOWN = "is_dialog_shown";
    public static boolean IS_LIVE = false;
    public static String RECORDING_IS_RINUUNG = "RECORDING_IS_RINUUNG";


    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo = null;
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        } catch (Exception ex) {

        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void showToast(Context context, String message) {
        try {
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, getPixelsFromDp(context, 70));
            toast.show();
        } catch (Exception ex) {

        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        float scale = 0;
        try {
            scale = context.getResources().getDisplayMetrics().density;
        } catch (Exception ex) {

        }
        return (int) (dp * scale + 0.5f);
    }

    public static void showProgress(final Context context) {
        try {
            if (!((Activity) context).isFinishing()) {
                View layout = LayoutInflater.from(context).inflate(R.layout.bb_popup_loading, null);
                popupWindow = new Dialog(context, android.R.style.Theme_Translucent);
                popupWindow.requestWindowFeature(Window.FEATURE_NO_TITLE);
                popupWindow.setContentView(layout);
                popupWindow.setCancelable(false);
                if (!((Activity) context).isFinishing()) {
                    popupWindow.show();
                }


            }

        } catch (Exception e)

        {
            e.printStackTrace();
        }
    }


    public static void hideProgress() {
        try {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static BitmapDrawable createCheckerBoard(Resources res, int size) {
        BitmapDrawable drawable = null;
        try {
            size *= res.getDisplayMetrics().density;

            BitmapShader shader = new BitmapShader(Bitmap.createBitmap(new int[]{
                    0xFFFFFFFF, 0xFFCCCCCC, 0xFFCCCCCC, 0xFFFFFFFF}, 2, 2, Bitmap.Config.RGB_565),
                    BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
            Matrix matrix = new Matrix();
            matrix.setScale(size, size);
            shader.setLocalMatrix(matrix);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(shader);

            Bitmap bm2 = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.RGB_565);
            new Canvas(bm2).drawPaint(paint);

            drawable = new BitmapDrawable(res, bm2);
            drawable.setTileModeXY(BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
        } catch (Exception ex) {

        }
        return drawable;
    }

    public static void setSharedPreString(Context context, String key, String text) {
        try {
            SharedPreferences settings;
            SharedPreferences.Editor editor;
            settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            editor.putString(key, text);
            editor.commit();
        } catch (Exception ex) {

        }
    }

    public static String getSharedPreString(Context context, String key) {
        String text = null;
        try {
            SharedPreferences settings;

            settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            text = settings.getString(key, "");
        } catch (Exception ex) {

        }
        return text;
    }

    public static void setBoolean(Context context, String key, boolean text) {
        try {
            SharedPreferences settings;
            SharedPreferences.Editor editor;
            settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            editor.putBoolean(key, text);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean getBoolean(Context context, String key) {
        boolean text = false;
        try {
            SharedPreferences settings;

            settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            text = settings.getBoolean(key, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return text;
    }

    public static boolean isExistKeyinPref(Context context, String key) {
        SharedPreferences pref;
        boolean isexist = false;
        try {
            pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            isexist = pref.contains(key);
        } catch (Exception ex) {

        }
        return isexist;
    }

    public static void removeSharedPref(Context context) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = settings.edit();

            editor.commit();
        } catch (Exception ex) {

        }

    }

    public static RequestBody imageToBody(String text) {
        RequestBody requestBody = null;
        try {

            if (text != null && text.length() > 0) {
                MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
                File file = new File(text);
                requestBody = RequestBody.create(MEDIA_TYPE_PNG, file);
            } else {
                requestBody = null;
            }
        } catch (Exception ex) {

        }
        return requestBody;
    }

    public static RequestBody textToBody(String text) {
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MediaType.parse("text/plain"), text);
        } catch (Exception ex) {

        }
        return requestBody;
    }

    public final static boolean isValidEmail(String email) {
        if (email.trim().length() == 0)
            return false;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static int pxToDp(int px, Context ctx) {
        int dp = 0;
        try {
            DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
            dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        } catch (Exception ex) {

        }
        return dp;
    }

    public static String getLocalTime(String dateStr) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df.setTimeZone(TimeZone.getDefault());
        String formattedDate = df.format(date);
        return formattedDate;
    }

}
