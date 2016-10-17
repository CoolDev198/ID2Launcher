package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import id2.id2me.com.id2launcher.models.ItemInfoModel;


/**
 * Created by sunita on 8/9/16.
 */

class PageDragListener implements View.OnDragListener, View.OnClickListener, View.OnLongClickListener, IWidgetDrag {

    final String TAG = "PageDragListener";
    LauncherApplication launcherApplication;
    Context context;
    int cellWidth, cellHeight;
    FrameLayout pageLayout;
    AppWidgetProviderInfo appWidgetProviderInfo;
    int appWidgetId = 0;
    View drag_view;
    boolean isItemCanPlaced, isLoaderStarted, isRequiredCellsCalculated, isAvailableCellsGreater;
    int spanX = 1, spanY = 1, X, Y;
    int ticks = 0;
    DatabaseHandler db;
    View dropTargetLayout, blur_relative;
    LinearLayout container;
    ObservableScrollView scrollView;
    ArrayList<View> reorderView;
    ArrayList<View> folderTempApps;
    private FrameLayout.LayoutParams layoutParams;
    private int[] nearestCell;
    private ItemInfoModel cellToBePlaced;
    private boolean isDragStarted = false;
    private ItemInfoModel dragInfo;
    private LauncherAppWidgetHostView hostView;

    PageDragListener(Context mContext, View desktopFragment) {
        this.pageLayout = (FrameLayout) desktopFragment.findViewById(R.id.relative_view);
        launcherApplication = (LauncherApplication) ((Activity) mContext).getApplication();
        cellWidth = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellWidth();
        cellHeight = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellHeight();
        this.context = mContext;
        db = DatabaseHandler.getInstance(context);

        dropTargetLayout = desktopFragment.findViewById(R.id.drop_target_layout);
        blur_relative = desktopFragment.findViewById(R.id.blur_relative);
        scrollView = (ObservableScrollView) desktopFragment.findViewById(R.id.scrollView);
        container = (LinearLayout) desktopFragment.findViewById(R.id.container);
        init();
    }

    public static Bitmap getScaledBitmap(Bitmap b, int reqWidth, int reqHeight) {
        int bWidth = b.getWidth();
        int bHeight = b.getHeight();

        int nWidth = reqWidth;
        int nHeight = reqHeight;

        float parentRatio = (float) reqHeight / reqWidth;

        nHeight = bHeight;
        nWidth = (int) (reqWidth * parentRatio);

        return Bitmap.createScaledBitmap(b, nWidth, nHeight, true);
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

                    isDragStarted = true;
                    dragInfo = launcherApplication.dragInfo;
                    init();
                    if (!dragInfo.getDropExternal()) {
                        dropTargetLayout.setVisibility(View.VISIBLE);
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

    public void boundryCheckUp() {
        if (!dragInfo.getDropExternal() && isDragStarted) {
            cellToBePlaced = null;
            onDrop();
        }
    }

    void onDrag(DragEvent event) {
        X = (int) event.getX();
        Y = (int) event.getY();
        Log.v(TAG, "X :: Y :: " + X + "  " + Y);
        int scroll = scrollView.getScrollY();
        int translatedY = Y - scroll;
        // make a scrolling up due the y has passed the threshold
        if (translatedY < 500) {
            // make a scroll up by 30 px
            scrollView.smoothScrollBy(0, -5);
        }
        // make a autoscrolling down due y has passed the 500 px border
        if (translatedY > 900) {
            // make a scroll down by 30 px
            scrollView.smoothScrollBy(0, 5);
        }
        goAhead();

    }

    private void calculateReqCells() {
        try {
            reorderView.clear();
            folderTempApps.clear();
            if (dragInfo.getDropExternal()) {
                findAvalCells();
            } else {
                pageLayout.removeView(drag_view);
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

                for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
                    for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                        if (!launcherApplication.getCellMatrixVal(new int[]{x, y})) {
                            noOfAvailCells++;

                        }
                    }

                    if (noOfReqCells <= noOfAvailCells) {
                        break;
                    }
                }


                if (noOfAvailCells >= noOfReqCells && !isRequiredCellsCalculated) {
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

        if (isAvailableCellsGreater) {
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
                calDragInfoCells();

            }



        }

    }

    private void calDragInfoCells() {

        try {

            int actualSpanX = spanX - 1;
            int actualSpanY = spanY - 1;

            int scaleXEndPos = nearestCell[0] + actualSpanX;
            int scaleYEndPos = nearestCell[1] + actualSpanY;

            if (scaleXEndPos > launcherApplication.getCellCountX() - 1) {
                int startPos = nearestCell[0] - actualSpanX;
                if (startPos < 0) {
                    startPos = 0;
                }
                nearestCell[0] = startPos;
            }

            if (scaleYEndPos > launcherApplication.getCellCountY() - 1) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void onDrop() {
        try {

            actionAfterDrop();

            isDragStarted = false;
            isAvailableCellsGreater = false;
            isRequiredCellsCalculated = false;
            isItemCanPlaced = false;
            cellToBePlaced = null;
            dropTargetLayout.setVisibility(View.INVISIBLE);
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


//        Log.v(TAG, "Printing status of cells :: ");
//        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
//            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
//
//                Log.v(TAG," occupied x :: y :: " +x + "  " + y + "  "+ launcherApplication.getCellMatrixVal(new int[]{x, y}));
//            }
//        }
    }


    private void createOrUpdateItemInfo() {

        try {


            if (cellToBePlaced != null && dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {


                if (cellToBePlaced.getIsExisitingFolder()) {
                    Log.v(TAG, "createOrUpdateItemInfo :: existing folder");
                    dragInfo.setDropExternal(false);
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

                        long folderId = db.addFolderToDatabase(folderInfo);
                        firstItemInfo.setContainer(folderId);
                        dragInfo.setContainer(folderId);
                        db.addOrMoveItemInfo(dragInfo);
                        db.addOrMoveItemInfo(firstItemInfo);
                        pageLayout.removeView(folderTempApps.get(0));


                        View child = createNewFolder(folderId);

                        if (child != null) {
                            child.setTag(folderInfo);
                        }
                        pageLayout.addView(child, layoutParams);


                        updateFoldersList();
                        addFragmentToHorizontalPagerAdapter();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else {
                dragInfo.setContainer(DatabaseHandler.CONTAINER_DESKTOP);
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
                    pageLayout.addView(child, layoutParams);
                } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APPWIDGET) {
                    if (dragInfo.getAppWidgetId() != -1) {
                        pageLayout.addView(drag_view, layoutParams);
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

                    pageLayout.addView(drag_view, layoutParams);


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
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                View child = (View) pageLayout.getChildAt(i);
                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
                cellInfo.setCellX(cellInfo.getTmpCellX());
                cellInfo.setCellY(cellInfo.getTmpCellY());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyActualMatricesToDragMatrices() {
        try {
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                View child = (View) pageLayout.getChildAt(i);
                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
                cellInfo.setTempCellX(cellInfo.getCellX());
                cellInfo.setTempCellY(cellInfo.getCellY());
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
                launcherApplication.setCellsMatrix(new int[]{x, y}, true);
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
                    launcherApplication.setCellsMatrix(new int[]{x, y}, false);
                }
            }
        }

    }


    private void shiftAndAddToNewPos() {

        for (View child : reorderView) {
            ItemInfoModel itemInfoModel = (ItemInfoModel) child.getTag();
            if (!launcherApplication.getCellMatrixVal(new int[]{itemInfoModel.getCellX(), itemInfoModel.getCellY()})) {
                unMarkCells(itemInfoModel.getTmpCellX(), itemInfoModel.getTmpCellY(), itemInfoModel.getSpanX(), itemInfoModel.getSpanY());
                markCells(itemInfoModel.getCellX(), itemInfoModel.getCellY(), itemInfoModel.getSpanX(), itemInfoModel.getSpanY());

                itemInfoModel.setTempCellX(itemInfoModel.getCellX());
                itemInfoModel.setTempCellY(itemInfoModel.getCellY());
                int leftMargin = itemInfoModel.getCellX() * cellWidth + (launcherApplication.getMaxGapLR() * (itemInfoModel.getCellX()));
                int topMargin = itemInfoModel.getCellY() * cellHeight + (launcherApplication.getMaxGapTB() * (itemInfoModel.getCellY()));
                int width = cellWidth * itemInfoModel.getSpanX();
                int height = cellHeight * itemInfoModel.getSpanY();
                layoutParams = new FrameLayout.LayoutParams(width, height);
                layoutParams.setMargins(leftMargin, topMargin, 0, 0);
                child.setLayoutParams(layoutParams);
                reorderView.remove(child);
            }
        }
        ArrayList<int[]> mTargetCells = getAllCellsList(nearestCell[0], nearestCell[1], dragInfo.getSpanX(), dragInfo.getSpanY());

        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            try {
                View child = (View) pageLayout.getChildAt(i);

                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();

                int width = cellWidth * cellInfo.getSpanX();
                int height = cellHeight * cellInfo.getSpanY();
                layoutParams = new FrameLayout.LayoutParams(width, height);

                int[] bestCell = null;

                ArrayList<int[]> mChildCells = getAllCellsList(cellInfo.getTmpCellX(), cellInfo.getTmpCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());

                boolean isAffedted = checkIsCellContain(mTargetCells, mChildCells);

                if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && isAffedted && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 100) {
                    cellToBePlaced = new ItemInfoModel();
                    cellToBePlaced.setIsExisitingFolder(false);
                    Log.v(TAG, "new folder created");
                    folderTempApps.add(child);

                } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER && isAffedted && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 100) {
                    Log.v(TAG, "added to existing folder");
                    cellToBePlaced = new ItemInfoModel();
                    cellToBePlaced.setIsExisitingFolder(true);
                    folderTempApps.clear();
                    folderTempApps.add(child);
                } else if (isAffedted) {
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

                }

                if (bestCell != null) {
                    cellInfo.setTempCellX(bestCell[0]);
                    cellInfo.setTempCellY(bestCell[1]);

                    reorderView.add(child);
                    int leftMargin = bestCell[0] * cellWidth + (launcherApplication.getMaxGapLR() * (bestCell[0]));
                    int topMargin = bestCell[1] * cellHeight + (launcherApplication.getMaxGapTB() * (bestCell[1]));
                    layoutParams.setMargins(leftMargin, topMargin, 0, 0);
                    child.setLayoutParams(layoutParams);

                    if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APPWIDGET) {
                        unMarkCells(cellInfo.getCellX(), cellInfo.getCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());
                        markCells(nearestCell[0], nearestCell[1], dragInfo.getSpanX(), dragInfo.getSpanY());
                    }
                    markCells(cellInfo.getTmpCellX(), cellInfo.getTmpCellY(), cellInfo.getSpanX(), cellInfo.getSpanY());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

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
            for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
                for (int y = 0; y < launcherApplication.getCellCountY(); y++) {

                    if (!launcherApplication.getCellMatrixVal(new int[]{x, y})) {
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

        if (scaleXEndPos > launcherApplication.getCellCountX() - 1) {
            return null;
        }

        if (scaleYEndPos > launcherApplication.getCellCountY() - 1) {
            return null;
        }

        for (int x = xPos; x <= scaleXEndPos; x++) {
            for (int y = yPos; y <= scaleYEndPos; y++) {
                if (launcherApplication.getCellMatrixVal(new int[]{x, y})) {
                    return null;
                }
            }
        }
        return new int[]{xPos, yPos};
    }

    private int[] findNearestCells() {

        float bestDistance = -1;
        int[] bestCell = null;

        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {

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


        int centerX = (x * cellWidth) + launcherApplication.getMaxGapLR() * x +
                (spanX * cellWidth) / 2;
        int centerY = (y * cellHeight) + launcherApplication.getMaxGapTB() * y +
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
        dragInfo.setAppWidgetId(appWidgetId);
        hostView = (LauncherAppWidgetHostView) launcherApplication.mAppWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo);
        hostView.setIWidgetInterface(this);
       // hostView.setBackgroundColor(Color.RED);
        AppWidgetProviderInfo appWidgetInfo = launcherApplication.getLauncher().mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        hostView.setForegroundGravity(Gravity.TOP);
        pageLayout.addView(hostView, layoutParams);
    }


    private boolean isWidgetConfigRequired(AppWidgetProviderInfo appWidgetProviderInfo) {
        if (appWidgetProviderInfo.configure != null) {
            return true;
        }
        return false;
    }

    private View addAppToPage() {

        try {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.grid_item, null, true);
            ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
            imageView.setImageBitmap(ItemInfoModel.getIconFromCursor(dragInfo.getIcon(), context));
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            return view;
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
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            pageLayout.addView(view, layoutParams);
            view.setTag(itemInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addFolderToPage(Bitmap icon, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {

        try {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.grid_item, null, true);
            ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
            imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.folder_icon));
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            pageLayout.addView(view, layoutParams);
            view.setTag(itemInfo);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            addFragmentToHorizontalPagerAdapter();
            updateFoldersFragment();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private View createNewFolder(long folderId) {
        try {
            ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.folder_view, null, true);

            Utility.setFolderView(context, view, itemInfoModels);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);


            return view;
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
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                ItemInfoModel cellInfo = (ItemInfoModel) ((View) pageLayout.getChildAt(i)).getTag();
                if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                    launcherApplication.folderFragmentsInfo.add(cellInfo);
                    ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(cellInfo.getId());
                    launcherApplication.getLauncher().updateFolderFragment(launcherApplication.folderFragmentsInfo.size(), itemInfoModels);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateFoldersList() {

        try {
            launcherApplication.folderFragmentsInfo.clear();
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                ItemInfoModel cellInfo = (ItemInfoModel) ((View) pageLayout.getChildAt(i)).getTag();
                if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                    launcherApplication.folderFragmentsInfo.add(cellInfo);
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

            layoutParams = new FrameLayout.LayoutParams(width, height);
            int leftMargin = dragInfo.getCellX() * cellWidth + (launcherApplication.getMaxGapLR() * (dragInfo.getCellX()));
            int topMargin = dragInfo.getCellY() * cellHeight + (launcherApplication.getMaxGapTB() * (dragInfo.getCellY()));
            layoutParams.setMargins(leftMargin, topMargin, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        ItemInfoModel itemInfoModel = (ItemInfoModel) v.getTag();
        if (itemInfoModel.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
            if (launcherApplication.folderView != null) {
                pageLayout.removeView(launcherApplication.folderView);
            }
            ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(itemInfoModel.getId());
            getPopUp(itemInfoModels);
        } else {
            try {
                Intent intent = null;
                String pckName = itemInfoModel.getPname();

                if (pckName != null) {
                    intent = context.getPackageManager()
                            .getLaunchIntentForPackage(pckName);

                    context.startActivity(intent);

                } else {
                    Toast.makeText(context,
                            context.getResources().getText(R.string.appNotFound),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onLongClick(View v) {
        try {
            launcherApplication.dragInfo = (ItemInfoModel) v.getTag();
            dragInfo.setDropExternal(false);
            launcherApplication.dragAnimation(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void getPopUp(ArrayList<ItemInfoModel> itemInfoModels) {
        try {

            blur_relative.setLayoutParams(new DrawerLayout.LayoutParams(launcherApplication.getScreenWidth(), launcherApplication.getScreenHeight()));

            scrollView.setVisibility(View.GONE);


            AppGridView appGridView = (AppGridView) blur_relative.findViewById(R.id.folder_gridView);
            appGridView.setNumColumns(3);
            FolderGridAdapter adapter = new FolderGridAdapter(itemInfoModels, context, R.layout.pop_up_grid, appGridView);
            appGridView.setAdapter(adapter);
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
            launcherApplication.dragAnimation(launcherAppWidgetHostView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void dropOutOfTheBox() {
        try {
            if (isDragStarted) {
                if (dragInfo.getDropExternal()) {
                    Toast.makeText(context, "Invalid Drop Location", Toast.LENGTH_LONG).show();
                } else {
                    onDrop();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeViewFromDesktop() {

        if (!dragInfo.getDropExternal()) {
            isDragStarted = false;
            unMarkCells(dragInfo.getCellX(), dragInfo.getCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
            pageLayout.removeView(drag_view);
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
        pageLayout.addView(hostView, layoutParams);
    }
}
