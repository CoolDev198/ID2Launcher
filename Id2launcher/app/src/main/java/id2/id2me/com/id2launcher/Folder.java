package id2.id2me.com.id2launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.itemviews.FolderIcon;
import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;
import timber.log.Timber;

/**
 * Created by CrazyInnoTech on 11-11-2016.
 */

public class Folder extends FrameLayout implements DragSource, View.OnClickListener,
        View.OnLongClickListener, DropTarget, FolderInfo.FolderListener, View.OnTouchListener {
    static final int STATE_NONE = -1;
    static final int STATE_SMALL = 0;
    static final int STATE_ANIMATING = 1;
    static final int STATE_OPEN = 2;
    private static final String TAG = "Launcher.Folder";
    private static final int REORDER_ANIMATION_DURATION = 230;
    private static final int ON_EXIT_CLOSE_DELAY = 800;
    private final LayoutInflater mInflater;
    private final IconCache mIconCache;
    protected DragController mDragController;
    protected Launcher mLauncher;
    protected FolderInfo mInfo;
    protected CellLayout mContent;
    boolean mItemsInvalidated = false;
    boolean mSuppressOnAdd = false;
    Rect outR;
    private int mExpandDuration;
    private int mState = STATE_NONE;
    private boolean mRearrangeOnClose = false;
    private FolderIcon mFolderIcon;
    private int mMaxCountX;
    private int mMaxCountY;
    private int mMaxNumItems;
    private ArrayList<View> mItemsInReadingOrder = new ArrayList<View>();
    private Drawable mIconDrawable;
    private ShortcutInfo mCurrentDragInfo;
    private View mCurrentDragView;
    OnAlarmListener mOnExitAlarmListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            completeDragExit();
        }
    };
    private int[] mTargetCell = new int[2];
    private int[] mPreviousTargetCell = new int[2];
    private int[] mEmptyCell = new int[2];
    OnAlarmListener mReorderAlarmListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            realTimeReorder(mEmptyCell, mTargetCell);
        }
    };
    private Alarm mReorderAlarm = new Alarm();
    private Alarm mOnExitAlarm = new Alarm();
    private Rect mTempRect = new Rect();
    private boolean mDragInProgress = false;
    private boolean mDeleteFolderOnDropCompleted = false;
    private boolean mSuppressFolderDeletion = false;
    private boolean mItemAddedBackToSelfViaIcon = false;
    private float mFolderIconPivotX;
    private float mFolderIconPivotY;
    private ObjectAnimator mOpenCloseAnimator;
    private boolean mDestroyed;

    public Folder(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Folder(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Folder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LauncherApplication app = LauncherApplication.getApp();
        setWillNotDraw(false);
        mInflater = LayoutInflater.from(context);
        mIconCache = ((LauncherApplication) context.getApplicationContext()).getIconCache();
        Resources res = getResources();
        mMaxCountX = res.getInteger(R.integer.folder_max_count_x);
        mMaxCountY = res.getInteger(R.integer.folder_max_count_y);
        mMaxNumItems = res.getInteger(R.integer.folder_max_num_items);
        if (mMaxCountX < 0 || mMaxCountY < 0 || mMaxNumItems < 0) {
            mMaxCountX = app.CELL_COUNT_X;
            mMaxCountY = app.CELL_COUNT_Y;
            mMaxNumItems = mMaxCountX * mMaxCountY;
        }


        mExpandDuration = res.getInteger(R.integer.config_folderAnimDuration);

        mLauncher = app.getLauncher();
    }

    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     * @return A new UserFolder.
     */
    public static Folder fromXml(Context context) {
        Folder folder = null;
        try {
            LauncherApplication app = LauncherApplication.getApp();
            folder = (Folder) LayoutInflater.from(context).inflate(R.layout.user_folder, null);
            // folder.setBackground(app.getLauncher().blurredWallpaper);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return folder;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContent = (CellLayout) findViewById(R.id.folder_content);

        mContent.setGridSize(0, 0);
        mContent.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mLauncher.closeFolder();
        return super.onTouchEvent(event);
    }

    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            // refactor this code from Folder
            ShortcutInfo item = (ShortcutInfo) tag;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            item.intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));

            mLauncher.startActivitySafely(v, item.intent, item);
        } else {
            mLauncher.closeFolder();
        }
    }

    public boolean onLongClick(View v) {
        // Return if global dragging is not enabled
        //if (!mLauncher.isDraggingEnabled()) return true;

        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            ShortcutInfo item = (ShortcutInfo) tag;
            if (!v.isInTouchMode()) {
                return false;
            }


            // mLauncher.getWokSpace().onDragStartedWithItem(v);
            mLauncher.getWokSpace().beginDragShared(v, this);
            //mIconDrawable = ((ImageView) v).getDrawable();
            mLauncher.getDropTargetBar().setVisibility(VISIBLE);

            mCurrentDragInfo = item;
            mEmptyCell[0] = item.cellX;
            mEmptyCell[1] = item.cellY;
            mCurrentDragView = v;

            mContent.removeView(mCurrentDragView);
            mInfo.remove(mCurrentDragInfo);
            mDragInProgress = true;
            mItemAddedBackToSelfViaIcon = false;
        }
        return true;
    }

    public Drawable getDragDrawable() {
        return mIconDrawable;
    }

    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    public void setFolderIcon(FolderIcon icon) {
        mFolderIcon = icon;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // When the folder gets focus, we don't want to announce the list of items.
        return true;
    }

    /**
     * @return the FolderInfo object associated with this folder
     */
    FolderInfo getInfo() {
        return mInfo;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof AppItemView)) {
            mLauncher.closeFolder();
        }
        return true;
    }

    private void placeInReadingOrder(ArrayList<ShortcutInfo> items) {
        int maxX = 0;
        int count = items.size();
        for (int i = 0; i < count; i++) {
            ShortcutInfo item = items.get(i);
            if (item.cellX > maxX) {
                maxX = item.cellX;
            }
        }

        GridComparator gridComparator = new GridComparator(maxX + 1);
        Collections.sort(items, gridComparator);
        final int countX = mContent.getCountX();
        for (int i = 0; i < count; i++) {
            int x = i % countX;
            int y = i / countX;
            ShortcutInfo item = items.get(i);
            item.cellX = x;
            item.cellY = y;
        }
    }

    public void bind(FolderInfo info) {
        mInfo = info;
        ArrayList<ShortcutInfo> children = info.contents;
        ArrayList<ShortcutInfo> overflow = new ArrayList<ShortcutInfo>();
        setupContentForNumItems(children.size());
        placeInReadingOrder(children);
        int count = 0;
        for (int i = 0; i < children.size(); i++) {
            ShortcutInfo child = (ShortcutInfo) children.get(i);
            if (!createAndAddShortcut(child)) {
                overflow.add(child);
            } else {
                count++;
            }
        }

        // We rearrange the items in case there are any empty gaps
        setupContentForNumItems(count);

        // If our folder has too many items we prune them from the list. This is an issue
        // when upgrading from the old Folders implementation which could contain an unlimited
        // number of items.
        for (ShortcutInfo item : overflow) {
            mInfo.remove(item);
            //  LauncherModel.deleteItemFromDatabase(mLauncher, item);
        }

        mItemsInvalidated = true;
        mInfo.addListener(this);


        updateItemLocationsInDatabase();
    }

    /**
     * This method is intended to make the UserFolder to be visually identical in size and position
     * to its associated FolderIcon. This allows for a seamless transition into the expanded state.
     */
    private void positionAndSizeAsIcon() {
        if (!(getParent() instanceof DragLayer)) return;
        setScaleX(0.8f);
        setScaleY(0.8f);
        setAlpha(0f);
        mState = STATE_SMALL;
    }

    public void animateOpen() {
        positionAndSizeAsIcon();

        if (!(getParent() instanceof DragLayer)) return;
        //  centerAboutIcon();
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
        final ObjectAnimator oa = mOpenCloseAnimator =
                LauncherAnimUtils.ofPropertyValuesHolder(this, alpha, scaleX, scaleY);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
//                sendCustomAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
//                        String.format(getContext().getString(R.string.folder_opened),
//                                mContent.getCountX(), mContent.getCountY()));
                mState = STATE_ANIMATING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_OPEN;
                setLayerType(LAYER_TYPE_NONE, null);

                setFocusOnFirstChild();
            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        buildLayer();
        post(new Runnable() {
            public void run() {
                // Check if the animator changed in the meantime
                if (oa != mOpenCloseAnimator)
                    return;
                oa.start();
            }
        });
    }

    private void sendCustomAccessibilityEvent(int type, String text) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            onInitializeAccessibilityEvent(event);
            event.getText().add(text);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    private void setFocusOnFirstChild() {
        View firstChild = mContent.getChildAt(0, 0);
        if (firstChild != null) {
            firstChild.requestFocus();
        }
    }

    public void animateClosed() {
        if (!(getParent() instanceof DragLayer)) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.9f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.9f);
        final ObjectAnimator oa = mOpenCloseAnimator =
                LauncherAnimUtils.ofPropertyValuesHolder(this, alpha, scaleX, scaleY);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseComplete();
                setLayerType(LAYER_TYPE_NONE, null);
                mState = STATE_SMALL;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                sendCustomAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                        getContext().getString(R.string.folder_closed));
                mState = STATE_ANIMATING;
            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        buildLayer();
        post(new Runnable() {
            public void run() {
                // Check if the animator changed in the meantime
                if (oa != mOpenCloseAnimator)
                    return;
                oa.start();
            }
        });
    }

    void notifyDataSetChanged() {
        // recreate all the children if the data set changes under us. We may want to do this more
        // intelligently (ie just removing the views that should no longer exist)
        mContent.removeAllViewsInLayout();
        bind(mInfo);
    }

    public boolean acceptDrop(DragObject d) {
        final ItemInfo item = (ItemInfo) d.dragInfo;
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                !isFull());
    }

    @Override
    public void getCustomHitRect(Rect outRect) {
        outRect.set(outR);
    }

    protected boolean findAndSetEmptyCells(ShortcutInfo item) {
        int[] emptyCell = new int[2];
        if (mContent.findCellForSpan(emptyCell, item.spanX, item.spanY)) {
            item.cellX = emptyCell[0];
            item.cellY = emptyCell[1];
            return true;
        } else {
            return false;
        }
    }

    protected boolean createAndAddShortcut(ShortcutInfo shortcutInfo) {
        final AppItemView appItemView =
                (AppItemView) mInflater.inflate(R.layout.app_item_view, this, false);
        appItemView.setShortCutModel(shortcutInfo);
        appItemView.setOnClickListener(this);
        appItemView.setOnLongClickListener(this);

        // We need to check here to verify that the given item's location isn't already occupied
        // by another item.
        if (mContent.getChildAt(shortcutInfo.cellX, shortcutInfo.cellY) != null || shortcutInfo.cellX < 0 || shortcutInfo.cellY < 0
                || shortcutInfo.cellX >= mContent.getCountX() || shortcutInfo.cellY >= mContent.getCountY()) {
            // This shouldn't happen, log it.
            Log.e(TAG, "Folder order not properly persisted during bind");
            if (!findAndSetEmptyCells(shortcutInfo)) {
                return false;
            }
        }

        CellLayout.LayoutParams lp =
                new CellLayout.LayoutParams(shortcutInfo.cellX, shortcutInfo.cellY, shortcutInfo.spanX, shortcutInfo.spanY);
        boolean insert = false;
        mContent.addViewToCellLayout(appItemView, insert ? 0 : -1, (int) shortcutInfo.id, lp, true);
        return true;
    }

    public void onDragEnter(DragObject d) {
        mPreviousTargetCell[0] = -1;
        mPreviousTargetCell[1] = -1;
        mOnExitAlarm.cancelAlarm();
    }

    boolean readingOrderGreaterThan(int[] v1, int[] v2) {
        if (v1[1] > v2[1] || (v1[1] == v2[1] && v1[0] > v2[0])) {
            return true;
        } else {
            return false;
        }
    }

    private void realTimeReorder(int[] empty, int[] target) {
        boolean wrap;
        int startX;
        int endX;
        int startY;
        int delay = 0;
        float delayAmount = 30;
        if (readingOrderGreaterThan(target, empty)) {
            wrap = empty[0] >= mContent.getCountX() - 1;
            startY = wrap ? empty[1] + 1 : empty[1];
            for (int y = startY; y <= target[1]; y++) {
                startX = y == empty[1] ? empty[0] + 1 : 0;
                endX = y < target[1] ? mContent.getCountX() - 1 : target[0];
                for (int x = startX; x <= endX; x++) {
                    View v = mContent.getChildAt(x, y);
                    if (mContent.animateChildToPosition(v, empty[0], empty[1],
                            REORDER_ANIMATION_DURATION, delay, true, true)) {
                        empty[0] = x;
                        empty[1] = y;
                        delay += delayAmount;
                        delayAmount *= 0.9;
                    }
                }
            }
        } else {
            wrap = empty[0] == 0;
            startY = wrap ? empty[1] - 1 : empty[1];
            for (int y = startY; y >= target[1]; y--) {
                startX = y == empty[1] ? empty[0] - 1 : mContent.getCountX() - 1;
                endX = y > target[1] ? 0 : target[0];
                for (int x = startX; x >= endX; x--) {
                    View v = mContent.getChildAt(x, y);
                    if (mContent.animateChildToPosition(v, empty[0], empty[1],
                            REORDER_ANIMATION_DURATION, delay, true, true)) {
                        empty[0] = x;
                        empty[1] = y;
                        delay += delayAmount;
                        delayAmount *= 0.9;
                    }
                }
            }
        }
    }

    public void onDragOver(DragObject d) {
        float[] r = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, d.dragView, null);
        mTargetCell = mContent.findNearestArea((int) r[0], (int) r[1], 1, 1, mTargetCell);

        if (mTargetCell[0] != mPreviousTargetCell[0] || mTargetCell[1] != mPreviousTargetCell[1]) {
            mReorderAlarm.cancelAlarm();
            mReorderAlarm.setOnAlarmListener(mReorderAlarmListener);
            mReorderAlarm.setAlarm(150);
            mPreviousTargetCell[0] = mTargetCell[0];
            mPreviousTargetCell[1] = mTargetCell[1];
        }
    }

    // This is used to compute the visual center of the dragView. The idea is that
    // the visual center represents the user's interpretation of where the item is, and hence
    // is the appropriate point to use when determining drop location.
    private float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
                                            DragView dragView, float[] recycle) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        // In order to find the visual center, we shift by half the dragRect
        res[0] = left + dragView.getDragRegion().width() / 2;
        res[1] = top + dragView.getDragRegion().height() / 2;

        return res;
    }

    public void completeDragExit() {
        mLauncher.closeFolder();
        mCurrentDragInfo = null;
        mCurrentDragView = null;
        mSuppressOnAdd = false;
        mRearrangeOnClose = true;
    }

    public void onDragExit(DragObject d) {
        // We only close the folder if this is a true drag exit, ie. not because a drop
        // has occurred above the folder.
        Timber.v(" folder ondragexit");
        if (!d.dragComplete) {
            mOnExitAlarm.setOnAlarmListener(mOnExitAlarmListener);
            mOnExitAlarm.setAlarm(ON_EXIT_CLOSE_DELAY);
        }
        mReorderAlarm.cancelAlarm();
    }

    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete,
                                boolean success) {
        if (success) {
            if (mDeleteFolderOnDropCompleted && !mItemAddedBackToSelfViaIcon) {
                replaceFolderWithFinalItem();
            }
        } else {
            // The drag failed, we need to return the item to the folder
            mFolderIcon.onDrop(d);

            // We're going to trigger a "closeFolder" which may occur before this item has
            // been added back to the folder -- this could cause the folder to be deleted
            if (mOnExitAlarm.alarmPending()) {
                mSuppressFolderDeletion = true;
            }
        }

        if (target != this) {
            if (mOnExitAlarm.alarmPending()) {
                mOnExitAlarm.cancelAlarm();
                completeDragExit();
            }
        }
        mDeleteFolderOnDropCompleted = false;
        mDragInProgress = false;
        mItemAddedBackToSelfViaIcon = false;
        mCurrentDragInfo = null;
        mCurrentDragView = null;
        mSuppressOnAdd = false;

        // Reordering may have occured, and we need to save the new item locations. We do this once
        // at the end to prevent unnecessary database operations.
        updateItemLocationsInDatabase();

        mLauncher.getDropTargetBar().setVisibility(GONE);
        mLauncher.getDeleteDropTarget().resetHoverColor();
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    public void onFlingToDelete(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // Do nothing
    }

    private void updateItemLocationsInDatabase() {
        ArrayList<View> list = getItemsInReadingOrder();
        for (int i = 0; i < list.size(); i++) {
            View v = list.get(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // LauncherModel.moveItemInDatabase(mLauncher, info, mInfo.id, 0,
            // info.cellX, info.cellY);
        }
    }

    public void notifyDrop() {
        if (mDragInProgress) {
            mItemAddedBackToSelfViaIcon = true;
        }
    }

    public boolean isDropEnabled() {
        return true;
    }

    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    private void setupContentDimensions(int count) {
        ArrayList<View> list = getItemsInReadingOrder();

        int countX = mContent.getCountX();
        int countY = mContent.getCountY();
        boolean done = false;

        while (!done) {
            int oldCountX = countX;
            int oldCountY = countY;
            if (countX * countY < count) {
                // Current grid is too small, expand it
                if ((countX <= countY || countY == mMaxCountY) && countX < mMaxCountX) {
                    countX++;
                } else if (countY < mMaxCountY) {
                    countY++;
                }
                if (countY == 0) countY++;
            } else if ((countY - 1) * countX >= count && countY >= countX) {
                countY = Math.max(0, countY - 1);
            } else if ((countX - 1) * countY >= count) {
                countX = Math.max(0, countX - 1);
            }
            done = countX == oldCountX && countY == oldCountY;
        }
        mContent.setGridSize(countX, countY);
        arrangeChildren(list);
    }

    public boolean isFull() {
        return getItemCount() >= mMaxNumItems;
    }

    private void centerAboutIcon() {
        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();

        int width = getPaddingLeft() + getPaddingRight() + mContent.getDesiredWidth();
        int height = getPaddingTop() + getPaddingBottom() + mContent.getDesiredHeight();
        DragLayer parent = (DragLayer) mLauncher.findViewById(R.id.drag_layer);

        float scale = parent.getDescendantRectRelativeToSelf(mFolderIcon, mTempRect);

        int centerX = (int) (mTempRect.left + mTempRect.width() * scale / 2);
        int centerY = (int) (mTempRect.top + mTempRect.height() * scale / 2);
        int centeredLeft = centerX - width / 2;
        int centeredTop = centerY - height / 2;

        int currentPage = mLauncher.getWokSpace().getCurrentPage();
        // In case the workspace is scrolling, we need to use the final scroll to compute
        // the folders bounds.
        //   mLauncher.getWokSpace().setFinalScrollForPageChange(currentPage);
        // We first fetch the currently visible CellLayoutChildren
        CellLayout currentLayout = (CellLayout) mLauncher.getWokSpace().getChildAt(currentPage);
        ShortcutAndWidgetContainer boundingLayout = currentLayout.getShortcutsAndWidgets();
        Rect bounds = new Rect();
        parent.getDescendantRectRelativeToSelf(boundingLayout, bounds);
        // We reset the workspaces scroll
        //   mLauncher.getWokSpace().resetFinalScrollForPageChange(currentPage);

        // We need to bound the folder to the currently visible CellLayoutChildren
        int left = Math.min(Math.max(bounds.left, centeredLeft),
                bounds.left + bounds.width() - width);
        int top = Math.min(Math.max(bounds.top, centeredTop),
                bounds.top + bounds.height() - height);
        // If the folder doesn't fit within the bounds, center it about the desired bounds
        if (width >= bounds.width()) {
            left = bounds.left + (bounds.width() - width) / 2;
        }
        if (height >= bounds.height()) {
            top = bounds.top + (bounds.height() - height) / 2;
        }

        int folderPivotX = width / 2 + (centeredLeft - left);
        int folderPivotY = height / 2 + (centeredTop - top);
        setPivotX(folderPivotX);
        setPivotY(folderPivotY);
        mFolderIconPivotX = 0;//(int) (mFolderIcon.getMeasuredWidth() *
        //  (1.0f * folderPivotX / width));
        mFolderIconPivotY = 0;//(int) (mFolderIcon.getMeasuredHeight() *
        // (1.0f * folderPivotY / height));

        lp.width = width;
        lp.height = height;
        lp.x = 0;
        lp.y = 0;
    }

    float getPivotXForIconAnimation() {
        return mFolderIconPivotX;
    }

    float getPivotYForIconAnimation() {
        return mFolderIconPivotY;
    }

    private void setupContentForNumItems(int count) {
        setupContentDimensions(count);

//        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
//        if (lp == null) {
//            lp = new DragLayer.LayoutParams(0, 0);
//          //  lp.customPosition = true;
//           // setLayoutParams(lp);
//        }
        //  centerAboutIcon();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LauncherApplication app = LauncherApplication.getApp();
        int width = app.getScreenWidth();
        int height = app.getScreenHeight();

        int contentWidthSpec = MeasureSpec.makeMeasureSpec(mContent.getDesiredWidth(),
                MeasureSpec.EXACTLY);
        int contentHeightSpec = MeasureSpec.makeMeasureSpec(mContent.getDesiredHeight(),
                MeasureSpec.EXACTLY);
        mContent.measure(contentWidthSpec, contentHeightSpec);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        int top = (height - mContent.getDesiredHeight()) / 2;
        int left = (width - mContent.getDesiredWidth()) / 2;
        Timber.v("Top : " + top + " h : " + mContent.getDesiredHeight());
        params.setMargins(left, top, 0, 0);
        mContent.setLayoutParams(params);
        outR = new Rect(left, top, left + mContent.getDesiredWidth(), top + mContent.getDesiredHeight());
        setMeasuredDimension(width, height);
    }

    private void arrangeChildren(ArrayList<View> list) {
        int[] vacant = new int[2];
        if (list == null) {
            list = getItemsInReadingOrder();
        }
        mContent.removeAllViews();

        for (int i = 0; i < list.size(); i++) {
            View v = list.get(i);
            mContent.getVacantCell(vacant, 1, 1);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
            lp.cellX = vacant[0];
            lp.cellY = vacant[1];
            ItemInfo info = (ItemInfo) v.getTag();
            if (info.cellX != vacant[0] || info.cellY != vacant[1]) {
                info.cellX = vacant[0];
                info.cellY = vacant[1];
                /// LauncherModel.addOrMoveItemInDatabase(mLauncher, info, mInfo.id, 0,
                //    info.cellX, info.cellY);
            }
            boolean insert = false;
            mContent.addViewToCellLayout(v, insert ? 0 : -1, (int) info.id, lp, true);
        }
        mItemsInvalidated = true;
    }

    public int getItemCount() {
        return mContent.getShortcutsAndWidgets().getChildCount();
    }

    public View getItemAt(int index) {
        return mContent.getShortcutsAndWidgets().getChildAt(index);
    }

    private void onCloseComplete() {
        DragLayer parent = (DragLayer) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        mDragController.removeDropTarget((DropTarget) this);
        clearFocus();
        mFolderIcon.requestFocus();

        if (mRearrangeOnClose) {
            setupContentForNumItems(getItemCount());
            mRearrangeOnClose = false;
        }
        if (getItemCount() <= 1) {
            if (!mDragInProgress && !mSuppressFolderDeletion) {
                replaceFolderWithFinalItem();
            } else if (mDragInProgress) {
                mDeleteFolderOnDropCompleted = true;
            }
        }
        mSuppressFolderDeletion = false;
    }

    private void replaceFolderWithFinalItem() {
        // Add the last remaining child to the workspace in place of the folder
        Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
                CellLayout cellLayout = mLauncher.getCellLayout(mInfo.container, mInfo.screen);

                View child = null;
                // Move the item from the folder to the workspace, in the position of the folder
                if (getItemCount() == 1) {
                    ShortcutInfo finalItem = mInfo.contents.get(0);
                    child = mLauncher.createShortcut(R.layout.app_item_view, cellLayout,
                            finalItem);
                    //  LauncherModel.addOrMoveItemInDatabase(mLauncher, finalItem, mInfo.container,
                    //   mInfo.screen, mInfo.cellX, mInfo.cellY);
                }
                if (getItemCount() <= 1) {
                    // Remove the folder
                    //LauncherModel.deleteItemFromDatabase(mLauncher, mInfo);
                    cellLayout.removeView(mFolderIcon);
                    if (mFolderIcon instanceof DropTarget) {
                        mDragController.removeDropTarget((DropTarget) mFolderIcon);
                    }
                    mLauncher.removeFolder(mInfo);
                }
                // We add the child after removing the folder to prevent both from existing at
                // the same time in the CellLayout.
                if (child != null) {
                    mLauncher.getWokSpace().addInScreen(child, mInfo.container, mInfo.screen,
                            mInfo.cellX, mInfo.cellY, mInfo.spanX, mInfo.spanY);
                }
            }
        };
        View finalChild = getItemAt(0);
        if (finalChild != null) {
            mFolderIcon.performDestroyAnimation(finalChild, onCompleteRunnable);
        }
        mDestroyed = true;
    }

    public boolean isDestroyed() {
        return mDestroyed;
    }

    public void onDrop(DragObject d) {
        ShortcutInfo item;
        if (d.dragInfo instanceof AppInfo) {
            // Came from all apps -- make a copy
            item = ((AppInfo) d.dragInfo).makeShortcut();
            item.spanX = 1;
            item.spanY = 1;
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        // Dragged from self onto self, currently this is the only path possible, however
        // we keep this as a distinct code path.
        if (item == mCurrentDragInfo) {
            ShortcutInfo si = (ShortcutInfo) mCurrentDragView.getTag();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mCurrentDragView.getLayoutParams();
            si.cellX = lp.cellX = mEmptyCell[0];
            si.cellX = lp.cellY = mEmptyCell[1];
            mContent.addViewToCellLayout(mCurrentDragView, -1, (int) item.id, lp, true);
            if (d.dragView.hasDrawn()) {
                mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, mCurrentDragView);
            } else {
                d.deferDragViewCleanupPostAnimation = false;
                mCurrentDragView.setVisibility(VISIBLE);
            }
            mItemsInvalidated = true;
            setupContentDimensions(getItemCount());
            mSuppressOnAdd = true;
        }
        mInfo.add(item);
    }

    public void onAdd(ShortcutInfo item) {
        mItemsInvalidated = true;
        // If the item was dropped onto this open folder, we have done the work associated
        // with adding the item to the folder, as indicated by mSuppressOnAdd being set
        if (mSuppressOnAdd) return;
        if (!findAndSetEmptyCells(item)) {
            // The current layout is full, can we expand it?
            setupContentForNumItems(getItemCount() + 1);
            findAndSetEmptyCells(item);
        }
        createAndAddShortcut(item);
        //LauncherModel.addOrMoveItemInDatabase(
        //  mLauncher, item, mInfo.id, 0, item.cellX, item.cellY);
    }

    public void onRemove(ShortcutInfo item) {
        mItemsInvalidated = true;
        // If this item is being dragged from this open folder, we have already handled
        // the work associated with removing the item, so we don't have to do anything here.
        if (item == mCurrentDragInfo) return;
        View v = getViewForInfo(item);
        mContent.removeView(v);
        if (mState == STATE_ANIMATING) {
            mRearrangeOnClose = true;
        } else {
            setupContentForNumItems(getItemCount());
        }
        if (getItemCount() <= 1) {
            replaceFolderWithFinalItem();
        }
    }

    private View getViewForInfo(ShortcutInfo item) {
        for (int j = 0; j < mContent.getCountY(); j++) {
            for (int i = 0; i < mContent.getCountX(); i++) {
                View v = mContent.getChildAt(i, j);
                if (v.getTag() == item) {
                    return v;
                }
            }
        }
        return null;
    }

    public void onItemsChanged() {
        //   updateTextViewFocus();
    }

    @Override
    public void addItem(ShortcutInfo destInfo) {

    }

    public void onTitleChanged(CharSequence title) {
    }

    public ArrayList<View> getItemsInReadingOrder() {
        return getItemsInReadingOrder(true);
    }

    public ArrayList<View> getItemsInReadingOrder(boolean includeCurrentDragItem) {
        if (mItemsInvalidated) {
            mItemsInReadingOrder.clear();
            for (int j = 0; j < mContent.getCountY(); j++) {
                for (int i = 0; i < mContent.getCountX(); i++) {
                    View v = mContent.getChildAt(i, j);
                    if (v != null) {
                        ShortcutInfo info = (ShortcutInfo) v.getTag();
                        if (info != mCurrentDragInfo || includeCurrentDragItem) {
                            mItemsInReadingOrder.add(v);
                        }
                    }
                }
            }
            mItemsInvalidated = false;
        }
        return mItemsInReadingOrder;
    }

    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    public void onFocusChange(View v, boolean hasFocus) {
//        if (v == mFolderName && hasFocus) {
//            startEditingFolderName();
//        }
    }

    private class GridComparator implements Comparator<ShortcutInfo> {
        int mNumCols;

        public GridComparator(int numCols) {
            mNumCols = numCols;
        }

        @Override
        public int compare(ShortcutInfo lhs, ShortcutInfo rhs) {
            int lhIndex = lhs.cellY * mNumCols + lhs.cellX;
            int rhIndex = rhs.cellY * mNumCols + rhs.cellX;
            return (lhIndex - rhIndex);
        }
    }


}
