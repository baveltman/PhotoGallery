package apps.baveltman.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.IOException;

/**
 * Created by borisvelt on 3/20/15.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String LOGGERTAG = "PhotoGalleryFragment";

    GridView mGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //call on nested class to act in a background thread
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, parent, false);

        mGridView = (GridView)v.findViewById(R.id.photo_gridview);

        return v;
    }


    /**
     * nested class used to facilitate a background task of retrieving info from url
     */
    private class FetchItemsTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new FlickerFetcher().getUrl("http://www.android.com/");
                Log.i(LOGGERTAG, "Fetched contents of URL: " + result);
            } catch (IOException ioe) {
                Log.e(LOGGERTAG, "Failed to fetch URL: ", ioe);
            }
            return null;
        }

    }
}
