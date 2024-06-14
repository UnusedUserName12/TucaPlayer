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
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new AlbumsTab();
                break;
            case 1:
                fragment = new PlaylistsTab();
                break;
            case 2:
                fragment = new SongsTab();
                break;
            case 3:
                fragment = new SongsTab();
            default:
                fragment = new SongsTab();
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
