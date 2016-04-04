package com.greyogproducts.greyog.fts2;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by greyog on 28/03/16.
 */
public class LoadThread extends AsyncTask<Void, Void, Void> {
    private String URL1 = "http://tsw.forexprostools.com/index.php?timeframe=60";
    private String URL2 = "http://tsw.forexprostools.com/index.php?timeframe=300";
    private String URL3 = "http://tsw.forexprostools.com/index.php?timeframe=900";
    private String URL4 = "http://tsw.forexprostools.com/index.php?timeframe=1800";
    private String URL5 = "http://tsw.forexprostools.com/index.php?timeframe=3600";
    private String URL6 = "http://tsw.forexprostools.com/index.php?timeframe=18000";
    private String URL7 = "http://tsw.forexprostools.com/index.php?timeframe=86400";
    private String URL8 = "http://tsw.forexprostools.com/index.php?timeframe=week";
    private String[] URLs = new String[] {URL1,URL2,URL3,URL4,URL5,URL6,URL7,URL8};
    private String URLforex = "&forex=1,1691,2186,2,3,9,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20";
    private String URLcommodities = "&commodities=8830,8836,8910,8831,8833,8849,8832";
    private String URLindices = "&indices=172,175,%20167,27,166,179,40830";
    private String URLstocks = "&stocks=282,7888,284,9251,941155,243,6408&tabs=1,2,3,4&amp;forex=1,1691,2186,2,3,9,5,6,7,8&amp;commodities=8830,8836,8910,8831,8833,8849,8832&amp;indices=172,175,%20167,27,166,179,40830&amp;stocks=282,7888,284,9251,941155,243,6408&amp;tabs=1,2,3,4";
    private ProgressDialog mProgressDialog;
    private MainActivity activity;
    private ArrayList<Document> docs;

    public LoadThread(MainActivity mainActivity) {
        activity = mainActivity;
        // Create a progressdialog
        mProgressDialog = new ProgressDialog(mainActivity);
        // Set progressdialog title
        mProgressDialog.setTitle("Technical Summary");
        // Set progressdialog message
        mProgressDialog.setMessage("Updating...");
        mProgressDialog.setIndeterminate(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        for (int i = 0; i < URLs.length; i++) {
            URLs[i] +=URLforex+URLcommodities+URLindices+URLstocks;
        }
        docs = new ArrayList<>();
        activity.db.open();
        activity.db.getDB().rawQuery("delete from "+activity.db.getDefTable(),null);
        // Show progressdialog
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int i = 0; i < URLs.length; i++) {
            String url = URLs[i];
            try {
                Log.d("Tag", "Trying to connect " + url);
                Document doc = Jsoup.connect(url).get();
                docs.add(doc);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Tag", "Failed to connect");
                activity.disabledTimeFrames.add(i);
            }
        }
        fillDB();
        fillGroupData();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (docs.isEmpty()) {
            Toast.makeText(activity, "Nothing to show", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(activity, "OK", Toast.LENGTH_SHORT).show();

        }
        activity.db.close();
        activity.getSectionsPagerAdapter().notifyDataSetChanged();
//        MainActivity.fillList();
        mProgressDialog.dismiss();
    }

    private void fillGroupData() {
        Cursor cGroupData = activity.db.getGroupData();
        ArrayList<Map<String,String>> mGroupData;
        ArrayList<ArrayList<Map<String,String>>> mChildData;
        if (cGroupData != null) {
            if (cGroupData.moveToFirst()) {
                String str;
                mGroupData = new ArrayList<>();
                mChildData = new ArrayList<>();
                do {
                    str = "";
                    Map<String,String> m = new HashMap<>();
                    for (String cn : cGroupData.getColumnNames()) {
                        String me = cGroupData.getString(cGroupData.getColumnIndex(cn));
                        m.put(cn, me);
                        str = str.concat(cn + ":" + me + "; ");
                    }
                    Log.d("Tag", str);
                    mGroupData.add(m);
                    str = m.get(Constants.ATTR_GROUP_NAME);
                    fillChildData(str,mChildData);
                } while (cGroupData.moveToNext());
                activity.data = mGroupData;
                activity.childData = mChildData;
            }
            cGroupData.close();

        } else
            Log.d("Tag", "Cursor is null");
    }

    private void fillChildData(String groupName, ArrayList<ArrayList<Map<String,String>>> childData) {
        Cursor cChildData = activity.db.getChildData(groupName);
        ArrayList<Map<String,String>> childDataItem;
        if (cChildData != null) {
            if (cChildData.moveToFirst()) {
                String str;
                do {
                    str = groupName+": ";
                    childDataItem = new ArrayList<>();
                    Map<String,String> m = new HashMap<>();
                    for (String cn : cChildData.getColumnNames()) {
                        String me = cChildData.getString(cChildData.getColumnIndex(cn));
                        m.put(cn, me);
                        str = str.concat(cn + ":" + me + "; ");
                    }
                    Log.d("Tag", str);
                    childDataItem.add(m);
                } while (cChildData.moveToNext());
                childData.add(childDataItem);
            }
            cChildData.close();

        } else
            Log.d("Tag", "Child Cursor is null. Group: "+groupName);
    }

    private void fillDB(){
        String summaryName,technicalSummary,maBuy,maSell,tiBuy,tiSell,summaryLast,timeFrame,updateTime;

        for (Document doc : docs) {
            if (doc != null) {
//                updateTime = doc.getElementById("updateTime").text();
                Element tmFrm = doc.getElementById("timeframe");
                timeFrame = tmFrm.getElementsByAttribute("selected").text();
//                Log.d("Tag", updateTime + timeFrame);
                String attrSum = Constants.ATTR_ADVICE_1min;
                String attrMaBuy = Constants.ATTR_ADVICE_1min_MABuy;
                String attrMaSell = Constants.ATTR_ADVICE_1min_MASell;
                String attrIndBuy = Constants.ATTR_ADVICE_1min_IndBuy;
                String attrIndSell = Constants.ATTR_ADVICE_1min_IndSell;
                switch (timeFrame) {
                    case "5 mins":
                        attrSum = Constants.ATTR_ADVICE_5min;
                        attrMaBuy = Constants.ATTR_ADVICE_5min_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_5min_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_5min_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_5min_IndSell;
                        break;
                    case "15 mins":
                        attrSum = Constants.ATTR_ADVICE_15min;
                        attrMaBuy = Constants.ATTR_ADVICE_15min_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_15min_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_15min_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_15min_IndSell;
                        break;
                    case "30 mins":
                        attrSum = Constants.ATTR_ADVICE_30min;
                        attrMaBuy = Constants.ATTR_ADVICE_30min_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_30min_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_30min_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_30min_IndSell;
                        break;
                    case "Hourly":
                        attrSum = Constants.ATTR_ADVICE_1Hour;
                        attrMaBuy = Constants.ATTR_ADVICE_1Hour_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_1Hour_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_1Hour_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_1Hour_IndSell;
                        break;
                    case "5 Hours":
                        attrSum = Constants.ATTR_ADVICE_5Hour;
                        attrMaBuy = Constants.ATTR_ADVICE_5Hour_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_5Hour_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_5Hour_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_5Hour_IndSell;
                        break;
                    case "Daily":
                        attrSum = Constants.ATTR_ADVICE_Day;
                        attrMaBuy = Constants.ATTR_ADVICE_Day_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_Day_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_Day_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_Day_IndSell;
                        break;
                    case "Weekly":
                        attrSum = Constants.ATTR_ADVICE_Week;
                        attrMaBuy = Constants.ATTR_ADVICE_Week_MABuy;
                        attrMaSell = Constants.ATTR_ADVICE_Week_MASell;
                        attrIndBuy = Constants.ATTR_ADVICE_Week_IndBuy;
                        attrIndSell = Constants.ATTR_ADVICE_Week_IndSell;
                        break;
                }
                for (Element mainSumDiv : doc.getElementsByAttributeValueContaining("id", "mainSummaryDiv")) {
                    summaryName = mainSumDiv.getElementById("summaryName").text();
                    summaryLast = mainSumDiv.getElementById("summaryLast").text();
                    technicalSummary = mainSumDiv.getElementById("technicalSummary").text();
                    maBuy = mainSumDiv.getElementById("maBuy").text();
                    maSell = mainSumDiv.getElementById("maSell").text();
                    tiBuy = mainSumDiv.getElementById("tiBuy").text();
                    tiSell = mainSumDiv.getElementById("tiSell").text();
//                    Log.d("Tag",timeFrame+ summaryName + summaryLast + technicalSummary + maBuy + maSell + tiBuy + tiSell);
                    ContentValues cv = new ContentValues();
                    summaryName = ""+summaryName+"";
                    cv.put(Constants.ATTR_PRICE, summaryLast);
                    cv.put(attrSum, technicalSummary);
                    cv.put(attrIndBuy, tiBuy);
                    cv.put(attrIndSell, tiSell);
                    cv.put(attrMaBuy, maBuy);
                    cv.put(attrMaSell, maSell);
                    //try to update field
                    int updCount = activity.db.getDB().update(activity.db.getDefTable(), cv, Constants.ATTR_GROUP_NAME + "=?", new String[]{summaryName});
//                    Log.d("Tag", "Updated " + String.valueOf(updCount));
                    if ( updCount == 0) {
                        // заполним таблицу
                        cv.put(Constants.ATTR_GROUP_NAME, summaryName);
                        long id = activity.db.getDB().insert(activity.db.getDefTable(), null, cv);
//                        Log.d("Tag","Inserted "+String.valueOf(id));
                    }
                }
            }
        }
    }
}
