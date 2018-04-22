package com.symdesign.smartlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dennis on 10/17/16.
 */

public class OptionAdapter extends ArrayAdapter<ListItem> {
    int layout;

    public OptionAdapter(Context context, int box_layout, ArrayList<ListItem> items)
    {
        super(context,0,items);
        layout = box_layout;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(layout,parent,false);
        TextView listName = (TextView) convertView.findViewById(R.id.name);
        listName.setText(item.name);
        return convertView;

    }

}
