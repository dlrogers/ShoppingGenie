package com.symdesign.smartlist;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.symdesign.smartlist.MainActivity.day;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.getTime;
import static com.symdesign.smartlist.MainActivity.listView;
import static com.symdesign.smartlist.MainActivity.suggestView;
import static com.symdesign.smartlist.MainActivity.week;


/**
 * PickList is Adapter for picking from list of items
 * Created by dennis on 7/1/16.
 */
public class PickList extends DialogFragment implements AdapterView.OnItemSelectedListener {
    Context context;
    ExpandableListAdapter expListAdapter;
    ExpandableListView expListView;
    AutoCompleteTextView nameView;
    Button checkView;
    int groupPos,childPos=-1;
    Spinner frequency;
    static long freq;
    ContentValues values = new ContentValues();

    static ArrayList<String> catagories;         // Food catagories
    static ArrayList<ArrayList<String>> pickItems = new ArrayList<>(); // Mapping from catagories to lists of food items
    static ArrayList<String> currItems = new ArrayList<>();
    static ArrayList<String> srchItems = new ArrayList<>();
    static SQLiteDatabase db;
    static CharSequence name;
    static int dBid;
    static boolean selected;
    final String[] cols = {"_id","name","flags","last_time","last_avg","ratio"};
    static Item item = null;
    Boolean newFreq = false;

    public PickList() {
        // Empty contstuctor required for DialogFragment
    }
    public interface Listener {
        void pickDone();
    }
    private Listener listener;

    public void setListener(Listener l) {
        listener = l;
    }

    /**
     * Create a new instance of PickList, providing "name", "Dbid", and "selected"
     * as an arguments.
     */
    static PickList newInstance(String name,long id,Boolean selected) {

        PickList pl = new PickList();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name",name);
        args.putLong("dBid",id);
        args.putBoolean("selected",selected);
        pl.setArguments(args);
        return pl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = getActivity();
        setListener((Listener) getActivity());
        View pickView = inflater.inflate(R.layout.pick_list, container, false);
        ArrayAdapter<String> srchAdapter = new ArrayAdapter<String>(context,
                R.layout.dropdown_layout,srchItems);
        //
        nameView = (AutoCompleteTextView)
                pickView.findViewById(R.id.name);
        nameView.setAdapter(srchAdapter);
        nameView.requestFocus();
        name = getArguments().getString("name");
        nameView.setText(name);
        TextView food_cats = pickView.findViewById(R.id.food_cats);
        selected = getArguments().getBoolean("selected");
        dBid = (int) getArguments().getLong("_id");
        checkView = (Button) pickView.findViewById(R.id.pick_button);
        freq = -1;

        final ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(context,
                R.array.frequencies,R.layout.dropdown_layout);
        frequency = (Spinner) pickView.findViewById(R.id.freq);
        frequency.setAdapter(freq_adapter);
//        frequency.setPopupBackgroundResource(R.drawable.dialog_bk);
        frequency.setOnItemSelectedListener(this);
        db = MainActivity.itemDb.getWritableDatabase();
        if(selected) {   //selected set if called from clicking on name (already has data)
            item = getDbItem(name.toString());
            item.last_time = item.last_time+1;
            freq = item.last_avg;
            if(item.last_avg < 453600)
                frequency.setSelection(0);
            else if(item.last_avg <907200)
                frequency.setSelection(1);
            else if(item.last_avg < 1814400)
                frequency.setSelection(2);
            else if(item.last_avg < 3628800)
                frequency.setSelection(3);
            else if(item.last_avg < 6048000)
                frequency.setSelection(4);
            else if(item.last_avg < 8467200)
                frequency.setSelection(5);
            else if(item.last_avg == 36500*day)
                frequency.setSelection(6);
        } else {
            frequency.setSelection(3);
            freq = (long) (8*week);
        }
        nameView.addTextChangedListener(new TextWatcher(){      // Set up text listener
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                logF("onTC: %s\t%s\t%d\t%d\t%d", s,
//                        s.subSequence(start, start + count).toString(),start,before,count);
                if(count!=0 && s.charAt(start)=='\n'){
                    newItem(nameView.getText().toString());
                    getDialog().dismiss();
                }
            }
            public void afterTextChanged(Editable s) {
            }
        });
        if(!selected) {
            expListView = (ExpandableListView) pickView.findViewById(R.id.lvExp);
            expListAdapter = new ExpandableListAdapter(context, catagories, pickItems);
            // setting list adapter
            expListView.setAdapter(expListAdapter);
            // Listview Group click listener
            expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, View v,
                                            int groupPosition, long id) {
    /*                Toast.makeText(getApplicationContext(),
                    "Group Clicked " + catagories.get(groupPosition),
                    Toast.LENGTH_SHORT).show();
    */
                    return false;   // continue click processing
                }
            });

            // Listview Group expanded listener
            expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
    /*                Toast.makeText(getApplicationContext(),
                            catagories.get(groupPosition) + " Expanded",
                            Toast.LENGTH_SHORT).show();
    */
                }
            });

            // Listview Group collasped listener
            expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                @Override
                public void onGroupCollapse(int groupPosition) {
    /*                Toast.makeText(context,
                            catagories.get(groupPosition) + " Collapsed",
                            Toast.LENGTH_SHORT).show();
    */
                }
            });

            // Listview on child click listener
            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    groupPos = groupPosition;
                    childPos = childPosition;
                    String name = pickItems.get(groupPosition).get(childPosition).substring(1);
                    if((item = getDbItem(name))!=null) {      // if already in dB
                        item.flags = 1;
                        item.last_time+=1;  // Increase last_time to force update of cloud on sync
                        changeItem(item);
                        if (inDb_flags <= 1) {
                            showToast("Item already in lists!");
                        } else {
                            showToast("item added");
                        }
                        listener.pickDone();
                    } else {
                        item = item.newItem(name);
                        item.last_time = getTime();
                        item.last_avg = freq;
                        addItem(item);
                        showToast("item added!");
                        listener.pickDone();
                    }
                    return false;
                }
            });
        }
        else
            food_cats.setText("");

        // Select listener
        //  name = name passed from clicking name in list, blank if called from "add" button
        //  formName   = name from form
        checkView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String formName = (nameView.getText().toString()).trim();     // Get name from form
                if (formName.length()!= 0) {
/*                    if (selected) {      // Edit (item selected from shopping list) ?
                        if(item == null)
                            item = getDbItem(name.toString());   // get item from database
                        if (freq > 0)
                            item.last_avg = freq;
                        item.last_time += item.last_time;
                        changeItem(item);
*/                  if(!selected) {                // new name not selected from table?
                        if ((item = getDbItem(formName))!=null) {      // if already in dB
                            item.flags = 1;
                            item.last_time+=1;  // In case cloud copy has same last_time
//                            if(freq>0)
//                                item.last_avg = freq;
                            changeItem(item);
                            showToast("Item added (already in lists!");
                        } else {
                            addItem(formName, 1, MainActivity.getTime(), 30 * MainActivity.day, 0);
                            showToast("item added!");
                        }
                    }
                } else {
                    showToast("No item entered!");
                }
                getDialog().dismiss();
                listener.pickDone();
            }
        });
        nameView.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(nameView, InputMethodManager.SHOW_IMPLICIT);
        return pickView;
    }
    static long inDb_id;
    static int inDb_flags;
    static String inDb_name;
    public boolean inDb(String name) {
        Cursor curs = db.query("'"+MainActivity.currList+"'",cols,
                "UPPER(name)=UPPER('"+name+"')",null,"","","name ASC");
        int cnt = curs.getCount();
        boolean found = cnt!=0;
        if(curs.moveToFirst()) {
            inDb_id = curs.getLong(0);
            inDb_flags = curs.getInt(2);
            inDb_name = curs.getString(1);
            curs.close();
        } else
            log("Not found");
        return found;
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
//        logF("item = %s",parent.getItemAtPosition(pos));
        newFreq = true;
        switch(pos){
            case 0: freq = (long) (3.5*day);
                break;
            case 1: freq = 7*day;
                break;
            case 2: freq = 14*day;
                break;
            case 3: freq = 31*day;
                break;
            case 4: freq = 62*day;
                break;
            case 5: freq = 93*day;
                break;
            case 6: freq = 36500*day;
                break;
        }
        //    logF("selected item %d",pos);
    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public Item getDbItem(String name) {
        Cursor curs =db.query("'"+MainActivity.currList+"'",cols,
                "UPPER(name)=UPPER('"+name+"')",null,"","","name ASC");
        if(curs.moveToFirst()) {
            Item c = new Item(curs.getLong(0), curs.getString(1), curs.getInt(2), curs.getLong(3), curs.getLong(4), curs.getFloat(5));
            curs.close();
            return c;
        } else {
            curs.close();
            return null;
        }
    }
    public void addItem(Item item) {
        addItem(item.name,(int) item.flags,item.last_time,item.last_avg,item.ratio);
    }
    public void changeItem(Item item) {
        changeItem(item.name,(int) item.flags,item.last_time,item.last_avg,item.ratio,item.id);
    }
    public static void changeItem(String nm,int il,long lt,long la,double r,long id){
        ContentValues listValues = new ContentValues();
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("flags",il);
        listValues.put("last_time", Math.abs(lt));
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.update("'"+MainActivity.currList+"'", listValues, "_id=" + Long.toString(id), null);
    }
    public static void addItem(String nm,int il,long lt,long la,double r) {
        ContentValues listValues = new ContentValues();
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("flags", il);
        listValues.put("last_time", lt);
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.insert("'"+MainActivity.currList+"'", null, listValues);
    }
    public static void newItem(String nm){
        addItem(nm, 1, MainActivity.getTime(), 30 * MainActivity.day, 0);
    }
    /*
     * Preparing the list data
     *   Read data from "Master_Grocery_List.txt"
      *  into pickItems ArrayList
     */
    public static void prepareListData() {

        BufferedReader rdr;
        InputStream input;
        String currCat;
        catagories = new ArrayList<>();

        try {
            input = MainActivity.assetManager.open("Master_Grocery_List.txt");
            rdr = new BufferedReader(new InputStreamReader(input));
            boolean first = true;
            int chr;
//            for(chr=rdr.read(); chr!=-1; chr=rdr.read()){
            for (; ; ) {
                chr = rdr.read();
                if (chr == -1) break;
                if (!(chr == 61551)) {      // is a Catagory
                    if (!first) {
                        pickItems.add(currItems);
//                        first = false;
                    }
                    first = false;
                    if ((currCat = rdr.readLine()) == null) break;
                    currCat = ((char) chr) + currCat;
                    catagories.add(currCat);
                    currItems = new ArrayList<>();
//                    logF("Catagory = %s",currCat);

                } else {                      // is item
                    String itm = rdr.readLine();
                    currItems.add(itm);
                    srchItems.add(itm);
//                    logF("item = %s",itm);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] cols = {"_id","name"};
        db = MainActivity.itemDb.getWritableDatabase();
        Cursor curs = db.query("'" + MainActivity.currList + "'", cols, "flags=0 or flags=1", null, "", "", null);
        for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
            String nm = curs.getString(1);
            srchItems.add(nm);
        }
    }
    void showToast(String txt){
        MainActivity.showToast(context,txt,Toast.LENGTH_LONG);
    }


    @Override public void onPause() {
        super.onPause();
        log("Pausing PickList");
        db.close();
    }
    @Override public void onResume() {
        super.onResume();
        log("Resuming PickList");
        db = MainActivity.itemDb.getWritableDatabase();
    }
}
