package com.chklab.apppass.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.chklab.apppass.app.models.CustomGridViewAdapter;
import com.chklab.apppass.app.models.GridItem;
import com.chklab.apppass.app.models.LruCacheSample;
import com.chklab.apppass.app.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 010144 on 14/03/25.
 */
public class SpotFragment  extends Fragment {

    private View v;
    private Context context;
    private RequestQueue mQueue;

    private JSONArray jsonArray;
    private GridView gridView;
    private ArrayList<GridItem> gridArray = new ArrayList<GridItem>();
    private CustomGridViewAdapter customGridAdapter;

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
        v = inflater.inflate(R.layout.fragment_spot, container, false);
        context = v.getContext();

        // ボタンを取得して、ClickListenerをセット
        //Button btn = (Button)v.findViewById(R.id.button1);
        //btn.setOnClickListener(mClickListener);

        //ネットワークからデータを取得
        getData();

        return v;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){
            int a = 0;
            a++;
        }
    };

    private void getData()
    {
        UserInfo userInfo = UserInfo.getInstance();

        //リクエストURL
        String url = context.getString(R.string.webapi_url);
        url += "SpotApi";
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

                                String strDate = json.get("date").toString();
                                String startTime = json.get("start_time").toString();
                                String endTime = json.get("end_time").toString();
                                String title = json.get("title").toString();
                                String description = json.get("description").toString();
                                String coverphotoUrl = json.get("coverphoto_url").toString();

                                SimpleDateFormat formatA = new SimpleDateFormat("yyyy年MM月dd日");
                                strDate = strDate.replaceAll("-","/");
                                Date date = DateFormat.getDateInstance().parse(strDate);
                                String formatDate = formatA.format(date);
                                formatDate = formatDate + startTime + "～" + endTime;

                                String imageurl = context.getString(R.string.webimage_url);

                                String imagePath = "";
                                if (coverphotoUrl != null && coverphotoUrl != ""){
                                    imagePath = imageurl + coverphotoUrl;
                                }
                                gridArray.add(new GridItem(imagePath, formatDate, title, description));
                            }

                            gridView = (GridView)v.findViewById(R.id.gridView1);
                            customGridAdapter = new CustomGridViewAdapter(v.getContext(), R.layout.grid_imgtext, gridArray);
                            gridView.setAdapter(customGridAdapter);

                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    try
                                    {
                                        JSONObject json = jsonArray.getJSONObject(position);
                                        String spotId = json.get("spotid").toString();

                                        // インテントへのインスタンス生成
                                        Intent intent = new Intent(context, SpotDetailActivity.class);
                                        //　インテントに値をセット
                                        intent.putExtra("SpotID", spotId);
                                        // サブ画面の呼び出し
                                        context.startActivity(intent);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
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
