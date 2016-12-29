package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import java.util.Calendar;


/**
 * Created by sunita on 8/21/16.
 */

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    final String TAG = "AppWidgetHostView";
    LayoutInflater mInflater;
    LauncherApplication launcherApplication;
    private Handler handler;
    private Runnable runnable;
    private static final int MIN_CLICK_DURATION = 1000;
    private long eventDownTime;
    private boolean longPress = false;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addHandler();
    }

    private void addHandler() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //long clickDuration = Calendar.getInstance().getTimeInMillis() - eventDownTime;
                long eventDuration = android.os.SystemClock.elapsedRealtime() - eventDownTime;
                if (eventDuration >= MIN_CLICK_DURATION && longPress) {
                    onDragWidget();
                }
            }
        };
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            eventDownTime = ev.getDownTime();
            //eventDownTime = Calendar.getInstance().getTimeInMillis();
            handler.postDelayed(runnable, 500);
            longPress = true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (handler != null && runnable != null && longPress) {
                longPress = false;
                handler.removeCallbacks(runnable);
            }

        }
        if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (handler != null && runnable != null && longPress) {
                longPress = false;
                handler.removeCallbacks(runnable);
            }
        }
        //return super.onInterceptTouchEvent(ev);
        return false;
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
}