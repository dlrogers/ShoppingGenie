package com.symdesign.smartlist;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dennis on 7/6/16.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private ArrayList<String> _catagories; // header titles
    // child data in format of header title, child title
    private ArrayList<ArrayList<String>> _items;

    public ExpandableListAdapter(Context context, ArrayList<String> catagories,
                                 ArrayList<ArrayList<String>> listChildData) {
        this._context = context;
        this._catagories = catagories;
        this._items = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._items.get(groupPosition).get(childPosition);
//        return this._items.get(this._catagories.get(groupPosition))
//                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.pick_child, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._items.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._catagories.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._catagories.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.pick_parent, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.pick_parent);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
