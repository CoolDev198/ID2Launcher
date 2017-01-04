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

    final String TAG = "AppWidgetHostView";
    LayoutInflater mInflater;
    LauncherApplication launcherApplication;
    LauncherAppWidgetHostView.GestureListener gestureListener;
    GestureDetector gestureDetector;
    private long startClickTime;
    private static final int MAX_CLICK_DURATION = 100;
    private Context mContext;
    private int mPreviousOrientation;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mContext = context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(context, gestureListener);
    }

    @Override
    protected View getErrorView() {
        return mInflater.inflate(R.layout.appwidget_error, this, false);
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        // Store the orientation in which the widget was inflated
        mPreviousOrientation = mContext.getResources().getConfiguration().orientation;
        super.updateAppWidget(remoteViews);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Timber.v("onInterceptTouchEvent Widgets");
        startClickTime = Calendar.getInstance().getTimeInMillis();
        return gestureDetector.onTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    private void onDragWidget() {
        try {
            launcherApplication.getLauncher().getWokSpace().startDragWidget(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        GestureListener(){
        }

        @Override
        public void onLongPress(MotionEvent e) {
            /*super.onLongPress(e);*/
            long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
            Timber.v("long press Time StartClickTime : " + startClickTime + " Click duration: " + clickDuration);
            if (clickDuration < MAX_CLICK_DURATION) {
                Timber.v("long press achived : ");
                onDragWidget();
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            //super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }
    }

}