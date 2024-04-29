package com.example.myapplication;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;


public class PlaylistsTab extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_playlists, container, false);
            GridLayout gridLayout = view.findViewById(R.id.playlist_card_container);

            View.OnClickListener listener = new View.OnClickListener() {
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

                cardView.setOnClickListener(listener);
                gridLayout.addView(cardView);
            }

            return view;
        }


    }

