package id2.id2me.com.id2launcher;

import android.view.MotionEvent;

/**
 * Created by sunita on 11/30/16.
 */

public interface DragScroller {


    /**
     * Handles scrolling while dragging
     *
     */
        void scrollUp();
        void scrollDown();

        /**
         * The touch point has entered the scroll area; a scroll is imminent.
         * This event will only occur while a drag is active.
         *
         * @param direction The scroll direction
         */
        boolean onEnterScrollArea(int x, int y, int direction);

        /**
         * The touch point has left the scroll area.
         * NOTE: This may not be called, if a drop occurs inside the scroll area.
         */
        boolean onExitScrollArea();
    }


