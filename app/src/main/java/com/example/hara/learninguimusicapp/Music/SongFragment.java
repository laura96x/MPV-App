package com.example.hara.learninguimusicapp.Music;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.hara.learninguimusicapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SongFragment extends Fragment {

    private ArrayList<Song> songs;
    private SongAdapter songAdapter;
    private static final String ARG_PARAM1 = "param1";
    private boolean sortASC = true; // 0 = DESC, 1 = ASC
    // default = ASC

    public SongFragment() {
        // Required empty public constructor
    }

    public static SongFragment newInstance(ArrayList<Song> songs) {
        Log.d("demo", "SongFragment.newInstance");
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, songs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("demo", "SongFragment.onCreate");
        if (getArguments() != null) {
            songs = (ArrayList<Song>) getArguments().getSerializable(ARG_PARAM1);
        } else {
            songs = new ArrayList<>();
        }
        setHasOptionsMenu(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("demo", "MusicFragment clicked " + item.getTitle());
//        switch (item.getItemId()) {
//            case R.id.menu_item_sort:
//                if (item.getTitle().toString().contains("ASC")) {
//                    // now sorting by ASC
//                    item.setTitle("Sort DESC");
//                    currentSort = 1;
//                } else {
//                    // now sorting by DESC
//                    item.setTitle("Sort ASC");
//                    currentSort = 0;
//                }
//                break;
//        }
//        sortLists(currentSort);
        return false;
    }

    public void sortLists(boolean sort) {
        Log.d("demo", "SongFragment.sortLists " + sort);
        if (sort) { // ASC
            Collections.sort(songs, new Comparator<Song>() {
                @Override
                public int compare(Song o1, Song o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
        } else { // DESC
            Collections.sort(songs, new Comparator<Song>() {
                @Override
                public int compare(Song o1, Song o2) {
                    return o2.getTitle().compareTo(o1.getTitle());
                }
            });
        }
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        Log.d("demo", "SongFragment.onCreateView");

        ListView listView = view.findViewById(R.id.songListview);
        songAdapter = new SongAdapter(getContext(), R.layout.song_list_item, songs);
        listView.setAdapter(songAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("demo", "song frag clicky, it never comes here ");
            }
        });

        // needed for the pop up menus for each song
        registerForContextMenu(listView);

//        if (!sortASC) {
//            // reset sort, I'm not sure how to change the (3 dots) menu options on back press
//            sortASC = true;
//            sortLists(sortASC);
//        }
        return view;
    }

    //////////////////////////////////////////////////////////
    // HANDLE THE POP-UP MENUS ON LONG CLICKS ON SONGS
    //////////////////////////////////////////////////////////

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.song_options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_options_add_to_playlist:
                Log.d("demo", "clicked " + item.getTitle());
                return true;
            case R.id.song_options_delete:
                Log.d("demo", "clicked " + item.getTitle());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("demo", "song.onResume");
    }
}
