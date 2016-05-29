package com.greyogproducts.greyog.fts;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter implements MainActivity.onListHeaderClickListener {

    final int pageCount = 4;
    private Map<Integer, String> mFragmentTags;
    private FragmentManager mFragmentManager;
    private Context mContext;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragmentManager = fm;
        mContext = context;
        mFragmentTags = new HashMap<>();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (Fragment fragment : mFragmentManager.getFragments()) {
            ((PlaceholderFragment) fragment).resetListAdapter();
//            Log.d("Tag", "notifyDataSetChanged " + fragment.getArguments().getInt("section_number"));
        }

    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return pageCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Forex";
            case 1:
                return "Commodities";
            case 2:
                return "Indices";
            case 3:
                return "Stock";
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        if (object instanceof Fragment) {
            Fragment f = (Fragment) object;
            String tag = f.getTag();
//            Log.d("Tag", "instantiateItem " + tag);
            mFragmentTags.put(position, tag);
        }
        return object;
    }

    public Fragment getFragment(int position) {
        String tag = mFragmentTags.get(position);
        if (tag == null) {
            return null;
        }
        return mFragmentManager.findFragmentByTag(tag);
    }

    @Override
    public void onListHeaderClick(int column) {
        Toast.makeText(mContext, "Sort by column " + column, Toast.LENGTH_SHORT).show();
        for (Fragment fragment : mFragmentManager.getFragments()) {
            if (fragment.isVisible()) {
                ((PlaceholderFragment) fragment).setSortColumn(column);
//            Log.d("Tag", "onListHeaderClick " + fragment.getArguments().getInt("section_number"));
            }
        }
    }
}
