package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public class PlaylistsTab extends Fragment {

    DatabaseManager databaseManager;

    RelativeLayout get_image_button;
    ImageView chosen_image;
    GridLayout gridLayout;
    LayoutInflater inflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseManager = new DatabaseManager(getContext());
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
        @Override
        public View onCreateView(LayoutInflater inflaterr, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.inflater = inflaterr;
            View view = inflater.inflate(R.layout.fragment_playlists, container, false);
            gridLayout = view.findViewById(R.id.playlist_card_container);
            updatePlaylistLayout(gridLayout,inflater);

            return view;
        }

        private void showDialog(){
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.add_playlist_dialog);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.corners25dp);

            Button cancel_btn = dialog.findViewById(R.id.cancel_button);
            Button create_btn = dialog.findViewById(R.id.create_playlist_button);
            EditText playlist_name_field = dialog.findViewById(R.id.insert_playlist_name);
            get_image_button = dialog.findViewById(R.id.insert_image);
            chosen_image = dialog.findViewById(R.id.chosenImage);
            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            create_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = String.valueOf(playlist_name_field.getText());
                    String image_path = "null";
                    if(chosen_image.getDrawable()!=null){
                        Bitmap bm=((BitmapDrawable)chosen_image.getDrawable()).getBitmap();
                        image_path = String.valueOf(saveToInternalStorage(bm));
                    }

                    if(image_path.equals("null")) image_path = "placeholder.png";
                    Toast.makeText(getContext(),image_path,Toast.LENGTH_SHORT).show();
                    databaseManager.insertPlaylist(name,image_path);
                    updatePlaylistLayout(gridLayout,inflater);
                    dialog.dismiss();
                }
            });

            get_image_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(i, "Select Picture"), 3);
                }
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

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

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

    private void loadImageFromStorage(String path, ImageView v)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            v.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private void updatePlaylistLayout(GridLayout gridLayout,LayoutInflater inflater){
        gridLayout.removeAllViews();
        Cursor cursor = databaseManager.fetchPlaylists();

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
                ImageView imageView = cardView.findViewById(R.id.cardImage);
                loadImageFromStorage(image_path,imageView);

                View.OnClickListener playlist_name_listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView textView = v.findViewById(R.id.cardText);
                        String text = (String) textView.getText();
                        Toast.makeText(getContext(),text, Toast.LENGTH_SHORT).show();
                    }
                };

                cardView.setOnClickListener(playlist_name_listener);
                gridLayout.addView(cardView);

            }while (cursor.moveToNext());
        }
        CardView add_playlist_card_View = (CardView) inflater.inflate(R.layout.add_playlist_card, gridLayout, false);
        add_playlist_card_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        gridLayout.addView(add_playlist_card_View);
    }


    }

