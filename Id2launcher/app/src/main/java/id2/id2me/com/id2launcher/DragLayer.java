package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by sunita on 8/31/16.
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {

    String TAG = "DragLayer";

    private DragController dragController;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        return dragController.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         int X = (int) event.getX();
         int Y = (int) event.getY();
        return  dragController.onTouchEvent(event);


    }

    public void setDragController(DragController dragController) {
        this.dragController = dragController;
    }

    public float getLocationInDragLayer(View child, int[] loc) {
        loc[0] = 0;
        loc[1] = 0;
        return getDescendantCoordRelativeToSelf(child, loc);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param coord The coordinate that we want mapped.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord) {
        float scale = 1.0f;
        float[] pt = {coord[0], coord[1]};
        descendant.getMatrix().mapPoints(pt);
        scale *= descendant.getScaleX();
        pt[0] += descendant.getLeft();
        pt[1] += descendant.getTop();
        ViewParent viewParent = descendant.getParent();
        while (viewParent instanceof View && viewParent != this) {
            final View view = (View)viewParent;
            view.getMatrix().mapPoints(pt);
            scale *= view.getScaleX();
            pt[0] += view.getLeft() - view.getScrollX();
            pt[1] += view.getTop() - view.getScrollY();
            viewParent = view.getParent();
        }
        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    void onEnterScrollArea(int direction) {
        //mInScrollArea = true;
        invalidate();
    }

    void onExitScrollArea() {
       // mInScrollArea = false;
        invalidate();
    }



}

