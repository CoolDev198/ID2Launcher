package id2.id2me.com.id2launcher;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
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

public class Launcher extends AppCompatActivity implements LauncherModel.Callbacks,View.OnLongClickListener,View.OnClickListener
{
    static final int APPWIDGET_HOST_ID = 1024;
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPWIDGET = 6;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_BIND_APPWIDGET = 11;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static ViewPager pager;
    public AppWidgetManager mAppWidgetManager;
    HorizontalPagerAdapter pageAdapter;
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
    private ObservableScrollView scrollView;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;
    private int[] mTmpAddItemCellCoordinates = new int[2];
    private boolean mRestoring;
    private LauncherModel mModel;
    private boolean mWorkspaceLoading = true;
    private boolean mWaitingForResult;

    private static ArrayList<PendingAddArguments> sPendingAddList
            = new ArrayList<PendingAddArguments>();

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        int screen;
        int cellX;
        int cellY;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            db = DatabaseHandler.getInstance(this);
            mInflater = getLayoutInflater();
            LauncherApplication launcherApplication = LauncherApplication.getApp();
            mModel = launcherApplication.setLauncher(this);

            mAppWidgetManager = AppWidgetManager.getInstance(this);
            mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
            mAppWidgetHost.startListening();

            mModel.startLoader(true, -1);
            setTranslucentStatus(true);
            getSupportActionBar().hide();
            setContentView(R.layout.activity_launcher);
            init();
            //  openNotificationAccess();
           // loadDesktop();
            setStatusBarStyle();


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

    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Timber.e("Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    void init() {
        dragController = new DragController(this);
        dragLayer = (DragLayer) findViewById(R.id.drag_layer);
        List<Fragment> fragments = getFragments();
        pager = (ViewPager) findViewById(R.id.viewpager);

        pageAdapter = new HorizontalPagerAdapter(getSupportFragmentManager(), fragments, pager);
        pager.setAdapter(pageAdapter);
        resetPage();

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


    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Timber.e("problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null, mPendingAddWidgetInfo);
            }
            return;
        }

        boolean delayExitSpringLoadedMode = false;
        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);
        mWaitingForResult = false;

        // We have special handling for widgets
        if (isWidgetDrop) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (appWidgetId < 0) {
                Timber.e("Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\" +
                        "widget configuration activity.");
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else {
                completeTwoStageWidgetDrop(resultCode, appWidgetId);
            }
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = new PendingAddArguments();
            args.requestCode = requestCode;
            args.intent = data;
            args.container = mPendingAddInfo.container;
            args.screen = mPendingAddInfo.screen;
            args.cellX = mPendingAddInfo.cellX;
            args.cellY = mPendingAddInfo.cellY;
            sPendingAddList.add(args);
            /*if (isWorkspaceLocked()) {
                sPendingAddList.add(args);
            } else {
                delayExitSpringLoadedMode = completeAdd(args);
            }*/
            delayExitSpringLoadedMode = completeAdd(args);
        }
        dragLayer.clearAnimatedView();
        // Exit spring loaded mode if necessary after cancelling the configuration of a widget
        /*exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), delayExitSpringLoadedMode,
                null);*/

    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout =
                (CellLayout) wokSpace.getChildAt(mPendingAddInfo.screen);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = wokSpace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screen, layout, null);
                    /*exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);*/
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            animationType = wokSpace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    /*exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);*/
                }
            };
        }
        if (dragLayer.getAnimatedView() != null) {
            wokSpace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) dragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private boolean completeAdd(PendingAddArguments args) {
        boolean result = false;
        switch (args.requestCode) {
            case REQUEST_PICK_APPLICATION:
                /*completeAddApplication(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);*/
                break;
            case REQUEST_PICK_SHORTCUT:
                processShortcut(args.intent);
                break;
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                result = true;
                break;
            case REQUEST_CREATE_APPWIDGET:
                int appWidgetId = args.intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                completeAddAppWidget(appWidgetId, args.container, args.screen, null, null);
                result = true;
                break;
            /*case REQUEST_PICK_WALLPAPER:
                // We just wanted the activity result here so we can clear mWaitingForResult
                break;*/
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return result;
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param //cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, int screen, int cellX,
                                     int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;

        CellLayout layout = (CellLayout) wokSpace.getChildAt(screen);

        boolean foundCellSpan = false;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
        if (info == null) {
            return;
        }
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            /*if (wokSpace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null,null)) {
                return;
            }
            DropTarget.DragObject dragObject = new DropTarget.DragObject();
            dragObject.dragInfo = info;
            if (wokSpace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }*/
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage();
            return;
        }

        //LauncherModel.addItemToDatabase(this, info, container, screen, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            wokSpace.addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1,
                    false);
        }
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
        Timber.v("on long click called ");
        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent().getParent();
        }

        resetAddInfo();
        CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
        // This happens when long clicking an item with the dpad/trackball
        if (longClickCellInfo == null) {
            return true;
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final View itemUnderLongClick = longClickCellInfo.cell;
        if (!dragController.isDragging()) {
            if (itemUnderLongClick == null) {
                // User long pressed on empty space
                wokSpace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            } else {
                if (!(itemUnderLongClick instanceof FolderItemView)) {
                    // User long pressed on an item
                    wokSpace.startDrag(longClickCellInfo);
                }
            }
        }
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
        this.wokSpace.setOnLongClickListener(this);
        dragController.addDropTarget(wokSpace);
        dragController.addDragListener(wokSpace);
    }

    public WorkSpace getWokSpace() {
        return wokSpace;
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.app_item_view,
                null, info, null);
    }



    public View createShortcut(int app_item_view, CellLayout cellLayout, ShortcutInfo info, DragSource dragSource) {
        AppItemView favorite = (AppItemView) mInflater.inflate(app_item_view, cellLayout, false);
      //  favorite.setOnLongClickListener(this);
        favorite.setOnClickListener(this);
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

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
                                  int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCell(context.getResources(), requiredWidth, requiredHeight, null);
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }


    void showOutOfSpaceMessage() {
        Toast.makeText(this, R.string.out_of_space, Toast.LENGTH_SHORT).show();

    }

    public FolderIcon addFolder(CellLayout target, long container, int screen, int i, int i1) {

        return null;
    }

    public void setScrollView(ObservableScrollView scrollView) {
        this.scrollView = scrollView;
    }

    public ObservableScrollView getScrollView() {
        return scrollView;
    }

    @Override
    public void onClick(View view) {
        if(view instanceof AppItemView){
            final ShortcutInfo shortcutInfo = (ShortcutInfo) view.getTag();
            startActivitySafely(view,shortcutInfo.intent,shortcutInfo);
        }

    }
    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screen = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }
    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param //position The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, int screen,
                                 int[] cell, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screen = screen;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_application));
            startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    /**
     * Process a widget drop.
     *
     * @param info The PendingAppWidgetInfo of the widget being added.
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param //position The location on the screen where it was dropped, optional
     */
    void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, int screen,
                              int[] cell, int[] span, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screen = info.screen = screen;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success = false;
            if (options != null) {
                //TODO add options bundle for 4.2+
                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                        info.componentName);
            } else {
                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                        info.componentName);
            }
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void addAppWidgetImpl(final int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget,
                          AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;

            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            completeAddAppWidget(appWidgetId, info.container, info.screen, boundWidget,
                    appWidgetInfo);
            // Exit spring loaded mode if necessary after adding the widget
            //exitSpringLoadedDragModeDelayed(true, false, null);
        }
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param //cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(final int appWidgetId, long container, int screen,
                                      AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = (CellLayout) wokSpace.getChildAt(screen);

        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);

        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        boolean foundCellSpan = false;
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }

        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                    }
                }.start();
            }
            showOutOfSpaceMessage();
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = mPendingAddInfo.minSpanY;

        /*LauncherModel.addItemToDatabase(this, launcherInfo,
                container, screen, cellXY[0], cellXY[1], false);*/

        //if (!mRestoring) {
            if (hostView == null) {
                // Perform actual inflatixzxon because we're live
                launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
                Timber.v("App Widget ID : " + appWidgetId + " name : " +
                        launcherInfo.hostView.getAppWidgetInfo().provider.getPackageName());
            } else {
                // The AppWidgetHostView has already been inflated and instantiated
                launcherInfo.hostView = hostView;
            }

        try {
            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(View.VISIBLE);
            launcherInfo.notifyWidgetSizeChanged(this);

            wokSpace.addInScreen(launcherInfo.hostView, container, screen, cellXY[0], cellXY[1],
                    launcherInfo.spanX, launcherInfo.spanY, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        //}
        resetAddInfo();
    }


}
