package apps.baveltman.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by borisvelt on 3/22/15.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int CACHE_SIZE = 4 * 1024 * 1024; // 4MiB

    Handler mHandler;
    Handler mResponseHandler;
    Listener<Token> mListener;

    Map<Token, String> requestMap =
            Collections.synchronizedMap(new HashMap<Token, String>());

    LruCache mCache;


    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        mCache = new LruCache(CACHE_SIZE);
    }

    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "Got an URL: " + url);
        requestMap.put(token, url);

        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);

            if (url == null) {
                return;
            }

            //check cache first before downloading
            byte[] bitmapBytes;
            byte[] bitmapBytesInCache = (byte[])mCache.get(url);

            if (bitmapBytesInCache != null){
                bitmapBytes = bitmapBytesInCache;
            } else {
                //not in cache, download from url
                bitmapBytes = new FlickerFetcher().getUrlBytes(url);
                mCache.put(url, bitmapBytes);
            }

            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            //pass message to main thread handler so that UI can be updated
            mResponseHandler.post(new Runnable() {
                public void run() {
                    if (url == null || requestMap.get(token) != url) {
                        return;
                    }

                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }


}
