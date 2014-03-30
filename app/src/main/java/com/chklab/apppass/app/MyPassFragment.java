package com.chklab.apppass.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.chklab.apppass.app.models.LruCacheSample;
import com.chklab.apppass.app.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 010144 on 14/03/25.
 */
public class MyPassFragment extends Fragment {

    private View v;
    private Context context;
    private RequestQueue mQueue;

    private int chekinSpotID = -1;

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
        v = inflater.inflate(R.layout.fragment_mypass, container, false);
        context = v.getContext();
        //ネットワークからデータを取得
        getDate();

        // ボタンを取得して、ClickListenerをセット
        //Button btn = (Button)v.findViewById(R.id.button1);
        //btn.setOnClickListener(mClickListener);

        return v;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){
            int a = 0;
            a++;
        }
    };

    private void getDate(){

        UserInfo userInfo = UserInfo.getInstance();

        //リクエストURL
        String url = context.getString(R.string.webapi_url);
        url += "AppPassApi";
        url += "?userid=" + userInfo.getUserId();
        mQueue = Volley.newRequestQueue(context);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // JSONArrayのパース、List、Viewへの追加等
                        try {
                            SimpleDateFormat formatA = new SimpleDateFormat("yyyy年MM月dd日");
                            SimpleDateFormat formatB = new SimpleDateFormat("yyyy年MM月dd日");

                            String appPassNo = response.get("apppass_no").toString();
                            String dateogexpiry = response.get("dateogexpiry").toString();

                            dateogexpiry = dateogexpiry.replaceAll("-","/");
                            Date date = DateFormat.getDateInstance().parse(dateogexpiry);
                            String formatDateogexpiry = formatA.format(date);

                            UserInfo userInfo = UserInfo.getInstance();
                            String birth = userInfo.getBirthday();
                            birth = birth.replaceAll("-","/");
                            Date dateBirth = DateFormat.getDateInstance().parse(birth);
                            String formatBirth = formatB.format(dateBirth);

                            String sexFormatte = "";
                            if (userInfo.getSex() == "1")
                            {
                                sexFormatte = "M";
                            }
                            else if (userInfo.getSex() =="2")
                            {
                                sexFormatte = "F";
                            }

                            //各テキストへ追加
                            TextView textAppPannNo = (TextView)v.findViewById(R.id.textAppPannNo);
                            textAppPannNo.setText(appPassNo);
                            TextView textDateOgExpiry = (TextView)v.findViewById(R.id.textDateOgExpiry);
                            textDateOgExpiry.setText(formatDateogexpiry);
                            TextView textSurName = (TextView)v.findViewById(R.id.textSurName);
                            textSurName.setText(userInfo.getFirstName());
                            TextView textGivenName = (TextView)v.findViewById(R.id.textGivenName);
                            textGivenName.setText(userInfo.getLastName());
                            TextView textDateOfBirth = (TextView)v.findViewById(R.id.textDateOfBirth);
                            textDateOfBirth.setText(formatBirth);
                            TextView textSex = (TextView)v.findViewById(R.id.textSex);
                            textSex.setText(sexFormatte);

                            String imageurl = context.getString(R.string.webimage_url);

                            //ユーザー画像を表示
                            Bitmap bmp = null;
                            Resources r = context.getResources();
                            if(userInfo.getSex() == "1")
                            {
                                bmp = BitmapFactory.decodeResource(r, R.drawable.man);
                            }
                            else if (userInfo.getSex() == "2")
                            {
                                bmp = BitmapFactory.decodeResource(r, R.drawable.woman);
                            }
                            ImageView iv = (ImageView)v.findViewById(R.id.imagePerson);
                            iv.setImageBitmap(bmp);

                            //チェックイン情報
                            Boolean isChecined = Boolean.getBoolean(response.get("ischeckined").toString());
                            if (isChecined)
                            {
                                String checinTitle = response.get("checkin_title").toString();
                                TextView textCheckin = (TextView)v.findViewById(R.id.textCheckin);
                                textCheckin.setText(checinTitle);

                                String checkinUrl = response.get("checkin_coverphotourl").toString();
                                if (checkinUrl != null && checkinUrl != "")
                                {
                                    RequestQueue queue = Volley.newRequestQueue(context);
                                    NetworkImageView view = (NetworkImageView)v.findViewById(R.id.imageCheckined);
                                    view.setImageUrl(imageurl + checkinUrl, new ImageLoader(queue, new LruCacheSample()));
                                    view.setBackground(null);
                                }

                                chekinSpotID = Integer.parseInt(response.get("checkin_spotid").toString());
                            }
                            else
                            {
                                chekinSpotID = -1;

                                TextView textCheckin = (TextView)v.findViewById(R.id.textCheckin);
                                textCheckin.setText("チェックイン中のスポットはありません。");

                                Bitmap bmp1 = BitmapFactory.decodeResource(r, R.drawable.notstamp);
                                NetworkImageView view = (NetworkImageView)v.findViewById(R.id.imageCheckined);
                                view.setImageBitmap(bmp1);
                                //view.setBackground(null);
                            }

                            //スタンプコレクション
                            Bitmap bmp1 = BitmapFactory.decodeResource(r, R.drawable.notstamp);
                            NetworkImageView imageStamp1 = (NetworkImageView)v.findViewById(R.id.imageStamp1);
                            imageStamp1.setImageBitmap(bmp1);
                            imageStamp1.setBackground(null);
                            NetworkImageView imageStamp2 = (NetworkImageView)v.findViewById(R.id.imageStamp2);
                            imageStamp2.setImageBitmap(bmp1);
                            imageStamp2.setBackground(null);
                            NetworkImageView imageStamp3 = (NetworkImageView)v.findViewById(R.id.imageStamp3);
                            imageStamp3.setImageBitmap(bmp1);
                            imageStamp3.setBackground(null);
                            NetworkImageView imageStamp4 = (NetworkImageView)v.findViewById(R.id.imageStamp4);
                            imageStamp4.setImageBitmap(bmp1);
                            imageStamp4.setBackground(null);

                            String stampimageurl1 = response.get("stampimageurl1").toString();
                            if (stampimageurl1 != null && stampimageurl1 != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(context);
                                NetworkImageView view = (NetworkImageView)v.findViewById(R.id.imageStamp1);
                                view.setImageUrl(imageurl + stampimageurl1, new ImageLoader(queue, new LruCacheSample()));
                            }
                            String stampimageurl2 = response.get("stampimageurl2").toString();
                            if (stampimageurl2 != null && stampimageurl2 != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(context);
                                NetworkImageView view = (NetworkImageView)v.findViewById(R.id.imageStamp2);
                                view.setImageUrl(imageurl + stampimageurl2, new ImageLoader(queue, new LruCacheSample()));
                            }
                            String stampimageurl3 = response.get("stampimageurl3").toString();
                            if (stampimageurl3 != null && stampimageurl3 != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(context);
                                NetworkImageView view = (NetworkImageView)v.findViewById(R.id.imageStamp3);
                                view.setImageUrl(imageurl + stampimageurl3, new ImageLoader(queue, new LruCacheSample()));
                            }
                            String stampimageurl4 = response.get("stampimageurl4").toString();
                            if (stampimageurl4 != null && stampimageurl4 != "")
                            {
                                RequestQueue queue = Volley.newRequestQueue(context);
                                NetworkImageView view = (NetworkImageView)v.findViewById(R.id.imageStamp4);
                                view.setImageUrl(imageurl + stampimageurl4, new ImageLoader(queue, new LruCacheSample()));
                            }


                        } catch (JSONException e) {
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
