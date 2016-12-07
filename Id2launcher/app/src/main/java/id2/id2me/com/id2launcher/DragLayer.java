package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by sunita on 8/31/16.
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {

    String TAG = "DragLayer";
    ImageView mOutlineView;
    private int _xDelta;
    private int _yDelta;
    private DragController dragController;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        setOnHierarchyChangeListener(this);

        mOutlineView = (ImageView) findViewById(R.id.drag_outline_img);
    }


    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(((DragView)findViewById(R.id.drag_view)).isLongClick) {
            Log.v(TAG, "  on  intercept touch");
            int X = (int) event.getX();
            int Y = (int) event.getY();
            findViewById(R.id.drag_view).setVisibility(VISIBLE);
            findViewById(R.id.drag_outline_img).setVisibility(VISIBLE);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    Log.v(TAG, " on ACTION_DOWN  " + event.getX() + "   " + event.getY());
                    FrameLayout.LayoutParams lParams = (LayoutParams) findViewById(R.id.drag_view).getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.v(TAG, " ACTION_UP  " + event.getX() + "   " + event.getY());
                    ((DragView) findViewById(R.id.drag_view)).isLongClick = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.v(TAG, " ACTION_POINTER_DOWN  " + event.getX() + "   " + event.getY());
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    Log.v(TAG, " ACTION_POINTER_UP  " + event.getX() + "   " + event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.v(TAG, " ACTION_UP  " + event.getX() + "   " + event.getY());
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         int X = (int) event.getX();
         int Y = (int) event.getY();
        findViewById(R.id.drag_view).setVisibility(VISIBLE);
        findViewById(R.id.drag_outline_img).setVisibility(VISIBLE);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                Log.v(TAG, " on ACTION_DOWN  " + event.getX() + "   " + event.getY());
                FrameLayout.LayoutParams lParams = (LayoutParams) findViewById(R.id.drag_view).getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG, " ACTION_UP  " + event.getX() + "   " + event.getY());
                ((DragView) findViewById(R.id.drag_view)).isLongClick = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.v(TAG, " ACTION_POINTER_DOWN  " + event.getX() + "   " + event.getY());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.v(TAG, " ACTION_POINTER_UP  " + event.getX() + "   " + event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                Log.v(TAG, " ACTION_MOVE  " + event.getX() + "   " + event.getY());
                FrameLayout.LayoutParams layoutParams = (LayoutParams) findViewById(R.id.drag_view).getLayoutParams();
                FrameLayout.LayoutParams layoutparams = (LayoutParams) findViewById(R.id.drag_outline_img).getLayoutParams();

                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                findViewById(R.id.drag_view).setLayoutParams(layoutParams);
                findViewById(R.id.drag_outline_img).setLayoutParams(layoutparams);
                dragController.enterScroll((int)event.getY(),(int)event.getX(),event);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.v(TAG, " ACTION_UP  " + event.getX() + "   " + event.getY());
                break;
        }



        return super.onTouchEvent(event);
    }

    public void setDragController(DragController dragController) {
        this.dragController = dragController;
    }
//
//
//    void onDrag(DragEvent event) {
//        try {
//
//            X = (int) event.getX();
//            Y = (int) event.getY();
//
//            goAhead();
//
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//    }
//
//    void switchFrame() {
//
//        // Log.v(TAG, " switch frame called ::   " + "   Y " + Y);
//
//        int currentScreen = Integer.parseInt(cellLayout.getTag().toString());
//        if (currentScreen == 2 && Y < 50 && launcherApplication.isTimerTaskCompleted) {
//            launcherApplication.isTimerTaskCompleted = false;
//            //Log.v(TAG, "incremented");
//            container.scrollTo(0, containerL.getChildAt(0).getTop());
//        } else if (Y > 800 && launcherApplication.isTimerTaskCompleted) {
//            launcherApplication.isTimerTaskCompleted = false;
//            int margin = context.getResources().getDimensionPixelSize(R.dimen.extra_move_down);
//
//            //Log.v(TAG, "decremented " + currentScreen + "  current screen top :: " + containerL.getChildAt(currentScreen + 1).getTop());
//            //Log.v(TAG, " after minus margin :: " + (containerL.getChildAt(currentScreen + 1).getTop() - margin));
//
//            container.scrollTo(0, containerL.getChildAt(currentScreen + 1).getTop() - margin);
//            startTimer();
//
//        } else if (Y < 50 && launcherApplication.isTimerTaskCompleted) {
//            int margin = context.getResources().getDimensionPixelSize(R.dimen.extra_move_up);
//            launcherApplication.isTimerTaskCompleted = false;
//            // /  Log.v(TAG, "incremented " + currentScreen + "  current screen top :: " + containerL.getChildAt(currentScreen-1).getTop());
//            //Log.v(TAG, " after minus margin :: " + (containerL.getChildAt(currentScreen - 1).getTop() - margin));
//
//            container.scrollTo(0, containerL.getChildAt(currentScreen - 1).getTop() - margin);
//            startTimer();
//
//        }
//    }
//
//    public void startTimer() {
//        //set a new Timer
//        timer = new Timer();
//
//        //initialize the TimerTask's job
//        initializeTimerTask();
//
//        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
//        timer.schedule(timerTask, 1000, 5); //
//    }
//
//    public void initializeTimerTask() {
//
//
//        timerTask = new TimerTask() {
//            public void run() {
//
//                //use a handler to run a toast that shows the current timestamp
//                handler.post(new Runnable() {
//                    public void run() {
//                        //get the current timeStamp
//                        Log.v(TAG, "timer xpires");
//                        launcherApplication.isTimerTaskCompleted = true;
//                        timer.cancel();
//                    }
//                });
//            }
//        };
//    }
//
//    private void calculateReqCells() {
//        try {
//            reorderView.clear();
//            folderTempApps.clear();
//            if (dragInfo.getDropExternal()) {
//                findAvalCells();
//            } else {
//                cellLayout.unMarkCells(dragInfo.getTmpCellX(), dragInfo.getTmpCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
//                findAvalCells();
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void findAvalCells() {
//        try {
//            if (!isRequiredCellsCalculated) {
//                int noOfReqCells = -1, noOfAvailCells = 0;
//
//                spanX = dragInfo.getSpanX();
//                spanY = dragInfo.getSpanY();
//
//                noOfReqCells = spanX * spanY;
//
//                for (int x = 0; x < cellLayout.getCellCountX(); x++) {
//                    for (int y = 0; y < cellLayout.getCellCountY(); y++) {
//                        if (!cellLayout.getCellMatrixVal(new int[]{x, y})) {
//                            noOfAvailCells++;
//
//                        }
//                    }
//
//                    if (noOfReqCells <= noOfAvailCells) {
//                        break;
//                    }
//                }
//
//
//                if (noOfAvailCells >= noOfReqCells && !isRequiredCellsCalculated) {
//                    mOutlineView.setImageBitmap(launcherApplication.outlineBmp);
//                    mOutlineView.setVisibility(View.VISIBLE);
//                    isAvailableCellsGreater = true;
//                }
//                isRequiredCellsCalculated = true;
//
//                nearestCell[0] = -1;
//                nearestCell[1] = -1;
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    void goAhead() {
//
//        if (isAvailableCellsGreater || dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {
//            int[] nearCellsObj = findNearestCells();
//            //   Log.v(TAG, "matrixpos :: " + "  " + nearCellsObj.get(0) + "  " + nearCellsObj.get(1));
//            if (Arrays.equals(nearestCell, nearCellsObj)) {
//                ticks++;
//            } else {
//                nearestCell = nearCellsObj;
//                ticks = 0;
//            }
//
//            if (ticks > 5) {
//                nearestCell = nearCellsObj;
//                //transition
//                outlineAnimation(nearestCell);
//                calDragInfoCells();
//            }
//
//            outlineAnimation(nearestCell);
//            System.out.println("goAhead nearestCell");
//
//        }
//
//    }
//
//    private void calDragInfoCells() {
//
//        try {
//
//            int actualSpanX = spanX - 1;
//            int actualSpanY = spanY - 1;
//
//            int scaleXEndPos = nearestCell[0] + actualSpanX;
//            int scaleYEndPos = nearestCell[1] + actualSpanY;
//
//            if (scaleXEndPos > cellLayout.getCellCountX() - 1) {
//                int startPos = nearestCell[0] - actualSpanX;
//                if (startPos < 0) {
//                    startPos = 0;
//                }
//                nearestCell[0] = startPos;
//            }
//
//            if (scaleYEndPos > cellLayout.getCellCountY() - 1) {
//                int startPos = nearestCell[1] - actualSpanY;
//                if (startPos < 0) {
//                    startPos = 0;
//                }
//                nearestCell[1] = startPos;
//            }
//
//            cellLayout.unMarkCells(dragInfo.getTmpCellX(), dragInfo.getTmpCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
//            dragInfo.setTempCellX(nearestCell[0]);
//            dragInfo.setTempCellY(nearestCell[1]);
//            cellLayout.markCells(dragInfo.getTmpCellX(), dragInfo.getTmpCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
//            dragInfo.setIsItemCanPlaced(true);
//            shiftAndAddToNewPos();
//
//            //outlineAnimation(nearestCell);
//            System.out.println("calDragInfoCells nearest cell");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    void onDropOutOfCellLayout() {
//        launcherApplication.isTimerTaskCompleted = true;
//        if (timer != null)
//            timer.cancel();
//        launcherApplication.removeMargin();
//        mOutlineView.setVisibility(View.GONE);
//        launcherApplication.isDragStarted = false;
//    }
//
//    void onDrop() {
//        try {
//            launcherApplication.isTimerTaskCompleted = true;
//            if (timer != null)
//                timer.cancel();
//            launcherApplication.removeMargin();
//            drag_view.setScaleX(1f);
//            drag_view.setScaleY(1f);
//            mOutlineView.setVisibility(View.GONE);
//            drag_view.setVisibility(View.VISIBLE);
//            actionAfterDrop();
//            // launcherApplication.removeScreen();
//            launcherApplication.isDragStarted = false;
//            isAvailableCellsGreater = false;
//            isRequiredCellsCalculated = false;
//            isItemCanPlaced = false;
//            cellToBePlaced = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void actionAfterDrop() {
//        try {
//
//            int[] bestCell = null;
//            if (dragInfo.getIsItemCanPlaced()) {
//
//                if (dragInfo.getTmpCellX() == -1 && dragInfo.getTmpCellY() == -1) {
//                    bestCell = findBestCell(dragInfo.getSpanX(), dragInfo.getSpanY());
//
//                    if (bestCell != null) {
//                        dragInfo.setTempCellX(bestCell[0]);
//                        dragInfo.setTempCellY(bestCell[1]);
//                        acceptDrop();
//                    } else {
//                        Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
//
//                    }
//                } else {
//                    acceptDrop();
//                }
//
//
//            } else {
//                if (dragInfo.getDropExternal())
//                    Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    void acceptDrop() {
//        dragInfo.setCellX(dragInfo.getTmpCellX());
//
//        dragInfo.setCellY(dragInfo.getTmpCellY());
//
//        //   Toast.makeText(context, "cellx :: " + nearestCell[0] + " celly:: " + nearestCell[1], Toast.LENGTH_LONG).show();
//
//        cellLayout.markCells(dragInfo.getCellX(), dragInfo.getCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
//
//        calculateLayoutParams();
//
//        createOrUpdateItemInfo();
//
//        copyDragMatricesToActualMatrices();
//
//
//    }
//
//    private void createOrUpdateItemInfo() {
//
//        try {
//
//            int screen = Integer.parseInt(cellLayout.getTag().toString());
//            dragInfo.setScreen(screen);
//
//            if (cellToBePlaced != null && dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {
//
//
//                if (cellToBePlaced.getIsExisitingFolder()) {
//                    Log.v(TAG, "createOrUpdateItemInfo :: existing folder");
//                    dragInfo.setDropExternal(false);
//                    removeBackground(folderTempApps.get(0));
//                    FolderItemView folderItemView = (FolderItemView) folderTempApps.get(0);
//                    ItemInfoModel folderInfo = (ItemInfoModel) folderTempApps.get(0).getTag();
//                    dragInfo.setContainer(folderInfo.getId());
//                    db.addOrMoveItemInfo(dragInfo);
//                    folderItemView.addedToExistingFolder();
//
//                } else {
//                    try {
//
//                        dragInfo.setDropExternal(false);
//                        ItemInfoModel folderInfo = db.createFolderInfo(dragInfo);
//                        ItemInfoModel firstItemInfo = (ItemInfoModel) folderTempApps.get(0).getTag();
//                        removeBackground(folderTempApps.get(0));
//                        long folderId = db.addFolderToDatabase(folderInfo);
//                        firstItemInfo.setContainer(folderId);
//                        dragInfo.setContainer(folderId);
//                        db.addOrMoveItemInfo(dragInfo);
//                        db.addOrMoveItemInfo(firstItemInfo);
//                        cellLayout.removeView(folderTempApps.get(0));
//
//
//                        FolderItemView folderItemView = createNewFolder(folderId);
//
//                        if (folderItemView != null) {
//                            folderItemView.setTag(folderInfo);
//                        }
//
//                        cellLayout.addView(folderItemView, layoutParams);
//
//                        folderItemView.addFragmentToHorizontalPagerAdapter();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            } else {
//                dragInfo.setContainer(DatabaseHandler.CONTAINER_DESKTOP);
//                dragInfo.setScreen(Integer.parseInt(cellLayout.getTag().toString()));
//                if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {
//                    View child = null;
//                    if (dragInfo.getDropExternal()) {
//                        child = addAppToPage();
//                    } else {
//                        child = drag_view;
//                    }
//                    dragInfo.setDropExternal(false);
//                    db.addOrMoveItemInfo(dragInfo);
//
//                    if (child != null) {
//                        child.setTag(dragInfo);
//                    }
//
//                    cellLayout.addView(child, layoutParams);
//                } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APPWIDGET) {
//                    if (dragInfo.getAppWidgetId() != -1) {
//                        cellLayout.addView(drag_view, layoutParams);
//                    } else {
//                        addWidgetToPage();
//                    }
//                    dragInfo.setDropExternal(false);
//                    db.addOrMoveItemInfo(dragInfo);
//                    if (hostView != null) {
//                        hostView.setTag(dragInfo);
//                    }
//                } else {
//                    dragInfo.setDropExternal(false);
//                    db.addOrMoveItemInfo(dragInfo);
//
//                    drag_view.setTag(dragInfo);
//
//                    cellLayout.addView(drag_view, layoutParams);
//                }
//
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void copyDragMatricesToActualMatrices() {
//        try {
//            for (int i = 0; i < cellLayout.getChildCount(); i++) {
//                View child = (View) cellLayout.getChildAt(i);
//                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
//                if (cellInfo != null) {
//                    cellInfo.setCellX(cellInfo.getTmpCellX());
//                    cellInfo.setCellY(cellInfo.getTmpCellY());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void copyActualMatricesToDragMatrices() {
//        try {
//            for (int i = 0; i < cellLayout.getChildCount(); i++) {
//                View child = (View) cellLayout.getChildAt(i);
//                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
//                if (cellInfo != null) {
//                    cellInfo.setTempCellX(cellInfo.getCellX());
//                    cellInfo.setTempCellY(cellInfo.getCellY());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void shiftAndAddToNewPos() {
//
//        for (View child : reorderView) {
//            ItemInfoModel itemInfoModel = (ItemInfoModel) child.getTag();
//            if (!cellLayout.getCellMatrixVal(new int[]{itemInfoModel.getCellX(), itemInfoModel.getCellY()})) {
//                cellLayout.unMarkCells(itemInfoModel.getTmpCellX(), itemInfoModel.getTmpCellY(), itemInfoModel.getSpanX(), itemInfoModel.getSpanY());
//                cellLayout.markCells(itemInfoModel.getCellX(), itemInfoModel.getCellY(), itemInfoModel.getSpanX(), itemInfoModel.getSpanY());
//
//                itemInfoModel.setTempCellX(itemInfoModel.getCellX());
//                itemInfoModel.setTempCellY(itemInfoModel.getCellY());
//                int leftMargin = itemInfoModel.getCellX() * cellWidth;
//                int topMargin = itemInfoModel.getCellY() * cellHeight;
//                int width = cellWidth * itemInfoModel.getSpanX();
//                int height = cellHeight * itemInfoModel.getSpanY();
//                layoutParams = getFrameLayoutParams(leftMargin, topMargin);
//                child.setLayoutParams(layoutParams);
//                reorderView.remove(child);
//            }
//        }
//        ArrayList<int[]> mTargetCells = getAllCellsList(nearestCell[0], nearestCell[1], dragInfo.getSpanX(), dragInfo.getSpanY());
//
//        for (int i = 0; i < cellLayout.getChildCount(); i++) {
//
//            try {
//
//                View child = (View) cellLayout.getChildAt(i);
//                if (child.getVisibility() == View.VISIBLE) {
//                    ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
//
//                    if (cellInfo != null) {
//                        int width = cellWidth * cellInfo.getSpanX();
//                        int height = cellHeight * cellInfo.getSpanY();
//
//                        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//
//                        int[] bestCell = null;
//
//                        ArrayList<int[]> mChildCells = getAllCellsList(cellInfo.getTmpCellX(), cellInfo.getTmpCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());
//
//                        boolean isAffedted = checkIsCellContain(mTargetCells, mChildCells);
//
//                        if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && isAffedted && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 100) {
//                            ((AppItemView) child).setBackground(ContextCompat.getDrawable(context, R.drawable.background));
//                            mOutlineView.setVisibility(View.GONE);
//                            cellToBePlaced = new ItemInfoModel();
//                            // ((ImageView) child).setBackground(ContextCompat.getDrawable(context, R.drawable.background));
//                            cellToBePlaced.setIsExisitingFolder(false);
//                            Log.v(TAG, "new folder created");
//                            folderTempApps.add(child);
//
//                        } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER && isAffedted && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 100) {
//                            Log.v(TAG, "added to existing folder");
//                            ((LinearLayout) child).setBackground(ContextCompat.getDrawable(context, R.drawable.background));
//                            mOutlineView.setVisibility(View.GONE);
//                            cellToBePlaced = new ItemInfoModel();
//                            cellToBePlaced.setIsExisitingFolder(true);
//                            folderTempApps.clear();
//                            folderTempApps.add(child);
//
//                        } else if (isAffedted) {
//                            mOutlineView.setVisibility(View.VISIBLE);
//                            removeBackground(child);
//                            folderTempApps.clear();
//                            cellToBePlaced = null;
//                            Log.v(TAG, "move affected cell");
//                            try {
//                                bestCell = findBestCell(cellInfo.getSpanX(), cellInfo.getSpanY());
//                                if (bestCell == null) {
//                                    dragInfo.setTempCellY(dragInfo.getCellY());
//                                    dragInfo.setTempCellX(dragInfo.getCellX());
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                        } else {
//                            mOutlineView.setVisibility(View.VISIBLE);
//                            removeBackground(child);
//
//                        }
//
//                        if (bestCell != null) {
//
//                            cellInfo.setTempCellX(bestCell[0]);
//                            cellInfo.setTempCellY(bestCell[1]);
//
//                            reorderView.add(child);
//                            int leftMargin = bestCell[0] * cellWidth;
//                            int topMargin = bestCell[1] * cellHeight;
//                            //layoutParams.setMargins(leftMargin, topMargin, 0, 0);
//                            layoutParams = getFrameLayoutParams(leftMargin, topMargin);
//
//                            child.setLayoutParams(layoutParams);
//
//                            if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APPWIDGET) {
//                                cellLayout.unMarkCells(cellInfo.getCellX(), cellInfo.getCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());
//                                cellLayout.markCells(nearestCell[0], nearestCell[1], dragInfo.getSpanX(), dragInfo.getSpanY());
//                            }
//                            cellLayout.markCells(cellInfo.getTmpCellX(), cellInfo.getTmpCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());
//
//
//                        }
//
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    void removeBackground(View child) {
//
//        if (child instanceof LinearLayout) {
//            ((LinearLayout) child).setBackground(null);
//        } else if (child instanceof ImageView) {
//            ((ImageView) child).setBackground(null);
//        }
//    }
//
//    private boolean checkIsCellContain(ArrayList<int[]> mTargetCells, ArrayList<int[]> mChildCells) {
//        for (int i = 0; i < mTargetCells.size(); i++) {
//            for (int j = 0; j < mChildCells.size(); j++) {
//                if (Arrays.equals(mChildCells.get(j), mTargetCells.get(i))) {
//                    return true;
//                }
//
//            }
//        }
//        return false;
//    }
//
//    private ArrayList<int[]> getAllCellsList(int cellx, int celly, int spanx, int spany) {
//
//        ArrayList<int[]> collCells = new ArrayList<>();
//        int xStart = cellx;
//        int yStart = celly;
//        int xEnd = (cellx + spanx - 1);
//        int yEnd = (celly + spany - 1);
//
//
//        for (int x = xStart; x <= xEnd; x++) {
//            for (int y = yStart; y <= yEnd; y++) {
//                collCells.add(new int[]{x, y});
//            }
//        }
//        return collCells;
//    }
//
//    private int[] findBestCell(int spanX, int spanY) {
//
//        try {
//            HashMap<Float, int[]> arrayListHashMap = new HashMap<>();
//            ArrayList<Float> distances = new ArrayList<>();
//            for (int x = 0; x < cellLayout.getCellCountX(); x++) {
//                for (int y = 0; y < cellLayout.getCellCountY(); y++) {
//
//                    if (!cellLayout.getCellMatrixVal(new int[]{x, y})) {
//                        int[] scalableList = isScalable(x, y, spanX, spanY);
//
//                        if (scalableList != null) {
//                            float distance = findDistanceFromEachCell(x, y);
//                            arrayListHashMap.put(distance, scalableList);
//                            distances.add(distance);
//                        }
//
//                    }
//
//                }
//            }
//
//            if (distances.size() > 0) {
//                Collections.sort(distances);
//                return arrayListHashMap.get(distances.get(0));
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//
//    }
//
//    public int[] isScalable(int xPos, int yPos, int spanX, int spanY) {
//
//        int actualSpanX = spanX - 1;
//        int actualSpanY = spanY - 1;
//
//        int scaleXEndPos = xPos + actualSpanX;
//        int scaleYEndPos = yPos + actualSpanY;
//
//        if (scaleXEndPos > cellLayout.getCellCountX() - 1) {
//            return null;
//        }
//
//        if (scaleYEndPos > cellLayout.getCellCountY() - 1) {
//            return null;
//        }
//
//        for (int x = xPos; x <= scaleXEndPos; x++) {
//            for (int y = yPos; y <= scaleYEndPos; y++) {
//                if (cellLayout.getCellMatrixVal(new int[]{x, y})) {
//                    return null;
//                }
//            }
//        }
//        return new int[]{xPos, yPos};
//    }
//
//    private int[] findNearestCells() {
//
//        float bestDistance = -1;
//        int[] bestCell = null;
//
//        for (int x = 0; x < cellLayout.getCellCountX(); x++) {
//            for (int y = 0; y < cellLayout.getCellCountY(); y++) {
//
//                float distance = findDistanceFromEachCell(x, y);
//                if (bestDistance == -1) {
//                    bestDistance = distance;
//                    bestCell = new int[]{x, y};
//                } else if (distance < bestDistance) {
//                    bestDistance = distance;
//                    bestCell = new int[]{x, y};
//                }
//            }
//        }
//
//
//        return bestCell;
//    }
//
//    private float findDistanceFromEachCell(int x, int y) {
//
//
//        int centerX = (x * cellWidth) +
//                (spanX * cellWidth) / 2;
//        int centerY = (y * cellHeight) +
//                (spanY * cellHeight) / 2;
//
//        float distance = (float) Math.sqrt(Math.pow(X - centerX, 2) +
//                Math.pow(Y - centerY, 2));
//
//        // Log.v(TAG, "  " + x + "   " + y + "  " + distance);
//
//        return distance;
//    }
//
//    private void addWidgetToPage() {
//
//        try {
//            this.appWidgetId = ((LauncherApplication) ((Activity) context).getApplication()).mAppWidgetHost.allocateAppWidgetId();
//
//            checkIsWidgetBinded();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void checkIsWidgetBinded() {
//        boolean success = launcherApplication.getLauncher().mAppWidgetManager.bindAppWidgetIdIfAllowed(this.appWidgetId, dragInfo.getComponentName());
//        if (success) {
//            askToConfigure(null);
//        } else {
//            launcherApplication.getLauncher().startActivityForBindingWidget(appWidgetId, dragInfo.getComponentName());
//        }
//    }
//
//    public void addWidgetImpl(Intent data) {
//
//        if (data != null) {
//            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
//            if (appWidgetId != -1) {
//                addWidget();
//            }
//        } else {
//            addWidget();
//        }
//    }
//
//    public void askToConfigure(Intent data) {
//        if (data != null) {
//            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
//            if (appWidgetId != -1) {
//                addWidgetImpl(null);
//            }
//        }
//        appWidgetProviderInfo = dragInfo.getAppWidgetProviderInfo();
//        boolean isConfig = isWidgetConfigRequired(appWidgetProviderInfo);
//
//        if (isConfig) {
//            launcherApplication.getLauncher().startActivityForWidgetConfig(appWidgetId, appWidgetProviderInfo);
//        } else {
//            addWidgetImpl(null);
//        }
//
//
//    }
//
//    void addWidget() {
//        System.out.println("Add Widget");
//        dragInfo.setAppWidgetId(appWidgetId);
//        hostView = (LauncherAppWidgetHostView) launcherApplication.mAppWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo);
//        // hostView.setBackgroundColor(Color.RED);
//        AppWidgetProviderInfo appWidgetInfo = launcherApplication.getLauncher().mAppWidgetManager.getAppWidgetInfo(appWidgetId);
//        hostView.setAppWidget(appWidgetId, appWidgetInfo);
//        hostView.setForegroundGravity(Gravity.TOP);
//        cellLayout.addView(hostView, layoutParams);
//    }
//
//    private boolean isWidgetConfigRequired(AppWidgetProviderInfo appWidgetProviderInfo) {
//        if (appWidgetProviderInfo.configure != null) {
//            return true;
//        }
//        return false;
//    }
//
//    private View addAppToPage() {
//
//        try {
//            AppItemView appItemView = new AppItemView(context, dragInfo);
//            return appItemView;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private FolderItemView createNewFolder(long folderId) {
//        try {
//
//            FolderItemView folderItemView = new FolderItemView(context, db, folderId);
//            return folderItemView;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//
//    private void calculateLayoutParams() {
//
//        try {
//            int width = cellWidth * dragInfo.getSpanX();
//            int height = cellHeight * dragInfo.getSpanY();
//
//            //layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            int leftMargin = dragInfo.getCellX() * cellWidth;
//            int topMargin = dragInfo.getCellY() * cellHeight;
//            //layoutParams.setMargins(leftMargin, topMargin, 0, 0);
//
//            layoutParams = getFrameLayoutParams(leftMargin, topMargin);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    public void addWidgetToPage(int appWidgetId, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {
//        appWidgetProviderInfo = launcherApplication.getLauncher().mAppWidgetManager.getAppWidgetInfo
//                (appWidgetId);
//        hostView = (LauncherAppWidgetHostView) launcherApplication.mAppWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo);
//        //  hostView.setBackgroundColor(Color.RED);
//        hostView.setAppWidget(appWidgetId, appWidgetProviderInfo);
//        hostView.setForegroundGravity(Gravity.TOP);
//        hostView.setTag(itemInfo);
//        cellLayout.addView(hostView, layoutParams);
//    }
//
//    private void outlineAnimation(int nearestCell[]) {
//        try {
//
//            int topLayoutMargin = cellLayout.getTop();
//
//            System.out.println("Top Margin Layout :  " + topLayoutMargin);
//            /*FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);*/
//            int leftMargin = nearestCell[0] * cellWidth;// + (launcherApplication.getMaxGapLR() * (nearestCell[0]));
//            int topMargin = nearestCell[1] * cellHeight + topLayoutMargin;
//
//           /* params.leftMargin = leftMargin + mCellPaddingLeft;
//            params.topMargin = topMargin + mCellPaddingTop;*/
//
//            FrameLayout.LayoutParams params = getFrameLayoutParams(leftMargin, topMargin);
//            //params.gravity = Gravity.CENTER;
//
//
//            mOutlineView.setLayoutParams(params);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    private FrameLayout.LayoutParams getFrameLayoutParams(int left, int top) {
//        try {
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//
//            System.out.println("Params Padding left : " + mCellPaddingLeft + " top : " + mCellPaddingTop);
//            left = left + mCellPaddingLeft;
//            top = top + mCellPaddingTop;
//            layoutParams.setMargins(left, top, 0, 0);
//            return layoutParams;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


}

