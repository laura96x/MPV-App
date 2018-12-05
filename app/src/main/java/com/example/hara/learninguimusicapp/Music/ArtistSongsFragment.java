package com.example.hara.learninguimusicapp.Music;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.R;

import java.util.ArrayList;
import java.util.List;

public class ArtistSongsFragment extends Fragment {

    private onArtistSongsFragment mListener;
    private static final String ARG_PARAM1 = "param1";

    private ArrayList<Song> songs;
    private ArtistSongsAdapter artistSongsAdapter;

    public ArtistSongsFragment() {
        // Required empty public constructor
    }

    public static ArtistSongsFragment newInstance(ArrayList<Song> param1) {
        ArtistSongsFragment fragment = new ArtistSongsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songs = (ArrayList<Song>) getArguments().getSerializable(ARG_PARAM1);
        } else {
            songs = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_songs, container, false);
        Log.d("demo", "ArtistSongsFragment.onCreateView");
        ListView listView = view.findViewById(R.id.songArtistListview);

        artistSongsAdapter = new ArtistSongsAdapter(getContext(), this, R.layout.artist_list_item, songs);
        listView.setAdapter(artistSongsAdapter);

        return view;
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof onArtistSongsFragment) {
//            mListener = (onArtistSongsFragment) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement onArtistSongsFragment");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface onArtistSongsFragment {

    }

    public class ArtistSongsAdapter extends ArrayAdapter<Song> {

        private MainActivity mainActivity;

        public ArtistSongsAdapter(@NonNull Context context, ArtistSongsFragment fragment, int resource, @NonNull List<Song> objects) {
            super(context, resource, objects);
            mainActivity = (MainActivity) context;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Song currentSong = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.artist = convertView.findViewById(R.id.artistName);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.artist.setText(currentSong.getTitle());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("demo", "ArtistSongsAdapter clicked " + position);
                    mainActivity.playSong(currentSong);
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView artist;
        }
    }
}
