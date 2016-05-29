package com.greyogproducts.greyog.fts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.greyogproducts.greyog.fts2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_OLD_SORT_COLUMN = "old_column";
    private MyOtherListAdapter sctAdapter;
    private View rootView;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putInt(ARG_OLD_SORT_COLUMN, -1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        resetListAdapter();
        return rootView;
    }

    public void resetListAdapter() {
        ExpandableListView elvMain;
        if (rootView == null || MainActivity.baseData == null) {
            return;
        }
        int sectionNumber = this.getArguments().getInt(ARG_SECTION_NUMBER);
        Log.d("Tag", "resetListAdapter: sectionNumber = " + sectionNumber);
        elvMain = (ExpandableListView) rootView.findViewById(R.id.mainList);
//        Log.d("Tag", "Base adapter count: " + String.valueOf(MainActivity.baseData.size()));

        ArrayList<Map<String, String>> data;
        ArrayList<ArrayList<Map<String, String>>> childData;
        data = new ArrayList<>();
        childData = new ArrayList<>();
        for (int i = 0; i < MainActivity.baseData.size(); i++) {
            Map<String, String> map = MainActivity.baseData.get(i);
            String strTabNum = map.get(Constants.ATTR_TAB_NUM);
//            String strName = map.get(Constants.ATTR_GROUP_NAME);

            int groupTabNum = Integer.parseInt(strTabNum);
//            Log.d("Tag", "resetListAdapter Page: "+String.valueOf(sectionNumber)+strName+" getTab: " + String.valueOf(groupTabNum));
            if (groupTabNum == sectionNumber) {
                data.add(map);
                childData.add(MainActivity.baseChildData.get(i));
            }
        }

        Comparator<Map<String, String>> defaultComparator = new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                String sort = "groupName";
                String strL = lhs.get(sort);
                String strR = rhs.get(sort);
                return strL.compareTo(strR);
            }
        };
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final int mSortColumn = mPreferences.getInt("sort", 0);
        int oldSortColumn = mPreferences.getInt("oldSort_" + sectionNumber, -1);
        int order = mPreferences.getInt("oldSortOrder_" + sectionNumber, 1);
        Log.d("Tag", "resetListAdapter: mSortColumn = " + mSortColumn);
        Log.d("Tag", "resetListAdapter: oldSortColumn = " + oldSortColumn);
        if (mSortColumn == oldSortColumn) order = (-1) * order;
        Log.d("Tag", "resetListAdapter: order = " + order);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("oldSortOrder_" + sectionNumber, order);
        editor.putInt("oldSort_" + sectionNumber, mSortColumn);
        editor.commit();
        final int finalOrder = order;
        Comparator<Map<String, String>> comparator = new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                String sort;
                switch (mSortColumn) {
                    case 1:
                        sort = "one_min";
                        break;
                    case 2:
                        sort = "five_min";
                        break;
                    case 3:
                        sort = "onefive_min";
                        break;
                    case 4:
                        sort = "thirty_min";
                        break;
                    case 5:
                        sort = "one_hour";
                        break;
                    case 6:
                        sort = "five_hour";
                        break;
                    case 7:
                        sort = "Day";
                        break;
                    case 8:
                        sort = "Week";
                        break;
                    default:
                        sort = "groupName";
                }
                String strL = lhs.get(sort);
                String strR = rhs.get(sort);

                if (finalOrder < 0) {
                    String t = strL;
                    strL = strR;
                    strR = t;
                }
                if (mSortColumn == 0) return strL.compareTo(strR);
                else {
                    Integer digL = signalToDigit(strL);
                    Integer digR = signalToDigit(strR);
                    return digL.compareTo(digR);
                }

            }
        };
        Collections.sort(data, defaultComparator);
        Collections.sort(data, comparator);

//        Log.d("Tag", "resetListAdapter Page: "+String.valueOf(sectionNumber)+" adapter count: " + String.valueOf(data.size()));
        String[] groupFrom = new String[]{Constants.ATTR_GROUP_NAME,
                Constants.ATTR_PRICE,
                Constants.ATTR_ADVICE_1min,
                Constants.ATTR_ADVICE_5min,
                Constants.ATTR_ADVICE_15min,
                Constants.ATTR_ADVICE_30min,
                Constants.ATTR_ADVICE_1Hour,
                Constants.ATTR_ADVICE_5Hour,
                Constants.ATTR_ADVICE_Day,
                Constants.ATTR_ADVICE_Week};
        int[] groupTo = new int[]{R.id.tvPairName,
                R.id.tvPairPrice,
                R.id.tv1min,
                R.id.tv5min,
                R.id.tv15min,
                R.id.tv30min,
                R.id.tv1hour,
                R.id.tv5Hour,
                R.id.tvDay,
                R.id.tvWeek};
        String[] childFrom = new String[]{Constants.ATTR_ADVICE_1min_MABuy,
                Constants.ATTR_ADVICE_5min_MABuy,
                Constants.ATTR_ADVICE_15min_MABuy,
                Constants.ATTR_ADVICE_30min_MABuy,
                Constants.ATTR_ADVICE_1Hour_MABuy,
                Constants.ATTR_ADVICE_5Hour_MABuy,
                Constants.ATTR_ADVICE_Day_MABuy,
                Constants.ATTR_ADVICE_Week_MABuy,
                Constants.ATTR_ADVICE_1min_MASell,
                Constants.ATTR_ADVICE_5min_MASell,
                Constants.ATTR_ADVICE_15min_MASell,
                Constants.ATTR_ADVICE_30min_MASell,
                Constants.ATTR_ADVICE_1Hour_MASell,
                Constants.ATTR_ADVICE_5Hour_MASell,
                Constants.ATTR_ADVICE_Day_MASell,
                Constants.ATTR_ADVICE_Week_MASell,
                Constants.ATTR_ADVICE_1min_IndBuy,
                Constants.ATTR_ADVICE_5min_IndBuy,
                Constants.ATTR_ADVICE_15min_IndBuy,
                Constants.ATTR_ADVICE_30min_IndBuy,
                Constants.ATTR_ADVICE_1Hour_IndBuy,
                Constants.ATTR_ADVICE_5Hour_IndBuy,
                Constants.ATTR_ADVICE_Day_IndBuy,
                Constants.ATTR_ADVICE_Week_IndBuy,
                Constants.ATTR_ADVICE_1min_IndSell,
                Constants.ATTR_ADVICE_5min_IndSell,
                Constants.ATTR_ADVICE_15min_IndSell,
                Constants.ATTR_ADVICE_30min_IndSell,
                Constants.ATTR_ADVICE_1Hour_IndSell,
                Constants.ATTR_ADVICE_5Hour_IndSell,
                Constants.ATTR_ADVICE_Day_IndSell,
                Constants.ATTR_ADVICE_Week_IndSell};
        int[] childTo = new int[]{R.id.tv1minMABuy,
                R.id.tv5minMABuy,
                R.id.tv15minMABuy,
                R.id.tv30minMABuy,
                R.id.tv1hourMABuy,
                R.id.tv5HourMABuy,
                R.id.tvDayMABuy,
                R.id.tvWeekMABuy,
                R.id.tv1minMASell,
                R.id.tv5minMASell,
                R.id.tv15minMASell,
                R.id.tv30minMASell,
                R.id.tv1hourMASell,
                R.id.tv5HourMASell,
                R.id.tvDayMASell,
                R.id.tvWeekMASell,
                R.id.tv1minIndicatorBuy,
                R.id.tv5minIndicatorBuy,
                R.id.tv15minIndicatorBuy,
                R.id.tv30minIndicatorBuy,
                R.id.tv1hourIndicatorBuy,
                R.id.tv5HourIndicatorBuy,
                R.id.tvDayIndicatorBuy,
                R.id.tvWeekIndicatorBuy,
                R.id.tv1minIndicatorSell,
                R.id.tv5minIndicatorSell,
                R.id.tv15minIndicatorSell,
                R.id.tv30minIndicatorSell,
                R.id.tv1hourIndicatorSell,
                R.id.tv5HourIndicatorSell,
                R.id.tvDayIndicatorSell,
                R.id.tvWeekIndicatorSell};
//        for (Map<String, String> map : MainActivity.data) {
//            for (String s : map.values()) {
//                Log.d("Tag",s);
//            }
//        }
        sctAdapter = new MyOtherListAdapter(getContext(), data,
                R.layout.group_list_item,                     // Your row layout for a group
                groupFrom,                      // Field(s) to use from group cursor
                groupTo,                 // Widget ids to put group data into
                childData,
                R.layout.child_list_item,                 // Your row layout for a child
                childFrom,  // Field(s) to use from child cursors
                childTo);          // Widget ids to put child data into

        elvMain.setAdapter(sctAdapter);
//        Log.d("Tag", "finish");
    }

    private int signalToDigit(String signal) {
        int result = 0;
        switch (signal) {
            case Constants.BUY:
                result = 2;
                break;
            case Constants.STRONG_BUY:
                result = 1;
                break;
            case Constants.NEUTRAL:
                result = 3;
                break;
            case Constants.SELL:
                result = 4;
                break;
            case Constants.STRONG_SELL:
                result = 5;
                break;
        }
        return result;
    }

    public void setSortColumn(int sortColumn) {
        Log.d("Tag", "setSortColumn: " + sortColumn);
//        this.mSortColumn = sortColumn;
        resetListAdapter();
    }
}
