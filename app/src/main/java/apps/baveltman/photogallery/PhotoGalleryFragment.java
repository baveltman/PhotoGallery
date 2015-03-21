package apps.baveltman.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by borisvelt on 3/20/15.
 */
public class PhotoGalleryFragment extends Fragment {

    GridView mGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, parent, false);

        mGridView = (GridView)v.findViewById(R.id.photo_gridview);

        return v;
    }
}