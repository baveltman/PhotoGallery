package apps.baveltman.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Broadcast reciever to wake the app on boot
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        //on device reboot see if polling service was on by looking at shared prefs
        //set polling service alarm accordingly
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = prefs.getBoolean(PollService.PREF_IS_ALARM_ON, false);
        PollService.setServiceAlarm(context, isOn);
    }

}
