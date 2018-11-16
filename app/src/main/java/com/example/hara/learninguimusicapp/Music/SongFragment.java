package com.example.hara.learninguimusicapp.Music;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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

    public SongFragment() {
        // Required empty public constructor
    }

    ArrayList<Song> songs;
    SongAdapter songAdapter;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Menu menu2;
    int currentSort = 1; // 0 = DESC, 1 = ASC
    // default = ASC

    public static SongFragment newInstance(ArrayList<Song> songs) {
        Log.d("demo", "SongFragment.newInstance");
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, songs);
//        args.putInt(ARG_PARAM2, sort); // 0 = DESC, 1 = ASC
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("demo", "SongFragment.onCreate");
        if (getArguments() != null) {
            songs = (ArrayList<Song>) getArguments().getSerializable(ARG_PARAM1);
            Log.d("demo", "SongFragment.onCreate " + songs.toString());
        } else {
            Log.d("demo", "SongFragment.onCreate EMPTY ARRAY");
            songs = new ArrayList<>();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d("demo", "SongFragment.onPrepareOptionsMenu");
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
        songs = sortLists(songs, currentSort);
        songAdapter.notifyDataSetChanged();
        return false;
    }

    public ArrayList<Song> sortLists(ArrayList<Song> songs, int sort) {
        if (sort == 1) { // ASC
            Collections.sort(songs, new Comparator<Song>() {
                @Override
                public int compare(Song o1, Song o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        } else { // DESC
            Collections.sort(songs, new Comparator<Song>() {
                @Override
                public int compare(Song o1, Song o2) {
                    return o2.getName().compareTo(o1.getName());
                }
            });
        }
        return songs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
                Log.d("demo", "song frag clicky");
            }
        });

        // needed for the pop up menus for each song
        registerForContextMenu(listView);
        return view;
    }

    //////////////////////////////////////////////////////////
    // HANDLE THE POP-UP MENUS
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

}
