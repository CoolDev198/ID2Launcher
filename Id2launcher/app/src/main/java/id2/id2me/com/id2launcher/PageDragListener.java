package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
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

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.database.CellInfo;
import id2.id2me.com.id2launcher.database.FolderInfo;
import id2.id2me.com.id2launcher.database.WidgetInfo;
import id2.id2me.com.id2launcher.general.AppGridView;


/**
 * Created by sunita on 8/9/16.
 */

class PageDragListener implements View.OnDragListener, View.OnClickListener, View.OnLongClickListener, IWidgetDrag {

    LauncherApplication launcherApplication;
    Context context;
    int cellWidth, cellHeight;
    FrameLayout pageLayout;
    private FrameLayout.LayoutParams layoutParams;
    private DragInfo dragInfo;
    AppWidgetProviderInfo appWidgetProviderInfo;
    int appWidgetId = 0;
    View drag_view;
    final String TAG = "PageDragListener";
    private ArrayList<Integer> nearestCell;
    private CellInfo cellToBePlaced;
    boolean isItemCanPlaced, isLoaderStarted, isRequiredCellsCalculated, isAvailableCellsGreater, isShiftingDone;
    int spanX = 1, spanY = 1, X, Y;
    private ArrayList<Integer> affectedChilds;
    TransitionDrawable trans;
    int ticks = 0;
    private boolean isDragStarted = false;

    PageDragListener(Context mContext, FrameLayout pageLayout) {
        this.pageLayout = pageLayout;
        launcherApplication = (LauncherApplication) ((Activity) mContext).getApplication();
        cellWidth = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellWidth();
        cellHeight = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellHeight();
        this.context = mContext;
        init();
    }


    void init() {
        Resources res = context.getResources();
        trans = (TransitionDrawable) res.getDrawable(R.drawable.transition_border_drawable);
        isRequiredCellsCalculated = false;
        isAvailableCellsGreater = false;
        isLoaderStarted = false;
        isItemCanPlaced = false;
        nearestCell = new ArrayList<>();
    }

    int px = 0, py = 0;

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                //   Log.v(TAG, "Drag Started");
                drag_view = (View) event.getLocalState();
                dragInfo = launcherApplication.dragInfo;
                dragInfo.setDragMatrices(new ArrayList<ArrayList<Integer>>());
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
        isDragStarted = true;
        X = (int) event.getX();
        Y = (int) event.getY();

        calculateReqCells();

    }

    private void calculateReqCells() {
        if (dragInfo.getDropExternal()) {
            findAvalCells();
        } else {
            //Tun mark cells in launcher cell matrix
            pageLayout.removeView(drag_view);
            unMarkCells(((CellInfo) drag_view.getTag()).getMatrixCells());
            findAvalCells();
        }

    }

    private void findAvalCells() {


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

        if (noOfAvailCells > noOfReqCells) {

            ArrayList<Integer> nearCellsObj = findNearestCells();
            //   Log.v(TAG, "matrixpos :: " + "  " + nearCellsObj.get(0) + "  " + nearCellsObj.get(1));

            findDistanceFromEachCell(nearCellsObj.get(0), nearCellsObj.get(1));
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


    }


    private void calDragInfoCells() {

        ArrayList<ArrayList<Integer>> tempAllCellInfo = new ArrayList<>();

        int actualSpanX = spanX - 1;
        int actualSpanY = spanY - 1;

        int scaleXEndPos = nearestCell.get(0) + actualSpanX;
        int scaleYEndPos = nearestCell.get(1) + actualSpanY;

        if (scaleXEndPos > launcherApplication.getCellCountX() - 1) {
            int startPos = nearestCell.get(0) - actualSpanX;
            if (startPos < 0) {
                startPos = 0;
            }
            nearestCell.set(0, startPos);
            scaleXEndPos = nearestCell.get(0) + actualSpanX;
        }

        if (scaleYEndPos > launcherApplication.getCellCountY() - 1) {
            int startPos = nearestCell.get(1) - actualSpanY;
            if (startPos < 0) {
                startPos = 0;
            }
            nearestCell.set(1, startPos);
            scaleYEndPos = nearestCell.get(1) + actualSpanY;
        }

        Log.v(TAG, " new cell structure :: ");
        for (int x = nearestCell.get(0); x <= scaleXEndPos; x++) {
            for (int y = nearestCell.get(1); y <= scaleYEndPos; y++) {

                ArrayList<Integer> matrix = new ArrayList<>();
                matrix.add(x);
                matrix.add(y);
                Log.v(TAG, "x  :: y :: " + x + "  " + y);
                tempAllCellInfo.add(matrix);
            }

        }

        unMarkCells(dragInfo.getDragMatrices());
        dragInfo.setDragMatrices(tempAllCellInfo);
        markCells(tempAllCellInfo);

        shiftAndAddToNewPos();

    }

    void onDrop() {
        try {
            actionAfterDrop();
            isAvailableCellsGreater = false;
            isRequiredCellsCalculated = false;
            isItemCanPlaced = false;
            cellToBePlaced = null;
            drag_view.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionAfterDrop() {
        isDragStarted = false;
        if (dragInfo.getIsItemCanPlaced()) {


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

    }

    private void copyDragMatricesToActualMatrices() {
        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            View child = (View) pageLayout.getChildAt(i);
            CellInfo cellInfo = (CellInfo) child.getTag();


            if (cellInfo.getDragMatrices() != null) {
                ArrayList<ArrayList<Integer>> tempCellInfo = new ArrayList<>();
                copyArray(tempCellInfo, cellInfo.getDragMatrices());
                cellInfo.setMatrixCells(tempCellInfo);
            } else {
                ArrayList<ArrayList<Integer>> tempCellInfo = new ArrayList<>();
                copyArray(tempCellInfo, cellInfo.getMatrixCells());
                cellInfo.setDragMatrices(tempCellInfo);
            }
        }
    }

    private void markCells(ArrayList<ArrayList<Integer>> allCellsInfo) {
        //  mark++;
        //Log.v(TAG, "cells marked :: " + mark);
        for (int i = 0; i < allCellsInfo.size(); i++) {
            ArrayList<Integer> cells = allCellsInfo.get(i);
            launcherApplication.setCellsMatrix(new int[]{cells.get(0), cells.get(1)}, true);
        }
    }


    void unMarkCells(ArrayList<ArrayList<Integer>> allCellsInfo) {
        //Make this places free after remove
        //unmark++;
        // Log.v(TAG, "cells un marked :: " + unmark);
        for (int i = 0; i < allCellsInfo.size(); i++) {
            ArrayList<Integer> arrayList = allCellsInfo.get(i);
            //Log.v(TAG, "cells unmarked :: " + arrayList.get(0) + "  " + arrayList.get(1));
            launcherApplication.setCellsMatrix(new int[]{arrayList.get(0), arrayList.get(1)}, false);
        }

    }


    private void shiftAndAddToNewPos() {

        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            try {
                View child = (View) pageLayout.getChildAt(i);

                CellInfo cellInfo = (CellInfo) child.getTag();

                int width = cellWidth * cellInfo.getSpanX();
                int height = cellHeight * cellInfo.getSpanY();
                layoutParams = new FrameLayout.LayoutParams(width, height);
                ArrayList<ArrayList<Integer>> bestCell = null;


                if (checkCellOccupied(cellInfo.getDragMatrices(), dragInfo.getDragMatrices())) {

                    if (dragInfo.getIsAppOrFolderOrWidget() == 1 && cellInfo.getDragMatrices().equals(cellInfo.getMatrixCells()) && findDistanceFromEachCell(nearestCell.get(0), nearestCell.get(1)) < 75) {

                        cellToBePlaced = new CellInfo();
                        copyCellInfo(cellInfo, cellToBePlaced);

                        if (dragInfo.getIsAppOrFolderOrWidget() == 2) {
                            cellInfo.setAddToExitingFolder(true);
                            //Add To Existing Folder for Temp
                        } else {
                            //Create Folder For Temp
                            cellToBePlaced.setFolderInfo(new FolderInfo(dragInfo.getAppInfo(), cellInfo.getAppInfo()));
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

        ArrayList<ArrayList<Integer>> allCellInfo = new ArrayList<>();
        boolean isCellAffected = false;
        for (int i = 0; i < dragMatrices.size(); i++) {
            for (int j = 0; j < dragInfoMatrices.size(); j++) {
                if (dragMatrices.get(i).equals(dragInfoMatrices.get(j))) {
                    isCellAffected = true;
                } else {
                    ArrayList<Integer> arrayList = new ArrayList<>();
                    arrayList.add(dragMatrices.get(i).get(0));
                    arrayList.add(dragMatrices.get(i).get(1));
                    allCellInfo.add(arrayList);
                }

            }

        }
        if(isCellAffected) {
            unMarkCells(allCellInfo);
            markCells(dragInfoMatrices);
        }
        return isCellAffected;
    }

    private ArrayList<ArrayList<Integer>> findBestCell(int spanX, int spanY) {

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

    private ArrayList<Integer> findNearestCells() {
        ArrayList arrayList = new ArrayList<Integer>();

        HashMap<Integer, ArrayList<Integer>> arrayListHashMap = new HashMap<>();
        ArrayList<Integer> distances = new ArrayList<>();

        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                ArrayList<Integer> listArrayList = new ArrayList<>();
                listArrayList.add(x);
                listArrayList.add(y);
                int distance = findDistanceFromEachCell(x, y);
                arrayListHashMap.put(distance, listArrayList);

                distances.add(distance);
            }
        }


        if (distances.size() > 0) {
            Collections.sort(distances);
        }
        arrayList.add(arrayListHashMap.get(distances.get(0)).get(0));
        arrayList.add(arrayListHashMap.get(distances.get(0)).get(1));


        return arrayList;
    }

    void copyCellInfo(CellInfo fromCellInfo, CellInfo toCellInfo) {
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

        if (dragInfo.getDropExternal()) {
            this.appWidgetId = ((LauncherApplication) ((Activity) context).getApplication()).mAppWidgetHost.allocateAppWidgetId();
            this.appWidgetProviderInfo = dragInfo.getWidgetInfo().getAppWidgetProviderInfo();

            checkIsWidgetBinded();
        } else {
            addWidgetOnInternalDragAndDrop();
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
        createOrUpdateCellInfo(3, null, null, dragInfo.getWidgetInfo(), hostView);
    }

    void addWidgetOnInternalDragAndDrop() {
        pageLayout.addView(drag_view, layoutParams);
        createOrUpdateCellInfo(3, null, null, dragInfo.getWidgetInfo(), drag_view);
    }

    private boolean isWidgetConfigRequired(AppWidgetProviderInfo appWidgetProviderInfo) {
        if (appWidgetProviderInfo.configure != null) {
            return true;
        }
        return false;
    }

    private void addAppToPage() {

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R
                .layout.grid_item, null, true);
        ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
        imageView.setImageDrawable(dragInfo.getAppInfo().getIcon());
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        pageLayout.addView(view, layoutParams);
        createOrUpdateCellInfo(1, dragInfo.getAppInfo(), null, null, view);

    }


    private void createOrUpdateCellInfo(int isAppOrFolderOrWidget, AppInfo appInfo, FolderInfo folderInfo, WidgetInfo widgetInfo, View view) {
        CellInfo cellInfo = new CellInfo();
        cellInfo.setAppInfo(appInfo);
        cellInfo.setFolderInfo(folderInfo);
        cellInfo.setIsAppOrFolderOrWidget(isAppOrFolderOrWidget);
        cellInfo.setView(view);
        cellInfo.setWidgetInfo(widgetInfo);
        cellInfo.setSpanX(dragInfo.getSpanX());
        cellInfo.setSpanY(dragInfo.getSpanY());
        cellInfo.setLayoutParams(layoutParams);
        cellInfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), dragInfo.getDragMatrices()));
        view.setTag(cellInfo);
    }

    ArrayList<ArrayList<Integer>> copyArray(ArrayList<ArrayList<Integer>> copyInto, ArrayList<ArrayList<Integer>> copyFrom) {

        for (int i = 0; i < copyFrom.size(); i++) {
            copyInto.add(copyFrom.get(i));
        }
        return copyInto;
    }


    private void createNewFolder() {


        View child = cellToBePlaced.getView();
        cellToBePlaced.setIsAppOrFolderOrWidget(2);
        child.setTag(cellToBePlaced);
        ImageView imageView = (ImageView) child.findViewById(R.id.grid_image);
        imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.folder_icon));
        child.setOnClickListener(this);
        child.setOnLongClickListener(this);

        updateFoldersList();
        addFragmentToHorizontalPagerAdapter();

    }

    private void addFragmentToHorizontalPagerAdapter() {
        launcherApplication.getLauncher().addNewFolderFragment();
    }

    private void updateFoldersList() {

        launcherApplication.folderFragmentsInfo.clear();
        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            CellInfo cellInfo = (CellInfo) ((View) pageLayout.getChildAt(i)).getTag();
            if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
                launcherApplication.folderFragmentsInfo.add(cellInfo.getFolderInfo());
            }

        }

    }

    private void addToExistingFolder() {
        cellToBePlaced.getFolderInfo().addNewAppInfo(dragInfo.getAppInfo());
        View child = cellToBePlaced.getView();
        child.setTag(cellToBePlaced);
        updateFoldersList();
    }

    private void calculateLayoutParams() {

        int width = cellWidth * dragInfo.getSpanX();
        int height = cellHeight * dragInfo.getSpanY();


        layoutParams = new FrameLayout.LayoutParams(width, height);
        int leftMargin = ((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(0) * cellWidth + (launcherApplication.getMaxGapLR() * (((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(0) + 1));
        int topMargin = ((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(1) * cellHeight + (launcherApplication.getMaxGapTB() * (((ArrayList<Integer>) dragInfo.getDragMatrices().get(0)).get(1) + 1));
        layoutParams.setMargins(leftMargin, topMargin, 0, 0);

        if (dragInfo.getIsAppOrFolderOrWidget() != 3) {
            if (dragInfo.getIsAppOrFolderOrWidget() == 2) {
                CellInfo cellInfo = (CellInfo) drag_view.getTag();
                cellInfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), dragInfo.getDragMatrices()));
                pageLayout.addView(drag_view, layoutParams);
            } else {
                addAppToPage();
            }
        } else {
            addWidgetToPage();
        }

    }

    @Override
    public void onClick(View v) {
        CellInfo cellInfo = (CellInfo) v.getTag();
        if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
            if (launcherApplication.folderView != null) {
                pageLayout.removeView(launcherApplication.folderView);
            }
            getPopUp(cellInfo.getFolderInfo().getAppInfos(), v);
        }

    }

    @Override
    public boolean onLongClick(View v) {
        DragInfo dragInfo = new DragInfo();
        CellInfo cellInfo = (CellInfo) v.getTag();
        dragInfo.setAppInfo(cellInfo.getAppInfo());
        dragInfo.setFolderInfo(cellInfo.getFolderInfo());
        dragInfo.setDropExternal(false);
        dragInfo.setIsAppOrFolderOrWidget(cellInfo.getIsAppOrFolderOrWidget());
        dragInfo.setIsItemCanPlaced(true);
        dragInfo.setSpanX(1);
        dragInfo.setSpanY(1);
        dragInfo.setMatrixCells(cellInfo.getMatrixCells());
        launcherApplication.dragInfo = dragInfo;
        launcherApplication.dragAnimation(v, View.INVISIBLE);
        return false;
    }

    void getPopUp(ArrayList<AppInfo> appInfos, View view) {
        int x = (int) view.getLeft();
        int y = (int) view.getTop();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(x, y, 0, 0);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View v = inflater.inflate(R
                .layout.popup_view, null);
        //v.setLayoutParams(params);
        pageLayout.addView(v);
        launcherApplication.folderView = v;

        AppGridView appGridView = (AppGridView) v.findViewById(R.id.mygridview);
        appGridView.setNumColumns(3);
        FolderGridAdapter adapter = new FolderGridAdapter(appInfos, context, R.layout.grid_item, appGridView);
        appGridView.setAdapter(adapter);
    }


    @Override
    public void onDragWidget(LauncherAppWidgetHostView launcherAppWidgetHostView) {
        DragInfo dragInfo = new DragInfo();
        dragInfo.setIsAppOrFolderOrWidget(3);
        dragInfo.setIsItemCanPlaced(true);
        CellInfo cellInfo = (CellInfo) launcherAppWidgetHostView.getTag();
        dragInfo.setWidgetInfo(cellInfo.getWidgetInfo());
        dragInfo.setDropExternal(false);
        dragInfo.setMatrixCells(cellInfo.getMatrixCells());
        launcherApplication.dragInfo = dragInfo;
        launcherApplication.dragAnimation(launcherAppWidgetHostView, View.INVISIBLE);
    }


    public void dropOutOfTheBox() {
     /*   if (isDragStarted) {
            if (dragInfo.getDropExternal()) {
                Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
            } else {

                if (dragInfo.getDragMatrices() != null) {
                    unMarkCells(dragInfo.getDragMatrices());
                    shiftAndAddToNewPos();
                }
                drag_view.setVisibility(View.VISIBLE);
                markCells(dragInfo.getMatrixCells());
                onDrop();
            }
        }*/
    }
}
