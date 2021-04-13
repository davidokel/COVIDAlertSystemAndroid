package com.davidokelly.covidalertsystem.data.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.davidokelly.covidalertsystem.home.MapsFragment;

import java.util.Map;

public class ReminderBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        notificationHelper.sendReminderNotification("Remember to bring your mask!","You usually leave the house soon, please be safe.", MapsFragment.class);
    }
}
