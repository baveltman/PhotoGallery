package apps.baveltman.photogallery;

import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by borisvelt on 3/22/15.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";

    public ThumbnailDownloader() {
        super(TAG);
    }

    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "Got an URL: " + url);
    }
}
