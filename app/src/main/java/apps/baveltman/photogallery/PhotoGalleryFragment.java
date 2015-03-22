package apps.baveltman.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by borisvelt on 3/20/15.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String LOGGERTAG = "PhotoGalleryFragment";

    GridView mGridView;
    ArrayList<GalleryItem> mItems;

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

        setupAdapter();

        return v;
    }

    /**
     * set up ArrayAdapter for this GridView
     */
    private void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;

        if (mItems != null) {
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }


    /**
     * nested class used to facilitate a background task of retrieving info from url
     */
    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            return new FlickerFetcher().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }

    }

    /**
     * custom adapter for this gridView
     */
    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{

        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView
                .findViewById(R.id.gallery_item_imageView);

            imageView.setImageResource(R.drawable.brian_up_close);

            return convertView;
        }
    }
}
