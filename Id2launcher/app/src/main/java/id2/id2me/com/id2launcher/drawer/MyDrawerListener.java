package id2.id2me.com.id2launcher.drawer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import id2.id2me.com.id2launcher.LauncherApplication;

/**
 * Created by bliss76 on 22/06/16.
 */
public class MyDrawerListener implements DrawerLayout.DrawerListener {
    public static DrawerHandler handler;
    Context context;
    DrawerLayout drawerLayout;

    public MyDrawerListener(DrawerHandler handler, Context context,DrawerLayout drawer) {
        this.drawerLayout=drawer;
        this.handler = handler;
        this.context = context;
    }

    @Override
    public void onDrawerClosed(View arg0) {
        Log.v("drawer close","");
        handler.drawerClose();
       // hideKeyBoard();

    }



    private void hideKeyBoard() {

        try {
            View v = null;
            InputMethodManager imm = null;

            if (context != null) {
                v = ((Activity)context).getCurrentFocus();
                imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                ((Activity)context).getCurrentFocus().setPadding(0, 0, 0, 0);
            }

            if (v != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawerOpened(View arg0) {
        handler.drawerOpen();

        Log.v("drawer opened","");
    }

    @Override
    public void onDrawerSlide(View arg0, float arg1) {
        Log.v("drawer slide","");
    }

    @Override
    public void onDrawerStateChanged(int state) {
        Log.v("drawer opened","");
        if(state==DrawerLayout.STATE_DRAGGING)
        ((LauncherApplication) ((Activity)context).getApplication()).isDrawerOpen=true;

    }


}
