package apps.baveltman.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by borisvelt on 3/28/15.
 */
public abstract class VisibleFragment extends Fragment {
    public static final String TAG = "VisibleFragment";

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
        Toast.makeText(getActivity(),
                "Got a broadcast:" + intent.getAction(),
                Toast.LENGTH_LONG)
                .show();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //register private receiver
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,
                PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
