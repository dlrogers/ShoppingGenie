package com.symdesign.smartlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by dennis on 10/27/15.
 */
class NonScrollListView extends ListView {

    public NonScrollListView(Context context) {
        super(context);
    }
    public NonScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NonScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
         return false;
    }
}
