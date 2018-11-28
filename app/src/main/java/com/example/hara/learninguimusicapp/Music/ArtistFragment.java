package com.example.hara.learninguimusicapp.Music;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.hara.learninguimusicapp.R;

import java.util.ArrayList;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment {

    private onArtistFragment mListener;
    private static final String ARG_PARAM1 = "param1";

    private ArrayList<Song> songs;
    private ArtistAdapter artistAdapter;
    private TreeMap<String, ArrayList<Song>> artistTree;

    public ArtistFragment() {
        // Required empty public constructor
    }

    public static ArtistFragment newInstance(ArrayList<Song> songs) {
        Log.d("demo", "ArtistFragment.newInstance");
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, songs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("demo", "ArtistFragment.onCreate");
        if (getArguments() != null) {
            songs = (ArrayList<Song>) getArguments().getSerializable(ARG_PARAM1);
        } else {
            songs = new ArrayList<>();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // this will make the whole menu disappear
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        Log.d("demo", "ArtistFragment.onCreateView");


        ListView listView = view.findViewById(R.id.artistListview);

        // initialize tree map and populate it
        artistTree = new TreeMap<>();
        for (int i = 0; i < songs.size(); i++) {
            Song currentSong = songs.get(i);
            if (artistTree.containsKey(currentSong.getArtist())) {
                artistTree.get(currentSong.getArtist()).add(currentSong);
            } else {
                ArrayList<Song> values = new ArrayList<>();
                values.add(currentSong);
                artistTree.put(currentSong.getArtist(), values);
            }
        }
//        Log.d("demo", "treemap " + artistTree.toString());

        // convert the keys to an array list
        final ArrayList<String> keyArray = new ArrayList<>(artistTree.keySet());
        artistAdapter = new ArtistAdapter(getContext(), this, R.layout.artist_list_item, keyArray);
        listView.setAdapter(artistAdapter);

        return view;
    }

    public void sendArtistToMain(String artist) {
        Log.d("demo", "sendArtistToMain");
        mListener.fromArtistToTheirSongs(artistTree.get(artist));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onArtistFragment) {
            mListener = (onArtistFragment) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onArtistFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface onArtistFragment {
        void fromArtistToTheirSongs(ArrayList<Song> array);
    }

}
