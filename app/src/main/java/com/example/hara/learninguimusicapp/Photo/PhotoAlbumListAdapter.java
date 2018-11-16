package com.example.hara.learninguimusicapp.Photo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hara.learninguimusicapp.R;

import java.util.List;

public class PhotoAlbumListAdapter extends ArrayAdapter<String> {
    public PhotoAlbumListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.photo_album_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.photoAlbumTitle = convertView.findViewById(R.id.textPhotoAlbum);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return super.getView(position, convertView, parent);
    }
    private static class ViewHolder {
        TextView photoAlbumTitle;
    }
}
