package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Stack;

/**
 * Created by sunita on 10/17/16.
 */

public class CellLayout extends FrameLayout {

    private final int[] mTmpXY = new int[2];
    private final Stack<Rect> mTempRectStack = new Stack<Rect>();
    LauncherApplication launcherApplication;
    private int cellCountY;
    private int cellCountX;
    private String TAG = "CellLayout";
    private int mCellPaddingLeft;
    private int mCellPaddingTop;
    private boolean[][] mOccupied;
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

    public CellLayout(Context context) {
        super(context);
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        cellCountX = launcherApplication.CELL_COUNT_X;
        cellCountY = launcherApplication.CELL_COUNT_Y;
        mOccupied = new boolean[cellCountX][cellCountY];
        init(context);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        cellWidth = getResources().getDimensionPixelSize(R.dimen.cell_width);
        cellHeight = getResources().getDimensionPixelSize(R.dimen.cell_height);
        mWidthGap=getResources().getDimensionPixelSize(R.dimen.workspace_width_gap_port);
        mWidthGap=getResources().getDimensionPixelSize(R.dimen.workspace_height_gap_port);

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
                    left += dragOffset.x + ((cellWidth * spanX) + ((spanX - 1) * mWidthGap)
                            - dragRegion.width()) / 2;
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
     * @param ignoreView Considers space occupied by this view as unoccupied
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

    public FrameLayout.LayoutParams getFrameLayoutParams(int left, int top) {
        try {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            System.out.println("Params Padding left : " + mCellPaddingLeft + " top : " + mCellPaddingTop);
            left = left + mCellPaddingLeft;
            top = top + mCellPaddingTop;
            layoutParams.setMargins(left, top, 0, 0);
            return layoutParams;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
}

