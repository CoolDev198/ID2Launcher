package id2.id2me.com.id2launcher;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.LauncherAppWidgetInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;
import id2.id2me.com.id2launcher.notificationWidget.NotificationService;
import timber.log.Timber;

public class Launcher extends AppCompatActivity implements LauncherModel.Callbacks,View.OnLongClickListener
{
    static final int APPWIDGET_HOST_ID = 1024;
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
    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            db = DatabaseHandler.getInstance(this);
            mInflater = getLayoutInflater();
            launcherApplication = ((LauncherApplication) getApplication());
            LauncherModel mModel=launcherApplication.setLauncher(this);
            mModel.startLoader(true, -1);
            setTranslucentStatus(true);
            getSupportActionBar().hide();
            setContentView(R.layout.activity_launcher);
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
            Timber.e("problem while stopping AppWidgetHost during Launcher destruction", ex);
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
        getWokSpace().startDrag(v);
        return true;
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

    public View createShortcut(int app_item_view, CellLayout cellLayout, ShortcutInfo info, DragSource dragSource) {
        AppItemView favorite = (AppItemView) mInflater.inflate(app_item_view, cellLayout, false);
        favorite.setOnLongClickListener(this);
        favorite.setShortCutModel(info);
        return favorite;
    }

   public boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Timber.e( "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }
    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            if (useLaunchAnimation) {
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                        v.getMeasuredWidth(), v.getMeasuredHeight());

                startActivity(intent, opts.toBundle());
            } else {
                startActivity(intent);
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Timber.e( "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        return false;
    }

    @Override
    public boolean setLoadOnResume() {
        return false;
    }

    @Override
    public int getCurrentWorkspaceScreen() {
        return 0;
    }

    @Override
    public void startBinding() {

    }

    @Override
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {

    }

    @Override
    public void bindFolders(HashMap<Long, FolderInfo> folders) {

    }

    @Override
    public void finishBindingItems() {

    }

    @Override
    public void bindAppWidget(LauncherAppWidgetInfo info) {

    }

    @Override
    public void bindAllApplications(ArrayList<AppInfo> apps) {

    }

    @Override
    public void bindAppsAdded(ArrayList<AppInfo> added) {

    }

    @Override
    public void bindAppsUpdated(ArrayList<AppInfo> apps) {

    }

    @Override
    public void bindAppsRemoved(ArrayList<String> packageNames, boolean permanent) {

    }

    @Override
    public void bindPackagesUpdated() {

    }

    @Override
    public boolean isAllAppsVisible() {
        return false;
    }

    @Override
    public boolean isAllAppsButtonRank(int rank) {
        return false;
    }

    @Override
    public void bindSearchablesChanged() {

    }

    @Override
    public void onPageBoundSynchronously(int page) {

    }


}
