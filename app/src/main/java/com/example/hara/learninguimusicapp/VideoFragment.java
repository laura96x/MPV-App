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

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {

    private onVideoFragment mListener;

    private ArrayList<String> arrayList;

    private ArrayAdapter<String> adapter;


    public ListView listView;
    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        listView = view.findViewById(R.id.vidListView);

        mListener.setFragmentTitle("Videos");


        Log.d("demo", "We are in the video fragment");

        if (getArguments() != null) {
//            Log.d("demo", "MusicFragment getArguments not null");
            arrayList = (ArrayList<String>) getArguments().getSerializable(MainActivity.videoListKey);
        }

        Log.d("demo", arrayList.toString());

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arrayList);


        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                String passTitle = (String) listView.getAdapter().getItem(i);

                Intent intent = new Intent(getActivity(), VideoPlayer.class);
                intent.putExtra("KEY",passTitle);

                startActivity(intent);

            }

        });



        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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
        Log.d("demo", "MusicFragment clicked " + item.getTitle());
        return true;
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
    }
}
