package id2.id2me.com.id2launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by sunita on 8/31/16.
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {

    ImageView mOutlineView;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        setOnHierarchyChangeListener(this);

        mOutlineView = (ImageView) findViewById(R.id.drag_outline_img);
    }


    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }

    public void setOutLineBitmap(Bitmap outlineBmp) {
        mOutlineView.setImageBitmap(outlineBmp);

    }


}

