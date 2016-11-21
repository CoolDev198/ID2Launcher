package id2.id2me.com.id2launcher;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by sunita on 10/13/16.
 */

public class NonSwipePager extends ViewPager {
    GestureDetector gestureDetector;
    public NonSwipePager(Context context, AttributeSet attrs) {
        super(context, attrs);

        gestureDetector = new GestureDetector(context,new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Launcher.pager.setCurrentItem(1);
            return false;
        }



}

}
