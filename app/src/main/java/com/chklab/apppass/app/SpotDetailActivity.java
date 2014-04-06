package com.chklab.apppass.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chklab.apppass.app.models.CustomGridViewAdapter;
import com.chklab.apppass.app.models.GridItem;
import com.chklab.apppass.app.models.LruCacheSample;
import com.chklab.apppass.app.models.UserInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SpotDetailActivity extends Activity {

    private String spotId = "9999999";
    private String userId = "9999999";
    private Boolean isFollow = false;

    private Activity myActivity;
    private RequestQueue mQueue;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_detail);

        // インテントを取得
        Intent intent = getIntent();
        // インテントに保存されたデータを取得
        spotId = intent.getStringExtra("SpotID");

        //非同期WebAPI取得に使用するアクティビティを取得
        myActivity = this;
        //GoogleMapを設定
        if (mMap == null)
        {
            //mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        //ネットワークからデータを取得
        getData();

        //フォローボタンのクリックイベントリスナーを取得
        Button button = (Button)findViewById(R.id.btnFollow);
        button.setOnClickListener(btnFollowClickListener);

        //チェックインボタンのクリックイベントリスナーを取得
        Button btnCheckin = (Button)findViewById(R.id.btnCheckin);
        btnCheckin.setOnClickListener(btnCheckinClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.spot_detail, menu);
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
     * フォローボタンをクリックした時に発生します。
     */
    private View.OnClickListener btnFollowClickListener = new View.OnClickListener(){
        public void onClick(View v){
            setFollow();
        }
    };

    /**
     * チェックインボタンをクリックしたときに発生します。
     */
    private View.OnClickListener btnCheckinClickListener = new View.OnClickListener()
    {
        public  void onClick(View v){
            setCheckin();
        }
    };

    /**
     * チェックイン処理
     */
    private void setCheckin(){

        //現在の日付と時間を取得
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        month++; //月は何故か0から始まるので+1する
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int ms = calendar.get(Calendar.MILLISECOND);

        String strDate = Integer.toString(year) + "/" + Integer.toString(month)  + "/" +  Integer.toString(day);
        String strTime = Integer.toString(hour)  + ":" +  Integer.toString(minute)  + ":" +  Integer.toString(second);

        UserInfo userInfo = UserInfo.getInstance();

        //リクエストURL
        String url = getString(R.string.webapi_url);
        url += "checkinapi";
        url += "?spotid=" + spotId;
        url += "&userid=" + userInfo.getUserId();
        url += "&date=" + strDate;
        url += "&time=" + strTime;
        mQueue = Volley.newRequestQueue(this);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Boolean isChecined = Boolean.valueOf(response.get("isCheckined").toString());
                            String message = response.get("message").toString();

                            if (isChecined)
                            {
                                Toast toast = Toast.makeText(myActivity, "チェックインしました。", Toast.LENGTH_LONG);
                                toast.show();
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

    /**
     * フォロー処理
     */
    private void setFollow() {

        String url = getString(R.string.webapi_url);
        url += "userfollowingspotapi";

        UserInfo userInfo = UserInfo.getInstance();
        userId = userInfo.getUserId();

        mQueue = Volley.newRequestQueue(this);

        if(!isFollow) //フォローするの時
        {
            mQueue.add(new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
            )
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("spotid", spotId);
                    params.put("userid", userId);

                    return params;
                }
            });

            //本当は「onResponse」でレスポンスが帰ってきたら処理するほうがよいが、
            //WebAPIが戻り値なしにしているため、とりあえずはここで処理。後で要変更
            Button btnFollow = (Button)findViewById(R.id.btnFollow);
            btnFollow.setText("フォロー済");

            isFollow = true;
        }
        else //フォロー済みの時
        {
            url += "?userid=" + userId;
            url += "&spotid=" + spotId;
            mQueue.add(new StringRequest(Request.Method.DELETE, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error.

                        }
                    }
            ));

            //本当は「onResponse」でレスポンスが帰ってきたら処理するほうがよいが、
            //WebAPIが戻り値なしにしているため、とりあえずはここで処理。後で要変更
            Button btnFollow = (Button)findViewById(R.id.btnFollow);
            btnFollow.setText("フォローする");

            isFollow = false;
        }
    }

    /**
     * ネットワークからデータを取得します。
     */
    private void getData()
    {

        UserInfo userInfo = UserInfo.getInstance();

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
                            String imageurl = getString(R.string.webimage_url);

                            Button btnFollow = (Button)findViewById(R.id.btnFollow);
                            TextView textDate = (TextView)findViewById(R.id.textDate);
                            TextView textTitle = (TextView)findViewById(R.id.textTitle);
                            TextView textDetail = (TextView)findViewById(R.id.textDetail);
                            TextView textAddress = (TextView)findViewById(R.id.textAddress);


                            //TextView imageStamp = (TextView)findViewById(R.id.imageStamp);

                            String strDate = response.get("date").toString();
                            String startTime = response.get("start_time").toString();
                            String endTime = response.get("end_time").toString();
                            String title = response.get("title").toString();
                            String description = response.get("description").toString();
                            String address = response.get("address").toString();
                            String coverphotoUrl = response.get("coverphoto_url").toString();
                            isFollow = Boolean.valueOf(response.get("isfollow").toString());
                            int followNum = Integer.valueOf(response.get("follow_num").toString());
                            Double lat = Double.valueOf(response.get("lat").toString());
                            Double lng = Double.valueOf(response.get("lon").toString());
                            String stampImageUrl = response.get("stampimageurl").toString();

                            //カバーフォトを設定
                            if (coverphotoUrl != null && coverphotoUrl != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(myActivity);
                                NetworkImageView view = (NetworkImageView)findViewById(R.id.imageCoverPhoto);
                                view.setImageUrl(imageurl + coverphotoUrl, new ImageLoader(queue, new LruCacheSample()));
                                view.setBackground(null);
                            }

                            //フォロー済み確認
                            if(isFollow)
                            {
                                btnFollow.setText("フォロー済");
                            }
                            else
                            {
                                btnFollow.setText("フォローする");
                            }
                            //フォロー数を表示
                            //Todo:フォロー数を表示 followNum;

                            //テキストを表示
                            SimpleDateFormat formatA = new SimpleDateFormat("yyyy年MM月dd日");
                            strDate = strDate.replaceAll("-","/");
                            Date date = DateFormat.getDateInstance().parse(strDate);
                            String formatDate = formatA.format(date);
                            formatDate = formatDate + startTime + "～" + endTime;

                            textDate.setText(formatDate);
                            textTitle.setText(title);
                            textDetail.setText(description);
                            textAddress.setText(address);

                            //GoogleMapにマーカーを追加して移動
                            if (mMap != null)
                            {
                                LatLng latLng = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(latLng).title(title));
                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 13, 0,0)));
                            }

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
}
