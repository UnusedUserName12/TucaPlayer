package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PlaylistsTab playlistsTab = new PlaylistsTab();
        SongsTab songsTab = new SongsTab();

        switch (position){
            case 0: return playlistsTab;
            case 1: return songsTab;
            default: return songsTab;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
