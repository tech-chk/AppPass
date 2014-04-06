package com.chklab.apppass.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chklab.apppass.app.models.UserInfo;

public class BeaconViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_view);

        // インテントを取得
        Intent intent = getIntent();
        // インテントに保存されたデータを取得
        String uuid = intent.getStringExtra("uuid");
        String major = intent.getStringExtra("major");
        String minor = intent.getStringExtra("minor");

        //リクエストURL
        UserInfo userInfo = UserInfo.getInstance();
        String url = getString(R.string.webimage_url);
        String http =  url + "beacon?uuid=" + uuid + "&major=" + major + "&minor=" + minor +
                "&proximity=0" + "&accuracy=0" + "&rssi=0" + "&userid=" + userInfo.getUserId();

        WebView web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new WebViewClient()); //リンクをタップしたときに標準ブラウザを起動させない
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.loadUrl(http);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.beacon_view, menu);
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
