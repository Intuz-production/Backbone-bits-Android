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

package com.backbonebits.fcm;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.backbonebits.BBPastRequestActivity;
import com.backbonebits.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationManager mNotificationManager;
    public static int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                String message = remoteMessage.getData().get("bb_message");
                sendNotification(message);

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void sendNotification(String message) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "Backbonebit";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Intent intent = new Intent(this, BBPastRequestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(true);
        mBuilder.setContentTitle(getString(R.string.app_name));
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setContentText(message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setColor(getResources().getColor(R.color.bb_lightbluetextclor));
            mBuilder.setSmallIcon(R.drawable.logo_bb);
        } else {
            mBuilder.setSmallIcon(R.drawable.logo_bb);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = mNotificationManager != null ? mNotificationManager.getNotificationChannel(String.valueOf(CHANNEL_ID)) : null;
            if (mChannel == null) {
                mChannel = new NotificationChannel(String.valueOf(CHANNEL_ID), "Backbone", NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        NOTIFICATION_ID++;
    }

}