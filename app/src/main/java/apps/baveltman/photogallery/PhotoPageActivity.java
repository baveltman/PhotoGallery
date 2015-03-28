package apps.baveltman.photogallery;

import android.support.v4.app.Fragment;

/**
 * Activity class to host PhotoPageFragment
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PhotoPageFragment();
    }
}
