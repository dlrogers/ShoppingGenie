package com.symdesign.smartlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.support.v7.widget.AppCompatEditText;

/**
 * Created by dennis on 3/8/17
 *
 */
public class NameBox extends AppCompatEditText {

    public NameBox(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        MainActivity.clickLocation = MainActivity.ClickLocation.name;
        return false;
    }
}