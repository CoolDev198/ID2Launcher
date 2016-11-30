package id2.id2me.com.id2launcher;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by sunita on 10/20/16.
 */

public class HorizontalPager extends ViewPager {
    String TAG ="HorizontalPager";
    public HorizontalPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        Log.i(TAG," on touch ");
//        FrameLayout dragLayer = (FrameLayout) this.getParent();
//        if(((DragView)dragLayer.findViewById(R.id.drag_view)).isLongClick){
//            return false;
//        }else {
//            return super.onTouchEvent(ev);
//        }
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
