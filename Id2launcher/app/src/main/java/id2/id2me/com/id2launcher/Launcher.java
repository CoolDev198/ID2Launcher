package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.notificationWidget.NotificationService;

public class Launcher extends AppCompatActivity implements View.OnLongClickListener, View.OnTouchListener {
    static final int APPWIDGET_HOST_ID = 1024;
    static final String TAG = "Launcher";
    private static final int REQUEST_PICK_APPWIDGET = 6;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_BIND_APPWIDGET = 11;
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static ViewPager pager;
    public AppWidgetManager mAppWidgetManager;
    HorizontalPagerAdapter pageAdapter;
    LauncherApplication launcherApplication;
    DatabaseHandler db;
    DragLayer dragLayer;
    private LauncherAppWidgetHost mAppWidgetHost;
    private DesktopFragment desktopFragment;
    private DragController dragController;
    private WorkSpace wokSpace;
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            db = DatabaseHandler.getInstance(this);
            mInflater = getLayoutInflater();
            setContentView(R.layout.activity_home);
            setTranslucentStatus(true);
            launcherApplication = ((LauncherApplication) getApplication());
            getSupportActionBar().hide();
            launcherApplication.setLauncher(this);

            init();
            //  openNotificationAccess();
            loadDesktop();
            setStatusBarStyle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void loadDesktop() {
        db.getItemsInfo();
    }

    public void setStatusBarStyle() {
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
        if (!NotificationService.isNotificationAccessEnabled)
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
        dragController = new DragController(this);
        dragLayer = (DragLayer) findViewById(R.id.drag_layer);
        List<Fragment> fragments = getFragments();
        pager = (ViewPager) findViewById(R.id.viewpager);

        pageAdapter = new HorizontalPagerAdapter(getSupportFragmentManager(), fragments, pager);
        pager.setAdapter(pageAdapter);
        resetPage();
        setAppWidgetManager();
    }


    private List<Fragment> getFragments() {
        List<Fragment> fList = null;
        try {
            fList = new ArrayList<Fragment>();
            dragLayer.setDragController(dragController);
            desktopFragment = DesktopFragment.newInstance(dragController);
            fList.add(DrawerFragment.newInstance());
            fList.add(desktopFragment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fList;
    }


   public void resetPage() {
        pager.setCurrentItem(1);
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
            if (mAppWidgetHost != null)
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

//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_BIND_APPWIDGET) {
//                ((LauncherApplication) getApplication()).getPageDragListener()
//                        .askToConfigure(data);
//            }
//
//            if (requestCode == REQUEST_PICK_APPWIDGET) {
//                ((LauncherApplication) getApplication()).getPageDragListener()
//                        .askToConfigure(data);
//            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
//                ((LauncherApplication) getApplication()).getPageDragListener().addWidgetImpl(data);
//            }
//        } else if (resultCode == RESULT_CANCELED && data != null) {
//        }

    }


    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            pager.setCurrentItem(1);
        }
    }


    public void addNewFolderFragment(long folderId) {
        pageAdapter.addNewFolderFragment(folderId);
    }

    public void updateFolderFragment(long folderId) {
        pageAdapter.updateFragments(folderId);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.v(TAG, " dispatch ");
        return super.dispatchTouchEvent(ev);
    }

    public DragController getDragController() {
        return dragController;
    }

    public DragLayer getDragLayer() {
        return dragLayer;
    }

    public void setWokSpace(WorkSpace wokSpace) {
        this.wokSpace = wokSpace;
        dragController.addDropTarget(wokSpace);
    }

    public WorkSpace getWokSpace() {
        return wokSpace;
    }

    public View createShortcut(int app_item_view, CellLayout cellLayout, ShortcutInfo info,DragSource dragSource) {
        AppItemView favorite = (AppItemView) mInflater.inflate(app_item_view, cellLayout, false);
        favorite.setDragSource(dragSource);
        favorite.setShortCutModel(info);
        return favorite;
    }
}
