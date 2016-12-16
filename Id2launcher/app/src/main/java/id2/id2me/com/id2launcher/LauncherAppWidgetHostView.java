package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import id2.id2me.com.id2launcher.models.ItemInfo;


/**
 * Created by sunita on 8/21/16.
 */

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    final String TAG = "AppWidgetHostView";
    LayoutInflater mInflater;
    LauncherApplication launcherApplication;
    private Handler handler;
    private Runnable runnable;

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
                onDragWidget();
            }
        };
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            handler.postDelayed(runnable, 500);
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (handler != null && runnable != null)
                handler.removeCallbacks(runnable);
        }
        if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (handler != null && runnable != null)
                handler.removeCallbacks(runnable);
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    private void onDragWidget() {

        try {
            launcherApplication.dragInfo = (ItemInfo) this.getTag();

            Canvas canvas = new Canvas();
            //outlineBmp = launcherApplication.createDragOutline(launcherAppWidgetHostView );
            //launcherApplication.dragAnimation(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}