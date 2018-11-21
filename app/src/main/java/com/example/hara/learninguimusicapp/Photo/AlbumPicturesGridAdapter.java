package com.example.hara.learninguimusicapp.Photo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hara.learninguimusicapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AlbumPicturesGridAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap< String, String >> data;

    public AlbumPicturesGridAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
        activity = a;
        data = d;
    }

    public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SingleAlbumViewHolder holder;

        if (convertView == null) {
            holder = new SingleAlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.album_picture_row, parent, false);
            holder.galleryImage = convertView.findViewById(R.id.galleryImage);
            convertView.setTag(holder);
        } else {
            holder = (SingleAlbumViewHolder) convertView.getTag();
        }

        holder.galleryImage.setId(position);

        HashMap < String, String > song = data.get(position);
        try {
            Glide.with(activity)
                    .load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    class SingleAlbumViewHolder {
        ImageView galleryImage;
    }
}
