package id2.id2me.com.id2launcher;

import android.view.DragEvent;
import android.view.View;

/**
 * Created by sunita on 9/4/16.
 */
public class WallpaperDragListener implements  View.OnDragListener {
    PageDragListener pageDragListener;
    public WallpaperDragListener(PageDragListener pageDragListener) {
        this.pageDragListener = pageDragListener;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                //   Log.v(TAG, "Drag Started");
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //  Log.v(TAG, "Drag Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                // Log.v(TAG, "Drag EXITED");
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                try {
                    //   Log.v(TAG, "Drag locating");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DROP:
                //  Log.v(TAG, "DROP Action");
                pageDragListener.dropOutOfTheBox();
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                // Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }

}
