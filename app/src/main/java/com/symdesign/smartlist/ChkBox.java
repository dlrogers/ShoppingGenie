package com.symdesign.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.ImageView;

/**
 * Created by dennis on 11/15/15.
 */
public class ChkBox extends ImageView {

    public ChkBox(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        MainActivity.clickLocation = MainActivity.ClickLocation.box;
        return false;
    }
 }
