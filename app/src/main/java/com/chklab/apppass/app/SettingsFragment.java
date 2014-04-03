package com.chklab.apppass.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by 010144 on 14/03/25.
 */
public class SettingsFragment extends Fragment {

    private ListView lv;
    private View v;
    private Context context;
    public static final String PREFERENCES_FILE_NAME = "preference";

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
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        context = v.getContext();

        // ボタンを取得して、ClickListenerをセット
        //Button btn = (Button)v.findViewById(R.id.button1);
        //btn.setOnClickListener(mClickListener);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        // アイテムを追加します
        adapter.add("Facebookページ");
        adapter.add("利用規約");
        adapter.add("ログアウト");
        ListView listView = (ListView)v.findViewById(R.id.listView);
        // アダプターを設定します
        listView.setAdapter(adapter);
        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                String item = (String) listView.getItemAtPosition(position);
                //Toast.makeText(ListViewSampleActivity.this, item, Toast.LENGTH_LONG).show();

                if (item == "Facebookページ") {
                    Uri uri = Uri.parse("https://m.facebook.com/rcrs.tech/");
                    Intent i = new Intent(Intent.ACTION_VIEW,uri);
                    context.startActivity(i);
                }
                else if(item == "利用規約") {
                    String imageurl = context.getString(R.string.webimage_url);
                    imageurl += "Home/Legal";
                    Uri uri = Uri.parse(imageurl);
                    Intent i = new Intent(Intent.ACTION_VIEW,uri);
                    context.startActivity(i);
                }
                else if (item == "ログアウト") {
                    SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0); // 0 -> MODE_PRIVATE
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("logged-in", 0);
                    editor.commit();

                    Intent intent = new Intent(context.getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // ← FLAG_ACTIVITY_CLEAR_TOPを設定
                    context.startActivity(intent);
                }
            }
        });
        // リストビューのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ListView listView = (ListView) parent;
                // 選択されたアイテムを取得します
                String item = (String) listView.getSelectedItem();
                //Toast.makeText(ListViewSampleActivity.this, item, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return v;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){
            int a = 0;
            a++;
        }
    };
}
