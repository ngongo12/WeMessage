package com.wemessage.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.wemessage.fragment.FriendFragment;
import com.wemessage.fragment.GroupFragment;
import com.wemessage.fragment.MessageFragment;
import com.wemessage.fragment.OtherFragment;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    int numOfTab;
    MessageFragment messageFragment;
    FriendFragment friendFragment;
    GroupFragment groupFragment;
    OtherFragment otherFragment;

    public MainViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        numOfTab = 4;
        messageFragment = new MessageFragment();
        friendFragment = new FriendFragment();
        groupFragment = new GroupFragment();
        otherFragment = new OtherFragment();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position)
        {
            case 0: fragment = messageFragment; break;
            case 1: fragment = friendFragment; break;
            case 2: fragment = groupFragment; break;
            case 3: fragment = otherFragment; break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
