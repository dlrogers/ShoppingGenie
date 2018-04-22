package com.symdesign.smartlist;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.symdesign.smartlist.MainActivity;

/**
 * Created by dennis on 10/27/15.
 */
public class TextHandler implements TextWatcher {

    View view;

    public TextHandler(View v) {
        this.view = v;
    }
    public void afterTextChanged(Editable e) {
//        MainActivity.logF("getview = %s", view.toString());
//         MainActivity.logF("Editable = %s",e.toString());
    }
    public void beforeTextChanged(CharSequence c,int start,int count,int after) {
//			log("before");
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//			log("on");
    }
}
