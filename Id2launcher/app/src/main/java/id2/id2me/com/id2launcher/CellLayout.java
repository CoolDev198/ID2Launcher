package id2.id2me.com.id2launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sunita on 10/17/16.
 */

public class CellLayout extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {


    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        setOnHierarchyChangeListener(this);
        setMinimumHeight(getResources().getDisplayMetrics().heightPixels);
    }


    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }

}

