package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public class PlaylistsTab extends Fragment {

    DatabaseManager databaseManager;

    RelativeLayout get_image_button;
    ImageView chosen_image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseManager = new DatabaseManager(getContext());
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        //databaseManager.dropAllTables();
        //databaseManager.createTables();

        //databaseManager.insertPlaylist("Hello","hello.jpg");


        Cursor cursor = databaseManager.fetchPlaylists();

        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String ID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_ID));
                Toast.makeText(getContext(),""+ID,Toast.LENGTH_SHORT).show();

                Log.i("DATABASE_TAG", "I have read ID : "+ID);

            }while (cursor.moveToNext());
        }


    }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_playlists, container, false);
            GridLayout gridLayout = view.findViewById(R.id.playlist_card_container);

            CardView add_playlist_card_View = (CardView) inflater.inflate(R.layout.add_playlist_card, gridLayout, false);
            add_playlist_card_View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                }
            });
            gridLayout.addView(add_playlist_card_View);



            View.OnClickListener playlist_name_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView textView = v.findViewById(R.id.cardText);
                    String text = (String) textView.getText();
                        Toast.makeText(getContext(),text, Toast.LENGTH_SHORT).show();
                }
            };


            for (int i = 0; i < 8; i++) {
                CardView cardView = (CardView) inflater.inflate(R.layout.playlist_card, gridLayout, false);

                TextView cardText = cardView.findViewById(R.id.cardText);
                cardText.setText(""+i);

                cardView.setOnClickListener(playlist_name_listener);
                gridLayout.addView(cardView);
            }

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
                    String image_name = String.valueOf(chosen_image.getTag());
                    if(image_name == null) image_name = "placeholder.png";
                    databaseManager.insertPlaylist(name,image_name);
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


    }

