package id2.id2me.com.id2launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;

/**
 * Created by sunita on 11/29/16.
 */

public class WorkSpace extends LinearLayout implements DropTarget, DragSource, DragScroller {
    public static final int DRAG_BITMAP_PADDING = 2;
    private static final Rect sTempRect = new Rect();
    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
    private final int[] mTempXY = new int[2];
    Launcher launcher;
    ObservableScrollView scrollView;
    private Bitmap mDragOutline = null;
    private float[] mDragViewVisualCenter = new float[2];
    private Matrix mTempInverseMatrix = new Matrix();
    private float[] mTempCellLayoutCenterCoordinates = new float[2];
    private float[] mTempDragBottomRightCoordinates = new float[2];
    private static final int BACKGROUND_FADE_OUT_DURATION = 350;
    private static final int ADJACENT_SCREEN_DROP_DURATION = 300;
    private static final int FLING_THRESHOLD_VELOCITY = 500;
    boolean mAnimatingViewIntoPlace = false;
    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = new int[2];
    private int mDragOverX = -1;
    private int mDragOverY = -1;
    /**
     * The CellLayout that is currently being dragged over
     */
    private CellLayout mDragTargetLayout = null;

    /**
     * The CellLayout which will be dropped to
     */
    /**
     * The CellLayout that we will show as glowing
     */
    private CellLayout mDragOverlappingLayout = null;
    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;
    private Point mDisplaySize = new Point();
    private int currentPage = 0;

    public WorkSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        launcher = (Launcher) context;
        Display display = launcher.getWindowManager().getDefaultDisplay();
        display.getSize(mDisplaySize);
    }

    static private float squaredDistance(float[] point1, float[] point2) {
        float distanceX = point1[0] - point2[0];
        float distanceY = point2[1] - point2[1];
        return distanceX * distanceX + distanceY * distanceY;
    }


    public void beginDragShared(View child, DragSource dragSource) {
        Resources r = getResources();
        final Canvas canvas = new Canvas();

        if (dragSource == this) {
            mDragInfo= new CellLayout.CellInfo();
            ShortcutInfo shortcutInfo = (ShortcutInfo) child.getTag();
            mDragInfo.cell = child;
            mDragInfo.spanX = shortcutInfo.spanX;
            mDragInfo.spanY = shortcutInfo.spanY;
            mDragInfo.screen = shortcutInfo.getScreen();

            // Make sure the drag was started by a long press as opposed to a long click.
            if (!child.isInTouchMode()) {
                return;
            }

            child.setVisibility(INVISIBLE);
            CellLayout layout = (CellLayout) child.getParent().getParent();
            layout.prepareChildForDrag(child);
        }

        scrollView = (ObservableScrollView) ((View) getParent());


        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(child, canvas, DRAG_BITMAP_PADDING);

        // The drag bitmap follows the touch point around on the screen
        final Bitmap b = createDragBitmap(child, new Canvas(), DRAG_BITMAP_PADDING);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = launcher.getDragLayer().getLocationInDragLayer(child, mTempXY);

        int dragLayerX =
                Math.round(mTempXY[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY =
                Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2
                        - DRAG_BITMAP_PADDING / 2);

        Point dragVisualizeOffset = null;
        Rect dragRect = null;

        if (child instanceof AppItemView) {
            int iconSize = r.getDimensionPixelSize(R.dimen.app_icon_size);
            int top = child.getPaddingTop();
            int left = (bmpWidth - iconSize) / 2;
            int right = left + iconSize;
            int bottom = top + iconSize;
            dragLayerY += top;


            // Note: The drag region is used to calculate drag layer offsets, but the
            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
            dragVisualizeOffset = new Point(-DRAG_BITMAP_PADDING / 2, DRAG_BITMAP_PADDING / 2);
            dragRect = new Rect(left, top, right, bottom);

        } else if (child instanceof FolderItemView) {
        }

        launcher.getDragController().startDrag(b, dragLayerX, dragLayerY, dragSource, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale);
        b.recycle();

    }
        /*
    *
    * We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
    * start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
    *
    * These methods mark the appropriate pages as accepting drops (which alters their visual
    * appearance).
    *
    */

    /**
     * Returns a new bitmap to show when the given View is being dragged around.
     * Responsibility for the bitmap is transferred to the caller.
     */
    public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
        Bitmap b = null;

        if (v instanceof AppItemView) {
            View icon = v.findViewById(R.id.drawer_grid_image);
            b = Bitmap.createBitmap(
                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
        } else {
        }

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        canvas.setBitmap(null);

        return b;
    }

//    public void onDragStartedWithItem(PendingAddItemInfo info, Bitmap b, boolean clipAlpha) {
//        final Canvas canvas = new Canvas();
//
//        int[] size = estimateItemSize(info.spanX, info.spanY, info, false);
//
//        // The outline is used to visualize where the item will land if dropped
//        mDragOutline = createDragOutline(b, canvas, DRAG_BITMAP_PADDING, size[0],
//                size[1], clipAlpha);
//    }
//    // estimate the size of a widget with spans hSpan, vSpan. return MAX_VALUE for each
//    // dimension if unsuccessful
//    public int[] estimateItemSize(int hSpan, int vSpan,
//                                  ItemInfo itemInfo, boolean springLoaded) {
//        int[] size = new int[2];
//        if (getChildCount() > 0) {
//            CellLayout cl = (CellLayout) mLauncher.getWorkspace().getChildAt(0);
//            Rect r = estimateItemPosition(cl, itemInfo, 0, 0, hSpan, vSpan);
//            size[0] = r.width();
//            size[1] = r.height();
//            if (springLoaded) {
//                size[0] *= mSpringLoadedShrinkFactor;
//                size[1] *= mSpringLoadedShrinkFactor;
//            }
//            return size;
//        } else {
//            size[0] = Integer.MAX_VALUE;
//            size[1] = Integer.MAX_VALUE;
//            return size;
//        }
//    }


    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */

    private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
        final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        final Bitmap b = Bitmap.createBitmap(v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        canvas.setBitmap(null);
        return b;
    }

    /**
     * Draw the View v into the given Canvas.
     *
     * @param v          the view to draw
     * @param destCanvas the canvas to draw on
     * @param padding    the horizontal and vertical padding to use when drawing
     */
    private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
        final Rect clipRect = sTempRect;
        v.getDrawingRect(clipRect);
        destCanvas.save();
        if (v instanceof AppItemView) {
            View icon = v.findViewById(R.id.drawer_grid_image);
            destCanvas.translate(-icon.getScrollX() + padding / 2, -icon.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Region.Op.REPLACE);
            icon.draw(destCanvas);
        } else if (v instanceof FolderItemView) {

        }

        destCanvas.restore();
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(Bitmap orig, Canvas canvas, int padding, int w, int h,
                                     boolean clipAlpha) {
        final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);

        Rect src = new Rect(0, 0, orig.getWidth(), orig.getHeight());
        float scaleFactor = Math.min((w - padding) / (float) orig.getWidth(),
                (h - padding) / (float) orig.getHeight());
        int scaledWidth = (int) (scaleFactor * orig.getWidth());
        int scaledHeight = (int) (scaleFactor * orig.getHeight());
        Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);

        // center the image
        dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);

        canvas.drawBitmap(orig, src, dst, null);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor,
                clipAlpha);
        canvas.setBitmap(null);

        return b;
    }


    @Override
    public boolean supportsFlingToDelete() {
        return false;
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

    @Override
    public void onFlingToDeleteCompleted() {
    }

    /**
     * Returns a specific CellLayout
     */
    CellLayout getParentCellLayoutForView(View v) {
        ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layout : layouts) {
            if (layout.getShortcutsAndWidgets().indexOfChild(v) > -1) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the CellLayouts in the workspace.
     */
    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            View layout=getChildAt(screen);
            if(layout instanceof  CellLayout)
            layouts.add(((CellLayout)layout ));
        }
        return layouts;
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success) {
        if (success) {
            if (target != this) {
                if (mDragInfo != null) {
                    getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                    if (mDragInfo.cell instanceof DropTarget) {
                        //mDragController.removeDropTarget((DropTarget) mDragInfo.cell);
                    }
                }
            }
        } else if (mDragInfo != null) {
            CellLayout cellLayout;
//            if (launcher.isHotseatLayout(target)) {
//                cellLayout = launcher.getHotseat().getLayout();
//            } else {
            cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
            //}
            cellLayout.onDropChild(mDragInfo.cell);
        }
        if (d.cancelled && mDragInfo.cell != null) {
            mDragInfo.cell.setVisibility(VISIBLE);
        }
        mDragOutline = null;
        mDragInfo = null;

        // Hide the scrolling indicator after you pick up an item
        // hideScrollingIndicator(false);
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject d) {
        if (mDragTargetLayout != null)
            mDragTargetLayout.onDragExit();
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, d.dragView,
                mDragViewVisualCenter);

        CellLayout dropTargetLayout = mDragTargetLayout;// mDropToLayout;

        // We want the point to be mapped to the dragTarget.
        if (dropTargetLayout != null) {
//            if (launcher.isHotseatLayout(dropTargetLayout)) {
//                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
//            } else {
            mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            // }
        }
//
        int snapScreen = -1;
        boolean resizeOnDrop = false;
        if (d.dragSource != this) {
            final int[] touchXY = new int[]{(int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1]};
            onDropExternal(touchXY, d.dragInfo, dropTargetLayout, false, d);
        } else if (mDragInfo != null) {
            final View cell = mDragInfo.cell;

            Runnable resizeRunnable = null;
            if (dropTargetLayout != null) {
                // Move internally
                boolean hasMovedLayouts = (getParentCellLayoutForView(cell) != dropTargetLayout);

                long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                int screen = (mTargetCell[0] < 0) ?
                        mDragInfo.screen : indexOfChild(dropTargetLayout);
                int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                int spanY = mDragInfo != null ? mDragInfo.spanY : 1;
                // First we find the cell nearest to point at which the item is
                // dropped, without any consideration to whether there is an item there.

                mTargetCell = findNearestArea((int) mDragViewVisualCenter[0], (int)
                        mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout, mTargetCell);
                float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);

                // If the item being dropped is a shortcut and the nearest drop
                // cell also contains a shortcut, then create a folder with the two shortcuts.
//                if (!mInScrollArea && createUserFolderIfNecessary(cell, container,
//                        dropTargetLayout, mTargetCell, distance, false, d.dragView, null)) {
//                    return;
//                }
//
//                if (addToExistingFolderIfNecessary(cell, dropTargetLayout, mTargetCell,
//                        distance, d, false)) {
//                    return;
//                }

                // Aside from the special case where we're dropping a shortcut onto a shortcut,
                // we need to find the nearest cell location that is vacant
                ItemInfo item = (ItemInfo) d.dragInfo;
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }

                int[] resultSpan = new int[2];
                mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY, cell,
                        mTargetCell, resultSpan, CellLayout.MODE_ON_DROP);

                boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;

                // if the widget resizes on drop
//                if (foundCell && (cell instanceof AppWidgetHostView) &&
//                        (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY)) {
//                    resizeOnDrop = true;
//                    item.spanX = resultSpan[0];
//                    item.spanY = resultSpan[1];
//                    AppWidgetHostView awhv = (AppWidgetHostView) cell;
//                    AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, resultSpan[0],
//                            resultSpan[1]);
//                }

//                if (mCurrentPage != screen && !hasMovedIntoHotseat) {
//                    snapScreen = screen;
//                    snapToPage(screen);
//                }

                if (foundCell) {
                    final ItemInfo info = (ItemInfo) cell.getTag();
                    if (hasMovedLayouts) {
                        // Reparent the view
                        getParentCellLayoutForView(cell).removeView(cell);
                        addInScreen(cell, container, screen, mTargetCell[0], mTargetCell[1],
                                info.spanX, info.spanY);
                    }

                    // update the item's position after drop
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    lp.cellX = lp.tmpCellX = mTargetCell[0];
                    lp.cellY = lp.tmpCellY = mTargetCell[1];
                    lp.cellHSpan = item.spanX;
                    lp.cellVSpan = item.spanY;
                    lp.isLockedToGrid = true;
                    cell.setId(LauncherModel.getCellLayoutChildId(container, mDragInfo.screen,
                            mTargetCell[0], mTargetCell[1], mDragInfo.spanX, mDragInfo.spanY));

//                    if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
//                            cell instanceof LauncherAppWidgetHostView) {
//                        final CellLayout cellLayout = dropTargetLayout;
//                        // We post this call so that the widget has a chance to be placed
//                        // in its final location
//
//                        final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
//                        AppWidgetProviderInfo pinfo = hostView.getAppWidgetInfo();
//                        if (pinfo != null &&
//                                pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE) {
//                            final Runnable addResizeFrame = new Runnable() {
//                                public void run() {
//                                    DragLayer dragLayer = mLauncher.getDragLayer();
//                                    dragLayer.addResizeFrame(info, hostView, cellLayout);
//                                }
//                            };
//                            resizeRunnable = (new Runnable() {
//                                public void run() {
//                                    if (!isPageMoving()) {
//                                        addResizeFrame.run();
//                                    } else {
//                                        mDelayedResizeRunnable = addResizeFrame;
//                                    }
//                                }
//                            });
//                        }
//                    }

                    //  LauncherModel.moveItemInDatabase(mLauncher, info, container, screen, lp.cellX,
                    //lp.cellY);
                } else {
                    // If we can't find a drop location, we return the item to its original position
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    CellLayout layout = (CellLayout) cell.getParent().getParent();
                    layout.markCellsAsOccupiedForView(cell);
                }
            }

            final CellLayout parent = (CellLayout) cell.getParent().getParent();
            final Runnable finalResizeRunnable = resizeRunnable;
            // Prepare it to be animated into its new position
            // This must be called after the view has been re-parented
            final Runnable onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    mAnimatingViewIntoPlace = false;
//                    updateChildrenLayersEnabled(false);
//                    if (finalResizeRunnable != null) {
//                        finalResizeRunnable.run();
//                    }
                }
            };
            // mAnimatingViewIntoPlace = true;
            if (d.dragView.hasDrawn()) {
               final ItemInfo info = (ItemInfo) cell.getTag();
//                if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
//                    int animationType = resizeOnDrop ? ANIMATE_INTO_POSITION_AND_RESIZE :
//                            ANIMATE_INTO_POSITION_AND_DISAPPEAR;
//                    animateWidgetDrop(info, parent, d.dragView,
//                            onCompleteRunnable, animationType, cell, false);
//                } else {
                   int duration = snapScreen < 0 ? -1 : ADJACENT_SCREEN_DROP_DURATION;
                    launcher.getDragLayer().animateViewIntoPosition(d.dragView, cell, duration,
                            onCompleteRunnable, this);
               // }
            } else {
                //   d.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
            }
            parent.onDropChild(cell);
        }
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child  The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x      The X position of the child in the screen's grid.
     * @param y      The Y position of the child in the screen's grid.
     * @param spanX  The number of cells spanned horizontally by the child.
     * @param spanY  The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, container, screen, x, y, spanX, spanY, false);
    }

    /**
     * Drop an item that didn't originate on one of the workspace screens.
     * It may have come from Launcher (e.g. from all apps or customize), or it may have
     * come from another app altogether.
     * <p>
     * NOTE: This can also be called when we are outside of a drag event, when we want
     * to add an item to one of the workspace screens.
     */
    private void onDropExternal(final int[] touchXY, final Object dragInfo,
                                final CellLayout cellLayout, boolean insertAtFirst, DragObject d) {

        ItemInfo info = (ItemInfo) dragInfo;
        int spanX = info.getSpanX();
        int spanY = info.getSpanY();
        if (mDragInfo != null) {
            spanX = mDragInfo.spanX;
            spanY = mDragInfo.spanY;
        }

        final long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        final int screen = indexOfChild(cellLayout);


//        if (info instanceof PendingAddItemInfo) {
//            final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;
//
//            boolean findNearestVacantCell = true;
//            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
//                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
//                        cellLayout, mTargetCell);
//                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
//                        mDragViewVisualCenter[1], mTargetCell);
////                if (willCreateUserFolder((ItemInfo) d.dragInfo, cellLayout, mTargetCell,
////                        distance, true) || willAddToExistingUserFolder((ItemInfo) d.dragInfo,
////                        cellLayout, mTargetCell, distance)) {
////                    findNearestVacantCell = false;
////                }
//            }
//
//            final ItemInfo item = (ItemInfo) d.dragInfo;
//            boolean updateWidgetSize = false;
//            if (findNearestVacantCell) {
//                int minSpanX = item.getSpanX();
//                int minSpanY = item.getSpanY();
//                if (item.getMinSpanX() > 0 && item.getMinSpanY() > 0) {
//                    minSpanX = item.getMinSpanX();
//                    minSpanY = item.getMinSpanY();
//                }
//                int[] resultSpan = new int[2];
//                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
//                        (int) mDragViewVisualCenter[1], minSpanX, minSpanY, info.spanX, info.spanY,
//                        null, mTargetCell, resultSpan, CellLayout.MODE_ON_DROP_EXTERNAL);
//
//                if (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY) {
//                    updateWidgetSize = true;
//                }
//                item.spanX = resultSpan[0];
//                item.spanY = resultSpan[1];
//            }
//
//            Runnable onAnimationCompleteRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    // When dragging and dropping from customization tray, we deal with creating
//                    // widgets/shortcuts/folders in a slightly different way
//                    switch (pendingInfo.itemType) {
//                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
//                            int span[] = new int[2];
//                            span[0] = item.spanX;
//                            span[1] = item.spanY;
//                            mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingInfo,
//                                    container, screen, mTargetCell, span, null);
//                            break;
//                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
//                            mLauncher.processShortcutFromDrop(pendingInfo.componentName,
//                                    container, screen, mTargetCell, null);
//                            break;
//                        default:
//                            throw new IllegalStateException("Unknown item type: " +
//                                    pendingInfo.itemType);
//                    }
//                }
//            };
//            View finalView = pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
//                    ? ((PendingAddWidgetInfo) pendingInfo).boundWidget : null;
//
//            if (finalView instanceof AppWidgetHostView && updateWidgetSize) {
//                AppWidgetHostView awhv = (AppWidgetHostView) finalView;
//                AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, item.spanX,
//                        item.spanY);
//            }
//
////            int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
////            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET &&
////                    ((PendingAddWidgetInfo) pendingInfo).info.configure != null) {
////                animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
////            }
////            animateWidgetDrop(info, cellLayout, d.dragView, onAnimationCompleteRunnable,
////                    animationStyle, finalView, true);
//        } else {
        // This is for other drag/drop cases, like dragging from All Apps
        View view = null;

        switch (info.getItemType()) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info instanceof AppInfo) {
                    // Came from all apps -- make a copy
                    info = new ShortcutInfo((AppInfo) info);
                }

                view = launcher.createShortcut(R.layout.app_item_view, cellLayout,
                        (ShortcutInfo) info, this);
                break;
//                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
//                    view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher, cellLayout,
//                            (FolderInfo) info, mIconCache);
//                    break;
            default:
                //throw new IllegalStateException("Unknown item type: " + info.itemType);
        }

        // First we find the cell nearest to point at which the item is
        // dropped, without any consideration to whether there is an item there.
        if (touchXY != null) {
            mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                    cellLayout, mTargetCell);
            float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                    mDragViewVisualCenter[1], mTargetCell);

            //d.postAnimationRunnable = exitSpringLoadedRunnable;
//                if (createUserFolderIfNecessary(view, container, cellLayout, mTargetCell, distance,
//                        true, d.dragView, d.postAnimationRunnable)) {
//                    return;
//                }
//                if (addToExistingFolderIfNecessary(view, cellLayout, mTargetCell, distance, d,
//                        true)) {
//                    return;
//                }
        }

        if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], 1, 1, 1, 1,
                    null, mTargetCell, null, CellLayout.MODE_ON_DROP_EXTERNAL);
        } else {
            cellLayout.findCellForSpan(mTargetCell, 1, 1);
        }

        addInScreen(view, container, screen, mTargetCell[0], mTargetCell[1], info.spanX,
                info.spanY, insertAtFirst);

        cellLayout.onDropChild(view);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
        cellLayout.getShortcutsAndWidgets().measureChild(view);


//            LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen,
//                    lp.cellX, lp.cellY);

        if (d.dragView != null) {
            // We wrap the animation call in the temporary set and reset of the current
            // cellLayout to its final transform -- this means we animate the drag view to
            // the correct final location.
//                setFinalTransitionTransform(cellLayout);
//                mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, view,
//                        exitSpringLoadedRunnable);
            // resetTransitionTransform(cellLayout);
        }
        // }
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child  The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x      The X position of the child in the screen's grid.
     * @param y      The Y position of the child in the screen's grid.
     * @param spanX  The number of cells spanned horizontally by the child.
     * @param spanY  The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY,
                     boolean insert) {
        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            if (screen < 0 || screen >= getChildCount()) {
                Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                        + " (was " + screen + "); skipping child");
                return;
            }
        }

        final CellLayout layout;
        // Show folder title if not in the hotseat
        if (child instanceof FolderItemView) {
            // ((FolderItemView) child).setTextVisible(true);
        }

        layout = (CellLayout) getChildAt(screen);
        // child.setOnKeyListener(new IconKeyEventListener());


        ViewGroup.LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        if (spanX < 0 && spanY < 0) {
            lp.isLockedToGrid = false;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        int childId = 0;//LauncherModel.getCellLayoutChildId(container, screen, x, y, spanX, spanY);
        boolean markCellsAsOccupied = !(child instanceof FolderItemView);
        if (!layout.addViewToCellLayout(child, insert ? 0 : -1, childId, lp, markCellsAsOccupied)) {
            // TODO: This branch occurs when the workspace is adding views
            // outside of the defined grid
            // maybe we should be deleting these items from the LauncherModel?
            Log.w(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout");
        }

//        if (!(child instanceof FolderItemView)) {
//            child.setHapticFeedbackEnabled(false);
//          //  child.setOnLongClickListener(mLongClickListener);
//        }
//        if (child instanceof DropTarget) {
//            launcher.getDragController().addDropTarget((DropTarget) child);
//        }
    }

    //    boolean willCreateUserFolder(ItemInfo info, CellLayout target, int[] targetCell, float
//            distance, boolean considerTimeout) {
//        if (distance > mMaxDistanceForFolderCreation) return false;
//        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
//
//        if (dropOverView != null) {
//            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
//            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY)) {
//                return false;
//            }
//        }
//
//        boolean hasntMoved = false;
//        if (mDragInfo != null) {
//            hasntMoved = dropOverView == mDragInfo.cell;
//        }
//
//        if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
//            return false;
//        }
//
//        boolean aboveShortcut = (dropOverView.getTag() instanceof ShortcutInfo);
//        boolean willBecomeShortcut =
//                (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
//                        info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
//
//        return (aboveShortcut && willBecomeShortcut);
//    }
//
//    boolean willAddToExistingUserFolder(Object dragInfo, CellLayout target, int[] targetCell,
//                                        float distance) {
//        if (distance > mMaxDistanceForFolderCreation) return false;
//        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
//
//        if (dropOverView != null) {
//            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
//            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY)) {
//                return false;
//            }
//        }
//
//        if (dropOverView instanceof FolderIcon) {
//            FolderIcon fi = (FolderIcon) dropOverView;
//            if (fi.acceptDrop(dragInfo)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    boolean createUserFolderIfNecessary(View newView, long container, CellLayout target,
//                                        int[] targetCell, float distance, boolean external, DragView dragView,
//                                        Runnable postAnimationRunnable) {
//        if (distance > mMaxDistanceForFolderCreation) return false;
//        View v = target.getChildAt(targetCell[0], targetCell[1]);
//
//        boolean hasntMoved = false;
//        if (mDragInfo != null) {
//            CellLayout cellParent = getParentCellLayoutForView(mDragInfo.cell);
//            hasntMoved = (mDragInfo.cellX == targetCell[0] &&
//                    mDragInfo.cellY == targetCell[1]) && (cellParent == target);
//        }
//
//        if (v == null || hasntMoved || !mCreateUserFolderOnDrop) return false;
//        mCreateUserFolderOnDrop = false;
//        final int screen = (targetCell == null) ? mDragInfo.screen : indexOfChild(target);
//
//        boolean aboveShortcut = (v.getTag() instanceof ShortcutInfo);
//        boolean willBecomeShortcut = (newView.getTag() instanceof ShortcutInfo);
//
//        if (aboveShortcut && willBecomeShortcut) {
//            ShortcutInfo sourceInfo = (ShortcutInfo) newView.getTag();
//            ShortcutInfo destInfo = (ShortcutInfo) v.getTag();
//            // if the drag started here, we need to remove it from the workspace
//            if (!external) {
//                getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
//            }
//
//            Rect folderLocation = new Rect();
//            float scale = mLauncher.getDragLayer().getDescendantRectRelativeToSelf(v, folderLocation);
//            target.removeView(v);
//
//            FolderIcon fi =
//                    mLauncher.addFolder(target, container, screen, targetCell[0], targetCell[1]);
//            destInfo.cellX = -1;
//            destInfo.cellY = -1;
//            sourceInfo.cellX = -1;
//            sourceInfo.cellY = -1;
//
//            // If the dragView is null, we can't animate
//            boolean animate = dragView != null;
//            if (animate) {
//                fi.performCreateAnimation(destInfo, v, sourceInfo, dragView, folderLocation, scale,
//                        postAnimationRunnable);
//            } else {
//                fi.addItem(destInfo);
//                fi.addItem(sourceInfo);
//            }
//            return true;
//        }
//        return false;
//    }
//
//    boolean addToExistingFolderIfNecessary(View newView, CellLayout target, int[] targetCell,
//                                           float distance, DragObject d, boolean external) {
//        if (distance > mMaxDistanceForFolderCreation) return false;
//
//        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
//        if (!mAddToExistingFolderOnDrop) return false;
//        mAddToExistingFolderOnDrop = false;
//
//        if (dropOverView instanceof FolderIcon) {
//            FolderIcon fi = (FolderIcon) dropOverView;
//            if (fi.acceptDrop(d.dragInfo)) {
//                fi.onDrop(d);
//
//                // if the drag started here, we need to remove it from the workspace
//                if (!external) {
//                    getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
//                }
//                return true;
//            }
//        }
//        return false;
//    }
    @Override
    public void onDragEnter(DragObject dragObject) {
//        mDragEnforcer.onDragEnter();
//        mCreateUserFolderOnDrop = false;
//        mAddToExistingFolderOnDrop = false;
//


//        // Because we don't have space in the Phone UI (the CellLayouts run to the edge) we
//        // don't need to show the outlines
//        if (LauncherApplication.isScreenLarge()) {
//            showOutlines();
//        }
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
//    public CellLayout getCurrentDropLayout() {
//        return (CellLayout) getChildAt(getNextPage());
//    }
    @Override
    public void onDragOver(DragObject d) {


        Rect r = new Rect();
        CellLayout layout = null;
        ItemInfo item = (ItemInfo) d.dragInfo;
        // Ensure that we have proper spans for the item that we are dropping
        if (item.getSpanY() < 0 || item.getSpanX() < 0)
            throw new RuntimeException("Improper spans found");


        final View child = (mDragInfo == null) ? null : mDragInfo.cell;


        if (layout == null) {
            layout = findMatchingPageForDragOver(scrollView, d.x, d.y);
        }

        if (d.y > scrollView.getHeight() - 150) {
            scrollView.smoothScrollBy(0, 20);
        } else if (d.y < 200) {
            scrollView.smoothScrollBy(0, -20);
        }

        if (layout != mDragTargetLayout) {
            setCurrentDropLayout(layout);
            setCurrentDragOverlappingLayout(layout);
        }
        // Handle the drag over
        if (mDragTargetLayout != null) {
            // We want the point to be mapped to the dragTarget.
//            if (mLauncher.isHotseatLayout(mDragTargetLayout)) {
//                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
//            } else {
            mapPointFromSelfToChild(mDragTargetLayout, mDragViewVisualCenter, null);
            // }

            ItemInfo info = (ItemInfo) d.dragInfo;


            int ycalc = d.y - (mDragTargetLayout.getTop() - scrollView.getScrollY());
            mDragViewVisualCenter = getDragViewVisualCenter(d.x, ycalc, d.xOffset, d.yOffset,
                    d.dragView, mDragViewVisualCenter);
            mTargetCell = ((CellLayout) mDragTargetLayout).findNearestArea(d.x, d.y, 1, 1, mTargetCell);

            Log.v(" targetcell :: ", mTargetCell[0] + "  " + mTargetCell[1]);
            setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);

            float targetCellDistance = mDragTargetLayout.getDistanceFromCell(
                    mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);

            //  final View dragOverView = mDragTargetLayout.getChildAt(mTargetCell[0],
            //      mTargetCell[1]);

//            manageFolderFeedback(info, mDragTargetLayout, mTargetCell,
//                    targetCellDistance, dragOverView);

            int minSpanX = item.getSpanX();
            int minSpanY = item.getSpanY();
            if (item.getMinSpanX() > 0 && item.getMinSpanY() > 0) {
                minSpanX = item.getMinSpanX();
                minSpanY = item.getSpanY();
            }

            boolean nearestDropOccupied = mDragTargetLayout.isNearestDropLocationOccupied((int)
                            mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1], item.getSpanX(),
                    item.getSpanY(), child, mTargetCell);

            if (!nearestDropOccupied) {
                mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                        (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                        mTargetCell[0], mTargetCell[1], item.getSpanX(), item.getSpanY(), false,
                        d.dragView.getDragVisualizeOffset(), d.dragView.getDragRegion());
            }

//            else if ((mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER)
//                    && !mReorderAlarm.alarmPending() && (mLastReorderX != mTargetCell[0] ||
//                    mLastReorderY != mTargetCell[1])) {
//
//                // Otherwise, if we aren't adding to or creating a folder and there's no pending
//                // reorder, then we schedule a reorder
//                ReorderAlarmListener listener = new ReorderAlarmListener(mDragViewVisualCenter,
//                        minSpanX, minSpanY, item.spanX, item.spanY, d.dragView, child);
//                mReorderAlarm.setOnAlarmListener(listener);
//                mReorderAlarm.setAlarm(REORDER_TIMEOUT);
//            }

//            if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER ||
//                    !nearestDropOccupied) {
//                if (mDragTargetLayout != null) {
//                    mDragTargetLayout.revertTempState();
//                }
//            }
        }
    }

    void setCurrentDropOverCell(int x, int y) {
        if (x != mDragOverX || y != mDragOverY) {
            mDragOverX = x;
            mDragOverY = y;
            //setDragMode(DRAG_MODE_NONE);
        }
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     * <p>
     * pixelX and pixelY should be in the coordinate system of layout
     */
    private int[] findNearestArea(int pixelX, int pixelY,
                                  int spanX, int spanY, CellLayout layout, int[] recycle) {
        return layout.findNearestArea(
                pixelX, pixelY, spanX, spanY, recycle);
    }

    void setCurrentDropLayout(CellLayout layout) {
        if (mDragTargetLayout != null) {
            mDragTargetLayout.revertTempState();
            mDragTargetLayout.onDragExit();
        }
        mDragTargetLayout = layout;
        if (mDragTargetLayout != null) {
            mDragTargetLayout.onDragEnter();
        }
//        cleanupReorder(true);
//        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);
    }

    void setCurrentDragOverlappingLayout(CellLayout layout) {
        if (mDragOverlappingLayout != null) {
            // mDragOverlappingLayout.setIsDragOverlapping(false);
        }
        mDragOverlappingLayout = layout;
        if (mDragOverlappingLayout != null) {
            //   mDragOverlappingLayout.setIsDragOverlapping(true);
        }
        invalidate();
    }

    /*
    *
    * This method returns the CellLayout that is currently being dragged to. In order to drag
    * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
    * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
    *
    * Return null if no CellLayout is currently being dragged over
    *
    */

    private CellLayout findMatchingPageForDragOver(ScrollView scrollView, int x, int y) {
        for (int i = 0; i < getChildCount(); i++) {
            Rect rect = new Rect();
            View cellLayout = getChildAt(i);
            cellLayout.getHitRect(rect);

            int scrollx = scrollView.getScrollX();
            int scrolly = scrollView.getScrollY();
            int xN = x + scrollView.getScrollX();
            int yN = y + scrollView.getScrollY();


            if (rect.contains(xN, yN) && cellLayout instanceof CellLayout) {
                Log.v("top :: ", cellLayout.getTop() + "  " + cellLayout.getBottom() + "  " + i);
                return (CellLayout) cellLayout;
            }

        }
        return null;
    }

    void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
        if (cachedInverseMatrix == null) {
            v.getMatrix().invert(mTempInverseMatrix);
            cachedInverseMatrix = mTempInverseMatrix;
        }

        int scrollY = scrollView.getScrollY();

        xy[1] = xy[1] + scrollY - v.getTop();
        xy[0] = xy[0] + getScrollX() - v.getLeft();
        cachedInverseMatrix.mapPoints(xy);
    }


    /*
 *
 * Convert the 2D coordinate xy from this CellLayout's coordinate space to
 * the parent View's coordinate space. The argument xy is modified with the return result.
 *
 */
    void mapPointFromChildToSelf(View v, float[] xy) {
        v.getMatrix().mapPoints(xy);
        int scrollY = ((ObservableScrollView) findViewById(R.id.scrollView)).getScrollY();
//        if (mNextPage != INVALID_PAGE) {
//            scrollX = mScroller.getFinalX();
//        }
        xy[0] -= (getScrollX() - v.getLeft());
        xy[1] -= (scrollY - v.getTop());
    }


    // This is used to compute the visual center of the dragView. This point is then
    // used to visualize drop locations and determine where to drop an item. The idea is that
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

        // First off, the drag view has been shifted in a way that is not represented in the
        // x and y values or the x/yOffsets. Here we account for that shift.
        x += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
        y += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);

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

    @Override
    public void onDragExit(DragObject dragObject) {
        setCurrentDropLayout(null);
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {

    }

    @Override
    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return false;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        launcher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    @Override
    public void scrollUp() {

    }

    @Override
    public void scrollDown() {

    }

    @Override
    public boolean onEnterScrollArea(int x, int y, int direction) {
        return false;
    }

    @Override
    public boolean onExitScrollArea() {
        return false;
    }


    @Override
    public void getHitRect(Rect outRect) {
        // We want the workspace to have the whole area of the display (it will find the correct
        // cell layout to drop to in the existing drag/drop logic.
        outRect.set(0, 0, mDisplaySize.x, mDisplaySize.y);
    }

}
