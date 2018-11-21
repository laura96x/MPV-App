package com.example.hara.learninguimusicapp.Photo;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
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

import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.Music.MusicFragment;
import com.example.hara.learninguimusicapp.Music.Song;
import com.example.hara.learninguimusicapp.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotosFragment extends Fragment {

    private onPhotoFragment mListener;
    private static final String ARG_PARAM1 = "param1";

    private GridView galleryGridView;
    private ArrayList<HashMap<String, String>> albumList;

    public PhotosFragment() {
        // Required empty public constructor
    }

    public static PhotosFragment newInstance(ArrayList<HashMap<String, String>> albums) {
        Log.d("demo", "PhotosFragment.newInstance");
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, albums);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("demo", "PhotosFragment.onCreate");
        if (getArguments() != null) {
            albumList = (ArrayList<HashMap<String, String>>) getArguments().getSerializable(ARG_PARAM1);
            Log.d("demo", "albumList: " + albumList);
        } else {
            albumList = new ArrayList<>();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // hide all options
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("demo", "MusicFragment clicked " + item.getTitle());
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        mListener.setFragmentTitle("Photos");

        galleryGridView = view.findViewById(R.id.galleryGridView);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        Resources resources = getContext().getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if(dp < 360)
        {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getContext().getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        AlbumAdapter adapter = new AlbumAdapter(getActivity(), albumList);
        galleryGridView.setAdapter(adapter);

        galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.d("demo", "PhotosFragment clicked grid");
                mListener.fromAlbumToPictures(albumList.get(position).get(Function.KEY_ALBUM));
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onPhotoFragment) {
            mListener = (onPhotoFragment) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onPhotoFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface onPhotoFragment {
        void fromAlbumToPictures(String title);
        void setFragmentTitle(String title);
    }
}
