package id2.id2me.com.id2launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import id2.id2me.com.id2launcher.itemviews.AppItemView;

/**
 * Created by sunita on 10/17/16.
 */

public class CellLayout extends ViewGroup {

    private final Rect mRect = new Rect();
    private final DecelerateInterpolator mEaseOutInterpolator;
    private ArrayList<View> mIntersectingViews = new ArrayList<View>();
    private final int[] mTmpXY = new int[2];
    private final Stack<Rect> mTempRectStack = new Stack<Rect>();
    LauncherApplication launcherApplication;
    private int cellCountY;
    private int cellCountX;
    private String TAG = "CellLayout";
    private ShortcutAndWidgetContainer mShortcutsAndWidgets;
    private boolean[][] mOccupied;
    private static final boolean DESTRUCTIVE_REORDER = false;
    boolean[][] mTmpOccupied;
    private int cellWidth, cellHeight,mWidthGap,mHeightGap;
    // These arrays are used to implement the drag visualization on x-large screens.
    // They are used as circular arrays, indexed by mDragOutlineCurrent.
    private Rect[] mDragOutlines = new Rect[4];
    private float[] mDragOutlineAlphas = new float[mDragOutlines.length];
    private InterruptibleInOutAnimator[] mDragOutlineAnims =
            new InterruptibleInOutAnimator[mDragOutlines.length];
    private final Point mDragCenter = new Point();
    // Used as an index into the above 3 arrays; indicates which is the most current value.
    private int mDragOutlineCurrent = 0;
    private final Paint mDragOutlinePaint = new Paint();
    // When a drag operation is in progress, holds the nearest cell to the touch point
    private final int[] mDragCell = new int[2];
    private final int[] mTmpPoint = new int[2];
    private HashMap<LayoutParams, Animator> mReorderAnimators = new
            HashMap<CellLayout.LayoutParams, Animator>();
    private HashMap<View, ReorderHintAnimation>
            mShakeAnimators = new HashMap<View, ReorderHintAnimation>();
    private static final float REORDER_HINT_MAGNITUDE = 0.12f;
    private static final int REORDER_ANIMATION_DURATION = 150;
    private float mReorderHintAnimationMagnitude;
    private final static PorterDuffXfermode sAddBlendMode =
            new PorterDuffXfermode(PorterDuff.Mode.ADD);
    private final static Paint sPaint = new Paint();
    private boolean mItemPlacementDirty = false;
    private DropTarget.DragEnforcer mDragEnforcer;
    // If we're actively dragging something over this screen, mIsDragOverlapping is true
    private boolean mIsDragOverlapping = false;
    private final CellInfo mCellInfo = new CellInfo();

    private boolean mDragging = false;

    public CellLayout(Context context) {
        super(context);
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();

        cellWidth = getResources().getDimensionPixelSize(R.dimen.cell_width);
        cellHeight = getResources().getDimensionPixelSize(R.dimen.cell_height);
        mWidthGap=getResources().getDimensionPixelSize(R.dimen.workspace_width_gap_port);
        mWidthGap=getResources().getDimensionPixelSize(R.dimen.workspace_height_gap_port);
        cellCountX = launcherApplication.CELL_COUNT_X;
        cellCountY = launcherApplication.CELL_COUNT_Y;
        mOccupied = new boolean[cellCountX][cellCountY];

        mDragEnforcer = new DropTarget.DragEnforcer(context);

        init(context);
        // When dragging things around the home screens, we show a green outline of
        // where the item will land. The outlines gradually fade out, leaving a trail
        // behind the drag path.
        // Set up all the animations that are used to implement this fading.

        setWillNotDraw(false);
        mReorderHintAnimationMagnitude = (REORDER_HINT_MAGNITUDE *
                getResources().getDimensionPixelSize(R.dimen.app_icon_size));

        final int duration = 900;
        final float fromAlphaValue = 0;
        final float toAlphaValue = (float)128;
        mEaseOutInterpolator = new DecelerateInterpolator(2.5f); // Quint ease out
        for (int i = 0; i < mDragOutlines.length; i++) {
            mDragOutlines[i] = new Rect(-1, -1, -1, -1);
        }
        for (int i = 0; i < mDragOutlineAnims.length; i++) {
            final InterruptibleInOutAnimator anim =
                    new InterruptibleInOutAnimator(duration, fromAlphaValue, toAlphaValue);
            anim.getAnimator().setInterpolator(mEaseOutInterpolator);
            final int thisIndex = i;
            anim.getAnimator().addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final Bitmap outline = (Bitmap)anim.getTag();

                    // If an animation is started and then stopped very quickly, we can still
                    // get spurious updates we've cleared the tag. Guard against this.
                    if (outline == null) {
                        @SuppressWarnings("all") // suppress dead code warning
                        final boolean debug = false;
                        if (debug) {
                            Object val = animation.getAnimatedValue();
                            Log.d(TAG, "anim " + thisIndex + " update: " + val +
                                    ", isStopped " + anim.isStopped());
                        }
                        // Try to prevent it from continuing to run
                        animation.cancel();
                    } else {
                        mDragOutlineAlphas[thisIndex] = (Float) animation.getAnimatedValue();
                        CellLayout.this.invalidate(mDragOutlines[thisIndex]);
                    }
                }
            });
            // The animation holds a reference to the drag outline bitmap as long is it's
            // running. This way the bitmap can be GCed when the animations are complete.
            anim.getAnimator().addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if ((Float) ((ValueAnimator) animation).getAnimatedValue() == 0f) {
                        anim.setTag(null);
                    }
                }
            });
            mDragOutlineAnims[i] = anim;
        }

        mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context);
        mShortcutsAndWidgets.setCellDimensions(cellWidth, cellHeight, mWidthGap, mHeightGap);
        addView(mShortcutsAndWidgets);
    }

    public void setGridSize(int x, int y) {
        mOccupied = new boolean[cellCountX][cellCountY];
        mTmpOccupied = new boolean[cellCountX][cellCountY];
        mTempRectStack.clear();
        requestLayout();
    }

    public void enableHardwareLayers() {
        mShortcutsAndWidgets.setLayerType(LAYER_TYPE_HARDWARE, sPaint);
    }

    public void disableHardwareLayers() {
        mShortcutsAndWidgets.setLayerType(LAYER_TYPE_NONE, sPaint);
    }

    public void buildHardwareLayer() {
        mShortcutsAndWidgets.buildLayer();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public boolean addViewToCellLayout(View child, int index, int childId, LayoutParams params,
                                       boolean markCells) {
        final LayoutParams lp = params;

        // Hotseat icons - remove text
        if (child instanceof AppItemView) {
            AppItemView bubbleChild = (AppItemView) child;

            Resources res = getResources();
//            if (mIsHotseat) {
//                bubbleChild.setTextColor(res.getColor(android.R.color.transparent));
//            } else {
//                bubbleChild.setTextColor(res.getColor(R.color.workspace_icon_text_color));
//            }
        }

        child.setScaleX(getChildrenScale());
        child.setScaleY(getChildrenScale());

        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        if (lp.cellX >= 0 && lp.cellX <= cellCountX - 1 && lp.cellY >= 0 && lp.cellY <= cellCountY - 1) {
            // If the horizontal or vertical span is set to -1, it is taken to
            // mean that it spans the extent of the CellLayout
            if (lp.cellHSpan < 0) lp.cellHSpan = cellCountX;
            if (lp.cellVSpan < 0) lp.cellVSpan = cellCountY;

            child.setId(childId);

            mShortcutsAndWidgets.addView(child, index, lp);

            if (markCells) markCellsAsOccupiedForView(child);

            return true;
        }
        return false;
    }


    public void markCellsAsOccupiedForView(View view) {
        markCellsAsOccupiedForView(view, mOccupied);
    }

    private void clearOccupiedCells() {
        for (int x = 0; x < cellCountX; x++) {
            for (int y = 0; y < cellCountY; y++) {
                mOccupied[x][y] = false;
            }
        }
    }

    @Override
    public void removeAllViews() {
        clearOccupiedCells();
        mShortcutsAndWidgets.removeAllViews();
    }

    @Override
    public void removeAllViewsInLayout() {
        if (mShortcutsAndWidgets.getChildCount() > 0) {
            clearOccupiedCells();
            mShortcutsAndWidgets.removeAllViewsInLayout();
        }
    }

    public void removeViewWithoutMarkingCells(View view) {
        mShortcutsAndWidgets.removeView(view);
    }

    @Override
    public void removeView(View view) {
        markCellsAsUnoccupiedForView(view);
        mShortcutsAndWidgets.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        markCellsAsUnoccupiedForView(mShortcutsAndWidgets.getChildAt(index));
        mShortcutsAndWidgets.removeViewAt(index);
    }

    @Override
    public void removeViewInLayout(View view) {
        markCellsAsUnoccupiedForView(view);
        mShortcutsAndWidgets.removeViewInLayout(view);
    }

    @Override
    public void removeViews(int start, int count) {
        for (int i = start; i < start + count; i++) {
            markCellsAsUnoccupiedForView(mShortcutsAndWidgets.getChildAt(i));
        }
        mShortcutsAndWidgets.removeViews(start, count);
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        for (int i = start; i < start + count; i++) {
            markCellsAsUnoccupiedForView(mShortcutsAndWidgets.getChildAt(i));
        }
        mShortcutsAndWidgets.removeViewsInLayout(start, count);
    }
    public void markCellsAsUnoccupiedForView(View view) {
        markCellsAsUnoccupiedForView(view, mOccupied);
    }
    public void markCellsAsUnoccupiedForView(View view, boolean occupied[][]) {
        if (view == null || view.getParent() != mShortcutsAndWidgets) return;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
       // markCellsForView(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, occupied, false);
    }

    private void markCellsForView(int cellX, int cellY, int spanX, int spanY, boolean[][] occupied,
                                  boolean value) {
        if (cellX < 0 || cellY < 0) return;
        for (int x = cellX; x < cellX + spanX && x < cellCountX; x++) {
            for (int y = cellY; y < cellY + spanY && y < cellCountY; y++) {
                occupied[x][y] = value;
            }
        }
    }
    /**
     * A drag event has begun over this layout.
     * It may have begun over this layout (in which case onDragChild is called first),
     * or it may have begun on another layout.
     */
    void onDragEnter() {
        mDragEnforcer.onDragEnter();
        mDragging = true;
    }

    void setIsDragOverlapping(boolean isDragOverlapping) {
        if (mIsDragOverlapping != isDragOverlapping) {
            mIsDragOverlapping = isDragOverlapping;
            invalidate();
        }
    }

    boolean getIsDragOverlapping() {
        return mIsDragOverlapping;
    }

    /**
     * Called when drag has left this CellLayout or has been completed (successfully or not)
     */
    void onDragExit() {
        mDragEnforcer.onDragExit();
        // This can actually be called when we aren't in a drag, e.g. when adding a new
        // item to this layout via the customize drawer.
        // Guard against that case.
        if (mDragging) {
            mDragging = false;
        }

        // Invalidate the drag data
        mDragCell[0] = mDragCell[1] = -1;
        mDragOutlineAnims[mDragOutlineCurrent].animateOut();
        mDragOutlineCurrent = (mDragOutlineCurrent + 1) % mDragOutlineAnims.length;
        revertTempState();
        setIsDragOverlapping(false);
    }

    public ShortcutAndWidgetContainer getShortcutsAndWidgets() {
        if (getChildCount() > 0) {
            return (ShortcutAndWidgetContainer) getChildAt(0);
        }
        return null;
    }


    public View getChildAt(int x, int y) {
        return mShortcutsAndWidgets.getChildAt(x, y);
    }

//    public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration,
//                                          int delay, boolean permanent, boolean adjustOccupied) {
//        ShortcutAndWidgetContainer clc = getShortcutsAndWidgets();
//        boolean[][] occupied = mOccupied;
//        if (!permanent) {
//            occupied = mTmpOccupied;
//        }
//
//        if (clc.indexOfChild(child) != -1) {
//            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
//            final ItemInfo info = (ItemInfo) child.getTag();
//
//            // We cancel any existing animations
//            if (mReorderAnimators.containsKey(lp)) {
//                mReorderAnimators.get(lp).cancel();
//                mReorderAnimators.remove(lp);
//            }
//
//            final int oldX = lp.x;
//            final int oldY = lp.y;
//            if (adjustOccupied) {
//                occupied[lp.cellX][lp.cellY] = false;
//                occupied[cellX][cellY] = true;
//            }
//            lp.isLockedToGrid = true;
//            if (permanent) {
//                lp.cellX = info.cellX = cellX;
//                lp.cellY = info.cellY = cellY;
//            } else {
//                lp.tmpCellX = cellX;
//                lp.tmpCellY = cellY;
//            }
//            clc.setupLp(lp);
//            lp.isLockedToGrid = false;
//            final int newX = lp.x;
//            final int newY = lp.y;
//
//            lp.x = oldX;
//            lp.y = oldY;
//
//            // Exit early if we're not actually moving the view
//            if (oldX == newX && oldY == newY) {
//                lp.isLockedToGrid = true;
//                return true;
//            }
//
//            ValueAnimator va = LauncherAnimUtils.ofFloat(0f, 1f);
//            va.setDuration(duration);
//            mReorderAnimators.put(lp, va);
//
//            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float r = ((Float) animation.getAnimatedValue()).floatValue();
//                    lp.x = (int) ((1 - r) * oldX + r * newX);
//                    lp.y = (int) ((1 - r) * oldY + r * newY);
//                    child.requestLayout();
//                }
//            });
//            va.addListener(new AnimatorListenerAdapter() {
//                boolean cancelled = false;
//                public void onAnimationEnd(Animator animation) {
//                    // If the animation was cancelled, it means that another animation
//                    // has interrupted this one, and we don't want to lock the item into
//                    // place just yet.
//                    if (!cancelled) {
//                        lp.isLockedToGrid = true;
//                        child.requestLayout();
//                    }
//                    if (mReorderAnimators.containsKey(lp)) {
//                        mReorderAnimators.remove(lp);
//                    }
//                }
//                public void onAnimationCancel(Animator animation) {
//                    cancelled = true;
//                }
//            });
//            va.setStartDelay(delay);
//            va.start();
//            return true;
//        }
//        return false;
//    }
    void revertTempState() {
        if (!isItemPlacementDirty() || DESTRUCTIVE_REORDER) return;
        final int count = mShortcutsAndWidgets.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mShortcutsAndWidgets.getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY) {
                lp.tmpCellX = lp.cellX;
                lp.tmpCellY = lp.cellY;
//                animateChildToPosition(child, lp.cellX, lp.cellY, REORDER_ANIMATION_DURATION,
//                        0, false, false);
            }
        }
        completeAndClearReorderHintAnimations();
        setItemPlacementDirty(false);
    }

    void setItemPlacementDirty(boolean dirty) {
        mItemPlacementDirty = dirty;
    }
    boolean isItemPlacementDirty() {
        return mItemPlacementDirty;
    }

    /**
     * Mark a child as having been dropped.
     * At the beginning of the drag operation, the child may have been on another
     * screen, but it is re-parented before this method is called.
     *
     * @param child The child that is being dropped
     */
    void onDropChild(View child) {
        if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.dropped = true;
            child.requestLayout();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
    }

    public void setTagToCellInfoForPoint(int touchX, int touchY) {
        final CellInfo cellInfo = mCellInfo;
        Rect frame = mRect;
        final int x = touchX + getScrollX();
        final int y = touchY + getScrollY();
        final int count = mShortcutsAndWidgets.getChildCount();

        boolean found = false;
        for (int i = count - 1; i >= 0; i--) {
            final View child = mShortcutsAndWidgets.getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if ((child.getVisibility() == VISIBLE || child.getAnimation() != null) &&
                    lp.isLockedToGrid) {
                child.getHitRect(frame);

                float scale = child.getScaleX();
                frame = new Rect(child.getLeft(), child.getTop(), child.getRight(),
                        child.getBottom());
                // The child hit rect is relative to the CellLayoutChildren parent, so we need to
                // offset that by this CellLayout's padding to test an (x,y) point that is relative
                // to this view.
                frame.offset(getPaddingLeft(), getPaddingTop());
                frame.inset((int) (frame.width() * (1f - scale) / 2),
                        (int) (frame.height() * (1f - scale) / 2));

                if (frame.contains(x, y)) {
                    cellInfo.cell = child;
                    cellInfo.cellX = lp.cellX;
                    cellInfo.cellY = lp.cellY;
                    cellInfo.spanX = lp.cellHSpan;
                    cellInfo.spanY = lp.cellVSpan;
                    found = true;
                    break;
                }
            }
        }

        //mLastDownOnOccupiedCell = found;

        if (!found) {
            final int cellXY[] = mTmpXY;
            pointToCellExact(x, y, cellXY);

            cellInfo.cell = null;
            cellInfo.cellX = cellXY[0];
            cellInfo.cellY = cellXY[1];
            cellInfo.spanX = 1;
            cellInfo.spanY = 1;
        }
        setTag(cellInfo);
    }

    /**
     * Given a point, return the cell that strictly encloses that point
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellExact(int x, int y, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        result[0] = (x - hStartPadding) / (cellWidth + mWidthGap);
        result[1] = (y - vStartPadding) / (cellHeight + mHeightGap);

        final int xAxis = cellCountX;
        final int yAxis = cellCountY;

        if (result[0] < 0) result[0] = 0;
        if (result[0] >= xAxis) result[0] = xAxis - 1;
        if (result[1] < 0) result[1] = 0;
        if (result[1] >= yAxis) result[1] = yAxis - 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final Paint paint = mDragOutlinePaint;
        for (int i = 0; i < mDragOutlines.length; i++) {
            final float alpha = mDragOutlineAlphas[i];
            if (alpha > 0) {
                final Rect r = mDragOutlines[i];
                scaleRectAboutCenter(r, temp, getChildrenScale());
                final Bitmap b = (Bitmap) mDragOutlineAnims[i].getTag();
                paint.setAlpha((int)(alpha + .5f));
                canvas.drawBitmap(b, null, temp, paint);
            }
        }


        super.onDraw(canvas);
    }
    public void markCellsAsOccupiedForView(View view, boolean[][] occupied) {
        if (view == null || view.getParent() != mShortcutsAndWidgets) return;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
      //  markCellsForView(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, occupied, true);
    }

//    private void markCellsForView(int cellX, int cellY, int spanX, int spanY, boolean[][] occupied,
//                                  boolean value) {
//        if (cellX < 0 || cellY < 0) return;
//        for (int x = cellX; x < cellX + spanX && x < cellCountX; x++) {
//            for (int y = cellY; y < cellY + spanY && y < cellCountY; y++) {
//                occupied[x][y] = value;
//            }
//        }
//    }
    // Class which represents the reorder hint animations. These animations show that an item is
    // in a temporary state, and hint at where the item will return to.
    class ReorderHintAnimation {
        View child;
        float finalDeltaX;
        float finalDeltaY;
        float initDeltaX;
        float initDeltaY;
        float finalScale;
        float initScale;
        private static final int DURATION = 300;
        Animator a;

        public ReorderHintAnimation(View child, int cellX0, int cellY0, int cellX1, int cellY1,
                                    int spanX, int spanY) {
            regionToCenterPoint(cellX0, cellY0, spanX, spanY, mTmpPoint);
            final int x0 = mTmpPoint[0];
            final int y0 = mTmpPoint[1];
            regionToCenterPoint(cellX1, cellY1, spanX, spanY, mTmpPoint);
            final int x1 = mTmpPoint[0];
            final int y1 = mTmpPoint[1];
            final int dX = x1 - x0;
            final int dY = y1 - y0;
            finalDeltaX = 0;
            finalDeltaY = 0;
            if (dX == dY && dX == 0) {
            } else {
                if (dY == 0) {
                    finalDeltaX = - Math.signum(dX) * mReorderHintAnimationMagnitude;
                } else if (dX == 0) {
                    finalDeltaY = - Math.signum(dY) * mReorderHintAnimationMagnitude;
                } else {
                    double angle = Math.atan( (float) (dY) / dX);
                    finalDeltaX = (int) (- Math.signum(dX) *
                            Math.abs(Math.cos(angle) * mReorderHintAnimationMagnitude));
                    finalDeltaY = (int) (- Math.signum(dY) *
                            Math.abs(Math.sin(angle) * mReorderHintAnimationMagnitude));
                }
            }
            initDeltaX = child.getTranslationX();
            initDeltaY = child.getTranslationY();
            finalScale = getChildrenScale() - 4.0f / child.getWidth();
            initScale = child.getScaleX();
            this.child = child;
        }

        void animate() {
            if (mShakeAnimators.containsKey(child)) {
                ReorderHintAnimation oldAnimation = mShakeAnimators.get(child);
                oldAnimation.cancel();
                mShakeAnimators.remove(child);
                if (finalDeltaX == 0 && finalDeltaY == 0) {
                    completeAnimationImmediately();
                    return;
                }
            }
            if (finalDeltaX == 0 && finalDeltaY == 0) {
                return;
            }
            ValueAnimator va = LauncherAnimUtils.ofFloat(0f, 1f);
            a = va;
            va.setRepeatMode(ValueAnimator.REVERSE);
            va.setRepeatCount(ValueAnimator.INFINITE);
            va.setDuration(DURATION);
            va.setStartDelay((int) (Math.random() * 60));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float r = ((Float) animation.getAnimatedValue()).floatValue();
                    float x = r * finalDeltaX + (1 - r) * initDeltaX;
                    float y = r * finalDeltaY + (1 - r) * initDeltaY;
                    child.setTranslationX(x);
                    child.setTranslationY(y);
                    float s = r * finalScale + (1 - r) * initScale;
                    child.setScaleX(s);
                    child.setScaleY(s);
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                public void onAnimationRepeat(Animator animation) {
                    // We make sure to end only after a full period
                    initDeltaX = 0;
                    initDeltaY = 0;
                    initScale = getChildrenScale();
                }
            });
            mShakeAnimators.put(child, this);
            va.start();
        }

        private void cancel() {
            if (a != null) {
                a.cancel();
            }
        }

        private void completeAnimationImmediately() {
            if (a != null) {
                a.cancel();
            }

            AnimatorSet s = LauncherAnimUtils.createAnimatorSet();
            a = s;
            s.playTogether(
                    LauncherAnimUtils.ofFloat(child, "scaleX", getChildrenScale()),
                    LauncherAnimUtils.ofFloat(child, "scaleY", getChildrenScale()),
                    LauncherAnimUtils.ofFloat(child, "translationX", 0f),
                    LauncherAnimUtils.ofFloat(child, "translationY", 0f)
            );
            s.setDuration(REORDER_ANIMATION_DURATION);
            s.setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f));
            s.start();
        }
    }

    private void completeAndClearReorderHintAnimations() {
        for (ReorderHintAnimation a: mShakeAnimators.values()) {
            a.completeAnimationImmediately();
        }
        mShakeAnimators.clear();
    }
    public float getChildrenScale() {
        return 1.0f;
    }

    public void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    Rect temp = new Rect();
    void scaleRectAboutCenter(Rect in, Rect out, float scale) {
        int cx = in.centerX();
        int cy = in.centerY();
        out.set(in);
        out.offset(-cx, -cy);
        scaleRect(out, scale);
        out.offset(cx, cy);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = cellHeight * cellCountY;
        int width = cellWidth * cellCountX;
        setMeasuredDimension(width, height);
    }
    /**
     * Given a cell coordinate, return the point that represents the upper left corner of that cell
     *
     * @param cellX X coordinate of the cell
     * @param cellY Y coordinate of the cell
     *
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void cellToPoint(int cellX, int cellY, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        result[0] = hStartPadding + cellX * (cellWidth + mWidthGap);
        result[1] = vStartPadding + cellY * (cellHeight + mHeightGap);
    }

    void visualizeDropLocation(View v, Bitmap dragOutline, int originX, int originY, int cellX,
                               int cellY, int spanX, int spanY, boolean resize, Point dragOffset, Rect dragRegion) {
        final int oldDragCellX = mDragCell[0];
        final int oldDragCellY = mDragCell[1];

        if (v != null && dragOffset == null) {
            mDragCenter.set(originX + (v.getWidth() / 2), originY + (v.getHeight() / 2));
        } else {
            mDragCenter.set(originX, originY);
        }

        if (dragOutline == null && v == null) {
            return;
        }

       if (cellX != oldDragCellX || cellY != oldDragCellY) {
            mDragCell[0] = cellX;
            mDragCell[1] = cellY;
            // Find the top left corner of the rect the object will occupy
            final int[] topLeft = mTmpPoint;
            cellToPoint(cellX, cellY, topLeft);

            int left = topLeft[0];
            int top = topLeft[1];

            if (v != null && dragOffset == null) {
                // When drawing the drag outline, it did not account for margin offsets
                // added by the view's parent.
                MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
                left += lp.leftMargin;
                top += lp.topMargin;

                // Offsets due to the size difference between the View and the dragOutline.
                // There is a size difference to account for the outer blur, which may lie
                // outside the bounds of the view.
                top += (v.getHeight() - dragOutline.getHeight()) / 2;
                // We center about the x axis
                left += ((cellWidth * spanX) + ((spanX - 1) * mWidthGap)
                        - dragOutline.getWidth()) / 2;
            } else {
                if (dragOffset != null && dragRegion != null) {
                    // Center the drag region *horizontally* in the cell and apply a drag
                    // outline offset
                    left -= dragOffset.x + ((cellWidth * spanX) + ((spanX - 1) * mWidthGap) -
                             dragRegion.width()) / 2;
                    top += dragOffset.y;
                } else {
                    // Center the drag outline in the cell
                    left += ((cellWidth * spanX) + ((spanX - 1) * mWidthGap)
                            - dragOutline.getWidth()) / 2;
                    top += ((cellHeight * spanY) + ((spanY - 1) * mHeightGap)
                            - dragOutline.getHeight()) / 2;
                }
            }
            final int oldIndex = mDragOutlineCurrent;
            mDragOutlineAnims[oldIndex].animateOut();
            mDragOutlineCurrent = (oldIndex + 1) % mDragOutlines.length;
            Rect r = mDragOutlines[mDragOutlineCurrent];
            r.set(left, top, left + dragOutline.getWidth(), top + dragOutline.getHeight());
            if (resize) {
                cellToRect(cellX, cellY, spanX, spanY, r);
            }

            mDragOutlineAnims[mDragOutlineCurrent].setTag(dragOutline);
            mDragOutlineAnims[mDragOutlineCurrent].animateIn();
        }


    }
    public float getDistanceFromCell(float x, float y, int[] cell) {
        cellToCenterPoint(cell[0], cell[1], mTmpPoint);
        float distance = (float) Math.sqrt( Math.pow(x - mTmpPoint[0], 2) +
                Math.pow(y - mTmpPoint[1], 2));
        return distance;
    }

    /**
     * Computes a bounding rectangle for a range of cells
     *
     * @param cellX X coordinate of upper left corner expressed as a cell position
     * @param cellY Y coordinate of upper left corner expressed as a cell position
     * @param cellHSpan Width in cells
     * @param cellVSpan Height in cells
     * @param resultRect Rect into which to put the results
     */
    public void cellToRect(int cellX, int cellY, int cellHSpan, int cellVSpan, Rect resultRect) {


        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        int width = cellHSpan * cellWidth + ((cellHSpan - 1) * mWidthGap);
        int height = cellVSpan * cellHeight + ((cellVSpan - 1) * mHeightGap);

        int x = hStartPadding + cellX * (cellWidth + mWidthGap);
        int y = vStartPadding + cellY * (cellHeight + mHeightGap);

        resultRect.set(x, y, x + width, y + height);
    }

    public void clearDragOutlines() {
        final int oldIndex = mDragOutlineCurrent;
        mDragOutlineAnims[oldIndex].animateOut();
        mDragCell[0] = mDragCell[1] = -1;
    }


    private void lazyInitTempRectStack() {
        if (mTempRectStack.isEmpty()) {
            for (int i = 0; i < cellCountX * cellCountY; i++) {
                mTempRectStack.push(new Rect());
            }
        }
    }


    /**
     * Find a starting cell position that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     *
     * @param pixelX     The X location at which you want to search for a vacant area.
     * @param pixelY     The Y location at which you want to search for a vacant area.
     * @param spanX      Horizontal span of the object.
     * @param spanY      Vertical span of the object.
    // * @param ignoreView Considers space occupied by this view as unoccupied
     * @param result     Previously returned value to possibly recycle.
     * @return The X, Y cell of a vacant area that can contain this object,
     * nearest the requested location.
     */
    int[] findNearestArea(
            int pixelX, int pixelY, int spanX, int spanY, int[] result) {
        return findNearestArea(pixelX, pixelY, spanX, spanY, null, false, result);
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     *
     * @param pixelX         The X location at which you want to search for a vacant area.
     * @param pixelY         The Y location at which you want to search for a vacant area.
     * @param spanX          Horizontal span of the object.
     * @param spanY          Vertical span of the object.
     * @param ignoreOccupied If true, the result can be an occupied cell
     * @param result         Array in which to place the result, or null (in which case a new array will
     *                       be allocated)
     * @return The X, Y cell of a vacant area that can contain this object,
     * nearest the requested location.
     */
    int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, View ignoreView,
                          boolean ignoreOccupied, int[] result) {
        return findNearestArea(pixelX, pixelY, spanX, spanY,
                spanX, spanY, ignoreView, ignoreOccupied, result, null, mOccupied);
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     *
     * @param pixelX         The X location at which you want to search for a vacant area.
     * @param pixelY         The Y location at which you want to search for a vacant area.
     * @param minSpanX       The minimum horizontal span required
     * @param minSpanY       The minimum vertical span required
     * @param spanX          Horizontal span of the object.
     * @param spanY          Vertical span of the object.
     * @param ignoreOccupied If true, the result can be an occupied cell
     * @param result         Array in which to place the result, or null (in which case a new array will
     *                       be allocated)
     * @return The X, Y cell of a vacant area that can contain this object,
     * nearest the requested location.
     */
    int[] findNearestArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX, int spanY,
                          View ignoreView, boolean ignoreOccupied, int[] result, int[] resultSpan,
                          boolean[][] occupied) {
        lazyInitTempRectStack();
        // mark space take by ignoreView as available (method checks if ignoreView is null)
        //  markCellsAsUnoccupiedForView(ignoreView, occupied);

        // For items with a spanX / spanY > 1, the passed in point (pixelX, pixelY) corresponds
        // to the center of the item, but we are searching based on the top-left cell, so
        // we translate the point over to correspond to the top-left.
        pixelX -= (cellWidth) * (spanX - 1) / 2f;
        pixelY -= (cellHeight) * (spanY - 1) / 2f;

        // Keep track of best-scoring drop area
        final int[] bestXY = result != null ? result : new int[2];
        double bestDistance = Double.MAX_VALUE;
        final Rect bestRect = new Rect(-1, -1, -1, -1);
        final Stack<Rect> validRegions = new Stack<Rect>();

        final int countX = cellCountX;
        final int countY = cellCountY;

        if (minSpanX <= 0 || minSpanY <= 0 || spanX <= 0 || spanY <= 0 ||
                spanX < minSpanX || spanY < minSpanY) {
            return bestXY;
        }

        for (int y = 0; y < countY - (minSpanY - 1); y++) {
            inner:
            for (int x = 0; x < countX - (minSpanX - 1); x++) {
                int ySize = -1;
                int xSize = -1;
                if (ignoreOccupied) {
                    // First, let's see if this thing fits anywhere
                    for (int i = 0; i < minSpanX; i++) {
                        for (int j = 0; j < minSpanY; j++) {
                            if (occupied[x + i][y + j]) {
                                continue inner;
                            }
                        }
                    }
                    xSize = minSpanX;
                    ySize = minSpanY;

                    // We know that the item will fit at _some_ acceptable size, now let's see
                    // how big we can make it. We'll alternate between incrementing x and y spans
                    // until we hit a limit.
                    boolean incX = true;
                    boolean hitMaxX = xSize >= spanX;
                    boolean hitMaxY = ySize >= spanY;
                    while (!(hitMaxX && hitMaxY)) {
                        if (incX && !hitMaxX) {
                            for (int j = 0; j < ySize; j++) {
                                if (x + xSize > countX - 1 || occupied[x + xSize][y + j]) {
                                    // We can't move out horizontally
                                    hitMaxX = true;
                                }
                            }
                            if (!hitMaxX) {
                                xSize++;
                            }
                        } else if (!hitMaxY) {
                            for (int i = 0; i < xSize; i++) {
                                if (y + ySize > countY - 1 || occupied[x + i][y + ySize]) {
                                    // We can't move out vertically
                                    hitMaxY = true;
                                }
                            }
                            if (!hitMaxY) {
                                ySize++;
                            }
                        }
                        hitMaxX |= xSize >= spanX;
                        hitMaxY |= ySize >= spanY;
                        incX = !incX;
                    }
                    incX = true;
                    hitMaxX = xSize >= spanX;
                    hitMaxY = ySize >= spanY;
                }
                final int[] cellXY = mTmpXY;
                cellToCenterPoint(x, y, cellXY);

                // We verify that the current rect is not a sub-rect of any of our previous
                // candidates. In this case, the current rect is disqualified in favour of the
                // containing rect.
                Rect currentRect = mTempRectStack.pop();
                currentRect.set(x, y, x + xSize, y + ySize);
                boolean contained = false;
                for (Rect r : validRegions) {
                    if (r.contains(currentRect)) {
                        contained = true;
                        break;
                    }
                }
                validRegions.push(currentRect);
                double distance = Math.sqrt(Math.pow(cellXY[0] - pixelX, 2)
                        + Math.pow(cellXY[1] - pixelY, 2));

                if ((distance <= bestDistance && !contained) ||
                        currentRect.contains(bestRect)) {
                    bestDistance = distance;
                    bestXY[0] = x;
                    bestXY[1] = y;
                    if (resultSpan != null) {
                        resultSpan[0] = xSize;
                        resultSpan[1] = ySize;
                    }
                    bestRect.set(currentRect);
                }
            }
        }
        // re-mark space taken by ignoreView as occupied
        // markCellsAsOccupiedForView(ignoreView, occupied);

        // Return -1, -1 if no suitable location found
        if (bestDistance == Double.MAX_VALUE) {
            bestXY[0] = -1;
            bestXY[1] = -1;
        }
        recycleTempRects(validRegions);
        return bestXY;
    }

    private void recycleTempRects(Stack<Rect> used) {
        while (!used.isEmpty()) {
            mTempRectStack.push(used.pop());
        }
    }

    void cellToCenterPoint(int cellX, int cellY, int[] result) {
        regionToCenterPoint(cellX, cellY, 1, 1, result);
    }

    /**
     * Given a cell coordinate and span return the point that represents the center of the regio
     *
     * @param cellX  X coordinate of the cell
     * @param cellY  Y coordinate of the cell
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void regionToCenterPoint(int cellX, int cellY, int spanX, int spanY, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();
        result[0] = hStartPadding + cellX * (cellWidth) +
                (spanX * cellWidth) / 2;
        result[1] = vStartPadding + cellY * (cellHeight) +
                (spanY * cellHeight) / 2;
    }


    void init(Context context) {

       // cellsMatrix = new boolean[cellCountX][cellCountY];
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);


        int appIconSize = (int) context.getResources().getDimension(R.dimen.app_icon_size);

//        mCellPaddingLeft = (cellWidth - appIconSize) / 2;
//        mCellPaddingTop = (cellHeight - appIconSize) / 2;
    }




    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.v(TAG, "on touch x:: y " + event.getX() + "  " + event.getY());
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //  Log.v(TAG, "on touch x:: y " + event.getX() + "  " + event.getY());
        return super.dispatchTouchEvent(event);
    }

    boolean isNearestDropLocationOccupied(int pixelX, int pixelY, int spanX, int spanY,
                                          View dragView, int[] result) {
        result = findNearestArea(pixelX, pixelY, spanX, spanY, result);
        getViewsIntersectingRegion(result[0], result[1], spanX, spanY, dragView, null,
                mIntersectingViews);
        return !mIntersectingViews.isEmpty();
    }
    // For a given cell and span, fetch the set of views intersecting the region.
    private void getViewsIntersectingRegion(int cellX, int cellY, int spanX, int spanY,
                                            View dragView, Rect boundingRect, ArrayList<View> intersectingViews) {
        if (boundingRect != null) {
            boundingRect.set(cellX, cellY, cellX + spanX, cellY + spanY);
        }
        intersectingViews.clear();
        Rect r0 = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
        Rect r1 = new Rect();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == dragView) continue;
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
         //   r1.set(lp.cellX, lp.cellY, lp.cellX + lp.cellHSpan, lp.cellY + lp.cellVSpan);
            if (Rect.intersects(r0, r1)) {
                mIntersectingViews.add(child);
                if (boundingRect != null) {
                    boundingRect.union(r1);
                }
            }
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Temporary horizontal location of the item in the grid during reorder
         */
        public int tmpCellX;

        /**
         * Temporary vertical location of the item in the grid during reorder
         */
        public int tmpCellY;

        /**
         * Indicates that the temporary coordinates should be used to layout the items
         */
        public boolean useTmpCoords;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellVSpan;

        /**
         * Indicates whether the item will set its x, y, width and height parameters freely,
         * or whether these will be computed based on cellX, cellY, cellHSpan and cellVSpan.
         */
        public boolean isLockedToGrid = true;

        /**
         * Indicates whether this item can be reordered. Always true except in the case of the
         * the AllApps button.
         */
        public boolean canReorder = true;

        // X coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int x;
        // Y coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int y;

        boolean dropped;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.cellX = source.cellX;
            this.cellY = source.cellY;
            this.cellHSpan = source.cellHSpan;
            this.cellVSpan = source.cellVSpan;
        }

        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap) {
            if (isLockedToGrid) {
                final int myCellHSpan = cellHSpan;
                final int myCellVSpan = cellVSpan;
                final int myCellX = useTmpCoords ? tmpCellX : cellX;
                final int myCellY = useTmpCoords ? tmpCellY : cellY;

                width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) -
                        leftMargin - rightMargin;
                height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) -
                        topMargin - bottomMargin;
                x = (int) (myCellX * (cellWidth + widthGap) + leftMargin);
                y = (int) (myCellY * (cellHeight + heightGap) + topMargin);
            }
        }

        public String toString() {
            return "(" + this.cellX + ", " + this.cellY + ")";
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }


    // This class stores info for two purposes:
    // 1. When dragging items (mDragInfo in Workspace), we store the View, its cellX & cellY,
    //    its spanX, spanY, and the screen it is on
    // 2. When long clicking on an empty cell in a CellLayout, we save information about the
    //    cellX and cellY coordinates and which page was clicked. We then set this as a tag on
    //    the CellLayout that was long clicked

    static final class CellInfo {
        View cell;
        int cellX = -1;
        int cellY = -1;
        int spanX;
        int spanY;
        int screen;
        long container;

        @Override
        public String toString() {
            return "Cell[view=" + (cell == null ? "null" : cell.getClass())
                    + ", x=" + cellX + ", y=" + cellY + "]";
        }
    }

}

