package com.example.myapplication;

import static android.app.Activity.RESULT_OK;
import static com.example.myapplication.MyMediaPlayer.playMedia;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Playlist;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistView {
    private final MainActivity mainActivity;
    int id;
    private List<Song> SongList;
    private MP3ListAdapter AudioAdapter;
    private TextView playlist_name_view;
    private Playlist playlist;
    ImageView chosen_image;
    ImageView playlist_image_view;
    static boolean isExpanded = false;
    private ListView playlist_songs_view;
    ConstraintLayout constraintLayout;
    boolean isListSent;

    ActivityResultLauncher<Intent> activityLauncher;

    public PlaylistView(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        constraintLayout = mainActivity.findViewById(R.id.main_layout);
        activityLauncher = mainActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK) {
                loadAudio("title");
                AudioAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setListeners() {
        playlist_name_view = mainActivity.findViewById(R.id.playlist_name);
        playlist_image_view = mainActivity.findViewById(R.id.playlist_image);
        playlist_songs_view = mainActivity.findViewById(R.id.playlist_songs);
        ImageButton btn_add_songs = mainActivity.findViewById(R.id.btn_add_song_redirect);
        ImageButton btn_more = mainActivity.findViewById(R.id.btn_more_playlist);
        ImageView btn_close = mainActivity.findViewById(R.id.btn_close_playlist);

        SongList = new ArrayList<>();

        AudioAdapter = new MP3ListAdapter(mainActivity,SongList);
        playlist_songs_view.setAdapter(AudioAdapter);

        btn_add_songs.setOnClickListener(this::openAddSongsActivity);

        btn_more.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(mainActivity, v);

            popupMenu.getMenuInflater().inflate(R.menu.playlist_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {

                String option = (String) menuItem.getTitle();
                assert option != null;

                option = option.toLowerCase();
                switch (Objects.requireNonNull(option)){
                    case "rename playlist":
                        showDialogRename();
                        break;
                    case "change image":
                        showDialogChangeImage();
                        break;
                    case "delete playlist":
                        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
                        try {
                            databaseManager.open();
                        } catch (SQLDataException e) {
                            throw new RuntimeException(e);
                        }
                        databaseManager.deletePlaylist(id);
                        File f = new File(playlist.getImagePath(), playlist.getName()+".jpg");
                        if(f.delete()) Toast.makeText(mainActivity,"Playlist deleted",Toast.LENGTH_SHORT).show();
                        databaseManager.close();
                        shrinkPlaylist();
                        break;
                    default:
                        if(!option.equals("order by")) {
                            Toast.makeText(mainActivity, "By " + option, Toast.LENGTH_SHORT).show();
                            loadAudio(option);
                            AudioAdapter.notifyDataSetChanged();
                        }
                        break;
                }

                return true;
            });
            popupMenu.show();
        });

        btn_close.setOnClickListener(v -> {
            SongList.clear();
            AudioAdapter.notifyDataSetChanged();
            shrinkPlaylist();
        });

        playlist_songs_view.setOnItemClickListener((parent, view, position, id1) -> {
            if(!isListSent) {
                MyMediaPlayer.setSongList(SongList);
                mainActivity.settings.setLast_playlist_id(playlist.getId());
            }
            isListSent = true;

            playMedia(position);
            int currentSongId = MyMediaPlayer.getCurrentSongId();
            for(Song s : SongList) s.setSelected(currentSongId == s.getId());
            AudioAdapter.notifyDataSetChanged();
        });
    }

    public void openPlaylist(int id){
        playlist_songs_view.setVisibility(View.INVISIBLE);
        isListSent = false;
        this.id = id;
        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        Cursor cursor = databaseManager.getPlaylistById(id);

        if(cursor.moveToFirst()){
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_NAME));
            @SuppressLint("Range") String image_path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_PICTURE));

            playlist_name_view.setText(name);
            if(!image_path.equals("placeholder.png"))
            {
                playlist_image_view.setImageBitmap(loadImageFromStorage(name));
                playlist_image_view.setClipToOutline(true);
            }
            playlist = new Playlist(id,name,image_path);

        }

        cursor.close();
        loadAudio("title");
        AudioAdapter.notifyDataSetChanged();

        ThreadElementAutoSelector.SongList = SongList;
        ThreadElementAutoSelector.AudioAdapter = AudioAdapter;


        expandPlaylist();
    }

    private void expandPlaylist() {
        isExpanded = true;

        Fade fade = new Fade();
        fade.setDuration(200);
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(fade);

        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                ThreadSeekBar.isRunning = false;
                ThreadElementAutoSelector.isRunning = false;

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                ThreadElementAutoSelector.isRunning = true;

                TransitionManager.beginDelayedTransition(playlist_songs_view);
                playlist_songs_view.setVisibility(View.VISIBLE);

            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });

        TransitionManager.beginDelayedTransition(constraintLayout, transitionSet);

        if(mainActivity.settings.getLast_song_id()>0) {
            ConstraintSet song_view_visible_set = new ConstraintSet();
            song_view_visible_set.clone(mainActivity,R.layout.activity_main_playlist_view);
            song_view_visible_set.applyTo(constraintLayout);
        }
        else{
            ConstraintSet song_view_hidden_set = new ConstraintSet();   //To fix issue N4
            song_view_hidden_set.clone(mainActivity,R.layout.activity_main_playlist_view_song_hidden);
            song_view_hidden_set.applyTo(constraintLayout);
        }
    }

    void shrinkPlaylist() {
        isExpanded = false;

        Fade fade = new Fade();
        fade.setDuration(200);
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(fade);

        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                ThreadSeekBar.isRunning = false;
                ThreadElementAutoSelector.isRunning = false;
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                ThreadElementAutoSelector.isRunning = true;
            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });

        TransitionManager.beginDelayedTransition(constraintLayout, transitionSet);
        if(mainActivity.settings.getLast_song_id()>0) {
            ConstraintSet song_view_set = new ConstraintSet();
            song_view_set.clone(mainActivity, R.layout.activity_main);
            song_view_set.applyTo(constraintLayout);
        }
        else{
            ConstraintSet song_view_hidden_set = new ConstraintSet();
            song_view_hidden_set.clone(mainActivity,R.layout.activity_main_song_view_hidden);
            song_view_hidden_set.applyTo(constraintLayout);
        }

    }

    private void openAddSongsActivity(View v){
        Context context = mainActivity.getBaseContext();
        Intent intent = new Intent(context, AddSongsToPlaylistActivity.class);
        intent.putExtra("playlist_id",id);

        activityLauncher.launch(intent);

    }

    private Bitmap loadImageFromStorage(String playlistName) {
        ContextWrapper cw = new ContextWrapper(mainActivity);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        try {
            File f = new File(directory, playlistName+".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadAudio(String orderOption) {
        SongList.clear();
        AudioAdapter.notifyDataSetChanged();

        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor cursor = databaseManager.fetchPlaylistSongsFullInfo(playlist.getId(),orderOption);
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID)));
                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));
                @SuppressLint("Range") long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_DURATION)));

                Song song = new Song(id, filename, name, artist, album, genre, duration);
                SongList.add(song);

            }while (cursor.moveToNext());
        }

        databaseManager.close();
        cursor.close();
    }

    private void showDialogRename(){
        String oldName = String.valueOf(playlist_name_view.getText());
        Dialog dialog = new Dialog(mainActivity);
        dialog.setContentView(R.layout.rename_playlist_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.corners10dp);

        Button btn_rename = dialog.findViewById(R.id.btn_rename_playlist);
        EditText playlist_name_field = dialog.findViewById(R.id.change_playlist_name);

        playlist_name_field.setText(oldName);
        btn_rename.setOnClickListener(v -> {
            String newName = String.valueOf(playlist_name_field.getText());

            renamePlaylistImage(oldName,newName);

            DatabaseManager databaseManager = new DatabaseManager(mainActivity);
            try {
                databaseManager.open();
            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }
            playlist.setName(newName);
            databaseManager.updatePlaylist(playlist.getId(),playlist.getName(),playlist.getImagePath());

            playlist_name_view.setText(newName);
            dialog.dismiss();
        });
        dialog.show();
    }


    /**
     * Renames a playlist image file from {@code oldName} to {@code newName}.
     *
     * <p>This method attempts to rename a file located in the application's private
     * image directory. If the rename operation is successful, a success message
     * is displayed using a toast. If the rename operation fails, an error message
     * is displayed instead.
     * <p><b>This method is needed due to the fact that we save and load images using
     * playlist name</b>
     *
     *
     * @param oldName the current name of the file (without the .jpg extension)
     * @param newName the new name for the file (without the .jpg extension)
     */

    private void renamePlaylistImage(String oldName,String newName){
        ContextWrapper cw = new ContextWrapper(mainActivity);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        File f = new File(directory, oldName+".jpg");
        File n = new File(directory,newName+".jpg");
        if(f.renameTo(n))
            Toast.makeText(mainActivity,"File rename success",Toast.LENGTH_SHORT).show();
        else Toast.makeText(mainActivity,"Something went wrong",Toast.LENGTH_SHORT).show();

    }

    private void showDialogChangeImage(){
        Dialog dialog = new Dialog(mainActivity);
        dialog.setContentView(R.layout.change_image_playlist_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.corners10dp);

        Button btn_change = dialog.findViewById(R.id.btn_change_img);
        RelativeLayout btn_get_image = dialog.findViewById(R.id.btn_insert_image_change_dialog);
        chosen_image = dialog.findViewById(R.id.chosenImage_change_dialog);

        //TODO: Remember for future coloring
        ImageView plusIcon = dialog.findViewById(R.id.plus_icon_change_dialog);
        plusIcon.setColorFilter(ContextCompat.getColor(mainActivity, R.color.red), PorterDuff.Mode.SRC_IN);

        btn_change.setOnClickListener(v -> {

            DatabaseManager databaseManager = new DatabaseManager(mainActivity);

            try {
                databaseManager.open();
            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }

            String image_path = "null";
            if(chosen_image.getDrawable()!=null){
                Bitmap bm=((BitmapDrawable)chosen_image.getDrawable()).getBitmap();
                image_path = String.valueOf(saveToInternalStorage(bm,playlist.getName()));
            }

            if(image_path.equals("null")) image_path = "placeholder.png";
            playlist.setImagePath(image_path);
            databaseManager.updatePlaylist(playlist.getId(),playlist.getName(),playlist.getImagePath());
            databaseManager.close();

            playlist_image_view.setImageBitmap(loadImageFromStorage(playlist.getName()));
            dialog.dismiss();
        });

        btn_get_image.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);

            mainActivity.startActivityForResult(Intent.createChooser(i, "Select Picture"), 3);
        });
        dialog.show();
    }




    private String saveToInternalStorage(Bitmap bitmapImage,String playlistName){
        ContextWrapper cw = new ContextWrapper(mainActivity);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,playlistName+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
