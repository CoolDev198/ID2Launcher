package id2.id2me.com.id2launcher.drawer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by bliss76 on 22/06/16.
 */
public class MyDrawerListener implements DrawerLayout.DrawerListener {
 public static   DrawerHandler handler;
  public static  DrawerLayout drawerLayout;
    Activity activity;



    public MyDrawerListener(DrawerHandler handler, Activity activity, DrawerLayout drawerLayout) {
        this.handler = handler;
        this.drawerLayout = drawerLayout;
        this.activity = activity;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onDrawerClosed(View arg0) {

            drawerLayout.closeDrawer(Gravity.LEFT);
           // Utility.isDrawerOpen = false;
           // Utility.isDrawerDragged = false;


        hideKeyBoard();

    }

    private void hideKeyBoard() {

        try {
            View v = null;
            InputMethodManager imm = null;

            if (activity != null) {
                v = activity.getCurrentFocus();
                imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                activity.getCurrentFocus().setPadding(0, 0, 0, 0);
            }

            if (v != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public  void onDrawerOpened(View arg0) {
       // Utility.isDrawerOpen = true;
      //  handler.drawerOpen();
        /*if (activity != null) {
            Utility.isDrawerOpen = true;
            handler.drawerOpen();
        }*/
        // if (Utility.isIdle) {

           // }

    }

    @Override
    public void onDrawerSlide(View arg0, float arg1)
    {

    }

    @SuppressLint("RtlHardcoded")
    @Override
    public  void onDrawerStateChanged(int state) {
        try {
           /* if(state==DrawerLayout.STATE_DRAGGING) {
               // handler.drawerOpen();
               // drawerLayout.closeDrawer(Gravity.LEFT);
            }*/

           /* if (state == DrawerLayout.STATE_DRAGGING
                    || state == DrawerLayout.STATE_SETTLING) {

                if (Utility.isFolder) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }
            }*/



        } catch (Exception e) {
            e.printStackTrace();
        }

    }





}
