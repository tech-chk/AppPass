package com.chklab.apppass.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.chklab.apppass.app.models.CustomGridViewAdapter;
import com.chklab.apppass.app.models.GridItem;

import java.util.ArrayList;

/**
 * Created by 010144 on 14/03/25.
 */
public class FollowFragment extends Fragment {

    GridView gridView;
    ArrayList<GridItem> gridArray = new ArrayList<GridItem>();
    CustomGridViewAdapter customGridAdapter;


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
        View v = inflater.inflate(R.layout.fragment_follow, container, false);

        // ボタンを取得して、ClickListenerをセット
        //Button btn = (Button)v.findViewById(R.id.button1);
        //btn.setOnClickListener(mClickListener);

        Bitmap icon = BitmapFactory.decodeResource(v.getResources(), R.drawable.ic_launcher);
        gridArray.add(new GridItem(icon, "2014年3月14日", "株式会社　地域科学研究所", "詳細"));

        gridView = (GridView)v.findViewById(R.id.gridView1);
        customGridAdapter = new CustomGridViewAdapter(v.getContext(), R.layout.grid_imgtext, gridArray);
        gridView.setAdapter(customGridAdapter);


        return v;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){
            int a = 0;
            a++;
        }
    };
}