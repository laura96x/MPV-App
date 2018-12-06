package com.example.hara.learninguimusicapp.Music;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.R;

import java.util.List;

// TODO - perhaps use recycler view instead
public class SongAdapter extends ArrayAdapter<Song> {

    private MainActivity mainActivity;

    public SongAdapter(@NonNull Context context, int resource, @NonNull List<Song> objects) {
        super(context, resource, objects);
        mainActivity = (MainActivity) context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Song currentSong = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.song_title);
            viewHolder.artist = convertView.findViewById(R.id.song_artist);
            viewHolder.options = convertView.findViewById(R.id.songOptions);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(currentSong.getTitle());
        viewHolder.artist.setText(currentSong.getArtist());
        viewHolder.options.setImageResource(0);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "clicked " + currentSong.getTitle());
                // go back to the main activity and play the selected song
                mainActivity.playSong(currentSong);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("demo", "long clicked " + currentSong.getTitle());
                // somehow the pop-up (context) menu appears
                return false;
            }
        });
        return convertView;
    }

    private class ViewHolder {
        TextView title, artist;
        ImageView options;
    }

}
