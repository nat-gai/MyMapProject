package com.nat.mymapproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class InfoImageAdapter extends FragmentStatePagerAdapter{

    List<String> data;

    public InfoImageAdapter(FragmentManager fm, List<String> data) {
        super(fm);
        this.data = data;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment itemFragment = new ItemImageInfo();

        Bundle args = new Bundle();
        args.putString(ItemImageInfo.COUNT, String.valueOf(getCount()));
        args.putString(ItemImageInfo.IMG_URL, data.get(position));

        itemFragment.setArguments(args);

        return itemFragment;
    }

    @Override
    public int getCount() {
        return data.size();
    }

}
