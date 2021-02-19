package com.r4.notifications.mirror;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

class NotificationMirror {
    private final static String TAG = "NotifiactionMirror";

    /**
     * sends the given notification via tcp and the Mirror Class over the network to the specified socket address
     * logs the contents of the notification
     *
     * @param notification notification to
     * @param IP           of the socket
     * @param PORT         of the socket
     */
    public static void mirror(MirrorNotification notification, String IP, int PORT) {
        notification.log();

        Mirror mirror = new Mirror(IP, PORT);
        mirror.execute(notification);
    }

    /**
     * sends a notification over the network which dismisses the target notification
     *
     * @param notification notification to be dismissed
     */
    public static void mirrorCancel(MirrorNotification notification, String IP, int PORT) {

    }

    /**
     * checks if the notification is one that is sensible to store
     * excludes charging state updates, low battery warnings and mobile data warnings
     *
     * @param sbn notification to be checked
     * @return if the notification may pass the filter
     */
    public static boolean inFilter(StatusBarNotification sbn) {//here?
        Notification notification = sbn.getNotification();

        if ((notification.flags & Notification.FLAG_FOREGROUND_SERVICE) != 0
                || (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0
                || (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0
                || (notification.flags & NotificationCompat.FLAG_GROUP_SUMMARY) != 0
        )
            return true;
        if (sbn.getPackageName().equals("android"))
            return true;
        if (sbn.getPackageName().equals("com.android.systemui")) {
            if (sbn.getTag().equals("low_battery"))
                return true;
            else if (sbn.getTag().equals("charging_state"))
                return true;
            else if (sbn.getTag().contains("NetworkPolicy"))
                return true;
        }
        return false;
    }

    /**
     * reacts to a answer from the pc
     * replies to notifications
     * actions
     * dismisses
     *
     * @param networkPackage
     */
    private void onReceive(NetworkPackage networkPackage) {//maybe not here
//        MirrorNotification notification = getNotification(networkPackage.getID());
//        if (networkPackage.isReply())
//            notification.reply(networkPackage.getMessage());
//        else if (networkPackage.isAction())
//            notification.act(networkPackage.getActionName());
    }
}
