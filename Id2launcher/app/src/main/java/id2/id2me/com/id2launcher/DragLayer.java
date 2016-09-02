package id2.id2me.com.id2launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sunita on 8/31/16.
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {


    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Disable multitouch across the workspace/all apps/customize tray
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        setOnHierarchyChangeListener(this);
    }

    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }

}

