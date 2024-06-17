package com.example.myapplication;

import static android.app.Activity.RESULT_OK;
import static com.example.myapplication.MyMediaPlayer.CurrentIndex;
import static com.example.myapplication.MyMediaPlayer.playMedia;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.ContextThemeWrapper;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.interfaces.OnPlaylistChangeListener;
import com.example.myapplication.obj.Playlist;
import com.example.myapplication.obj.Song;

import java.io.File;
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
    boolean isAlbum;
    private static OnPlaylistChangeListener onPlaylistChangeListener;
    private boolean multi_select_mode;

    ActivityResultLauncher<Intent> activityLauncher;

    public PlaylistView(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        constraintLayout = mainActivity.findViewById(R.id.main_layout);
        activityLauncher = mainActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK) {
                loadAudioPlaylist("title");
                TextView sizeView = mainActivity.findViewById(R.id.playlist_size);
                String size = SongList.size()+" songs";
                sizeView.setText(size);
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
        ImageButton btn_delete = mainActivity.findViewById(R.id.btn_delete_from_playlist);

        SongList = new ArrayList<>();

        AudioAdapter = new MP3ListAdapter(mainActivity,SongList);
        playlist_songs_view.setAdapter(AudioAdapter);

        btn_add_songs.setOnClickListener(this::openAddSongsActivity);

        btn_more.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(mainActivity, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);

            if(!isAlbum) popupMenu.getMenuInflater().inflate(R.menu.playlist_options_menu, popupMenu.getMenu());
            else popupMenu.getMenuInflater().inflate(R.menu.album_options_menu, popupMenu.getMenu());
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
                    case "delete album":
                        showDeleteDialog();
                        break;
                    case "order by":
                        showOrderPopup(v);
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
            sendData();
            playMedia(position);
            int currentSongId = MyMediaPlayer.getCurrentSongId();
            if(!multi_select_mode) for(Song s : SongList) s.setSelected(currentSongId == s.getId());
            else
            {
                MyMediaPlayer.instance.pause();
                if(!SongList.get(position).isSelected())
                    SongList.get(position).setSelected(true);
                else SongList.get(position).setSelected(false);
            }
            AudioAdapter.notifyDataSetChanged();
        });

        playlist_songs_view.setOnItemLongClickListener((parent, view, position, id) -> {
            if(!isAlbum) {
                ThreadElementAutoSelector.isRunning=false;
                multi_select_mode = true;
                btn_delete.setVisibility(View.VISIBLE);
                btn_add_songs.setVisibility(View.INVISIBLE);
            }
            return false;
        });

        btn_delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.CustomAlertDialogTheme);
            int count=0;
            for(Song s: SongList){
                if(s.isSelected()) count++;
            }
            builder.setMessage("Are you sure you want to delete " + count+" songs?");
            builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                DatabaseManager databaseManager = new DatabaseManager(mainActivity);
                try {
                    databaseManager.open();

                    for (Song s : SongList) {
                        if (s.isSelected()) {
                            databaseManager.deletePlaylistSong(id, s.getId());
                        }
                    }
                    SongList.removeIf(Song::isSelected);
                    AudioAdapter.notifyDataSetChanged();

                } catch (SQLDataException e) {
                    throw new RuntimeException(e);
                } finally {
                    databaseManager.close();
                }
                multi_select_mode = false;
                ThreadElementAutoSelector.isRunning = true;
                isListSent = false;
                sendData();

                btn_delete.setVisibility(View.INVISIBLE);
                btn_add_songs.setVisibility(View.VISIBLE);

                TextView sizeView = mainActivity.findViewById(R.id.playlist_size);
                String size = SongList.size() + " songs";
                sizeView.setText(size);
                dialog.dismiss();
            });

            builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                multi_select_mode=false;
                for(Song song : SongList){
                    song.setSelected(false);
                }
                AudioAdapter.notifyDataSetChanged();
                v.setVisibility(View.INVISIBLE);
                btn_add_songs.setVisibility(View.VISIBLE);
                ThreadElementAutoSelector.isRunning = true;
                dialog.cancel();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void showOrderPopup(View v) {
        Context wrapper = new ContextThemeWrapper(mainActivity, R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, v);

        popupMenu.getMenuInflater().inflate(R.menu.sort_order_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {

            String option = (String) menuItem.getTitle();
            assert option != null;

            option = option.toLowerCase();

            Toast.makeText(mainActivity, "By " + option, Toast.LENGTH_SHORT).show();
            if (isAlbum) loadAudioAlbum(option);
            else loadAudioPlaylist(option);
            return true;
        });
        popupMenu.show();
    }

    /**
     * Sends the song list and updates the UI with the playlist image
     <ul>
     *     <li>Sets the song list in the MyMediaPlayer.</li>
     *     <li>Updates the last playlist ID in the settings.</li>
     *     <li>Loads and sets the playlist image in the UI.</li>
     * </ul>
     *
     * <p>The method ensures that these actions are only performed once by checking the {@code isListSent} flag.
     * If the list has already been sent, the method will not perform any actions.
     */

    private void sendData() {
        if(!isListSent) {
            MyMediaPlayer.setSongList(SongList);
            mainActivity.settings.setLast_playlist_id(playlist.getId());
            mainActivity.settings.setIsAlbum(isAlbum);
            ImageView SongViewImage = mainActivity.findViewById(R.id.song_view_image);
            Bitmap song_pic_bitmap = mainActivity.loadImageFromStorage(playlist.getName());
            SongViewImage.setImageBitmap(song_pic_bitmap);
        }
        isListSent = true;
    }

    public void openPlaylist(int id){
        isAlbum = false;
        ImageButton btn_add_songs = mainActivity.findViewById(R.id.btn_add_song_redirect);
        btn_add_songs.setVisibility(View.VISIBLE);
        ImageButton btn_delete = mainActivity.findViewById(R.id.btn_delete_from_playlist);
        btn_delete.setVisibility(View.GONE);
        openView(id);
    }

    private void expandPlaylist() {
        isExpanded = true;
        playlist_songs_view.setVisibility(View.GONE);

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

                Fade fade = new Fade();
                fade.setDuration(200);
                TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(fade);
                TransitionManager.beginDelayedTransition(playlist_songs_view,transitionSet);
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


        if(onPlaylistChangeListener!=null){
            onPlaylistChangeListener.OnPlaylistChanged();
        }
    }

    private void openAddSongsActivity(View v){
        Context context = mainActivity.getBaseContext();
        Intent intent = new Intent(context, AddSongsToPlaylistActivity.class);
        intent.putExtra("playlist_id",id);

        activityLauncher.launch(intent);

    }

    private void loadAudioPlaylist(String orderOption) {
        SongList.clear();
        Cursor cursor = null;
        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try{
            databaseManager.open();
            cursor = databaseManager.fetchPlaylistSongsFullInfo(id,orderOption);
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
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(cursor!=null) cursor.close();
            databaseManager.close();
            AudioAdapter.notifyDataSetChanged();
        }
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
            String newName = String.valueOf(playlist_name_field.getText()).trim();
            if(!newName.isEmpty()) {
                renamePlaylistImage(oldName, newName);

                DatabaseManager databaseManager = new DatabaseManager(mainActivity);
                try {
                    databaseManager.open();
                } catch (SQLDataException e) {
                    throw new RuntimeException(e);
                }
                playlist.setName(newName);
                databaseManager.updatePlaylist(playlist.getId(), playlist.getName(), playlist.getImagePath());

                playlist_name_view.setText(newName);
                dialog.dismiss();
            }else {
                Toast.makeText(mainActivity,"Field must not be empty",Toast.LENGTH_LONG).show();
            }
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
                image_path = String.valueOf(mainActivity.saveToInternalStorage(bm,playlist.getName()));
            }

            if(image_path.equals("null")) image_path = "placeholder.png";
            playlist.setImagePath(image_path);
            databaseManager.updatePlaylist(playlist.getId(),playlist.getName(),playlist.getImagePath());
            databaseManager.close();
            Bitmap bitmap = mainActivity.loadImageFromStorage(playlist.getName());
            playlist_image_view.setImageBitmap(bitmap);
            AudioAdapter.setSong_pic(bitmap);
            AudioAdapter.notifyDataSetChanged();
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

    public void openAlbum(int id){
        isAlbum = true;
        ImageButton btn_add_songs = mainActivity.findViewById(R.id.btn_add_song_redirect);
        btn_add_songs.setVisibility(View.GONE);
        openView(id);
    }

    private void openView(int id) {
        multi_select_mode = false;
        isListSent = false;
        this.id = id;
        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try {
            databaseManager.open();

            Cursor cursor;
            String mName = null;
            String mImage = null;
            if (isAlbum) {
                cursor = databaseManager.fetchAlbumById(id);
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_NAME));
                    @SuppressLint("Range") String image_path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_PICTURE));
                    mName = name;
                    mImage = image_path;
                }
                loadAudioAlbum("title");
            } else {
                cursor = databaseManager.getPlaylistById(id);
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_NAME));
                    @SuppressLint("Range") String image_path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_PICTURE));
                    mName = name;
                    mImage = image_path;
                }
                loadAudioPlaylist("title");
            }
            playlist_name_view.setText(mName);
                if (mImage!=null && !mImage.equals("placeholder.png")) {
                    Bitmap song_pic_bitmap = mainActivity.loadImageFromStorage(mName);
                    playlist_image_view.setImageBitmap(song_pic_bitmap);
                    playlist_image_view.setClipToOutline(true);
                    AudioAdapter.setSong_pic(song_pic_bitmap);
                }else if (mImage.equals("placeholder.png")){
                    playlist_image_view.setImageResource(R.drawable.placeholder);
                }
                playlist = new Playlist(id, mName, mImage);

                cursor.close();


                ThreadElementAutoSelector.SongList = SongList;
                ThreadElementAutoSelector.AudioAdapter = AudioAdapter;

                TextView sizeView = mainActivity.findViewById(R.id.playlist_size);
                String size = SongList.size()+" songs";
                sizeView.setText(size);

                expandPlaylist();
            } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        finally {

            databaseManager.close();
        }
    }

    private void loadAudioAlbum(String orderOption) {
        SongList.clear();
        Cursor cursor = null;
        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try{
            databaseManager.open();
            cursor = databaseManager.fetchAlbumsSongsFullInfo(id,orderOption);
            if(cursor.moveToFirst()){
                do {
                    @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_SONG_TABLE_SONG_ID)));
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
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(cursor!=null) cursor.close();
            databaseManager.close();
            AudioAdapter.notifyDataSetChanged();
        }
    }

    private void showDeleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity,R.style.CustomAlertDialogTheme);

        builder.setMessage("Are you sure you want to delete " + playlist.getName()+"?");
        if(isAlbum) builder.setMessage("Are you sure you want to delete " + playlist.getName()+" with all songs from the device?");

        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            DatabaseManager databaseManager = new DatabaseManager(mainActivity);
            try {
                databaseManager.open();
                if(isAlbum) {
                    databaseManager.deleteAlbum(id);

                    Cursor cursor = databaseManager.fetchAlbumsSongsFullInfo(id, "title");
                        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File[] files = null;
                        if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
                            files = downloadsFolder.listFiles();
                        }
                        if (cursor.moveToFirst()) {
                            do {
                                @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ID)));
                                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));

                                databaseManager.deleteSong(id);
                                if (files != null) {
                                    for (File file : files)
                                        if (file.isFile() && file.getName().equals(filename))
                                        {
                                            file.delete();
                                            break;
                                        }
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                else {
                    databaseManager.deletePlaylist(id);
                }

                File f = new File(playlist.getImagePath(), playlist.getName()+".jpg");
                if(f.delete()) Toast.makeText(mainActivity,"Playlist deleted",Toast.LENGTH_SHORT).show();

            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }
            finally {
                cleanUp();
                shrinkPlaylist();
                databaseManager.close();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            multi_select_mode = false;
            isListSent=false;
            sendData();
            ThreadElementAutoSelector.isRunning=true;
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void cleanUp(){
        multi_select_mode=false;
        List<Song> lSongList = new ArrayList<>();
        mainActivity.settings.setLast_playlist_id(0);
        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try {
            databaseManager.open();
            Cursor cursor = databaseManager.fetchSongs("title");
            if (cursor.moveToFirst())
                do {
                    @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ID)));
                    @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                    @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                    @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                    @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));
                    @SuppressLint("Range") long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_DURATION)));

                    Song song = new Song(id,filename,name,artist,album,genre,duration);
                    lSongList.add(song);
                }while (cursor.moveToNext());
            cursor.close();

            MyMediaPlayer.setSongList(lSongList);
            CurrentIndex=0;
            MyMediaPlayer.playMedia(CurrentIndex);
            MyMediaPlayer.instance.pause();
            mainActivity.settings.setLast_song_id(lSongList.get(0).getId());
            ThreadElementAutoSelector.isRunning=true;
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setOnPlaylistChangeListener(OnPlaylistChangeListener listener) {
        onPlaylistChangeListener = listener;
    }
}

