package com.example.hara.learninguimusicapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.hara.learninguimusicapp.Music.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VideoFragment extends Fragment {

    private onVideoFragment mListener;
    private static final String ARG_PARAM1 = "param1";

    private ArrayList<String> videoList;
    ArrayAdapter<String> videoAdapter;
    private ListView listView;

    private int currentSort = 1; // 0 = DESC, 1 = ASC
    // default = ASC

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance(ArrayList<String> array) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, array);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("demo", "VideoFragment.onCreate");
        if (getArguments() != null) {
            videoList = (ArrayList<String>) getArguments().getSerializable(ARG_PARAM1);
        } else {
            videoList = new ArrayList<>();
        }
        sortLists(currentSort); // reset the sort
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d("demo", "VideoFragment.onPrepareOptionsMenu");

        // hide all options but the sort
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i) == menu.findItem(R.id.menu_item_sort)) {
                menu.getItem(i).setVisible(true);
            } else {
                menu.getItem(i).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("demo", "VideoFragment clicked " + item.getTitle());
        switch (item.getItemId()) {
            case R.id.menu_item_sort:
                if (item.getTitle().toString().contains("ASC")) {
                    // now sorting by ASC
                    item.setTitle("Sort DESC");
                    currentSort = 1;
                } else {
                    // now sorting by DESC
                    item.setTitle("Sort ASC");
                    currentSort = 0;
                }
                break;
        }
        sortLists(currentSort);
        videoAdapter.notifyDataSetChanged();
        return false;
    }

    public void sortLists(int sort) {
        if (sort == 1) { // ASC
            Collections.sort(videoList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        } else { // DESC
            Collections.sort(videoList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        mListener.setFragmentTitle("Videos");
        Log.d("demo", "VideoFragment.onCreateView");

        listView = view.findViewById(R.id.vidListView);

        videoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, videoList);
        listView.setAdapter(videoAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Log.d("demo", "video frag clickkyyy");
                mListener.pauseMusicForVideo();
                String passTitle = (String) listView.getAdapter().getItem(i);
                Intent intent = new Intent(getActivity(), VideoPlayer.class);
                intent.putExtra("KEY", passTitle);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onVideoFragment) {
            mListener = (onVideoFragment) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onVideoFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface onVideoFragment {
        void setFragmentTitle(String title);
        void pauseMusicForVideo();
    }
}
