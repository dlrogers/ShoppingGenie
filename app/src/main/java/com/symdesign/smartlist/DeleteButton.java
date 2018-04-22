package com.symdesign.smartlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by dennis on 12/3/15.
 *
 * Button to delete entry
 */
public class DeleteButton extends ImageView {

    public DeleteButton(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        MainActivity.clickLocation = MainActivity.clickLocation.del;
        return false;
    }

}
