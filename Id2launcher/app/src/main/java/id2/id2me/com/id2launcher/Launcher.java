package id2.id2me.com.id2launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityOptions;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.fragments.DesktopFragment;
import id2.id2me.com.id2launcher.fragments.DrawerFragment;
import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.itemviews.FolderIcon;
import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.LauncherAppWidgetInfo;
import id2.id2me.com.id2launcher.models.PendingAddWidgetInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;
import id2.id2me.com.id2launcher.notificationWidget.NotificationService;
import timber.log.Timber;

public class Launcher extends FragmentActivity implements LauncherModel.Callbacks, View.OnLongClickListener, View.OnClickListener {
    static final int APPWIDGET_HOST_ID = 1024;
    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPWIDGET = 6;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_BIND_APPWIDGET = 11;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static ArrayList<PendingAddArguments> sPendingAddList
            = new ArrayList<PendingAddArguments>();
    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();
    public ViewPager pager;
    public AppWidgetManager mAppWidgetManager;
    public BitmapDrawable blurredWallpaper;
    HorizontalPagerAdapter pageAdapter;
    DatabaseHandler db;
    DragLayer dragLayer;
    private IconCache mIconCache;
    private LauncherAppWidgetHost mAppWidgetHost;
    private DesktopFragment desktopFragment;
    private DragController dragController;
    private WorkSpace wokSpace;
    private LayoutInflater mInflater;
    private ObservableScrollView scrollView;
    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;
    private int[] mTmpAddItemCellCoordinates = new int[2];
    private boolean mRestoring;
    private LauncherModel mModel;
    private boolean mWorkspaceLoading = true;
    private boolean mWaitingForResult;
    private FrameLayout dropTargetBar;
    private DeleteDropTarget deleteDropTarget;
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();

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

    public FrameLayout getDropTargetBar() {
        return dropTargetBar;
    }

    public void setDropTargetBar(FrameLayout dropTargetBar) {
        this.dropTargetBar = dropTargetBar;
    }

    public DeleteDropTarget getDeleteDropTarget() {
        return deleteDropTarget;
    }

    public void setDeleteDropTarget(DeleteDropTarget deleteDropTarget) {
        this.deleteDropTarget = deleteDropTarget;
    }

    public void removeFolder(FolderInfo mInfo) {

    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    public CellLayout getCellLayout(long container, int screen) {

        return (CellLayout) wokSpace.getChildAt(screen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = DatabaseHandler.getInstance(this);
        mInflater = getLayoutInflater();
        LauncherApplication app = LauncherApplication.getApp();
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        mModel.startLoader(true, -1);
        Utilities.setTranslucentStatus(true);
        Utilities.setStatusBarStyle(ContextCompat.getColor(this,android.R.color.transparent));

        setContentView(R.layout.activity_launcher);
        init();
        //  openNotificationAccess();
        // loadDesktop();
        //Bitmap bitmap = Bitmap.createBitmap(getDrawable(R.mipmap.wallpaper).getIntrinsicWidth(), getDrawable(R.mipmap.wallpaper).getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        //blurredWallpaper = new BitmapDrawable(getResources(), BlurBuilder.blur(this, bitmap));

    }

    private void loadDesktop() {
        db.getItemsInfo();
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
     * @param data       The intent describing the shortcut.
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
                if (!(itemUnderLongClick instanceof Folder)) {
                    // User long pressed on an item
                    dropTargetBar.setVisibility(View.VISIBLE);
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

    public WorkSpace getWokSpace() {
        return wokSpace;
    }

    public void setWokSpace(WorkSpace wokSpace) {
        this.wokSpace = wokSpace;
        this.wokSpace.setOnLongClickListener(this);
        dragController.addDragListener(wokSpace);
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.app_item_view,
                null, info);
    }

    public View createShortcut(int app_item_view, CellLayout cellLayout, ShortcutInfo info) {
        AppItemView favorite = (AppItemView) mInflater.inflate(app_item_view, cellLayout, false);
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
            Timber.e("Unable to launch. tag=" + tag + " intent=" + intent, e);
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
            Timber.e("Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag=" + tag + " intent=" + intent, e);
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

    void showOutOfSpaceMessage() {
        wokSpace.removeExtraEmptyScreen();
        Toast.makeText(this, R.string.out_of_space, Toast.LENGTH_SHORT).show();

    }

    public FolderIcon addFolder(CellLayout layout, long container, int screen, int cellX, int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        // Update the model
        //  LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
        //        false);

        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
                FolderIcon.fromXml(this, layout, folderInfo, mIconCache);
        wokSpace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        return newFolder;
    }

    public ObservableScrollView getScrollView() {
        return scrollView;
    }

    public void setScrollView(ObservableScrollView scrollView) {
        this.scrollView = scrollView;
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof ShortcutInfo) {
            final ShortcutInfo shortcutInfo = (ShortcutInfo) view.getTag();
            startActivitySafely(view, shortcutInfo.intent, shortcutInfo);
        } else if (tag instanceof FolderInfo) {
            if (view instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) view;
                handleFolderClick(fi);
            }
        }

    }

    private void handleFolderClick(FolderIcon folderIcon) {
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = wokSpace.getFolderForTag(info);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Timber.v("Folder info marked as open, but associated folder is not open. Screen: "
                    + info.screen + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }

        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
                folderScreen = wokSpace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != wokSpace.getCurrentPage()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }
        }
    }

    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;

        info.opened = true;

        // Just verify that the folder hasn't already been added to the DragLayer.
        // There was a one-off crash where the folder had a parent already.
        if (folder.getParent() == null) {
            dragLayer.addView(folder);
            dragController.addDropTarget((DropTarget) folder);
        } else {
            Timber.v("Opening folder (" + folder + ") which already has a parent (" +
                    folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.start();
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = dragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (dragLayer.indexOfChild(mFolderIconImageView) != -1) {
            dragLayer.removeView(mFolderIconImageView);
        }
        dragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    public void closeFolder() {
        Folder folder = wokSpace.getOpenFolder();
        if (folder != null) {

            closeFolder(folder);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;

        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) wokSpace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
        }
        folder.animateClosed();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        dragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    dragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
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
     * @param screen        The screen where it should be added
     * @param cell          The cell it should be added to, optional
     * @param //position    The location on the screen where it was dropped, optional
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
     * @param info       The PendingAppWidgetInfo of the widget being added.
     * @param screen     The screen where it should be added
     * @param cell       The cell it should be added to, optional
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
     * @param //cellInfo  The position on screen where to create the widget.
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

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        int screen;
        int cellX;
        int cellY;
    }


}
