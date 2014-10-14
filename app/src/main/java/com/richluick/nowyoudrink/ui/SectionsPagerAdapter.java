package com.richluick.nowyoudrink.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.richluick.nowyoudrink.R;

import java.util.Locale;

/**
 * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        switch (position) {
            case 0:
                return new InboxFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new FriendsFragment();
        }

        return null;
    }

    @Override
    public int getCount() { return 3; }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getString(R.string.title_inbox).toUpperCase(l);
            case 1:
                return mContext.getString(R.string.title_groups).toUpperCase(l);
            case 2:
                return mContext.getString(R.string.title_friends).toUpperCase(l);
        }
        return null;
    }

    public int getIcon(int position) {
        switch (position) {
            case 0:
                return R.drawable.ic_action_content_email;
            case 1:
                return R.drawable.ic_action_social_group;
            case 2:
                return R.drawable.ic_action_social_friends;
        }
        return R.drawable.ic_action_social_group;
    }
}