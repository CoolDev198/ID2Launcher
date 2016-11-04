package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by sunita on 8/23/16.
 */

public class ObservableScrollView extends ScrollView {

    private ScrollViewListener scrollViewListener = null;
    String TAG = "ObservableScrollView";
    Context context;
    GestureDetector gestureDetector;
    LauncherApplication launcherApplication;



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface ScrollViewListener {
        void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
    }

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        gestureDetector = new GestureDetector(context, new GestureListener(this));
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        Log.v(TAG, "on TOuch event  ::  X :: Y :: " + ev.getX() + "   " + ev.getY());
        return gestureDetector.onTouchEvent(ev);
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        ObservableScrollView observableScrollView;

        public GestureListener(ObservableScrollView observableScrollView) {

            this.observableScrollView = observableScrollView;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }




        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
            Log.v("Scrollview", "top scroll");

        }

        public void onSwipeBottom() {
            Log.v("Scrollview", "bottom scroll");
        }
    }

}

