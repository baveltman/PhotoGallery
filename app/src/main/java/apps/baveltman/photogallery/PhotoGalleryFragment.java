package apps.baveltman.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by borisvelt on 3/20/15.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private static final String LOGGERTAG = "PhotoGalleryFragment";

    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //add options menu
        setHasOptionsMenu(true);

        updateItems();

        //download images one at a time using a background thread
        //pass that thread the main handler to allow it to handle messages from background thread
        //set listener for desired messages
        mThumbnailThread = new ThumbnailDownloader(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>(){
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail){
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(LOGGERTAG, "Background thread started");
    }

    /**
     * use Async task to get data from flickr
     */
    public void updateItems() {
        //call on nested class to act in a background thread via async task
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, parent, false);

        mGridView = (GridView)v.findViewById(R.id.photo_gridview);

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //destroy background thread
        mThumbnailThread.quit();
        Log.i(LOGGERTAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
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
            Activity activity = getActivity();

            if (activity == null)
                return new ArrayList<GalleryItem>();

            //get query from shared prefs
            String query = PreferenceManager.getDefaultSharedPreferences(activity).getString(FlickerFetcher.PREF_SEARCH_QUERY, null);

            if (query != null) {
                return new FlickerFetcher().search(query);
            } else {
                return new FlickerFetcher().fetchItems();
            }
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

            imageView.setImageResource(R.drawable.media_image_placeholder);

            GalleryItem item = getItem(position);

            if (position == 0){
                //save id of first item in shared prefs
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickerFetcher.PREF_LAST_RESULT_ID, item.getId())
                        .commit();
            }

            mThumbnailThread.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }
    }

    /**
     * Events for options menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        //set up searchView for supported devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Pull out the SearchView
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView)searchItem.getActionView();

            // Get the data from our searchable.xml as a SearchableInfo
            SearchManager searchManager = (SearchManager)getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

            if (searchView != null)
                searchView.setSearchableInfo(searchInfo);
        }
    }

    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                //clear out shared prefs to erase query and update
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickerFetcher.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());

                if (shouldStartAlarm){
                    //start polling flicker through background service
                    PollService.setServiceAlarm(getActivity(), true);
                } else {
                    PollService.setServiceAlarm(getActivity(), false);
                }

                //invalidate options menu so we call onPrepareOptionsMenu and update the menu option
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    getActivity().invalidateOptionsMenu();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //decide what option to show for polling (start or stop)
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }
}
