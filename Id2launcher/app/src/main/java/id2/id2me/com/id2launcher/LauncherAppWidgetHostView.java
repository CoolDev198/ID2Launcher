package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.gesture.Gesture;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import id2.id2me.com.id2launcher.models.ItemInfo;
import timber.log.Timber;


/**
 * Created by sunita on 8/21/16.
 */

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    private CheckLongPressHelper mLongPressHelper;
    LayoutInflater mInflater;
    LauncherApplication launcherApplication;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mLongPressHelper = new CheckLongPressHelper(this);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    protected View getErrorView() {
        return super.getErrorView();
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        super.updateAppWidget(remoteViews);
    }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Consume any touch events for ourselves after longpress is triggered
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        // Watch for longpress events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLongPressHelper.postCheckForLongPress();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }
}