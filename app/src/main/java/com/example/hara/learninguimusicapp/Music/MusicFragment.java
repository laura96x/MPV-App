package com.example.hara.learninguimusicapp.Music;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MusicFragment extends Fragment {

    private onMusicFragment mListener;

    public TabLayout tabLayout;
    public ViewPager viewPager;
    ViewPagerAdapter adapter;

    ArrayList<Song> songs;
    SongFragment songFragment;
    ArtistFragment artistFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("demo", "MusicFragment.onCreateView");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        tabLayout = view.findViewById(R.id.music_tab);
        viewPager = view.findViewById(R.id.viewpager_id);

        Log.d("demo", "MusicFragment.onCreateView");
        mListener.setFragmentTitle("Music");

        if (getArguments() != null) {
//            Log.d("demo", "MusicFragment getArguments not null");
            songs = (ArrayList<Song>) getArguments().getSerializable(MainActivity.musicListKey);
        } else {
            Log.d("demo", "MusicFragment getArguments null");
            // use some random list
            songs = new ArrayList<>();
            songs.add(new Song("song 1", "artist F"));
            songs.add(new Song("song 2", "artist T"));
            songs.add(new Song("song 3", "artist I"));
            songs.add(new Song("fsong 4", "artist E"));
            songs.add(new Song("song 5", "artist Q"));
            songs.add(new Song("song 6", "artist A"));
            songs.add(new Song("bsong 7", "artist Z"));
            songs.add(new Song("song 8", "artist C"));
            songs.add(new Song("song 9", "artist M"));
        }

        // create new instances of the fragments
        songFragment = SongFragment.newInstance(songs);
        artistFragment = ArtistFragment.newInstance(songs);

        adapter = new ViewPagerAdapter(getFragmentManager());
        // add the fragments to the adapter to create the tabs
        adapter.addFragment(songFragment, "Song");
        adapter.addFragment(artistFragment, "Artist");

        // show everything
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onMusicFragment) {
            mListener = (onMusicFragment) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onMusicFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface onMusicFragment {
        void setFragmentTitle(String title);
    }
}