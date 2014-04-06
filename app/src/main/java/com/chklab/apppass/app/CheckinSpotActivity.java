package com.chklab.apppass.app;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.aplixcorp.android.ble.beacon.Beacon;
import com.aplixcorp.android.ble.beacon.BeaconLocationManager;
import com.aplixcorp.android.ble.beacon.BeaconRegion;
import com.chklab.apppass.app.models.LruCacheSample;
import com.chklab.apppass.app.models.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CheckinSpotActivity extends Activity {

    private Activity myActivity;

    private RequestQueue mQueue;
    private JSONObject jsonObject;

    private int currentMajor = -1;
    private int currentMinor = -1;

    //ここからBeaconで使用する変数、定数
    private Boolean isNews = false;
    //private MainActivity mainActivity;

    private static final String LOG_NAME = CheckinSpotActivity.class.getSimpleName() + ".";
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
        setContentView(R.layout.activity_checkin_spot);

        myActivity = this;

        //チェックアウトボタンのクリックイベントリスナーを取得
        Button btnCheckout = (Button)findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(btnCheckoutClickListener);

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


        //ここまでBeaconの設定
    }


    /**
     * Activityが前面になる時に呼び出される。
     */
    @Override
    protected void onResume(){
        super.onResume();

        //Beacon開始
        final BeaconRegion region = TestData.getRegion("ALL");
        final String regionName = region.getIdentifier();
        mWatcher.startMonitoring(regionName);
        mWatcher.startRanging(regionName);

        //ネットワークからデータを取得
        getData();

    }

    /**
     * Activityがバックグラウンドに移動するときに呼び出される。
     */
    @Override
    protected void onPause(){
        super.onPause();

        //Beaconストップ
        final BeaconRegion region = TestData.getRegion("ALL");
        final String regionName = region.getIdentifier();
        mWatcher.stopMonitoring(regionName);
        mWatcher.stopRanging(regionName);

    }

    /**
     * チェックアウトボタンをクリックしたときに発生します。
     */
    private View.OnClickListener btnCheckoutClickListener = new View.OnClickListener()
    {
        public  void onClick(View v){
            setCheckout();
        }
    };

    /**
     * チェックアウト処理
     */
    private void setCheckout(){

        UserInfo userInfo = UserInfo.getInstance();
        String spotId = null;
        try {
            spotId = jsonObject.get("spotid").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //リクエストURL
        String url = getString(R.string.webapi_url);
        url += "checkinapi";
        url += "?spotid=" + spotId;
        url += "&userid=" + userInfo.getUserId();
        mQueue = Volley.newRequestQueue(this);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Boolean isCheckout = Boolean.valueOf(response.get("isCheckout").toString());
                            String message = response.get("message").toString();

                            if (isCheckout)
                            {
                                Toast toast = Toast.makeText(myActivity, "チェックアウトしました。", Toast.LENGTH_LONG);
                                toast.show();

                                //アンケート画面へ
                                // インテントへのインスタンス生成
                                Intent intent = new Intent(myActivity, BeaconViewActivity.class);
                                //　インテントに値をセット
                                intent.putExtra("uuid", "");
                                intent.putExtra("major", "0");
                                intent.putExtra("minor", "10");
                                // サブ画面の呼び出し
                                myActivity.startActivity(intent);

                            }
                            else
                            {
                                Toast toast = Toast.makeText(myActivity, message, Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //catch (ParseException e) {
                        //    e.printStackTrace();
                        //}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // エラー処理 error.networkResponseで確認
                        // エラー表示など
                    }
                }
        ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checkin_spot, menu);
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

    /**
     * ネットワークからデータを取得します。
     */
    private void getData()
    {

        UserInfo userInfo = UserInfo.getInstance();
        // インテントを取得
        Intent intent = getIntent();
        // インテントに保存されたデータを取得
        String spotId = intent.getStringExtra("SpotID");

        //リクエストURL
        String url = getString(R.string.webapi_url);
        url += "SpotApi";
        url += "?spotid=" + spotId;
        url += "&userid=" + userInfo.getUserId();
        mQueue = Volley.newRequestQueue(this);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            jsonObject = response;

                            String imageurl = getString(R.string.webimage_url);

                            TextView textDate = (TextView)findViewById(R.id.textDate);
                            TextView textTitle = (TextView)findViewById(R.id.textTitle);
                            TextView textDescription = (TextView)findViewById(R.id.textDescription);
                            TextView textDetail = (TextView)findViewById(R.id.txtdetail);

                            //TextView imageStamp = (TextView)findViewById(R.id.imageStamp);

                            String strDate = response.get("date").toString();
                            String startTime = response.get("start_time").toString();
                            String endTime = response.get("end_time").toString();
                            String title = response.get("title").toString();
                            String description = response.get("description").toString();
                            String detail = response.get("detail").toString();
                            String coverphotoUrl = response.get("coverphoto_url").toString();
                            String stampImageUrl = response.get("stampimageurl").toString();

                            //カバーフォトを設定
                            if (coverphotoUrl != null && coverphotoUrl != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(myActivity);
                                NetworkImageView view = (NetworkImageView)findViewById(R.id.imageCoverPhoto);
                                view.setImageUrl(imageurl + coverphotoUrl, new ImageLoader(queue, new LruCacheSample()));
                                view.setBackground(null);
                            }

                            //テキストを表示
                            SimpleDateFormat formatA = new SimpleDateFormat("yyyy年MM月dd日");
                            strDate = strDate.replaceAll("-","/");
                            Date date = DateFormat.getDateInstance().parse(strDate);
                            String formatDate = formatA.format(date);
                            formatDate = formatDate + startTime + "～" + endTime;

                            textDate.setText(formatDate);
                            textTitle.setText(title);
                            textDescription.setText(description);
                            textDetail.setText(detail);

                            //スタンプ画像を設定
                            if (stampImageUrl != null && stampImageUrl != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(myActivity);
                                NetworkImageView view = (NetworkImageView)findViewById(R.id.imageStamp);
                                view.setImageUrl(imageurl + stampImageUrl, new ImageLoader(queue, new LruCacheSample()));
                                view.setBackground(null);
                            }

                        }catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // エラー処理 error.networkResponseで確認
                        // エラー表示など
                    }
                }
        ));
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

                    if (beacon.getMajor() != currentMajor || beacon.getMinor() != currentMinor)
                    {

                        if ((beacon.getMajor() == 0 && beacon.getMinor() == 1) ||
                                (beacon.getMajor() == 0 && beacon.getMinor() == 2) ||
                                (beacon.getMajor() == 0 && beacon.getMinor() == 3) ||
                                (beacon.getMajor() == 0 && beacon.getMinor() == 4))
                        {
                            String uuid = beacon.getUuid().toString();
                            String major = String.valueOf(beacon.getMajor());
                            String minor = String.valueOf(beacon.getMinor());
                            // インテントへのインスタンス生成
                            Intent intent = new Intent(myActivity, BeaconViewActivity.class);
                            //　インテントに値をセット
                            intent.putExtra("uuid", uuid);
                            intent.putExtra("major", major);
                            intent.putExtra("minor", minor);
                            // サブ画面の呼び出し
                            myActivity.startActivity(intent);

                            sendNotifecation();
                        }
                    }

                    currentMajor = beacon.getMajor();
                    currentMinor = beacon.getMinor();

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
            Intent intent = new Intent(myActivity, CheckinSpotActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(myActivity, 0, intent, 0);
            Notification notification = null;
            try {
                Class.forName("android.app.Notification$Builder");
                Notification.Builder builder = new Notification.Builder(myActivity);
                builder.setDefaults(Notification.DEFAULT_SOUND
                        | Notification.DEFAULT_VIBRATE
                        | Notification.DEFAULT_LIGHTS);
                builder.setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(myActivity.getResources(), android.R.drawable.btn_default))
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

            NotificationManager notificationManager = (NotificationManager) myActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(R.string.app_name, notification);
        }

    }
}
