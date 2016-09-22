package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.notificationWidget.NotificationService;

public class Launcher extends AppCompatActivity implements View.OnLongClickListener{
    private static final int REQUEST_PICK_APPWIDGET = 6;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_BIND_APPWIDGET = 11;

    public AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;
    HorizontalPagerAdapter pageAdapter;
    static final int APPWIDGET_HOST_ID = 1024;
    private DesktopFragment desktopFragment;
    static final String TAG = "Launcher";
    LauncherApplication launcherApplication;
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            db =  DatabaseHandler.getInstance(this);
            setContentView(R.layout.activity_home);
            setTranslucentStatus(true);
            launcherApplication = ((LauncherApplication) getApplication());
            getSupportActionBar().hide();
            launcherApplication.setLauncher(this);

            init();
            openNotificationAccess();
            loadDesktop();
            setStatusBarStyle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadDesktop() {
       db.getItemsInfo();
    }

    private void setStatusBarStyle() {
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(Color.TRANSPARENT);
    }

    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }



    private void openNotificationAccess() {
        if(!NotificationService.isNotificationAccessEnabled)
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    public void startActivityForBindingWidget(int appWidgetId, ComponentName componentName) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                componentName);
        startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
    }

    public void startActivityForWidgetConfig(int appWidgetId, AppWidgetProviderInfo appWidgetProviderInfo) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setComponent(appWidgetProviderInfo.configure);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
    }

    void init() {
        List<Fragment> fragments = getFragments();
        NonSwipeViewPager pager = (NonSwipeViewPager) findViewById(R.id.viewpager);
        pageAdapter = new HorizontalPagerAdapter(getSupportFragmentManager(), fragments, pager);
        pager.setPagerAdapter(pageAdapter);
        pager.setApplication(launcherApplication);
        pager.setAdapter(pageAdapter);

        setAppWidgetManager();
    }

    private void setAppWidgetManager() {
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        ((LauncherApplication) getApplication()).mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        this.mAppWidgetHost = ((LauncherApplication) getApplication()).mAppWidgetHost;
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mAppWidgetHost.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_BIND_APPWIDGET) {
                    ((LauncherApplication) getApplication()).getPageDragListener()
                            .askToConfigure(data);
                }

                if (requestCode == REQUEST_PICK_APPWIDGET) {
                    ((LauncherApplication) getApplication()).getPageDragListener()
                            .askToConfigure(data);
                } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                    ((LauncherApplication) getApplication()).getPageDragListener().addWidgetImpl(data);
                }
            } else if (resultCode == RESULT_CANCELED && data != null) {
            }

    }

    @Override
    public void onBackPressed() {
        //    ((DrawerHandler) desktopFragment).drawerClose();
    }

    private List<Fragment> getFragments() {
        List<Fragment> fList = null;
        try {
            fList = new ArrayList<Fragment>();
            desktopFragment = DesktopFragment.newInstance();
            fList.add(desktopFragment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fList;
    }

    public void addNewFolderFragment() {
        pageAdapter.addNewFolderFragment();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
