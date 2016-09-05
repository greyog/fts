package com.greyogproducts.greyog.fts;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.greyogproducts.greyog.fts2.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {


    public static ArrayList<Map<String, String>> baseData;
    public static ArrayList<ArrayList<Map<String, String>>> baseChildData;
    public static DB db;
    public static ArrayList<Integer> disabledTimeFrames;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private static Context ctx;
    public SharedPreferences preferences;
    InterstitialAd mInterstitialAd;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private AdView mAdView;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private long timerInterval;
    private MenuItem btUpdate;
    private boolean isUpdating = false;

    public static SectionsPagerAdapter getSectionsPagerAdapter() {
        return mSectionsPagerAdapter;
    }

    public static Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this.getApplicationContext();
        // подключаемся к БД
        db = new DB(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        doCheckTimerIsOn(preferences);
        disabledTimeFrames = new ArrayList<>();
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.

        mAdView = (AdView) findViewById(R.id.adView);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        } finally {
            reloadContent();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getApplicationContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("Tag", "MainActivity.onSharedPreferenceChanged key : " + key);
                if (key.equals("forexFilter") || key.equals("commoditiesFilter") || key.equals("indicesFilter") || key.equals("stocksFilter")) {
                    reloadContent();
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (timerInterval != Integer.parseInt(preferences.getString("edTimerInterval", "5"))) {
            doCheckTimerIsOn(preferences);
        }
    }

    private void doCheckTimerIsOn(SharedPreferences sharedPreferences) {
        Log.d("Tag", "doCheckTimerIsOn timer on : " + sharedPreferences.getBoolean("swTimerOn", false));
        if (mTimer != null) {
            mTimer.cancel();
        }
        // re-schedule timer here
        // otherwise, IllegalStateException of
        // "TimerTask is scheduled already"
        // will be thrown
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        long updTime = Integer.parseInt(sharedPreferences.getString("edTimerInterval", "5"));
        if (updTime < 1) {
            updTime = 1;
        }
        Log.d("Tag", "doCheckTimerIsOn update time : " + String.valueOf(updTime));
        timerInterval = updTime;
        updTime = updTime * 60000;
        mTimer.scheduleAtFixedRate(mMyTimerTask, updTime, updTime);
    }

    private void reloadContent() {
        if (!isUpdating) {
            new LoadThread().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        btUpdate = menu.findItem(R.id.action_update);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    public void doActionDonate(MenuItem item) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_url)));
        startActivity(browserIntent);
    }

    public void doActionUpdate(MenuItem item) {
        reloadContent();
        doCheckTimerIsOn(preferences);
    }

    public void doActionAbout(MenuItem item) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void doActionPrefs(MenuItem item) {
        startActivity(new Intent(this, PrefsActivity.class));
    }

    public void doDefaultSort(MenuItem item) {
        onHeaderClick(null);
    }

    public void onHeaderClick(View view) {
        int column = 0;
        if (view != null) {
            switch (view.getId()) {
                case R.id.tvColumn1m:
                    column = 1;
                    break;
                case R.id.tvColumn5m:
                    column = 2;
                    break;
                case R.id.tvColumn15m:
                    column = 3;
                    break;
                case R.id.tvColumn30m:
                    column = 4;
                    break;
                case R.id.tvColumn1H:
                    column = 5;
                    break;
                case R.id.tvColumn5H:
                    column = 6;
                    break;
                case R.id.tvColumnD:
                    column = 7;
                    break;
                case R.id.tvColumnW:
                    column = 8;
                    break;
            }
        }
        Log.d("Tag", "onHeaderClick: column = " + column);
        mSectionsPagerAdapter.onListHeaderClick(column);
    }

    public interface onListHeaderClickListener {
        void onListHeaderClick(int column);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (preferences.getBoolean("swTimerOn", false)) {
                        Log.d("Tag", "MyTimerTask runOnUiThread");
                        reloadContent();
                    }
                }
            });
        }
    }

    public class LoadThread extends AsyncTask<Void, Void, Void> {
        ArrayList<String> urlsList;
        Set<String> forexStringSet;
        Set<String> commoditiesStringSet;
        Set<String> indicesStringSet;
        Set<String> stocksStringSet;
        private String URL1 = "http://tsw.forexprostools.com/index.php?timeframe=60";
        private String URL2 = "http://tsw.forexprostools.com/index.php?timeframe=300";
        private String URL3 = "http://tsw.forexprostools.com/index.php?timeframe=900";
        private String URL4 = "http://tsw.forexprostools.com/index.php?timeframe=1800";
        private String URL5 = "http://tsw.forexprostools.com/index.php?timeframe=3600";
        private String URL6 = "http://tsw.forexprostools.com/index.php?timeframe=18000";
        private String URL7 = "http://tsw.forexprostools.com/index.php?timeframe=86400";
        private String URL8 = "http://tsw.forexprostools.com/index.php?timeframe=week";
        private String[] URLs = new String[]{URL1, URL2, URL3, URL4, URL5, URL6, URL7, URL8};
        private String[] defForex = new String[]{"1", "2", "3", "9", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "53", "55", "1691", "2186"};
        private String URLforex = "&forex=";
        private String[] defCommodities = new String[]{"8830", "8836", "8910", "8831", "8833"};
        private String URLcommodities = "&commodities=";
        private String[] defIndices = new String[]{"172", "175", "167", "27", "166"};
        private String URLindices = "&indices=";
        private String[] defStocks = new String[]{"282", "7888", "284", "9251", "941155"};
        private String URLstocks = "&stocks=";
        private String URLtabs = "&tabs=1,2,3,4";
        private ProgressDialog mProgressDialog;
        private ArrayList<Document> docs;
        private String msg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isUpdating = true;
            if (btUpdate != null) {
                btUpdate.setEnabled(false);
                invalidateOptionsMenu();
            }
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Technical Summary");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);

            Set<String> defForexStringSet = new HashSet<>();
            defForexStringSet.addAll(Arrays.asList(defForex));
            forexStringSet = preferences.getStringSet("forexFilter", defForexStringSet);
//            Log.d("Tag","onPreExec: forexStringSet.count = " + forexStringSet.size());
            ArrayList<String> fxStrList = new ArrayList<>(forexStringSet);
            int k = 0;
            ArrayList<String> fxUrlList = new ArrayList<>();
            String strItem = "";
            while (fxStrList.size() > 0) {
                String s = fxStrList.get(0);
//                URLforex += s + ",";
                strItem += s + ",";
                k++;
                if (k == 7) {
                    strItem = removeTrailingComma(strItem);
                    fxUrlList.add(strItem);
                    strItem = "";
                    k = 0;
                }
                fxStrList.remove(0);
            }
            strItem = removeTrailingComma(strItem);
            fxUrlList.add(strItem);
//            for (String s : forexStringSet) {
//                URLforex += s + ",";
//                forexStringSet.remove(s);
//                k++;
//            }
//            URLforex = removeTrailingComma(URLforex);

            Set<String> defCommoditiesStringSet = new HashSet<>();
//            for (String s : defCommodities) {
//                defCommoditiesStringSet.add(s);
//            }
            defCommoditiesStringSet.addAll(Arrays.asList(defCommodities));
            commoditiesStringSet = preferences.getStringSet("commoditiesFilter", defCommoditiesStringSet);
            for (String s : commoditiesStringSet) {
                URLcommodities += s + ",";
            }
            URLcommodities = removeTrailingComma(URLcommodities);

            Set<String> defIndicesStringSet = new HashSet<>();
//            for (String s : defIndices) {
//                defIndicesStringSet.add(s);
//            }
            defIndicesStringSet.addAll(Arrays.asList(defIndices));
            indicesStringSet = preferences.getStringSet("indicesFilter", defIndicesStringSet);
            for (String s : indicesStringSet) {
                URLindices += s + ",";
            }
            URLindices = removeTrailingComma(URLindices);

            Set<String> defStocksStringSet = new HashSet<>();
//            for (String s : defStocks) {
//                defStocksStringSet.add(s);
//            }
            defStocksStringSet.addAll(Arrays.asList(defStocks));
            stocksStringSet = preferences.getStringSet("stocksFilter", defStocksStringSet);
            for (String s : stocksStringSet) {
                URLstocks += s + ",";
            }
            URLstocks = removeTrailingComma(URLstocks);

            urlsList = new ArrayList<>();
            for (int j = 0; j < fxUrlList.size(); j++) {
                String urlFx = URLforex + fxUrlList.get(j);
                for (int i = 0; i < URLs.length; i++) {
                    String temp = URLs[i] + urlFx + URLcommodities + URLindices + URLstocks + URLtabs;
                    urlsList.add(temp);
                }
            }
            Log.d("Tag", "onPreExec: urlsList = " + urlsList.toString());
            docs = new ArrayList<>();

            db.open();
            // Show progressdialog
            mProgressDialog.show();

            Random random = new Random();
            if (mInterstitialAd.isLoaded() && (random.nextInt(100) < 20)) {
                mInterstitialAd.show();
            }

        }

        private String removeTrailingComma(String str) {
//            Log.d("Tag", "Main.removeTrailingComma at " + str);
            String result = str;
            if (str.endsWith(",")) {
                result = str.substring(0, str.length() - 1);
            }
            return result;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < urlsList.size(); i++) {
                String url = urlsList.get(i);
                try {
                    Log.d("Tag", "Trying to connect " + url);
                    Document doc = Jsoup.connect(url)
                            .timeout(0) // Relax the server by according it infinite time...
                            .maxBodySize(0) // We don't know the size of the server response...
                            .header("Accept-Encoding", "gzip") //
                            .userAgent("Dalvik") //
                            .get();
                    docs.add(doc);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Tag", "Failed to connect");
                    disabledTimeFrames.add(i);
                    if (msg == "") {
                        msg = "Some time frames unavailable: ";
                    }
                    msg += String.valueOf(i) + " ";
                }
            }
            publishProgress();
            fillDB();
            fillGroupData();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setMessage("Updating...");
        }

        @Override
        protected void onPostExecute(Void result) {
            db.close();
            getSectionsPagerAdapter().notifyDataSetChanged();
            if (btUpdate != null) {
                btUpdate.setEnabled(false);
                invalidateOptionsMenu();
            }
            mProgressDialog.dismiss();
            if (docs.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Nothing loaded", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Update done", Toast.LENGTH_SHORT).show();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTimeStamp = dateFormat.format(new Date());
                ((TextView) findViewById(R.id.tvTitle)).setText("Updated " + currentTimeStamp);
                if (msg != "") {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
            isUpdating = false;
        }

        private void fillGroupData() {
            Log.d("Tag", "fillGroupData start");
            Cursor cGroupData = db.getGroupData();
            ArrayList<Map<String, String>> mGroupData;
            ArrayList<ArrayList<Map<String, String>>> mChildData;
            if (cGroupData != null) {
                if (cGroupData.moveToFirst()) {
//                    String str;
                    mGroupData = new ArrayList<>();
                    mChildData = new ArrayList<>();
                    do {
//                        str = "fillGroupData ";
                        Map<String, String> m = new HashMap<>();
                        for (String cn : cGroupData.getColumnNames()) {
                            String me = cGroupData.getString(cGroupData.getColumnIndex(cn));
                            m.put(cn, me);
//                            str = str.concat(cn + ":" + me + "; ");
                        }
//                    Log.d("Tag", str);
                        mGroupData.add(m);
                        String groupID = m.get(Constants.ATTR_GROUP_ID);
                        fillChildData(groupID, mChildData);
                    } while (cGroupData.moveToNext());
                    baseData = mGroupData;
                    baseChildData = mChildData;
                }
                cGroupData.close();

            } else
                Log.d("Tag", "Cursor is null");
            Log.d("Tag", "fillGroupData end");
        }

        private void fillChildData(String groupID, ArrayList<ArrayList<Map<String, String>>> childData) {
//            Log.d("Tag", "fillChildData start");
            Cursor cChildData = db.getChildData(groupID);
//        Log.d("Tag", "fillChildData entered with "+groupID);
            ArrayList<Map<String, String>> childDataItem;
            if (cChildData != null) {
                if (cChildData.moveToFirst()) {
//                    String str;
                    do {
//                        str = groupID + ": ";
                        childDataItem = new ArrayList<>();
                        Map<String, String> m = new HashMap<>();
                        for (String cn : cChildData.getColumnNames()) {
                            String me = cChildData.getString(cChildData.getColumnIndex(cn));
                            m.put(cn, me);
//                            str = str.concat(cn + ":" + me + "; ");
                        }
//                    Log.d("Tag", "fillChildData "+str);
                        childDataItem.add(m);
                    } while (cChildData.moveToNext());
                    childData.add(childDataItem);
                }
                cChildData.close();

            } else
                Log.d("Tag", "fillChildData Child Cursor is null. Group: " + groupID);
//            Log.d("Tag", "fillChildData end");
        }

        private void fillDB() {
            Log.d("Tag", "fillDB start");
            HashSet<String> pairsSet = new HashSet<>();
            pairsSet.addAll(forexStringSet);
            pairsSet.addAll(commoditiesStringSet);
            pairsSet.addAll(indicesStringSet);
            pairsSet.addAll(stocksStringSet);
            Log.d("Tag", "fillDB: pairsSet.count = " + pairsSet.size());
            String summaryName, technicalSummary, maBuy, maSell, tiBuy, tiSell, summaryLast, timeFrame, updateTime;
            db.getDB().execSQL("delete from " + DB.getDefTable());
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
                    for (Element mainSumDiv : doc.getElementsByAttributeValueContaining("id", "mainSummaryDiv_")) {
                        String summaryId = mainSumDiv.id().substring(mainSumDiv.id().indexOf("_") + 1);
                        summaryName = mainSumDiv.getElementById("summaryName").text();
                        summaryLast = mainSumDiv.getElementById("summaryLast").text();
                        technicalSummary = mainSumDiv.getElementById("technicalSummary").text();
                        maBuy = mainSumDiv.getElementById("maBuy").text();
                        maSell = mainSumDiv.getElementById("maSell").text();
                        tiBuy = mainSumDiv.getElementById("tiBuy").text();
                        tiSell = mainSumDiv.getElementById("tiSell").text();
//                    Log.d("Tag","fillDB "+timeFrame+summaryId+ summaryName + summaryLast + technicalSummary + maBuy + maSell + tiBuy + tiSell);
                        ContentValues cv = new ContentValues();
                        cv.put(Constants.ATTR_GROUP_NAME, summaryName);
                        cv.put(Constants.ATTR_PRICE, summaryLast);
                        cv.put(attrSum, technicalSummary);
                        cv.put(attrIndBuy, tiBuy);
                        cv.put(attrIndSell, tiSell);
                        cv.put(attrMaBuy, maBuy);
                        cv.put(attrMaSell, maSell);
                        //try to update field
                        if (pairsSet.contains(summaryId)) {
                            int updCount = 0;
                            updCount = db.getDB().update(DB.getDefTable(), cv, Constants.ATTR_GROUP_ID + "=?", new String[]{summaryId});

//                    Log.d("Tag", "Updated " + String.valueOf(updCount));
                            if (updCount == 0) {
                                // заполним таблицу
                                cv.put(Constants.ATTR_GROUP_ID, summaryId);
                                long id = db.getDB().insert(DB.getDefTable(), null, cv);
//                        Log.d("Tag","Inserted "+String.valueOf(id));
                            }
                        }
                    }
                }
            }
            Document doc = null;
            if (!docs.isEmpty()) {
                doc = docs.get(0);
            }
            if (doc != null) {
                for (int i = 1; i < 5; i++) {
                    Elements tabElements = doc.getElementsByAttributeValue("id", "QBS_" + String.valueOf(i) + "_inner");
                    Element tabElement = tabElements.first();
                    if (tabElement != null) {
//                        String log = "Tab: " + String.valueOf(i) + ". Groups: ";
//                    Log.d("Tag",log);
//                        String log2 = "all: ";
                        for (Element element : tabElement.getElementsByAttribute("title")) {
                            String stringName = element.text();
                            String stringId = element.parent().id();
//                        Log.d("Tag","fill Tabs "+stringName+" : "+stringId);
                            ContentValues cv = new ContentValues();
                            cv.put(Constants.ATTR_TAB_NUM, String.valueOf(i - 1));
                            int updCount = db.getDB().update(DB.getDefTable(), cv, Constants.ATTR_GROUP_ID + "= \'" + stringId + "\'", null);
//                            if (updCount > 0) {
//                                log += stringName + ", ";
//                            }
//                            log2 += stringName + ", ";
                        }
//                    Log.d("Tag",log);
//                    Log.d("Tag",log2);

                    }
                }
            }
            Log.d("Tag", "fillDB end");
        }
    }

}
