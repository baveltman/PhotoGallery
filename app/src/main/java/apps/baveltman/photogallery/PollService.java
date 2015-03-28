package apps.baveltman.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * background service for app
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static final String ACTION_SHOW_NOTIFICATION =
            "apps.baveltman.photogallery.SHOW_NOTIFICATION";

    public static final String PERM_PRIVATE =
            "apps.baveltman.photogallery.PRIVATE";

    private static final int POLL_INTERVAL = 1000 * 60 * 5; // 5 minutes

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //first check for network connectivity in the background service
        if (!isNetworkAvailable()) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickerFetcher.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickerFetcher.PREF_LAST_RESULT_ID, null);

        ArrayList<GalleryItem> items;
        if (query != null) {
            items = new FlickerFetcher().search(query);
        } else {
            items = new FlickerFetcher().fetchItems();
        }

        if (items.size() == 0) return;

        String resultId = items.get(0).getId();

        if (!resultId.equals(lastResultId)) {
            Log.i(TAG, "Got a new result: " + resultId);

            SendNotificationAboutResults();

        } else {
            Log.i(TAG, "Got an old result: " + resultId);
        }

        prefs.edit()
                .putString(FlickerFetcher.PREF_LAST_RESULT_ID, resultId)
                .commit();
    }

    /**
     * send notification to user about results from background service
     */
    private void SendNotificationAboutResults() {
        Resources r = getResources();
        PendingIntent pi = PendingIntent
                .getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class),0);

        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(r.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(r.getString(R.string.new_pictures_title))
                .setContentText(r.getString(R.string.new_pictures_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        showBackgroundNotification(0, notification);
    }

    void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                         Activity.RESULT_OK, null, null);
    }

    /**
     * sets a repeating alarm to call on background service
     */
    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = new Intent(context, PollService.class);

        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            alarmManager.setRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        //set shared pref to store whether poll service alarm is on
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putBoolean(PollService.PREF_IS_ALARM_ON, isOn)
                .commit();
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() &&
                cm.getActiveNetworkInfo() != null;

        return isNetworkAvailable;
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, PollService.class);

        PendingIntent pi = PendingIntent.getService(
                context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pi != null;
    }
}
