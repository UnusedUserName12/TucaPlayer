package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.interfaces.OnPlaylistChangeListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLDataException;
import java.sql.SQLException;


public class PlaylistsTab extends Fragment implements OnPlaylistChangeListener {
    RelativeLayout get_image_button;
    ImageView chosen_image;
    GridLayout gridLayout;
    LayoutInflater inflater;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        PlaylistView.setOnPlaylistChangeListener(this);
    }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.inflater = inflater;
            View view = inflater.inflate(R.layout.fragment_playlists, container, false);
            gridLayout = view.findViewById(R.id.playlist_card_container);
            updatePlaylistLayout();

            return view;
        }

        private void showDialog(){
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.add_playlist_dialog);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.corners25dp);

            Button cancel_btn = dialog.findViewById(R.id.cancel_button);
            Button create_btn = dialog.findViewById(R.id.create_playlist_button);
            EditText playlist_name_field = dialog.findViewById(R.id.insert_playlist_name);
            get_image_button = dialog.findViewById(R.id.insert_image);
            chosen_image = dialog.findViewById(R.id.chosenImage);

            cancel_btn.setOnClickListener(v -> dialog.cancel());

            create_btn.setOnClickListener(v -> {
                String name = String.valueOf(playlist_name_field.getText());
                String image_path = "null";
                name = name.trim();
                if(!name.isEmpty()) {
                    if (chosen_image.getDrawable() != null) {
                        Bitmap bm = ((BitmapDrawable) chosen_image.getDrawable()).getBitmap();
                        image_path = saveToInternalStorage(bm, name);
                    }

                    if (image_path.equals("null")) image_path = "placeholder.png";
                    DatabaseManager databaseManager = new DatabaseManager(context);
                    try {
                        databaseManager.open();
                        databaseManager.insertPlaylist(name, image_path);
                    } catch (SQLiteConstraintException | SQLDataException e) {
                        Toast.makeText(context, "Playlist already exists", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } finally {
                        databaseManager.close();
                    }
                    updatePlaylistLayout();
                    dialog.dismiss();
                }else {
                    Toast.makeText(getContext(),"Playlist name must not be empty",Toast.LENGTH_LONG).show();
                }
            });

            get_image_button.setOnClickListener(v -> {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(i, "Select Picture"), 3);
            });

            dialog.show();
        }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 3) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    chosen_image.setImageURI(selectedImageUri);
                    chosen_image.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String playlistName){
        ContextWrapper cw = new ContextWrapper(context);
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

    @Override
    public void onResume() {
        super.onResume();
        updatePlaylistLayout();
    }

    public void updatePlaylistLayout() {
        gridLayout.removeAllViews();
        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        Cursor cursor = databaseManager.fetchPlaylists();

        MainActivity mainActivity = (MainActivity) getActivity();

        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String ID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_NAME));
                @SuppressLint("Range") String image_path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_PICTURE));

                CardView cardView = (CardView) inflater.inflate(R.layout.playlist_card, gridLayout, false);
                TextView playlistID = cardView.findViewById(R.id.playlistID);
                playlistID.setText(ID);
                TextView cardText = cardView.findViewById(R.id.cardText);
                cardText.setText(name);
                ImageView cardImage = cardView.findViewById(R.id.cardImage);

                if(!image_path.equals("placeholder.png") && mainActivity!=null) cardImage.setImageBitmap(mainActivity.loadImageFromStorage(name));


                //Open playlist_view on card click
                View.OnClickListener playlist_id_listener = v -> {
                    TextView textView = v.findViewById(R.id.playlistID);
                    int id = Integer.parseInt((String) textView.getText());
                    if (mainActivity != null) {
                        mainActivity.getPlaylistView().openPlaylist(id);
                    }
                };

                cardView.setOnClickListener(playlist_id_listener);
                gridLayout.addView(cardView);

            }while (cursor.moveToNext());
        }
        cursor.close();
        databaseManager.close();
        CardView add_playlist_card_View = (CardView) inflater.inflate(R.layout.add_playlist_card, gridLayout, false);

        add_playlist_card_View.setOnClickListener(v -> showDialog());

        gridLayout.addView(add_playlist_card_View);
    }

    @Override
    public void OnPlaylistChanged() {
        updatePlaylistLayout();
    }
}

