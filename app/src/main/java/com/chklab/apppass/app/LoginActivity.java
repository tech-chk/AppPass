package com.chklab.apppass.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.chklab.apppass.app.models.LruCacheSample;
import com.chklab.apppass.app.models.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends Activity {

    public static final String PREFERENCES_FILE_NAME = "preference";
    private RequestQueue mQueue;

    private Activity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myActivity = this;

        ((Button) findViewById(R.id.btnLogin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        ((Button) findViewById(R.id.btnNewAccount)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新規アカウント作成画面へ
                String weblogin_url = getString(R.string.weblogin_url);
                Uri uri = Uri.parse(weblogin_url);
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);

            }
        });
    }

    // ログイン処理
    public void login(){

        TextView mail = (TextView)findViewById(R.id.editText);
        TextView pass = (TextView)findViewById(R.id.editText2);

        //リクエストURL
        String url = getString(R.string.webaccount_url);
        url += "AccountApi";
        url += "?userName=" + mail.getText();
        url += "&password=" + pass.getText();
        mQueue = Volley.newRequestQueue(this);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // JSONArrayのパース、List、Viewへの追加等
                        try {
                            String userId = response.get("Id").toString();
                            String birthYear = response.get("BirthYear").toString();
                            String birthMonth = response.get("BirthMonth").toString();
                            String birthDay = response.get("BirthDay").toString();
                            String userName = response.get("UserName").toString();
                            String firstName = response.get("FirstName").toString();
                            String lastName = response.get("LastName").toString();
                            String sex = response.get("Sex").toString();

                            SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0); // 0 -> MODE_PRIVATE
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putLong("logged-in", 1);
                            editor.putString("userId", userId);
                            editor.putString("birthDay", birthYear + "-" + birthMonth + "-" + birthDay);
                            editor.putString("userName", userName);
                            editor.putString("firstName", firstName);
                            editor.putString("lastName", lastName);
                            editor.putString("sex",sex);
                            editor.commit();

                            // ログイン後は MainActivity に遷移
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);

                        } catch (JSONException e) {
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
                        Toast toast = Toast.makeText(myActivity, "ログインに失敗しました。", Toast.LENGTH_LONG);
                        toast.show();

                    }
                }
        ));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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

}
