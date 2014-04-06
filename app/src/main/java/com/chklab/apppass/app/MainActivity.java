package com.chklab.apppass.app;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.aplixcorp.android.ble.beacon.Beacon;
import com.aplixcorp.android.ble.beacon.BeaconLocationManager;
import com.aplixcorp.android.ble.beacon.BeaconRegion;
import com.chklab.apppass.app.models.UserInfo;

import org.json.JSONArray;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends Activity implements ActionBar.TabListener {

    private static NearFragment nearFragment = null;
    private static View nearView = null;

    protected LocationManager locationManager;
    protected String bestProvider;

    public static final String PREFERENCES_FILE_NAME = "preference";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;


    //ここからBeaconで使用する変数、定数
    private Boolean isNews = false;
    private MainActivity mainActivity;

    private static final String LOG_NAME = MainActivity.class.getSimpleName() + ".";
    private static final String LOG_TAG  = "BeaconSample";

    private static final int MSG_BEACON_MONITORING_NOT_SUPPORTED = R.string.msg_beacon_monitoring_not_supported;
    private static final int MSG_BEACON_RANGING_NOT_SUPPORTED    = R.string.msg_beacon_ranging_not_supported;
    private static final int MSG_BLE_NOT_SUPPORTED               = R.string.msg_ble_not_supported;
    private static final int MSG_BLUETOOTH_NOT_SUPPORTED         = R.string.msg_bluetooth_not_supported;

    private static final int MSG_INVOKED_BY_BEACON = R.string.msg_invoked_by_beacon;

    private BluetoothAdapter mBluetoothAdapter;

    private BeaconWatcher mWatcher;
    private PushParameter mPushParameter;

    private class PushParameter {
        final BeaconRegion mRegion;
        PushParameter(int trigger, BeaconRegion region) {
            mRegion  = region;
        }
    }

    //ここまでBeaconで使用する変数、定数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //プリファレンスからユーザー情報を取得
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0); // 0 -> MODE_PRIVATE
        UserInfo userInfo = UserInfo.getInstance();
        userInfo.setUserId(settings.getString("userId",""));
        userInfo.setSex(settings.getString("sex",""));
        userInfo.setBirthday(settings.getString("birthDay",""));
        userInfo.setFirstName(settings.getString("firstName",""));
        userInfo.setLastName(settings.getString("lastName",""));

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


        //ここからBeaconの設定
        //[SDKAPI.1] Check if the BluetoothLE(BLE) is supported on the deivce.
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, MSG_BLE_NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, MSG_BLUETOOTH_NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


//[SDKAPI.2]  Check if the BEACON-Ranging/Monitoring is supported.
        if (!BeaconLocationManager.isRangingAvailable(this)) {
            Toast.makeText(this, MSG_BEACON_RANGING_NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!BeaconLocationManager.isRegionMonitoringAvailable(this)) {
            Toast.makeText(this, MSG_BEACON_MONITORING_NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }

        //Sample app specific setup.
        mWatcher = new BeaconWatcher(this);
        mWatcher.setHandler(new BeaconWatcherHandler());

        final BeaconRegion region = TestData.getRegion("ALL");
        final String regionName = region.getIdentifier();
        mWatcher.startMonitoring(regionName);
        mWatcher.startRanging(regionName);

        mainActivity = this;
        //ここまでBeaconの設定
    }

    /**
     * Activityが前面になる時に呼び出される。
     */
    @Override
    protected void onResume(){
        super.onResume();

    }

    /**
     * Activityがバックグラウンドに移動するときに呼び出される。
     */
    @Override
    protected void onPause(){
        super.onPause();

        if (nearFragment != null)
        {
            nearFragment.onPause();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 4:
                    return getString(R.string.title_section5).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private PullToRefreshLayout mPullToRefreshLayout;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {


            View v = null;
            int index = getArguments().getInt(ARG_SECTION_NUMBER);

            if(index == 1){
                v = new MyPassFragment().onCreateView(inflater, container, savedInstanceState);
                return v;
            }
            else if(index == 2){
                v = new FollowFragment().onCreateView(inflater, container, savedInstanceState);
                return v;
            }
            else if(index == 3){
                v = new SpotFragment().onCreateView(inflater, container, savedInstanceState);
                return v;
            }
            else if(index == 4){
                //Fragmentに地図がのってる場合、何回もonCreateViewすると落ちる。
                //なので一時対応 ↓
                //なぜだー？？？
                if(nearView == null)
                {
                    nearFragment = new NearFragment();
                    nearView = nearFragment.onCreateView(inflater, container, savedInstanceState);
                    //v = new NearFragment().onCreateView(inflater, container, savedInstanceState);
                }
                return nearView;
            }
            else if(index == 5){
                v = new SettingsFragment().onCreateView(inflater, container, savedInstanceState);
                return v;
            }
            else{
                v = inflater.inflate(R.layout.fragment_main, container, false);
                TextView textView = (TextView) v.findViewById(R.id.section_label);
                textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
                return v;
            }


            //View v = inflater.inflate(R.layout.fragment_mypass, container, false);
            //TextView textView = (TextView) v.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            //return v;

        }

    }

    private class BeaconWatcherHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            Log.d(LOG_TAG, LOG_NAME + "handleMessage():" + msg.what);
            switch (msg.what) {
                case BeaconWatcher.EVENT_MONITORING_STARTED: break;
                case BeaconWatcher.EVENT_MONITORING_ENTERED: break;
                case BeaconWatcher.EVENT_MONITORING_EXIT: break;
                case BeaconWatcher.EVENT_MONITORING_ERROR: break;
                case BeaconWatcher.EVENT_RANGING_UPDATED: break;
                case BeaconWatcher.EVENT_RANGING_ERROR: break;
                case BeaconWatcher.EVENT_DETERMINED_STATE: break;
                default: return;
            }
            //BeaconWatcher.RegionStateHolder h = (BeaconWatcher.RegionStateHolder)msg.obj;

            BeaconWatcher.RegionStateHolder h = mWatcher.getStateHolder("ALL");
            setViewMonitoringStatus(h);
            setViewRangingStatus(h);

            //mRegionListAdapter.notifyDataSetChanged();
        }

        private void setViewMonitoringStatus(BeaconWatcher.RegionStateHolder h) {

            String text = "";
            if (h != null && h.isMonitoring()) {
                //viewHolder.sw_monitor.setChecked(true);
                if (h.isMonStarted()) {
                    text += "[MON:STARTED:" + h.getRegionState() +"]";
                } else {
                    text += "[MON:starting...]";
                }
            } else {
                //viewHolder.sw_monitor.setChecked(false);
                text += "STR_MONITORING_OFF";
            }
            if (h != null && h.isRequestingState()) {
                text += "[state:" + h.getDeterminedRegionState() +"]";
            }
            int err = (h != null ? h.getMonitorError() : 0);
            if ( err != 0) {
                text += "[ERROR:" + err + "]";
            }
            //viewHolder.monitor_status.setText(text);
            Log.d("MonitoringStatus", text);
        }

        private void setViewRangingStatus(BeaconWatcher.RegionStateHolder h) {
            String err_text = "";
            int err = (h != null ? h.getRangeError() : 0);
            if ( err != 0) {
                err_text = "[ERROR:" + err + "]";
            }
            if (h != null && h.isRanging()) {
                //viewHolder.sw_range.setChecked(true);
                ArrayList<Beacon> beacons = h.getBeacons();
                if (beacons != null) {
                    StringBuilder sb = new StringBuilder();

                    Beacon beacon = beacons.get(0); //たぶん最初が一番近い奴だと思う。
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(String.format("major=%d; minor=%d; proximity=%s\n", beacon.getMajor(),beacon.getMinor(),beacon.getProximity()));
                    Log.d("BeaconCheck",sb1.toString());

                    if (beacon.getMajor() == 1 && beacon.getMinor() == 9) //お知らせ
                    {
                        if (!isNews)
                        {
                            isNews = true;

                            String uuid = beacon.getUuid().toString();
                            String major = String.valueOf(beacon.getMajor());
                            String minor = String.valueOf(beacon.getMinor());
                            // インテントへのインスタンス生成
                            Intent intent = new Intent(mainActivity, BeaconViewActivity.class);
                            //　インテントに値をセット
                            intent.putExtra("uuid", uuid);
                            intent.putExtra("major", major);
                            intent.putExtra("minor", minor);
                            // サブ画面の呼び出し
                            mainActivity.startActivity(intent);

                            sendNotifecation();
                        }
                    }
                    else if (beacon.getMajor() == 0 && beacon.getMinor() == 0) //ワークショップチェックイン
                    {
                        UserInfo userInfo = UserInfo.getInstance();
                        int checkinSpotId = userInfo.getCheckinSpotId();

                        if (checkinSpotId == -1) //チェックインしていないとき
                        {
                            //とりあえず仮のチェックインスポットIDを設定
                            //チェックイン後正式なチェックインスポットIDがロードされるから大丈夫なはず
                            userInfo.setCheckinSpotId(9999999);

                            String uuid = beacon.getUuid().toString();
                            String major = String.valueOf(beacon.getMajor());
                            String minor = String.valueOf(beacon.getMinor());
                            // インテントへのインスタンス生成
                            Intent intent = new Intent(mainActivity, BeaconViewActivity.class);
                            //　インテントに値をセット
                            intent.putExtra("uuid", uuid);
                            intent.putExtra("major", major);
                            intent.putExtra("minor", minor);
                            // サブ画面の呼び出し
                            mainActivity.startActivity(intent);

                            sendNotifecation();
                        }
                    }
                    else if (beacon.getMajor() == 1 && beacon.getMinor() == 0) //宝さがしチェックイン
                    {
                        UserInfo userInfo = UserInfo.getInstance();
                        int checkinSpotId = userInfo.getCheckinSpotId();

                        if (checkinSpotId == -1) //チェックインしていないとき
                        {
                            //とりあえず仮のチェックインスポットIDを設定
                            //チェックイン後正式なチェックインスポットIDがロードされるから大丈夫なはず
                            userInfo.setCheckinSpotId(9999999);

                            String uuid = beacon.getUuid().toString();
                            String major = String.valueOf(beacon.getMajor());
                            String minor = String.valueOf(beacon.getMinor());
                            // インテントへのインスタンス生成
                            Intent intent = new Intent(mainActivity, BeaconViewActivity.class);
                            //　インテントに値をセット
                            intent.putExtra("uuid", uuid);
                            intent.putExtra("major", major);
                            intent.putExtra("minor", minor);
                            // サブ画面の呼び出し
                            mainActivity.startActivity(intent);

                            sendNotifecation();
                        }
                    }

//                    for (Beacon b: beacons) {
//                        sb.append(String.format("[%04x:%04x]%10s %4d[dB] %7.2f[m]\n",
//                                b.getMajor(), b.getMinor(),
//                                b.getProximity(), b.getRssi(), b.getAccuracy()));
//                    }
//                    Log.d("range_status", "STR_RANGING_UPDATED" + err_text);
//                    Log.d("range", sb.toString());
                    //viewHolder.range_status.setText(STR_RANGING_UPDATED + err_text);
                    //viewHolder.beacons.setText(sb);
                    //viewHolder.beacons.setVisibility(View.VISIBLE);
                } else {
                    Log.d("range_status", "STR_RANGING_STARTING" + err_text);
                    //viewHolder.range_status.setText(STR_RANGING_STARTING + err_text);
                    //viewHolder.beacons.setVisibility(View.GONE);
                }
            } else {
                Log.d("range_status", "STR_RANGING_OFF" + err_text);

//                viewHolder.sw_range.setChecked(false);
//                viewHolder.range_status.setText(STR_RANGING_OFF + err_text);
//                viewHolder.beacons.setVisibility(View.GONE);
            }
        }

        private void sendNotifecation()
        {
            //intentの設定
            Intent intent = new Intent(mainActivity, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(mainActivity, 0, intent, 0);
            Notification notification = null;
            try {
                Class.forName("android.app.Notification$Builder");
                Notification.Builder builder = new Notification.Builder(mainActivity);
                builder.setDefaults(Notification.DEFAULT_SOUND
                        | Notification.DEFAULT_VIBRATE
                        | Notification.DEFAULT_LIGHTS);
                builder.setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(mainActivity.getResources(), android.R.drawable.btn_default))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle("AppPass")
                        .setContentText("新しいお知らせを受信しました。");
                try{
                    notification = builder.build();
                }catch(NoSuchMethodError nsme){
                }
            } catch (ClassNotFoundException e) {
            }

//            notification.vibrate = new long[]{0, 200, 100, 200, 100, 200}; // バイブレータの振動を設定
//            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//            notification.sound = getAlarmUri();
//            notification.audioStreamType = audioManager.STREAM_NOTIFICATION;
//            notification.ledARGB = 0xff0000ff; // LEDを青色に点滅させる
//            notification.ledOnMS = 3000; // 点灯する時間は3000ミリ秒
//            notification.ledOffMS = 1000; // 消灯する時間は1000ミリ秒
//            notification.flags |= Notification.FLAG_SHOW_LIGHTS; // LED点灯のフラグを追加する

            NotificationManager notificationManager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(R.string.app_name, notification);
        }

    }
}
