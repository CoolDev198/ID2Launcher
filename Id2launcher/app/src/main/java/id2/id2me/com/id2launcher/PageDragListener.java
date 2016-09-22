package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.widget.DrawerLayout;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


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
    boolean isItemCanPlaced, isLoaderStarted, isRequiredCellsCalculated, isAvailableCellsGreater, isShiftingDone;
    int spanX = 1, spanY = 1, X, Y;
    TransitionDrawable trans;
    int ticks = 0;
    DatabaseHandler db;
    View dropTargetLayout, scrollView, blur_relative;
    private FrameLayout.LayoutParams layoutParams;
    private DragInfo dragInfo;
    private int[]nearestCell;
    private ItemInfo cellToBePlaced;
    private boolean isDragStarted = false;

    PageDragListener(Context mContext, View desktopFragment) {
        this.pageLayout = (FrameLayout) desktopFragment.findViewById(R.id.relative_view);
        launcherApplication = (LauncherApplication) ((Activity) mContext).getApplication();
        cellWidth = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellWidth();
        cellHeight = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellHeight();
        this.context = mContext;
        db = DatabaseHandler.getInstance(context);

        dropTargetLayout = desktopFragment.findViewById(R.id.drop_target_layout);
        blur_relative = desktopFragment.findViewById(R.id.blur_relative);
        scrollView = desktopFragment.findViewById(R.id.scrollView);
        init();
    }


    void init() {
        Resources res = context.getResources();
        trans = (TransitionDrawable) res.getDrawable(R.drawable.transition_border_drawable);
        isRequiredCellsCalculated = false;
        isAvailableCellsGreater = false;
        isLoaderStarted = false;
        isItemCanPlaced = false;
        nearestCell = new int[2];
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                try {
                    isDragStarted = true;
                    dragInfo = launcherApplication.dragInfo;

                    if (!dragInfo.getDropExternal()) {
                        dropTargetLayout.setVisibility(View.VISIBLE);
                    }
                    isRequiredCellsCalculated = true;
                    drag_view = (View) event.getLocalState();
                    dragInfo.setDragMatrices(new ArrayList<ArrayList<Integer>>());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //  Log.v(TAG, "Drag Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                // Log.v(TAG, "Drag EXITED");
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                try {
                    //   Log.v(TAG, "Drag locating");
                    // getTouchPositionFromDragEvent(v, event);
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
                // Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }


    void onDrag(DragEvent event) {
        X = (int) event.getX();
        Y = (int) event.getY();

        calculateReqCells();

    }

    private void calculateReqCells() {
        try {
            if (dragInfo.getDropExternal()) {
                findAvalCells();
            } else {
                //Tun mark cells in launcher cell matrix
                pageLayout.removeView(drag_view);
                unMarkCells(((ItemInfo) drag_view.getTag()));
                findAvalCells();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void findAvalCells() {


        try {
            int noOfReqCells = 1, noOfAvailCells = 0;

            if (dragInfo.getIsAppOrFolderOrWidget() != 1) {
                dragInfo.setSpanX(dragInfo.getWidgetInfo().getSpanX());
                dragInfo.setSpanY(dragInfo.getWidgetInfo().getSpanY());
                spanX = dragInfo.getWidgetInfo().getSpanX();
                spanY = dragInfo.getWidgetInfo().getSpanY();
                noOfReqCells = spanX * spanY;
            } else {
                spanX = 1;
                spanY = 1;
            }

            for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
                for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                    if (!launcherApplication.getCellMatrixVal(new int[]{x, y})) {
                        noOfAvailCells++;
                    }
                }
            }

            if (isRequiredCellsCalculated) {
                noOfReqCells = 0;
            }

            if (noOfAvailCells >= noOfReqCells) {
                isRequiredCellsCalculated = true;

               int[] nearCellsObj = findNearestCells();
                //   Log.v(TAG, "matrixpos :: " + "  " + nearCellsObj.get(0) + "  " + nearCellsObj.get(1));

                findDistanceFromEachCell(nearCellsObj[0], nearCellsObj[1]);

                if (nearestCell.equals(nearCellsObj)) {
                    ticks++;
                } else {
                    nearestCell = nearCellsObj;
                    ticks = 0;
                }

                if (ticks > 6) {
                    nearestCell = nearCellsObj;
                    calDragInfoCells();

                }

            } else {
                dragInfo.setIsItemCanPlaced(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void calDragInfoCells() {

        try {
            int[] cells = new int[2];

            int actualSpanX = spanX - 1;
            int actualSpanY = spanY - 1;

            int scaleXEndPos = nearestCell[0] + actualSpanX;
            int scaleYEndPos = nearestCell[1]+ actualSpanY;

            if (scaleXEndPos > launcherApplication.getCellCountX() - 1) {
                int startPos = nearestCell[0] - actualSpanX;
                if (startPos < 0) {
                    startPos = 0;
                }
                nearestCell[0]= startPos;
                scaleXEndPos = nearestCell[0] + actualSpanX;
            }

            if (scaleYEndPos > launcherApplication.getCellCountY() - 1) {
                int startPos = nearestCell[1] - actualSpanY;
                if (startPos < 0) {
                    startPos = 0;
                }
                nearestCell[1]= startPos;
                scaleYEndPos = nearestCell[1] + actualSpanY;
            }



            unMarkCells(dragInfo);
            markCells(dragInfo);

            shiftAndAddToNewPos();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void onDrop() {
        try {
            actionAfterDrop();
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
            isDragStarted = false;

            if (dragInfo.getIsItemCanPlaced() && dragInfo.getDragMatrices().size() > 0) {


                if (cellToBePlaced != null) {
                    if (cellToBePlaced.getIsAppOrFolderOrWidget() == 2) {
                        addToExistingFolder();
                    } else {
                        createNewFolder();
                    }
                } else {
                    markCells(dragInfo.getDragMatrices());
                    calculateLayoutParams();
                }

                copyDragMatricesToActualMatrices();

            } else {
                if (dragInfo.getDropExternal())
                    Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createOrUpdateItemInfo(View view) {

        if (dragInfo.getDropExternal()) {
            ItemInfo iteminfo = new ItemInfo();
            iteminfo.setAppInfo(dragInfo.getAppInfo());
            iteminfo.setFolderInfo(dragInfo.getFolderInfo());
            iteminfo.setIsAppOrFolderOrWidget(dragInfo.getIsAppOrFolderOrWidget());
            iteminfo.setView(dragInfo.getDragView());
            iteminfo.setWidgetInfo(dragInfo.getWidgetInfo());
            iteminfo.setView(view);
            iteminfo.setLayoutParams(layoutParams);
            iteminfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), dragInfo.getDragMatrices()));

            iteminfo.setSpanX(dragInfo.getSpanX());
            iteminfo.setSpanY(dragInfo.getSpanY());
            iteminfo.setIcon(ItemInfo.writeBitmap(AllAppsList.createIconBitmap(dragInfo.getAppInfo().getIcon(), context)));
            iteminfo.setCellX(dragInfo.getDragMatrices().get(0).get(0));
            iteminfo.setCellY(dragInfo.getDragMatrices().get(0).get(1));
            iteminfo.setPname(dragInfo.getAppInfo().getPname());


            if (cellToBePlaced != null) {
                iteminfo.setContainer(DatabaseHandler.CONTAINER_FOLDER);
            } else {
                iteminfo.setContainer(DatabaseHandler.CONTAINER_DESKTOP);
            }

            view.setTag(iteminfo);

            db.addOrMoveItemInfo(iteminfo);
        } else {
            ItemInfo cellInfo = (ItemInfo) view.getTag();
        }
    }

    private void copyDragMatricesToActualMatrices() {
        try {
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                View child = (View) pageLayout.getChildAt(i);
                ItemInfo cellInfo = (ItemInfo) child.getTag();


                if (cellInfo.getDragMatrices() != null) {
                    ArrayList<ArrayList<Integer>> tempCellInfo = new ArrayList<>();
                    copyArray(tempCellInfo, cellInfo.getDragMatrices());
                    cellInfo.setMatrixCells(tempCellInfo);
                    markCells(cellInfo.getMatrixCells());
                } else {
                    ArrayList<ArrayList<Integer>> tempCellInfo = new ArrayList<>();
                    copyArray(tempCellInfo, cellInfo.getMatrixCells());
                    cellInfo.setDragMatrices(tempCellInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markCells(ItemInfo itemInfo) {
        //  mark++;
        //Log.v(TAG, "cells marked :: " + mark);
        int xStart = itemInfo.getCellX();
        int yStart = itemInfo.getCellY();
        int xEnd = (itemInfo.getCellX() + itemInfo.getSpanX() - 1);
        int yEnd = (itemInfo.getCellY() + itemInfo.getSpanY() - 1);

        for (int x = xStart; x < xEnd; x++) {
            for (int y = yStart; y < yEnd; y++) {
                //Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                launcherApplication.setCellsMatrix(new int[]{x, y}, true);
            }

        }
    }


    void unMarkCells(ItemInfo itemInfo) {

        int xStart = itemInfo.getCellX();
        int yStart = itemInfo.getCellY();
        int xEnd = (itemInfo.getCellX() + itemInfo.getSpanX() - 1);
        int yEnd = (itemInfo.getCellY() + itemInfo.getSpanY() - 1);

        for (int x = xStart; x < xEnd; x++) {
            for (int y = yStart; y < yEnd; y++) {
                //Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                launcherApplication.setCellsMatrix(new int[]{x, y}, false);
            }

        }

    }


    private void shiftAndAddToNewPos() {

        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            try {
                View child = (View) pageLayout.getChildAt(i);

                ItemInfo cellInfo = (ItemInfo) child.getTag();

                int width = cellWidth * cellInfo.getSpanX();
                int height = cellHeight * cellInfo.getSpanY();
                layoutParams = new FrameLayout.LayoutParams(width, height);
                ArrayList<ArrayList<Integer>> bestCell = null;


                if (checkCellOccupied(cellInfo.getDragMatrices(), dragInfo.getDragMatrices())) {

                    if (dragInfo.getIsAppOrFolderOrWidget() == 1 && cellInfo.getIsAppOrFolderOrWidget() != 3 && cellInfo.getDragMatrices().equals(cellInfo.getMatrixCells()) && findDistanceFromEachCell(nearestCell.get(0), nearestCell.get(1)) < 75) {

                        cellToBePlaced = new ItemInfo();
                        copyCellInfo(cellInfo, cellToBePlaced);

                        if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
                            cellInfo.setAddToExitingFolder(true);
                            //Add To Existing Folder for Temp``
                        } else {
                            //Create Folder For Temp
                            cellToBePlaced.setFolderInfo(new FolderInfo(dragInfo.getItemInfo(), cellInfo));
                            cellToBePlaced.setAppInfo(null);
                        }

                    } else {

                        cellToBePlaced = null;

                        try {
                            bestCell = findBestCell(cellInfo.getSpanX(), cellInfo.getSpanY());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                ArrayList<ArrayList<Integer>> originalCellsVacant = isScalable(cellInfo.getMatrixCells().get(0).get(0), cellInfo.getMatrixCells().get(0).get(1), cellInfo.getSpanX(), cellInfo.getSpanY());

                if (originalCellsVacant != null) {
                    bestCell = originalCellsVacant;
                }

                if (bestCell != null) {
                    markCells(bestCell);
                    cellInfo.setDragMatrices(bestCell);
                    ArrayList<Integer> firstPos = bestCell.get(0);
                    int leftMargin = firstPos.get(0) * cellWidth + (launcherApplication.getMaxGapLR() * (firstPos.get(0) + 1));
                    int topMargin = firstPos.get(1) * cellHeight + (launcherApplication.getMaxGapTB() * (firstPos.get(1) + 1));

                    layoutParams.setMargins(leftMargin, topMargin, 0, 0);
                    pageLayout.removeView(child);
                    pageLayout.addView(child, layoutParams);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCellOccupied(ArrayList<ArrayList<Integer>> dragMatrices, ArrayList<ArrayList<Integer>> dragInfoMatrices) {

        boolean isCellAffected = false;
        try {
            ArrayList<ArrayList<Integer>> allCellInfo = new ArrayList<>();
            isCellAffected = false;


            for (int i = 0; i < dragMatrices.size(); i++) {
                boolean isContained = false;

                for (int j = 0; j < dragInfoMatrices.size(); j++) {
                    if (dragMatrices.get(i).equals(dragInfoMatrices.get(j))) {
                        isCellAffected = true;
                        isContained = true;
                    }
                }

                if (!isContained) {
                    ArrayList<Integer> arrayList = new ArrayList<>();
                    arrayList.add(dragMatrices.get(i).get(0));
                    arrayList.add(dragMatrices.get(i).get(1));
                    allCellInfo.add(arrayList);
                }
            }
            if (isCellAffected) {
                unMarkCells(allCellInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCellAffected;
    }

    private ArrayList<ArrayList<Integer>> findBestCell(int spanX, int spanY) {

        try {
            HashMap<Integer, ArrayList<ArrayList<ArrayList<Integer>>>> arrayListHashMap = new HashMap<>();
            ArrayList<Integer> distances = new ArrayList<>();
            for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
                for (int y = 0; y < launcherApplication.getCellCountY(); y++) {

                    if (!launcherApplication.getCellMatrixVal(new int[]{x, y})) {
                        ArrayList<ArrayList<Integer>> scalableList = isScalable(x, y, spanX, spanY);
                        if (scalableList != null) {
                            int distance = findDistanceFromEachCell(x, y);
                            if (arrayListHashMap.containsKey(distance)) {
                                arrayListHashMap.get(distance).add(scalableList);
                            } else {
                                ArrayList<ArrayList<ArrayList<Integer>>> listArrayList = new ArrayList<>();
                                listArrayList.add(scalableList);
                                arrayListHashMap.put(distance, listArrayList);
                            }
                            distances.add(distance);
                        }
                    }
                }
            }

            if (distances.size() > 0) {
                Collections.sort(distances);
                return arrayListHashMap.get(distances.get(0)).get(0);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    public ArrayList<ArrayList<Integer>> isScalable(int xPos, int yPos, int spanX, int spanY) {

        ArrayList<ArrayList<Integer>> scalableList = new ArrayList<>();
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

                ArrayList<Integer> arrayList = new ArrayList<>();
                arrayList.add(x);
                arrayList.add(y);
                scalableList.add(arrayList);
                if (launcherApplication.getCellMatrixVal(new int[]{x, y})) {
                    return null;
                }
            }
        }
        return scalableList;
    }

    private int[] findNearestCells() {

        HashMap<Integer, int[] > arrayListHashMap = new HashMap<>();
        ArrayList<Integer> distances = new ArrayList<>();

        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                int[] matrix = new int []{x,y};

                int distance = findDistanceFromEachCell(x, y);
                arrayListHashMap.put(distance, matrix);

                distances.add(distance);
            }
        }


        if (distances.size() > 0) {
            Collections.sort(distances);
        }

        return  arrayListHashMap.get(distances.get(0));
    }

    void copyCellInfo(ItemInfo fromCellInfo, ItemInfo toCellInfo) {
        toCellInfo.setDragLayoutParams(fromCellInfo.getDragLayoutParams());
        toCellInfo.setSpanY(fromCellInfo.getSpanY());
        toCellInfo.setDragMatrices(fromCellInfo.getDragMatrices());
        toCellInfo.setAppInfo(fromCellInfo.getAppInfo());
        toCellInfo.setFolderInfo(fromCellInfo.getFolderInfo());
        toCellInfo.setSpanX(fromCellInfo.getSpanX());
        toCellInfo.setLayoutParams(fromCellInfo.getLayoutParams());
        toCellInfo.setWidgetInfo(fromCellInfo.getWidgetInfo());
        toCellInfo.setView(fromCellInfo.getView());
        toCellInfo.setIsAppOrFolderOrWidget(fromCellInfo.getIsAppOrFolderOrWidget());
        toCellInfo.setMatrixCells(fromCellInfo.getMatrixCells());
    }


    private int findDistanceFromEachCell(int x, int y) {


        int centerX = (x * cellWidth) +
                (spanX * cellWidth) / 2;
        int centerY = (y * cellHeight) +
                (spanY * cellHeight) / 2;

        int distance = (int) Math.sqrt(Math.pow(X - centerX, 2) +
                Math.pow(Y - centerY, 2));

        // Log.v(TAG, "  " + x + "   " + y + "  " + distance);

        return distance;
    }

    private void addWidgetToPage() {

        try {
            if (dragInfo.getDropExternal()) {
                this.appWidgetId = ((LauncherApplication) ((Activity) context).getApplication()).mAppWidgetHost.allocateAppWidgetId();
                this.appWidgetProviderInfo = dragInfo.getWidgetInfo().getAppWidgetProviderInfo();

                checkIsWidgetBinded();
            } else {
                addWidgetOnInternalDragAndDrop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkIsWidgetBinded() {
        boolean success = launcherApplication.getLauncher().mAppWidgetManager.bindAppWidgetIdIfAllowed(this.appWidgetId, dragInfo.getWidgetInfo().getComponentName());
        if (success) {
            askToConfigure(null);
        } else {
            launcherApplication.getLauncher().startActivityForBindingWidget(appWidgetId, dragInfo.getWidgetInfo().getComponentName());
        }
    }

    public void askToConfigure(Intent data) {
        if (data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                addWidgetImpl(null);
            }
        }
        boolean isConfig = isWidgetConfigRequired(appWidgetProviderInfo);

        if (isConfig) {
            launcherApplication.getLauncher().startActivityForWidgetConfig(appWidgetId, appWidgetProviderInfo);
        } else {
            addWidgetImpl(null);
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

    void addWidget() {
        LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) launcherApplication.mAppWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo);
        hostView.setIWidgetInterface(this);
        AppWidgetProviderInfo appWidgetInfo = launcherApplication.getLauncher().mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        hostView.setForegroundGravity(Gravity.TOP);
        pageLayout.addView(hostView, layoutParams);
    }

    void addWidgetOnInternalDragAndDrop() {
        pageLayout.addView(drag_view, layoutParams);
    }

    private boolean isWidgetConfigRequired(AppWidgetProviderInfo appWidgetProviderInfo) {
        if (appWidgetProviderInfo.configure != null) {
            return true;
        }
        return false;
    }

    private void addAppToPage() {

        try {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R
                    .layout.grid_item, null, true);
            ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
            imageView.setImageDrawable(dragInfo.getAppInfo().getIcon());
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            pageLayout.addView(view, layoutParams);
            createOrUpdateItemInfo(view);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addAppToPage(Bitmap icon, ItemInfo itemInfo, FrameLayout.LayoutParams layoutParams) {

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

    public void addFolderToPage(Bitmap icon, ItemInfo itemInfo, FrameLayout.LayoutParams layoutParams) {

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

            updateFoldersList();
            addFragmentToHorizontalPagerAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    ArrayList<ArrayList<Integer>> copyArray(ArrayList<ArrayList<Integer>> copyInto, ArrayList<ArrayList<Integer>> copyFrom) {

        for (int i = 0; i < copyFrom.size(); i++) {
            copyInto.add(copyFrom.get(i));
        }
        return copyInto;
    }


    private void createNewFolder() {
        try {
            View child = cellToBePlaced.getView();
            cellToBePlaced.setIsAppOrFolderOrWidget(2);
            child.setTag(cellToBePlaced);
            ImageView imageView = (ImageView) child.findViewById(R.id.grid_image);
            imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.folder_icon));
            child.setOnClickListener(this);
            child.setOnLongClickListener(this);

            updateFoldersList();
            addFragmentToHorizontalPagerAdapter();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    private void addFragmentToHorizontalPagerAdapter() {
        launcherApplication.getLauncher().addNewFolderFragment();
    }

    private void updateFoldersList() {

        try {
            launcherApplication.folderFragmentsInfo.clear();
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                ItemInfo cellInfo = (ItemInfo) ((View) pageLayout.getChildAt(i)).getTag();
                if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
                    launcherApplication.folderFragmentsInfo.add(cellInfo.getFolderInfo());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addToExistingFolder() {
        try {
            //     cellToBePlaced.getFolderInfo().addNewItemInfo(dragInfo.getAppInfo());
            View child = cellToBePlaced.getView();
            child.setTag(cellToBePlaced);
            updateFoldersList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateLayoutParams() {

        try {
            int width = cellWidth * dragInfo.getSpanX();
            int height = cellHeight * dragInfo.getSpanY();


            layoutParams = new FrameLayout.LayoutParams(width, height);
            int leftMargin = ((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(0) * cellWidth + (launcherApplication.getMaxGapLR() * (((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(0) + 1));
            int topMargin = ((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(1) * cellHeight + (launcherApplication.getMaxGapTB() * (((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(1) + 1));
            layoutParams.setMargins(leftMargin, topMargin, 0, 0);

            if (dragInfo.getIsAppOrFolderOrWidget() != DatabaseHandler.ITEM_TYPE_APPWIDGET) {
                if (dragInfo.getIsAppOrFolderOrWidget() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                    ItemInfo cellInfo = (ItemInfo) drag_view.getTag();
                    cellInfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), dragInfo.getDragMatrices()));
                    pageLayout.addView(drag_view, layoutParams);
                } else {
                    addAppToPage();
                }
            } else {
                addWidgetToPage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        ItemInfo cellInfo = (ItemInfo) v.getTag();
        if (cellInfo.getIsAppOrFolderOrWidget() == DatabaseHandler.ITEM_TYPE_FOLDER) {
            if (launcherApplication.folderView != null) {
                pageLayout.removeView(launcherApplication.folderView);
            }
            getPopUp(cellInfo.getFolderInfo().getAppInfos());
        }

    }

    @Override
    public boolean onLongClick(View v) {
        try {
            DragInfo dragInfo = new DragInfo();
            ItemInfo cellInfo = (ItemInfo) v.getTag();
            dragInfo.setAppInfo(cellInfo.getAppInfo());
            dragInfo.setFolderInfo(cellInfo.getFolderInfo());
            dragInfo.setDropExternal(false);
            dragInfo.setOriginalView(v);
            dragInfo.setIsAppOrFolderOrWidget(cellInfo.getIsAppOrFolderOrWidget());
            dragInfo.setIsItemCanPlaced(true);
            dragInfo.setSpanX(1);
            dragInfo.setSpanY(1);
            dragInfo.setMatrixCells(cellInfo.getMatrixCells());
            launcherApplication.dragInfo = dragInfo;
            launcherApplication.dragAnimation(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void getPopUp(ArrayList<ItemInfo> appInfos) {
        try {

            blur_relative.setLayoutParams(new DrawerLayout.LayoutParams(launcherApplication.getScreenWidth(), launcherApplication.getScreenHeight()));

            scrollView.setVisibility(View.GONE);


            AppGridView appGridView = (AppGridView) blur_relative.findViewById(R.id.folder_grid);
            appGridView.setNumColumns(3);
            FolderGridAdapter adapter = new FolderGridAdapter(appInfos, context, R.layout.pop_up_grid, appGridView);
            appGridView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDragWidget(LauncherAppWidgetHostView launcherAppWidgetHostView) {
        try {
            DragInfo dragInfo = new DragInfo();
            dragInfo.setIsAppOrFolderOrWidget(3);
            dragInfo.setIsItemCanPlaced(true);
            ItemInfo cellInfo = null;
            try {
                cellInfo = (ItemInfo) launcherAppWidgetHostView.getTag();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dragInfo.setWidgetInfo(cellInfo.getWidgetInfo());
            dragInfo.setDropExternal(false);
            dragInfo.setMatrixCells(cellInfo.getMatrixCells());
            launcherApplication.dragInfo = dragInfo;
            launcherApplication.dragAnimation(launcherAppWidgetHostView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void dropOutOfTheBox() {
        try {
            if (isDragStarted) {
                if (dragInfo.getDropExternal()) {
                    if (dragInfo.getDragMatrices().size() > 0) {
                        onDrop();
                    } else {
                        Toast.makeText(context, "Invalid Drop Location", Toast.LENGTH_LONG).show();
                    }
                    unMarkCells(dragInfo.getDragMatrices());

                } else {

                    if (dragInfo.getDragMatrices() != null) {
                        unMarkCells(dragInfo.getDragMatrices());
                        ArrayList<ArrayList<Integer>> tempCellInfo = new ArrayList<>();
                        copyArray(tempCellInfo, dragInfo.getMatrixCells());
                        dragInfo.setDragMatrices(tempCellInfo);
                        shiftAndAddToNewPos();
                    }
                    onDrop();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeViewFromDesktop() {
        if (dragInfo.getDragMatrices() != null) {
            unMarkCells(dragInfo.getDragMatrices());
            shiftAndAddToNewPos();
            isAvailableCellsGreater = false;
            isRequiredCellsCalculated = false;
            isItemCanPlaced = false;
            cellToBePlaced = null;
            dropTargetLayout.setVisibility(View.INVISIBLE);
        }
    }
}
