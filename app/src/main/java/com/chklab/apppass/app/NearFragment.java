package com.chklab.apppass.app;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.chklab.apppass.app.models.UserInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 010144 on 14/03/25.
 */
public class NearFragment extends Fragment implements GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GoogleMap.OnCameraChangeListener {

    private MainActivity activity = null;

    private View v;
    private Context context;
    private RequestQueue mQueue;

    private JSONArray jsonArray;

    private GoogleMap mMap = null;
    private LocationClient mLocationClient = null;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000) // 5 seconds
            .setFastestInterval(16) // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        //return inflater.inflate(R.layout.fragment1, container, false);

        //Button button = (Button)getActivity().findViewById(R.id.button1);
        //button.setOnClickListener(mClickListener);

        // 第３引数のbooleanは"container"にreturnするViewを追加するかどうか
        //trueにすると最終的なlayoutに再度、同じView groupが表示されてしまうのでfalseでOKらしい
        v = inflater.inflate(R.layout.fragment_near, container, false);
        context = v.getContext();

        activity = (MainActivity)context;

        // ボタンを取得して、ClickListenerをセット
        //Button btn = (Button)v.findViewById(R.id.button1);
        //btn.setOnClickListener(mClickListener);


        //GoogleMapを設定
        if (mMap == null)
        {
            mMap = ((MapFragment)activity.getFragmentManager().findFragmentById(R.id.nearMap)).getMap();
            //mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.nearMap)).getMap();
            mMap.setMyLocationEnabled(true);
            // イベントハンドラの登録
            mMap.setOnCameraChangeListener(this);

            mLocationClient = new LocationClient(context.getApplicationContext(), this, this); // ConnectionCallbacks, OnConnectionFailedListener
            if (mLocationClient != null) {
                // Google Play Servicesに接続
                mLocationClient.connect();
            }
        }

        //ピンを設置します。
        setPin();

        return v;
    }

    public void onPause()
    {
        if(mLocationClient != null)
        {
            //位置情報取得解除
            mLocationClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        // 現在地に移動
        CameraPosition cameraPos = new CameraPosition.Builder()
        .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
        .bearing(0).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

        //位置情報取得解除...一旦一回取得したら解除しよう
        mLocationClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {

    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
    }

    @Override
    public void onDisconnected()
    {
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        // カメラ位置変更時の処理を実装
        Projection proj = mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        // 北東 = top/right, 南西 = bottom/left
        double topLatitude = vRegion.latLngBounds.northeast.latitude;
        double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        double rightLongitude = vRegion.latLngBounds.northeast.longitude;

        //データ取得
        setPin();
    }

    /**
     * ネットワークからデータを取得します。
     */
    private void setPin()
    {
        UserInfo userInfo = UserInfo.getInstance();

        //表示範囲の左上、右下の緯度経度を取得
        Projection proj = mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        // 北東 = top/right, 南西 = bottom/left
        double topLatitude = vRegion.latLngBounds.northeast.latitude;
        double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        double rightLongitude = vRegion.latLngBounds.northeast.longitude;

        //リクエストURL
        String url = context.getString(R.string.webapi_url);
        url += "SpotApi";
        url += "?lon1=" + leftLongitude;
        url += "&lat1=" + topLatitude;
        url += "&lon2=" + rightLongitude;
        url += "&lat2=" + bottomLatitude;
        mQueue = Volley.newRequestQueue(context);
        mQueue.add(new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override public void onResponse(JSONArray response) {
                        // レスポンス受け取り時の処理...
                        jsonArray = response;
                        try {

                            for (int i = 0; i < response.length(); i++)
                            {
                                JSONObject json = response.getJSONObject(i);

                                String spotid = json.get("spotid").toString();
                                String title = json.get("title").toString();
                                String description = json.get("description").toString();
                                String lat = json.get("lat").toString();
                                String lon = json.get("lon").toString();

                                // マーカー表示
                                LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
                                MarkerOptions options = new MarkerOptions();
                                options.position(location);
                                options.title(spotid + ":" + title);
                                options.snippet(description);
                                options.draggable(false);//ドラッグ不可
                                mMap.addMarker(options);
                            }

                            // インフォウィンドウのタッチイベントハンドラ登録
                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {

                                    String idTitle = marker.getTitle();
                                    String spotId = idTitle.substring(0, idTitle.indexOf(":"));

                                    // インテントへのインスタンス生成
                                    Intent intent = new Intent(context, SpotDetailActivity.class);
                                    //　インテントに値をセット
                                    intent.putExtra("SpotID", spotId);
                                    // サブ画面の呼び出し
                                    context.startActivity(intent);

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //catch (ParseException e) {
                        //    e.printStackTrace();
                        //}
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError error) {
                        // エラー時の処理...
                    }
                })
        );
    }
}