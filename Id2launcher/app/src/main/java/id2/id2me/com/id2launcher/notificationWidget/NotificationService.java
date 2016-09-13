package id2.id2me.com.id2launcher.notificationWidget;


import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.DatabaseHandler;


public class NotificationService extends NotificationListenerService {

    DatabaseHandler db;
    public static boolean isNotificationAccessEnabled = false;
    Context context;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
        db = DatabaseHandler.getInstance(context);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        try {
            String ticketText = null;
            // StatusBarNotification[] activeNos = getActiveNotifications();
            String pack = sbn.getPackageName();

            ArrayList<String> packName = (ArrayList<String>) db.getNotificationPackages();
            Log.v("NotificationListener", pack);
            if (pack.contains("com.facebook")) {  // change Sunita /* sometimes getting package as com.facebook.orca*/
                if (packName.contains("com.facebook.katana"))
                    db.updateNotificationData("com.facebook.katana");
            } else if (pack.equals("com.google.android.apps.messaging")) {
                if (packName.contains("com.google.android.apps.messaging")) {
                    String sortKey = sbn.getNotification().getSortKey();
                    if (sortKey == null) {
                        db.updateNotificationData("com.google.android.apps.messaging");
                    }
                }
            } else if (pack.equals("com.google.android.talk")) {
                if (packName.contains("com.google.android.talk")) {

                    db.updateNotificationData("com.google.android.talk");
                    Log.v("Hangout", "ID:" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

                }

            } else if (pack.equalsIgnoreCase("com.android.server.telecom")) {
                if (packName.contains("com.android.dialer")) {
                    db.updateNotificationData("com.android.dialer");
                }
            } else if (pack.equals("com.instagram.android")) {
                if (packName.contains("com.instagram.android"))
                    db.updateNotificationData("com.instagram.android");
            } else if (pack.equals("com.snapchat.android")) {
                if (packName.contains("com.snapchat.android"))
                    db.updateNotificationData("com.snapchat.android");
            } else if (pack.equals("com.twitter.android")) {
                if (packName.contains("com.twitter.android"))
                    db.updateNotificationData("com.twitter.android");
            } else if (pack.equals("com.whatsapp")) {
                if (packName.contains("com.whatsapp")) {
                    ticketText = sbn.getNotification().tickerText.toString();
                    if (sbn.getTag() != null && sbn.getTag().contains("whatsapp.net")) {
                        db.updateNotificationData("com.whatsapp");
                    } else if (ticketText != null) {
                        db.updateNotificationData("com.whatsapp");
                    }
                }
            }
            Intent intent = new Intent("Msg");
            intent.putExtra("packageName", pack);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        isNotificationAccessEnabled = true;
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isNotificationAccessEnabled = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg", "Notification Removed");
        String pack = sbn.getPackageName();
      /*  if (pack.equals("com.facebook.katana")) {
            db.resetNotificationCount("com.facebook.katana");
        } else if (pack.equals("com.android.mms")) {
            db.resetNotificationCount("com.android.mms");
        } else if (pack.equals("com.instagram.android")) {
            db.resetNotificationCount("com.instagram.android");
        } else if (pack.equals("com.snapchat.android")) {
            db.resetNotificationCount("com.snapchat.android");
        } else if (pack.equals("com.android.server.telecom")) {
            db.resetNotificationCount("com.android.dialer");
        } else if (pack.equals("com.google.android.apps.messaging")) {
            db.resetNotificationCount("com.google.android.apps.messaging");
        } else if (pack.equals("com.twitter.android")) {
            //  db.resetNotificationCount("com.twitter.android");
        } else if (pack.equals("com.whatsapp")) {
            db.resetNotificationCount("com.whatsapp");
        } else if (pack.equals("com.android.email")) {
            db.resetNotificationCount("com.android.email");
        }
        // else if (pack.equals("com.google.android.talk")) {
        //  db.resetNotificationCount("com.google.android.talk");
        // }
//else if(pack.equals("com.google.android.gm")){

//  //      db.resetNotificationCount("com.google.android.gm");
        // }
        *//*

        else if(pack.equals("com.android.dialer")){

            db.resetNotificationCount("com.android.dialer");

        }
         *//*
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("Msg"));
*/    }
}
