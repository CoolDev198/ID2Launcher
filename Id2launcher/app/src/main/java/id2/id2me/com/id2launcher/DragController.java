package id2.id2me.com.id2launcher;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by sunita on 11/29/16.
 */

public class DragController {

    private DragScroller dragScroller;
    private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();
    void DragController() {
    }


    /**
     * Interface to receive notifications when a drag starts or stops
     */
    interface DragListener {

        /**
         * A drag has begun
         *
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *        or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);

        /**
         * The drag has ended
         */
        void onDragEnd();
    }


    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
    public void addDragListener(DragListener l) {
        mListeners.add(l);
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListeners.remove(l);
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */

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

    public void setDragScroller(DragScroller dragScroller) {
        this.dragScroller = dragScroller;
    }

    public void enterScroll(int y,int x,MotionEvent event) {
        dragScroller.enterScrollArea(y,x,event);
    }
}
