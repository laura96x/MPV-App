package com.example.hara.learninguimusicapp.Photo;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.hara.learninguimusicapp.BlankFragment;
import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class AlbumPicturesFragment extends Fragment {

    private onAlbumPicturesFragment mListener;
    private static final String ARG_PARAM1 = "param1";

    private GridView galleryGridView;
    private LoadAlbumImages loadAlbumTask;
    private String album_name = "";

    public AlbumPicturesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener.getBackButton();
        setHasOptionsMenu(true);

    }

    public static AlbumPicturesFragment newInstance(String param1) {
        AlbumPicturesFragment fragment = new AlbumPicturesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d("demo", "AlbumPicturesFragment.onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);
        // hide all options but the sort date
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i) == menu.findItem(R.id.menu_item_sort_date)) {
                menu.getItem(i).setVisible(true);
            } else {
                menu.getItem(i).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("demo", "AlbumPicturesFragment clicked " + item.getTitle());
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_picture_grid_view, container, false);
        Log.d("demo", "AlbumPicturesFragment.onCreateView");
        if (getArguments() != null) {
            album_name = getArguments().getString(ARG_PARAM1);
        }

        galleryGridView = view.findViewById(R.id.galleryGridView);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        Resources resources = getContext().getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if(dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getContext().getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onAlbumPicturesFragment) {
            mListener = (onAlbumPicturesFragment) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onAlbumPicturesFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class LoadAlbumImages extends AsyncTask<String, Void, String> {

        ArrayList<HashMap<String, String>> imageList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };

            // add getActivity() before getContentResolver()
            Cursor cursorExternal = getActivity().getContentResolver().query(uriExternal, projection, "bucket_display_name = \""+album_name+"\"", null, null);
            Cursor cursorInternal = getActivity().getContentResolver().query(uriInternal, projection, "bucket_display_name = \""+album_name+"\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});
            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                imageList.add(Function.mappingInbox(album, path, timestamp, Function.convertToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            AlbumPicturesGridAdapter adapter = new AlbumPicturesGridAdapter(getActivity(), imageList);
            galleryGridView.setAdapter(adapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    mListener.fromPictureToGallery(imageList.get(+position).get(Function.KEY_PATH));
                }
            });
        }
    }

    public interface onAlbumPicturesFragment {
        void fromPictureToGallery(String path);
        void getBackButton();
        void setFragmentTitle(String title);
    }
}
