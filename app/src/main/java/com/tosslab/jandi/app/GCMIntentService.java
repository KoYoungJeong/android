package com.tosslab.jandi.app;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 9..
 */
public class GCMIntentService extends IntentService {
    private final Logger log = Logger.getLogger(GCMIntentService.class);
//    static final String TAG = "Jandi CGM Service";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), -1, -1);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString(), -1, -1);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String lastMessage = extras.getString("lastMessage");
                String strCdpId = extras.getString("toEntityId");
                int cdpId = Integer.parseInt(strCdpId);
                int cdpType = convertCdpTypeFromString(extras.getString("toEntityType", ""));

                // Post notification of received message.
                sendNotification(lastMessage, cdpType, cdpId);
                log.info("Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private int convertCdpTypeFromString(String cdpType) {
        if (cdpType.equals("channel")) {
            return JandiConstants.TYPE_CHANNEL;
        } else if (cdpType.equals("privateGroup")) {
            return JandiConstants.TYPE_PRIVATE_GROUP;
        } else if (cdpType.equals("user")) {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        } else {
            return -1;
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, int cdpType, int cdpId) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), MainActivity_.class);
        if (cdpType >= 0 && cdpId >= 0) {
            intent.putExtra(JandiConstants.EXTRA_CDP_ID, cdpId);
            intent.putExtra(JandiConstants.EXTRA_CDP_TYPE, cdpType);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext()
                , 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity_.class), 0);

        String notificationTitle = "Push from ";
        switch (cdpType) {
            case JandiConstants.TYPE_CHANNEL:
                notificationTitle += "Channel";
                break;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                notificationTitle += "Direct Message";
                break;
            case JandiConstants.TYPE_PRIVATE_GROUP:
                notificationTitle += "Private Group";
                break;
            default:
                break;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.jandi_actionb_logo)
                        .setTicker(msg)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentTitle(notificationTitle)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
