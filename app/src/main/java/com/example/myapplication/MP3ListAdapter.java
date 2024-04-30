package com.example.myapplication;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.obj.Song;

import java.util.List;

public class MP3ListAdapter extends ArrayAdapter<Song> {

    private final List<Song> SongList;
    private final LayoutInflater mInflater;
    public static int selectedItemPosition = -1;

    public MP3ListAdapter(Context context, List<Song> SongList) {
        super(context, R.layout.list_item, SongList);
        this.SongList = SongList;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.pic);
            holder.textView = convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String mp3FileName = SongList.get(position).getSongName();
        holder.textView.setText(mp3FileName);

        if (position == selectedItemPosition) {
            convertView.setBackground(new ColorDrawable(Color.rgb(255, 114, 114)));
            TextView text = convertView.findViewById(R.id.name);
            text.setTextColor(Color.WHITE);
        } else {
            convertView.setBackground(new ColorDrawable(Color.TRANSPARENT));
            TextView text = convertView.findViewById(R.id.name);
            text.setTextColor(Color.BLACK);
        }

        return convertView;
    }


    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

}