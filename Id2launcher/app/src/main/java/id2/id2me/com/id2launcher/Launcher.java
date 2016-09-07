package id2.id2me.com.id2launcher;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.adapters.HorizontalPagerAdapter;
import id2.id2me.com.id2launcher.database.DataBaseHandler;
import id2.id2me.com.id2launcher.drawer.DrawerHandler;
import id2.id2me.com.id2launcher.general.NonSwipeViewPager;

public class Launcher extends AppCompatActivity {
    private static final int REQUEST_PICK_APPWIDGET = 6;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_BIND_APPWIDGET = 11;

    public AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;
    public static ArrayList<String> mNames;
    HorizontalPagerAdapter pageAdapter;
    static final int APPWIDGET_HOST_ID = 1024;
    private DesktopFragment desktopFragment;
    static final String TAG = "Launcher";
    LauncherApplication launcherApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_home);
            launcherApplication = ((LauncherApplication) getApplication());
            launcherApplication.setLauncher(this);
            getSupportActionBar().hide();
            init();

        } catch (Exception e) {
            e.printStackTrace();
        }

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
        DataBaseHandler.dataBaseHandler = new DataBaseHandler(this);
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
        mAppWidgetHost.startListening();
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
}
