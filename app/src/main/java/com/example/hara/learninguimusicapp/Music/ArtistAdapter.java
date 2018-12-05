package com.example.hara.learninguimusicapp.Music;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.R;

import java.util.List;

public class ArtistAdapter extends ArrayAdapter<String>{

    private MainActivity mainActivity;
    private ArtistFragment artistFragment;
    public ArtistAdapter(@NonNull Context context, ArtistFragment fragment, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        mainActivity = (MainActivity) context;
        artistFragment = fragment;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String currentArtist = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item,parent, false);
            viewHolder = new ViewHolder();
            viewHolder.artist = convertView.findViewById(R.id.artistName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.artist.setText(currentArtist);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "clicked " + position);
//                mainActivity.fromArtistToTheirSongs(currentArtist);
                artistFragment.sendArtistToMain(currentArtist);

            }
        });
        return convertView;
    }

    private class ViewHolder {
        TextView artist;
    }
}
