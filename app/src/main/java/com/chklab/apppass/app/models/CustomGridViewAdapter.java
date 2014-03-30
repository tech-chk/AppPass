package com.chklab.apppass.app.models;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chklab.apppass.app.R;

import java.util.ArrayList;

/**
 * Created by 010144 on 14/03/25.
 */
public class CustomGridViewAdapter extends ArrayAdapter<GridItem> {
    Context context;
    int layoutResourceId;
    ArrayList<GridItem> data = new ArrayList<GridItem>();

    public CustomGridViewAdapter(Context context, int layoutResourceId,
                                 ArrayList<GridItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.txtDate = (TextView) row.findViewById(R.id.item_text_date);
            holder.txtTitle = (TextView) row.findViewById(R.id.item_text_title);
            holder.txtSub = (TextView) row.findViewById(R.id.item_text_sub);
            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        GridItem item = data.get(position);
        holder.txtDate.setText(item.getDate());
        holder.txtTitle.setText(item.getTitle());
        holder.txtSub.setText(item.getSub());
        holder.imageItem.setImageBitmap(item.getImage());
        return row;

    }

    static class RecordHolder {
        TextView txtDate;
        TextView txtTitle;
        TextView txtSub;
        ImageView imageItem;

    }
}
