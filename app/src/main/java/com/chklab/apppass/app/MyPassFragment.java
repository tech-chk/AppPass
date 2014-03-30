package com.chklab.apppass.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 010144 on 14/03/25.
 */
public class MyPassFragment extends Fragment {

    private RequestQueue mQueue;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        //ネットワークからデータを取得
        getDate(inflater.getContext());

        //return inflater.inflate(R.layout.fragment1, container, false);

        //Button button = (Button)getActivity().findViewById(R.id.button1);
        //button.setOnClickListener(mClickListener);

        // 第３引数のbooleanは"container"にreturnするViewを追加するかどうか
        //trueにすると最終的なlayoutに再度、同じView groupが表示されてしまうのでfalseでOKらしい
        View v = inflater.inflate(R.layout.fragment_mypass, container, false);

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

    private void getDate(Context context){

        //リクエストURL
        String url = context.getString(R.string.webapi_url);
        url += "AppPassApi";
        url += "?userid=c6c1f6c8-1307-46e9-8e47-e77709521281";
        mQueue = Volley.newRequestQueue(context);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // JSONArrayのパース、List、Viewへの追加等
                        try {
                            String appPassNo = response.get("apppass_no").toString();
                            JSONArray list = response.getJSONArray("");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // エラー処理 error.networkResponseで確認
                        // エラー表示など
                        int a = 0;
                    }
                }
        ));
    }
}
