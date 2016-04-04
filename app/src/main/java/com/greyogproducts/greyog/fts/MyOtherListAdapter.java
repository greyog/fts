package com.greyogproducts.greyog.fts;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.greyogproducts.greyog.fts2.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by greyog on 2/04/16.
 */
public class MyOtherListAdapter extends SimpleExpandableListAdapter {
    public MyOtherListAdapter(Context context, List<? extends Map<String, ?>> groupData, int groupLayout, String[] groupFrom, int[] groupTo, List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View mView = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        ArrayList<TextView> textViews = new ArrayList<>();
        textViews.add((TextView) mView.findViewById(R.id.tv1min));
        textViews.add((TextView) mView.findViewById(R.id.tv5min));
        textViews.add((TextView) mView.findViewById(R.id.tv15min));
        textViews.add((TextView) mView.findViewById(R.id.tv30min));
        textViews.add((TextView) mView.findViewById(R.id.tv1hour));
        textViews.add((TextView) mView.findViewById(R.id.tv5Hour));
        textViews.add((TextView) mView.findViewById(R.id.tvDay));
        textViews.add((TextView) mView.findViewById(R.id.tvWeek));
        for (TextView textView : textViews) {
            setTextColor(textView);
        }
        return mView;
    }

    private Void setTextColor(TextView tv) {
        if (tv != null) {
            if (tv.getText().equals(Constants.SELL)) {
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.RED);
            }
            if (tv.getText().equals(Constants.STRONG_SELL)) {
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.RED);
            }
            if (tv.getText().equals(Constants.BUY)) {
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GREEN);
            }
            if (tv.getText().equals(Constants.STRONG_BUY)) {
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GREEN);
            }
            if (tv.getText().equals(Constants.NEUTRAL)) {
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(tv.getDrawingCacheBackgroundColor());
            }
        }
        return null;
    }
}
