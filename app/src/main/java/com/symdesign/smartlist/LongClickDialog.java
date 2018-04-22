package com.symdesign.smartlist;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

import static com.symdesign.smartlist.MainActivity.db;
//import static com.symdesign.smartlistMainActivity.listTable;

/**
 * Created by dennis on 10/17/16.
 *
 * Code handles a long click on a list, displayed by the drawer
 *
 */

public class LongClickDialog extends DialogFragment {
    EditText nameView;
    Button deleteView;
    static String listName;
    ContentValues values = new ContentValues();
    static long listId;

    public LongClickDialog() {
        // Empty contstuctor required for DialogFragment
    }
    /**
     * Create a new instance of PickList, providing "num"
     * as an argument.
     */
    static LongClickDialog newInstance(String name,long id) {

        LongClickDialog f = new LongClickDialog();

        // Supply num input as an argument.
        listId = id;
        listName = name;
//        Bundle args = new Bundle();
//        args.putString("name",name);
//        args.putLong("dBid",id);
//        f.setArguments(args);
        return f;
    }
    interface Listener {
        void showLists();
        void onFinishDelList(String listName);
    }

    private Listener listener;

    public void setListener(Listener l) {
        this.listener = l;
//        this.listener = l;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context;
        setListener((Listener) getActivity());
//        context = getActivity();
        View optionView = inflater.inflate(R.layout.long_click, container, false);
        nameView = (EditText) optionView.findViewById(R.id.list_name);
//        listName = getArguments().getString("name");
//        listId = getArguments().getInt("dBid");
        nameView.setText(listName);
        deleteView = (Button) optionView.findViewById(R.id.delete_button);
        ImageView checkView = (ImageView) optionView.findViewById(R.id.check);
        values.clear();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        );
        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values.clear();
                listName = nameView.getText().toString();
                values.put("name",listName);
                db.update("lists",values,"_id="+listId,null);
                SharedPreferences.Editor ed = MainActivity.prefs.edit();     // Initialize shared preferences
                ed.putString("currList",listName);
                MainActivity.actionBar.setTitle(MainActivity.currList);
                ed.apply();
                listener.showLists();
                MainActivity.printLists();
                getDialog().dismiss();
            }
        });
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.delete("lists","name='"+listName+"'",null);
                db.execSQL("drop table '"+listName+"'");
                MainActivity.currList = "Groceries";
                SharedPreferences.Editor ed = MainActivity.prefs.edit();     // Initialize shared preferences
                ed.putString("currList","Groceries");
                MainActivity.actionBar.setTitle(MainActivity.currList);
                ed.apply();
                listener.showLists();
                if(MainActivity.syncReg)
                    listener.onFinishDelList(listName);
                MainActivity.printLists();
                getDialog().dismiss();
            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return optionView;
    }
}