package id2.id2me.com.id2launcher;

import android.view.MotionEvent;

/**
 * Created by sunita on 11/29/16.
 */

public class DragController {

    void DragController() {
    }

    public boolean onTouchEvent(MotionEvent event) {
        int X = (int) event.getX();
        int Y = (int) event.getY();
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        int X = (int) event.getX();
        int Y = (int) event.getY();
        return true;
    }

    void handleMoveEvent() {
    }

    void cancelDrag() {
    }

    void startDrag() {
    }

}
