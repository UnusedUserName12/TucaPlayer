package com.example.myapplication;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.obj.Song;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MP3ListAdapter extends ArrayAdapter<Song>{

    private List<Song> SongList;
    private List<Song> filterSongList;
    private List<Song> filteredList;
    private final LayoutInflater mInflater;
    public static int selectedItemPosition = -1;

    private Bitmap song_pic;

    public MP3ListAdapter(Context context, List<Song> SongList) {
        super(context, R.layout.list_item, SongList);
        this.SongList = SongList;
        this.filterSongList = SongList;
        this.filteredList = SongList;
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
        TextView authorView  = convertView.findViewById(R.id.author);
        TextView durationView = convertView.findViewById(R.id.duration);

        String songName = "";
        String authorName = "";
        String duration = "";
        if (position < SongList.size()) {
            Song song = SongList.get(position);
            songName = song.getSongName();
            authorName = song.getArtist();
            duration = convertToMMSS(song.getDuration());

            if(SongList.get(position).isSelected()){
                convertView.setBackground(new ColorDrawable(Color.rgb(255, 114, 114)));
                textView.setTextColor(Color.WHITE);
                //Toast.makeText(getContext(),"Colored",Toast.LENGTH_SHORT).show();
            }
            else {
                convertView.setBackground(new ColorDrawable(Color.TRANSPARENT));
                textView.setTextColor(Color.BLACK);
                //Toast.makeText(getContext(),"DISColored",Toast.LENGTH_SHORT).show();
            }
        }
        textView.setText(songName);
        authorView.setText(authorName);
        durationView.setText(duration);
        if(song_pic!=null){
            imageView.setImageBitmap(song_pic);
        }

        return convertView;
    }

    public void setSong_pic(Bitmap song_pic) {
        this.song_pic = scaleDown(song_pic,144,false);
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
                filteredList = SongList;
                notifyDataSetChanged();
            }
        };
    }

    public List<Song> getFilteredList(){
        return filteredList;
    }


    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public static String convertToMMSS(long duration){
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(duration) % 24;
        if(hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else return String.format("%02d:%02d", minutes, seconds);
    }
}