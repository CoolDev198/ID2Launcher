package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
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
 * Created by sunita on 8/9/16.
 */

class PageDragListener implements View.OnDragListener, IWidgetDrag {

    final String TAG = "PageDragListener";
    final Handler handler = new Handler();
    private final LinearLayout containerL;
    LauncherApplication launcherApplication;
    Context context;
    int cellWidth, cellHeight;
    CellLayout cellLayout;
    AppWidgetProviderInfo appWidgetProviderInfo;
    int appWidgetId = 0;
    View drag_view;
    boolean isItemCanPlaced, isLoaderStarted, isRequiredCellsCalculated, isAvailableCellsGreater;
    int spanX = 1, spanY = 1, X, Y;
    int ticks = 0;
    DatabaseHandler db;
    View dropTargetLayout;
    ObservableScrollView container;
    ArrayList<View> reorderView;
    ArrayList<View> folderTempApps;
    Timer timer;
    View desktopFragment;
    private FrameLayout.LayoutParams layoutParams;
    private int[] nearestCell;
    private ItemInfoModel cellToBePlaced;
    private ItemInfoModel dragInfo;
    private LauncherAppWidgetHostView hostView;
    private TimerTask timerTask;
    private int direction = -1;
    private int screen;
    private Bitmap outlineBmp;
    private ImageView mOutlineView;
    private int mCellLayoutHeight;
    private int mCellPaddingLeft;
    private int mCellPaddingTop;

    PageDragListener(Context mContext, View desktopFragment, CellLayout pageLayout) {
        this.cellLayout = pageLayout;
        launcherApplication = (LauncherApplication) ((Activity) mContext).getApplication();
        cellWidth = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellWidth();
        cellHeight = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellHeight();
        this.context = mContext;
        screen = Integer.parseInt(pageLayout.getTag().toString());
        mCellLayoutHeight = (int) context.getResources().getDimension(R.dimen.cell_layout_height);

        /*mCellPaddingLeft = (int) context.getResources().getDimension(R.dimen.cell_width) / 2;
        mCellPaddingTop = (int) context.getResources().getDimension(R.dimen.cell_height) / 2;*/

        int cellWidth = (int) context.getResources().getDimension(R.dimen.cell_width);
        int cellHeight = (int) context.getResources().getDimension(R.dimen.cell_height);

        int appIconSize = (int) context.getResources().getDimension(R.dimen.app_icon_size);

        mCellPaddingLeft = (cellWidth - appIconSize) / 2;
        mCellPaddingTop = (cellHeight - appIconSize) / 2;

        System.out.println("Cell Padding l : " + mCellPaddingLeft + " t : " + mCellPaddingTop);

        db = DatabaseHandler.getInstance(context);
        this.desktopFragment = desktopFragment;
        dropTargetLayout = desktopFragment.findViewById(R.id.drop_target_layout);
        container = (ObservableScrollView) desktopFragment.findViewById(R.id.scrollView);
        containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);
        mOutlineView = (ImageView) desktopFragment.findViewById(R.id.drag_outline_img);

        init();
    }


    void init() {
        isRequiredCellsCalculated = false;
        isAvailableCellsGreater = false;
        isLoaderStarted = false;
        isItemCanPlaced = false;
        reorderView = new ArrayList<>();
        folderTempApps = new ArrayList<>();
        nearestCell = new int[2];

    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                try {

                    dragInfo = launcherApplication.dragInfo;

                    if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP)
                        outlineBmp = launcherApplication.getOutLinerBitmap(ItemInfoModel.getIconFromCursor(dragInfo.getIcon(), context));
                    else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                        Bitmap folderBitmap = FolderItemView.getBitmapFolderView();
                        outlineBmp = launcherApplication.getOutLinerBitmap(folderBitmap);
                    }

                    copyActualMatricesToDragMatrices();
                    drag_view = (View) event.getLocalState();
                    calculateReqCells();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //  Log.v(TAG, "Drag Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                Log.v(TAG, "Drag EXITED");

                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                try {
                    //   Log.v(TAG, "Drag locating");
                    onDrag(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DROP:
                //  Log.v(TAG, "DROP Action");
                try {

                    onDrop();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:

                boundryCheckUp();

                Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }

    void extendDesktop() {
        String tag = ((FrameLayout) container.getChildAt(container.getChildCount() - 1)).getTag().toString();
        int count = ((FrameLayout) container.getChildAt(0)).getChildCount();
        if (((FrameLayout) container.getChildAt(container.getChildCount() - 1)).getChildCount() > 1) {
            CellLayout layout = (CellLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.cell_layout, null, false);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, R.dimen.cell_layout_height);
            layout.setLayoutParams(layoutParams);
            container.addView(layout);
            layout.setTag(container.getChildCount() - 1);
            layout.setOnDragListener(new PageDragListener(context, desktopFragment, layout));
        }
    }

    public void boundryCheckUp() {
        //mOutlineView.setVisibility(View.GONE);
//        if (!dragInfo.getDropExternal() && isDragStarted) {
//            cellToBePlaced = null;
//            onDrop();
//        }
    }


    void onDrag(DragEvent event) {
        try {

            X = (int) event.getX();
            Y = (int) event.getY();
            try {
                //   Log.v(TAG, "X :: Y :: height " + X + "  " + Y + "   ");
            } catch (Exception e) {
                e.printStackTrace();
            }

           // mOutlineView.setScaleX(0.98f);


            int currentScreen = Integer.parseInt(cellLayout.getTag().toString());

            if (launcherApplication.currentScreen != currentScreen && Y > 1200 && launcherApplication.isTimerTaskCompleted) {
                direction = 1;
                launcherApplication.isTimerTaskCompleted = false;
                Log.v(TAG, "decremented");
                int margin = context.getResources().getDimensionPixelSize(R.dimen.extra_move_up);
                container.scrollTo(0, containerL.getChildAt(currentScreen).getTop() - margin);
                launcherApplication.currentScreen = currentScreen;
                startTimer();

            } else if (launcherApplication.currentScreen != currentScreen && Y < 100 && launcherApplication.isTimerTaskCompleted) {
                direction = 0;
                launcherApplication.isTimerTaskCompleted = false;
                Log.v(TAG, "incremented");
                int margin = context.getResources().getDimensionPixelSize(R.dimen.extra_move_up);
                launcherApplication.currentScreen = currentScreen;
                container.scrollTo(0, containerL.getChildAt(currentScreen).getTop() - margin);
                startTimer();

            } else if (launcherApplication.currentScreen != currentScreen && currentScreen == 1 && Y > 500 && launcherApplication.isTimerTaskCompleted) {
                direction = 1;
                launcherApplication.isTimerTaskCompleted = false;
                Log.v(TAG, "incremented");
                container.scrollTo(0, containerL.getChildAt(0).getTop());
                launcherApplication.currentScreen = currentScreen;
                startTimer();
            } else if (direction == 1 && Y < 1200) {
                Log.v(TAG, "nullify up");
                direction = -1;
                launcherApplication.isTimerTaskCompleted = true;
                timer.cancel();
            } else if (direction == 0 && Y > 100) {
                Log.v(TAG, "nullify down");
                direction = -1;
                launcherApplication.isTimerTaskCompleted = true;
                timer.cancel();
            }

            goAhead();


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 1000, 5); //
    }

    public void initializeTimerTask() {


        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        Log.v(TAG, "timer xpires");
                        launcherApplication.isTimerTaskCompleted = true;
                        timer.cancel();
                        direction = -1;
                    }
                });
            }
        };
    }

    private void calculateReqCells() {
        try {
            reorderView.clear();
            folderTempApps.clear();
            if (dragInfo.getDropExternal()) {
                findAvalCells();
            } else {
                mOutlineView.setVisibility(View.GONE);
                cellLayout.removeView(drag_view);
                unMarkCells(dragInfo.getTmpCellX(), dragInfo.getTmpCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
                findAvalCells();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void findAvalCells() {
        try {
            if (!isRequiredCellsCalculated) {
                int noOfReqCells = -1, noOfAvailCells = 0;

                spanX = dragInfo.getSpanX();
                spanY = dragInfo.getSpanY();

                noOfReqCells = spanX * spanY;

                for (int x = 0; x < cellLayout.getCellCountX(); x++) {
                    for (int y = 0; y < cellLayout.getCellCountY(); y++) {
                        if (!cellLayout.getCellMatrixVal(new int[]{x, y})) {
                            noOfAvailCells++;

                        }
                    }

                    if (noOfReqCells <= noOfAvailCells) {
                        break;
                    }
                }


                if (noOfAvailCells >= noOfReqCells && !isRequiredCellsCalculated) {
                    mOutlineView.setImageBitmap(outlineBmp);
                    mOutlineView.setVisibility(View.VISIBLE);
                    isAvailableCellsGreater = true;
                }
                isRequiredCellsCalculated = true;

                nearestCell[0] = -1;
                nearestCell[1] = -1;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    void goAhead() {

        if (isAvailableCellsGreater || dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {
            int[] nearCellsObj = findNearestCells();
            //   Log.v(TAG, "matrixpos :: " + "  " + nearCellsObj.get(0) + "  " + nearCellsObj.get(1));
            if (Arrays.equals(nearestCell, nearCellsObj)) {
                ticks++;
            } else {
                nearestCell = nearCellsObj;
                ticks = 0;
            }

            if (ticks > 5) {
                nearestCell = nearCellsObj;
                //transition
                outlineAnimation(nearestCell);
                calDragInfoCells();
            }

            outlineAnimation(nearestCell);
            System.out.println("goAhead nearestCell");

        }

    }

    private void calDragInfoCells() {

        try {

            int actualSpanX = spanX - 1;
            int actualSpanY = spanY - 1;

            int scaleXEndPos = nearestCell[0] + actualSpanX;
            int scaleYEndPos = nearestCell[1] + actualSpanY;

            if (scaleXEndPos > cellLayout.getCellCountX() - 1) {
                int startPos = nearestCell[0] - actualSpanX;
                if (startPos < 0) {
                    startPos = 0;
                }
                nearestCell[0] = startPos;
            }

            if (scaleYEndPos > cellLayout.getCellCountY() - 1) {
                int startPos = nearestCell[1] - actualSpanY;
                if (startPos < 0) {
                    startPos = 0;
                }
                nearestCell[1] = startPos;
            }

            unMarkCells(dragInfo.getTmpCellX(), dragInfo.getTmpCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
            dragInfo.setTempCellX(nearestCell[0]);
            dragInfo.setTempCellY(nearestCell[1]);
            markCells(dragInfo.getTmpCellX(), dragInfo.getTmpCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
            dragInfo.setIsItemCanPlaced(true);
            shiftAndAddToNewPos();

            //outlineAnimation(nearestCell);
            System.out.println("calDragInfoCells nearest cell");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void onDropOutOfCellLayout(){
        launcherApplication.isTimerTaskCompleted = true;
        if (timer != null)
            timer.cancel();
        launcherApplication.removeMargin();
        mOutlineView.setVisibility(View.GONE);
        launcherApplication.isDragStarted = false;
    }
    void onDrop() {
        try {
            launcherApplication.isTimerTaskCompleted = true;
            if (timer != null)
                timer.cancel();
            launcherApplication.removeMargin();
            drag_view.setScaleX(1f);
            drag_view.setScaleY(1f);
            mOutlineView.setVisibility(View.GONE);
            drag_view.setVisibility(View.VISIBLE);
            actionAfterDrop();
            launcherApplication.isDragStarted = false;
            isAvailableCellsGreater = false;
            isRequiredCellsCalculated = false;
            isItemCanPlaced = false;
            cellToBePlaced = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionAfterDrop() {
        try {

            int[] bestCell = null;
            if (dragInfo.getIsItemCanPlaced()) {

                if (dragInfo.getTmpCellX() == -1 && dragInfo.getTmpCellY() == -1) {
                    bestCell = findBestCell(dragInfo.getSpanX(), dragInfo.getSpanY());

                    if (bestCell != null) {
                        dragInfo.setTempCellX(bestCell[0]);
                        dragInfo.setTempCellY(bestCell[1]);
                        acceptDrop();
                    } else {
                        Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();

                    }
                } else {
                    acceptDrop();
                }


            } else {
                if (dragInfo.getDropExternal())
                    Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void acceptDrop() {
        dragInfo.setCellX(dragInfo.getTmpCellX());

        dragInfo.setCellY(dragInfo.getTmpCellY());

        //   Toast.makeText(context, "cellx :: " + nearestCell[0] + " celly:: " + nearestCell[1], Toast.LENGTH_LONG).show();

        markCells(dragInfo.getCellX(), dragInfo.getCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());

        calculateLayoutParams();

        createOrUpdateItemInfo();

        copyDragMatricesToActualMatrices();


    }

    private void createOrUpdateItemInfo() {

        try {

            String tag = cellLayout.getTag().toString();
            if (cellToBePlaced != null && dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {


                if (cellToBePlaced.getIsExisitingFolder()) {
                    Log.v(TAG, "createOrUpdateItemInfo :: existing folder");
                    dragInfo.setDropExternal(false);
                    removeBackground(folderTempApps.get(0));
                    ItemInfoModel folderInfo = (ItemInfoModel) folderTempApps.get(0).getTag();
                    dragInfo.setContainer(folderInfo.getId());
                    db.addOrMoveItemInfo(dragInfo);
                    Utility.setFolderView(context, folderTempApps.get(0), db.getAppsListOfFolder(folderInfo.getId()));
                    updateFoldersFragment();


                } else {
                    try {

                        dragInfo.setDropExternal(false);
                        ItemInfoModel folderInfo = createFolderInfo();
                        ItemInfoModel firstItemInfo = (ItemInfoModel) folderTempApps.get(0).getTag();
                        removeBackground(folderTempApps.get(0));
                        long folderId = db.addFolderToDatabase(folderInfo);
                        firstItemInfo.setContainer(folderId);
                        dragInfo.setContainer(folderId);
                        db.addOrMoveItemInfo(dragInfo);
                        db.addOrMoveItemInfo(firstItemInfo);
                        cellLayout.removeView(folderTempApps.get(0));


                        View child = createNewFolder(folderId);

                        if (child != null) {
                            child.setTag(folderInfo);
                        }

                        cellLayout.addView(child, layoutParams);

                        updateFoldersList();
                        addFragmentToHorizontalPagerAdapter();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else {
                dragInfo.setContainer(DatabaseHandler.CONTAINER_DESKTOP);
                dragInfo.setScreen(Integer.parseInt(cellLayout.getTag().toString()));
                if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {
                    View child = null;
                    if (dragInfo.getDropExternal()) {
                        child = addAppToPage();
                    } else {
                        child = drag_view;
                    }
                    dragInfo.setDropExternal(false);
                    db.addOrMoveItemInfo(dragInfo);

                    if (child != null) {
                        child.setTag(dragInfo);
                    }

                    cellLayout.addView(child, layoutParams);
                } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APPWIDGET) {
                    if (dragInfo.getAppWidgetId() != -1) {
                        cellLayout.addView(drag_view, layoutParams);
                    } else {
                        addWidgetToPage();
                    }
                    dragInfo.setDropExternal(false);
                    db.addOrMoveItemInfo(dragInfo);
                    if (hostView != null) {
                        hostView.setTag(dragInfo);
                    }
                } else {
                    dragInfo.setDropExternal(false);
                    db.addOrMoveItemInfo(dragInfo);

                    drag_view.setTag(dragInfo);

                    cellLayout.addView(drag_view, layoutParams);
                    updateFoldersList();
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemInfoModel createFolderInfo() {
        ItemInfoModel itemInfoModel = new ItemInfoModel();
        itemInfoModel.setCellX(dragInfo.getCellX());
        itemInfoModel.setCellY(dragInfo.getCellY());
        itemInfoModel.setTempCellX(dragInfo.getCellX());
        itemInfoModel.setTempCellY(dragInfo.getCellY());
        itemInfoModel.setSpanX(dragInfo.getSpanX());
        itemInfoModel.setSpanY(dragInfo.getSpanY());
        itemInfoModel.setItemType(DatabaseHandler.ITEM_TYPE_FOLDER);
        itemInfoModel.setTitle("");
        itemInfoModel.setContainer(DatabaseHandler.CONTAINER_DESKTOP);
        itemInfoModel.setIcon(ItemInfoModel.writeBitmap(AllAppsList.createIconBitmap(ContextCompat.getDrawable(context, R.mipmap.folder_icon), context)));
        return itemInfoModel;
    }

    private void copyDragMatricesToActualMatrices() {
        try {
            for (int i = 0; i < cellLayout.getChildCount(); i++) {
                View child = (View) cellLayout.getChildAt(i);
                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
                if (cellInfo != null) {
                    cellInfo.setCellX(cellInfo.getTmpCellX());
                    cellInfo.setCellY(cellInfo.getTmpCellY());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyActualMatricesToDragMatrices() {
        try {
            for (int i = 0; i < cellLayout.getChildCount(); i++) {
                View child = (View) cellLayout.getChildAt(i);
                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
                if (cellInfo != null) {
                    cellInfo.setTempCellX(cellInfo.getCellX());
                    cellInfo.setTempCellY(cellInfo.getCellY());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markCells(int cellx, int celly, int spanx, int spany) {

        int xStart = cellx;
        int yStart = celly;
        int xEnd = (cellx + spanx - 1);
        int yEnd = (celly + spany - 1);


        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                //      Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                cellLayout.setCellsMatrix(new int[]{x, y}, true);
            }

        }

    }

    void unMarkCells(int cellx, int celly, int spanx, int spany) {

        int xStart = cellx;
        int yStart = celly;
        int xEnd = (cellx + spanx - 1);
        int yEnd = (celly + spany - 1);

        if (cellx != -1 && celly != -1) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    //Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                    cellLayout.setCellsMatrix(new int[]{x, y}, false);
                }
            }
        }

    }

    private void shiftAndAddToNewPos() {

        for (View child : reorderView) {
            ItemInfoModel itemInfoModel = (ItemInfoModel) child.getTag();
            if (!cellLayout.getCellMatrixVal(new int[]{itemInfoModel.getCellX(), itemInfoModel.getCellY()})) {
                unMarkCells(itemInfoModel.getTmpCellX(), itemInfoModel.getTmpCellY(), itemInfoModel.getSpanX(), itemInfoModel.getSpanY());
                markCells(itemInfoModel.getCellX(), itemInfoModel.getCellY(), itemInfoModel.getSpanX(), itemInfoModel.getSpanY());

                itemInfoModel.setTempCellX(itemInfoModel.getCellX());
                itemInfoModel.setTempCellY(itemInfoModel.getCellY());
                int leftMargin = itemInfoModel.getCellX() * cellWidth ;
                int topMargin = itemInfoModel.getCellY() * cellHeight;
                int width = cellWidth * itemInfoModel.getSpanX();
                int height = cellHeight * itemInfoModel.getSpanY();
                layoutParams = getFrameLayoutParams(leftMargin, topMargin);
                child.setLayoutParams(layoutParams);
                reorderView.remove(child);
            }
        }
        ArrayList<int[]> mTargetCells = getAllCellsList(nearestCell[0], nearestCell[1], dragInfo.getSpanX(), dragInfo.getSpanY());

        for (int i = 0; i < cellLayout.getChildCount(); i++) {

            try {
                View child = (View) cellLayout.getChildAt(i);

                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();

                if (cellInfo != null) {
                    int width = cellWidth * cellInfo.getSpanX();
                    int height = cellHeight * cellInfo.getSpanY();

                    layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    int[] bestCell = null;

                    ArrayList<int[]> mChildCells = getAllCellsList(cellInfo.getTmpCellX(), cellInfo.getTmpCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());

                    boolean isAffedted = checkIsCellContain(mTargetCells, mChildCells);

                    if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && isAffedted && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 100) {
                        ((ImageView) child).setBackground(ContextCompat.getDrawable(context, R.drawable.background));
                        mOutlineView.setVisibility(View.GONE);
                        cellToBePlaced = new ItemInfoModel();
                        // ((ImageView) child).setBackground(ContextCompat.getDrawable(context, R.drawable.background));
                        cellToBePlaced.setIsExisitingFolder(false);
                        Log.v(TAG, "new folder created");
                        folderTempApps.add(child);

                    } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER && isAffedted && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 100) {
                        Log.v(TAG, "added to existing folder");
                        ((LinearLayout) child).setBackground(ContextCompat.getDrawable(context, R.drawable.background));
                        mOutlineView.setVisibility(View.GONE);
                        cellToBePlaced = new ItemInfoModel();
                        cellToBePlaced.setIsExisitingFolder(true);
                        folderTempApps.clear();
                        folderTempApps.add(child);

                    } else if (isAffedted) {
                        removeBackground(child);
                        folderTempApps.clear();
                        cellToBePlaced = null;
                        Log.v(TAG, "move affected cell");
                        try {
                            bestCell = findBestCell(cellInfo.getSpanX(), cellInfo.getSpanY());
                            if (bestCell == null) {
                                dragInfo.setTempCellY(dragInfo.getCellY());
                                dragInfo.setTempCellX(dragInfo.getCellX());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        removeBackground(child);

                    }

                    if (bestCell != null) {

                        cellInfo.setTempCellX(bestCell[0]);
                        cellInfo.setTempCellY(bestCell[1]);

                        reorderView.add(child);
                        int leftMargin = bestCell[0] * cellWidth;
                        int topMargin = bestCell[1] * cellHeight ;
                        //layoutParams.setMargins(leftMargin, topMargin, 0, 0);
                        layoutParams = getFrameLayoutParams(leftMargin, topMargin);

                        child.setLayoutParams(layoutParams);

                        if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APPWIDGET) {
                            unMarkCells(cellInfo.getCellX(), cellInfo.getCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());
                            markCells(nearestCell[0], nearestCell[1], dragInfo.getSpanX(), dragInfo.getSpanY());
                        }
                        markCells(cellInfo.getTmpCellX(), cellInfo.getTmpCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());


                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    void removeBackground(View child) {

        if (child instanceof LinearLayout) {
            ((LinearLayout) child).setBackground(null);
        } else if (child instanceof ImageView) {
            ((ImageView) child).setBackground(null);
        }
    }

    private boolean checkIsCellContain(ArrayList<int[]> mTargetCells, ArrayList<int[]> mChildCells) {
        for (int i = 0; i < mTargetCells.size(); i++) {
            for (int j = 0; j < mChildCells.size(); j++) {
                if (Arrays.equals(mChildCells.get(j), mTargetCells.get(i))) {
                    return true;
                }

            }
        }
        return false;
    }

    private ArrayList<int[]> getAllCellsList(int cellx, int celly, int spanx, int spany) {

        ArrayList<int[]> collCells = new ArrayList<>();
        int xStart = cellx;
        int yStart = celly;
        int xEnd = (cellx + spanx - 1);
        int yEnd = (celly + spany - 1);


        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                collCells.add(new int[]{x, y});
            }
        }
        return collCells;
    }

    private int[] findBestCell(int spanX, int spanY) {

        try {
            HashMap<Float, int[]> arrayListHashMap = new HashMap<>();
            ArrayList<Float> distances = new ArrayList<>();
            for (int x = 0; x < cellLayout.getCellCountX(); x++) {
                for (int y = 0; y < cellLayout.getCellCountY(); y++) {

                    if (!cellLayout.getCellMatrixVal(new int[]{x, y})) {
                        int[] scalableList = isScalable(x, y, spanX, spanY);

                        if (scalableList != null) {
                            float distance = findDistanceFromEachCell(x, y);
                            arrayListHashMap.put(distance, scalableList);
                            distances.add(distance);
                        }

                    }

                }
            }

            if (distances.size() > 0) {
                Collections.sort(distances);
                return arrayListHashMap.get(distances.get(0));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    public int[] isScalable(int xPos, int yPos, int spanX, int spanY) {

        int actualSpanX = spanX - 1;
        int actualSpanY = spanY - 1;

        int scaleXEndPos = xPos + actualSpanX;
        int scaleYEndPos = yPos + actualSpanY;

        if (scaleXEndPos > cellLayout.getCellCountX() - 1) {
            return null;
        }

        if (scaleYEndPos > cellLayout.getCellCountY() - 1) {
            return null;
        }

        for (int x = xPos; x <= scaleXEndPos; x++) {
            for (int y = yPos; y <= scaleYEndPos; y++) {
                if (cellLayout.getCellMatrixVal(new int[]{x, y})) {
                    return null;
                }
            }
        }
        return new int[]{xPos, yPos};
    }

    private int[] findNearestCells() {

        float bestDistance = -1;
        int[] bestCell = null;

        for (int x = 0; x < cellLayout.getCellCountX(); x++) {
            for (int y = 0; y < cellLayout.getCellCountY(); y++) {

                float distance = findDistanceFromEachCell(x, y);
                if (bestDistance == -1) {
                    bestDistance = distance;
                    bestCell = new int[]{x, y};
                } else if (distance < bestDistance) {
                    bestDistance = distance;
                    bestCell = new int[]{x, y};
                }
            }
        }


        return bestCell;
    }

    private float findDistanceFromEachCell(int x, int y) {


        int centerX = (x * cellWidth) +
                (spanX * cellWidth) / 2;
        int centerY = (y * cellHeight)  +
                (spanY * cellHeight) / 2;

        float distance = (float) Math.sqrt(Math.pow(X - centerX, 2) +
                Math.pow(Y - centerY, 2));

        // Log.v(TAG, "  " + x + "   " + y + "  " + distance);

        return distance;
    }

    private void addWidgetToPage() {

        try {
            this.appWidgetId = ((LauncherApplication) ((Activity) context).getApplication()).mAppWidgetHost.allocateAppWidgetId();

            checkIsWidgetBinded();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkIsWidgetBinded() {
        boolean success = launcherApplication.getLauncher().mAppWidgetManager.bindAppWidgetIdIfAllowed(this.appWidgetId, dragInfo.getComponentName());
        if (success) {
            askToConfigure(null);
        } else {
            launcherApplication.getLauncher().startActivityForBindingWidget(appWidgetId, dragInfo.getComponentName());
        }
    }

    public void addWidgetImpl(Intent data) {

        if (data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                addWidget();
            }
        } else {
            addWidget();
        }
    }

    public void askToConfigure(Intent data) {
        if (data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                addWidgetImpl(null);
            }
        }
        appWidgetProviderInfo = dragInfo.getAppWidgetProviderInfo();
        boolean isConfig = isWidgetConfigRequired(appWidgetProviderInfo);

        if (isConfig) {
            launcherApplication.getLauncher().startActivityForWidgetConfig(appWidgetId, appWidgetProviderInfo);
        } else {
            addWidgetImpl(null);
        }


    }

    void addWidget() {
        System.out.println("Add Widget");
        dragInfo.setAppWidgetId(appWidgetId);
        hostView = (LauncherAppWidgetHostView) launcherApplication.mAppWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo);
        hostView.setIWidgetInterface(this);
        // hostView.setBackgroundColor(Color.RED);
        AppWidgetProviderInfo appWidgetInfo = launcherApplication.getLauncher().mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        hostView.setForegroundGravity(Gravity.TOP);
        cellLayout.addView(hostView, layoutParams);
    }

    private boolean isWidgetConfigRequired(AppWidgetProviderInfo appWidgetProviderInfo) {
        if (appWidgetProviderInfo.configure != null) {
            return true;
        }
        return false;
    }

    private View addAppToPage() {

        try {
            AppItemView appItemView = new AppItemView(context, dragInfo);
            return appItemView.getView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addAppToPage(Bitmap icon, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {

        try {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.grid_item, null, true);
            ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
            imageView.setImageBitmap(icon);
            //view.setOnTouchListener(this);
            cellLayout.addView(view, layoutParams);
            view.setTag(itemInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addFolderToPage(Bitmap icon, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {
        try {
            System.out.println("Folder Fragment");
            //layoutParams = getFrameLayoutParams();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.grid_item, null, true);
            ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
            imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.folder_icon));
            //  view.setOnTouchListener(this);
            cellLayout.addView(view, layoutParams);
            view.setTag(itemInfo);

            addFragmentToHorizontalPagerAdapter();
            updateFoldersFragment();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private View createNewFolder(long folderId) {
        try {
           /* ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.folder_view, null, true);

            Utility.setFolderView(context, view, itemInfoModels);*/
//            view.setOnTouchListener(this);d

            FolderItemView folderItemView = new FolderItemView(context, db, folderId);
            return folderItemView.getView();

            //return view;
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addFragmentToHorizontalPagerAdapter() {
        launcherApplication.getLauncher().addNewFolderFragment();
    }

    private void updateFoldersFragment() {

        try {
            launcherApplication.folderFragmentsInfo.clear();
            for (int i = 0; i < cellLayout.getChildCount(); i++) {
                ItemInfoModel cellInfo = (ItemInfoModel) ((View) cellLayout.getChildAt(i)).getTag();
                if (cellInfo != null) {
                    if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                        launcherApplication.folderFragmentsInfo.add(cellInfo);
                        ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(cellInfo.getId());
                        launcherApplication.getLauncher().updateFolderFragment(launcherApplication.folderFragmentsInfo.size(), itemInfoModels);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateFoldersList() {

        try {
            launcherApplication.folderFragmentsInfo.clear();
            for (int i = 0; i < cellLayout.getChildCount(); i++) {
                ItemInfoModel cellInfo = (ItemInfoModel) ((View) cellLayout.getChildAt(i)).getTag();
                if (cellInfo != null) {
                    if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                        launcherApplication.folderFragmentsInfo.add(cellInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void calculateLayoutParams() {

        try {
            int width = cellWidth * dragInfo.getSpanX();
            int height = cellHeight * dragInfo.getSpanY();

            //layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int leftMargin = dragInfo.getCellX() * cellWidth;
            int topMargin = dragInfo.getCellY() * cellHeight;
            //layoutParams.setMargins(leftMargin, topMargin, 0, 0);

            layoutParams = getFrameLayoutParams(leftMargin, topMargin);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    public void onDragWidget(LauncherAppWidgetHostView launcherAppWidgetHostView) {
        try {

            try {
                launcherApplication.dragInfo = (ItemInfoModel) launcherAppWidgetHostView.getTag();
            } catch (Exception e) {
                e.printStackTrace();
            }

            dragInfo.setDropExternal(false);
            Canvas canvas = new Canvas();
            //outlineBmp = launcherApplication.createDragOutline(launcherAppWidgetHostView );
            launcherApplication.dragAnimation(launcherAppWidgetHostView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void addWidgetToPage(int appWidgetId, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {
        appWidgetProviderInfo = launcherApplication.getLauncher().mAppWidgetManager.getAppWidgetInfo
                (appWidgetId);
        hostView = (LauncherAppWidgetHostView) launcherApplication.mAppWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo);
        hostView.setIWidgetInterface(this);
        //  hostView.setBackgroundColor(Color.RED);
        hostView.setAppWidget(appWidgetId, appWidgetProviderInfo);
        hostView.setForegroundGravity(Gravity.TOP);
        hostView.setTag(itemInfo);
        cellLayout.addView(hostView, layoutParams);
    }

    public void outlineAnimation(int nearestCell[]) {
        try {

            int topLayoutMargin = cellLayout.getTop();

            System.out.println("Top Margin Layout :  " + topLayoutMargin);
            /*FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);*/
            int leftMargin = nearestCell[0] * cellWidth;// + (launcherApplication.getMaxGapLR() * (nearestCell[0]));
            int topMargin =  nearestCell[1] * cellHeight + topLayoutMargin;

           /* params.leftMargin = leftMargin + mCellPaddingLeft;
            params.topMargin = topMargin + mCellPaddingTop;*/

            FrameLayout.LayoutParams params = getFrameLayoutParams(leftMargin, topMargin);
            //params.gravity = Gravity.CENTER;


            mOutlineView.setLayoutParams(params);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        gestureListener.setView(v);
//        return gestureDetector.onTouchEvent(event);
//    }e
//

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

    /*private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
        final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        final Bitmap b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        canvas.setBitmap(null);
        return b;
    }*/

    private void addScreen(){
        try {

            CellLayout child = new CellLayout(context, R.dimen.cell_layout_height);
            containerL.addView(child);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
