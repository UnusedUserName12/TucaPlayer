package com.example.myapplication;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.obj.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MP3ListAdapter extends ArrayAdapter<Song>{

    private List<Song> SongList;
    private final List<Song> filterSongList;
    private final LayoutInflater mInflater;
    public static int selectedItemPosition = -1;

    public MP3ListAdapter(Context context, List<Song> SongList) {
        super(context, R.layout.list_item, SongList);
        this.SongList = SongList;
        this.filterSongList = SongList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return SongList.size();
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.name);
        ImageView imageView = convertView.findViewById(R.id.pic);

        String songName = "";
        if (position < SongList.size()) {
            songName = SongList.get(position).getSongName();
        }
        textView.setText(songName);

        if (position == selectedItemPosition) {
            convertView.setBackground(new ColorDrawable(Color.rgb(255, 114, 114)));
            textView.setTextColor(Color.WHITE);
        } else {
            convertView.setBackground(new ColorDrawable(Color.TRANSPARENT));
            textView.setTextColor(Color.BLACK);
        }

        return convertView;
    }


    @NonNull
    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if(constraint !=null && constraint.length()>0){
                    constraint=constraint.toString().toLowerCase();

                    ArrayList<Song> filters = new ArrayList<>();
                    for(Song s : filterSongList){
                        if(s.getSongName().toLowerCase().contains(constraint))  filters.add(s);
                    }
                    results.count = filters.size();
                    results.values = filters;
                }
                else {
                    results.count = filterSongList.size();
                    results.values = filterSongList;
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                SongList = (List<Song>) results.values;
                notifyDataSetChanged();
            }
        };
    }


}