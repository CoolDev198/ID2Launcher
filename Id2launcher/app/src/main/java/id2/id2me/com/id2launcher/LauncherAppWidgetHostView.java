package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;



/**
 * Created by sunita on 8/21/16.
 */

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    LayoutInflater mInflater;
    final String TAG = "AppWidgetHostView";
    private IWidgetDrag IWidgetInterface;
    private Handler handler;
    private Runnable runnable;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addHandler();
    }

    private void addHandler() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                IWidgetInterface.onDragWidget(LauncherAppWidgetHostView.this);
            }
        };
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            handler.postDelayed(runnable, 500);
        }
        if(ev.getAction()==MotionEvent.ACTION_UP){
            if (handler != null && runnable != null)
                handler.removeCallbacks(runnable);
        }
        if(ev.getAction()==MotionEvent.ACTION_CANCEL){
            if (handler != null && runnable != null)
                handler.removeCallbacks(runnable);
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    public void setIWidgetInterface(IWidgetDrag IWidgetInterface) {
        this.IWidgetInterface = IWidgetInterface;
    }


}