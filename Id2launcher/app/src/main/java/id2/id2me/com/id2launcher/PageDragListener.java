package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
    View drag_view, transChild;
    final String TAG = "PageDragListener";
    private boolean tempCellsMatrix[][];
    private ArrayList<Object> greenSignal;
    private ArrayList<Integer> nearestCell;
    private ArrayList<ArrayList<Integer>> tempAllCellInfo;
    private CellInfo cellToBePlaced;

    private ArrayList<Object> childViews, cacheOriginalChildPos, affectedChilds;
    boolean isItemCanPlaced, isLoaderStarted, isRequiredCellsCalculated, isAvailableCellsGreater, isShiftingDone;
    int spanX = 1, spanY = 1, X, Y;
    private ArrayList<Integer> nearCellsObj;
    TransitionDrawable trans;
    private Point p;

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
        childViews = new ArrayList<>();
        affectedChilds = new ArrayList<>();
        cacheOriginalChildPos = new ArrayList<>();
        tempAllCellInfo = new ArrayList<>();
        nearestCell.add(-1);
        nearestCell.add(-1);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                //  Log.v(TAG, "Drag Started");
                copyPageLayoutChildrenToCache();
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //   Log.v(TAG, "Drag Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                // Log.v(TAG, "Drag EXITED");
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
                // Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }

    private void copyPageLayoutChildrenToCache() {
        cacheOriginalChildPos.clear();
        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            cacheOriginalChildPos.add(pageLayout.getChildAt(i));
        }
    }

    void onDrag(DragEvent event) {
        drag_view = (View) event.getLocalState();
        X = (int) event.getX();
        Y = (int) event.getY();
        Log.v(TAG, " pos::  X :: Y :: " + X + "  " + Y);
        checkSpaceAvail();
    }

    void onDrop() {
        //To Do -  manage drawer open handling
        try {
            actionAfterDrop();
            isAvailableCellsGreater = false;
            isRequiredCellsCalculated = false;
            isItemCanPlaced = false;
            isLoaderStarted = false;

            affectedChilds.clear();
            cellToBePlaced = null;
            drag_view.setVisibility(View.VISIBLE);
            cacheOriginalChildPos.clear();
            //  transition(transChild,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionAfterDrop() {

        if (isLoaderStarted) {
            if (isItemCanPlaced) {
                if (affectedChilds.size() > 0) {
                    copyDragMatriceLayoutParamsToOriginal();
                }


                makeDragMatricesEmpty();
                copyTempMatrixIntoLauncherCellMatrix();

                if (cellToBePlaced != null) {
                    if (cellToBePlaced.getIsAppOrFolderOrWidget() == 2) {
                        addToExistingFolder();
                    } else {
                        createNewFolder();
                    }
                } else {
                    calculateLayoutParams();
                }

            } else {
                if (dragInfo.getDropExternal())
                    Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
            }
        }

        if (!isItemCanPlaced) {
            if (dragInfo.getDropExternal())
                Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
        }
    }

    private void copyDragMatriceLayoutParamsToOriginal() {
        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            View child = pageLayout.getChildAt(i);
            CellInfo cellInfo = (CellInfo) child.getTag();
            if (cellInfo.getDragMatrices() != null) {
                cellInfo.getMatrixCells().clear();
                copyArray(cellInfo.getMatrixCells(), cellInfo.getDragMatrices());
                cellInfo.setDragMatrices(null);
                cellInfo.setLayoutParams(cellInfo.getDragLayoutParams());
                cellInfo.setDragLayoutParams(null);
            }
        }
    }


    private void makeDragMatricesEmpty() {
        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            View child = pageLayout.getChildAt(i);
            CellInfo cellInfo = (CellInfo) child.getTag();
            cellInfo.setDragMatrices(null);
            cellInfo.setDragLayoutParams(null);
        }

    }


    private void copyTempMatrixIntoLauncherCellMatrix() {
        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                launcherApplication.setCellsMatrix(new int[]{x, y}, tempCellsMatrix[x][y]);
                Log.v(TAG, "x :: y :: " + x + "  " + y + " val :: " + launcherApplication.getCellMatrixVal(new int[]{x, y}));
            }
        }
    }

    private void checkSpaceAvail() {

        nearCellsObj = findNearestCells();

        dragInfo = launcherApplication.dragInfo;
        if (nearCellsObj.size() > 0) {
            Log.v(TAG, "matrix pos :: x  :: y " + nearCellsObj.get(0) + "  " + nearCellsObj.get(1));
            if (!nearestCell.equals(nearCellsObj)) {

            nearestCell = nearCellsObj;

            if (!isRequiredCellsCalculated) {
                isAvailableCellsGreater = calculateReqCells();
            }

            if (isAvailableCellsGreater) {
                markTempCellsMatrix(nearestCell, spanX, spanY);

            } else {
                //Do some actions if drag view is app and it is dragged to center of
                // app/folder so that it can create or update folder
                isItemCanPlaced = false;
            }
        }
          }

    }

    private boolean calculateReqCells() {
        boolean isContinue;
        if (dragInfo.getDropExternal()) {
            isContinue = calTempCellMatrix();
        } else {
            Log.v(TAG, "removed");
            pageLayout.removeView(drag_view); //To Do unmark cells in launcher cell matrix
            unMarkCellsInOriginalMatrix();
            isContinue = calTempCellMatrix();
        }
        isRequiredCellsCalculated = true;

        return isContinue;

    }

    void unMarkCellsInOriginalMatrix() {
        //Make this places free after remove
        ArrayList<ArrayList<Integer>> allCellsInfo = ((CellInfo) drag_view.getTag()).getMatrixCells();

        for (int i = 0; i < allCellsInfo.size(); i++) {
            ArrayList<Integer> arrayList = allCellsInfo.get(i);
            launcherApplication.setCellsMatrix(new int[]{arrayList.get(0), arrayList.get(1)}, false);
        }
    }

    private boolean calTempCellMatrix() {


        int noOfReqCells = 1, noOfAvailCells = 0;

        if (!dragInfo.getIsAppOrWidget()) {
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
            return true;
        }
        return false;

    }

    private void markTempCellsMatrix(ArrayList<Integer> nearestCell, int spanX, int spanY) {

        tempAllCellInfo.clear();
        tempCellsMatrix = new boolean[launcherApplication.getCellCountX()][launcherApplication.getCellCountY()];
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


        for (int x = nearestCell.get(0); x <= scaleXEndPos; x++) {
            for (int y = nearestCell.get(1); y <= scaleYEndPos; y++) {

                ArrayList<Integer> matrixPos = new ArrayList<>();
                matrixPos.add(x);
                matrixPos.add(y);
                tempAllCellInfo.add(matrixPos);
                tempCellsMatrix[x][y] = true;
            }
        }

        startLoader();

    }

    private void startLoader() {
        try {


            ArrayList<ArrayList<Integer>> allCellsInfo;
            greenSignal = new ArrayList<>();
            isLoaderStarted = true;
            childViews.clear();
            affectedChilds.clear();

            if (pageLayout.getChildCount() > 0) {
                for (int i = 0; i < pageLayout.getChildCount(); i++) {
                    View child = pageLayout.getChildAt(i);
                    childViews.add(child);
                    CellInfo cellInfo = ((CellInfo) child.getTag());
                    if (cellInfo.getDragMatrices() != null) {
                        allCellsInfo = cellInfo.getDragMatrices();
                    } else {
                        allCellsInfo = cellInfo.getMatrixCells();
                    }
                    checkForEachChild(allCellsInfo, child);
                }

                if (greenSignal.size() > 0 && !greenSignal.contains(false)) {
                    shiftAndAddToNewPos();
                    isItemCanPlaced = true;
                } else {
                    Log.v(TAG, "no green signal ");
                    isItemCanPlaced = true;
                }

            } else {
                isItemCanPlaced = true;
            }

            revertActions();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void revertActions() {



        for (int i = 0; i < cacheOriginalChildPos.size(); i++) {
            View child = (View) cacheOriginalChildPos.get(i);
            CellInfo cellInfo = (CellInfo) child.getTag();
            boolean isAnyCellAffected = false;

            for (int j = 0; j < cellInfo.getMatrixCells().size(); j++) {
                ArrayList<Integer> matrixPos = cellInfo.getMatrixCells().get(j);
                if (tempCellsMatrix[matrixPos.get(0)][matrixPos.get(1)]) {
                    isAnyCellAffected = true;
                    break;

                }
            }

            if (!isAnyCellAffected) {
                if (cellInfo.getLayoutParams() != null && cellInfo.getDragLayoutParams() != null) {
                    pageLayout.removeView(child);
                    pageLayout.addView(child, cellInfo.getLayoutParams());
                    cellInfo.setDragMatrices(null);
                    cellInfo.setDragLayoutParams(null);
                }
            }
        }
    }

    private void checkForEachChild(ArrayList<ArrayList<Integer>> allCellsInfo, View child) {

        boolean stay = stayOrMoveForAppDrag(child.getLeft(), child.getRight(), child.getTop(), child.getBottom());

        boolean isAnyCellAffected = false;
        //Check for each cell are cellInfo.setAddToExitingFolder(true);they affected
        for (int i = 0; i < allCellsInfo.size(); i++) {
            ArrayList<Integer> matrixPos = allCellsInfo.get(i);
            if (tempCellsMatrix[matrixPos.get(0)][matrixPos.get(1)]) {
                isAnyCellAffected = true;
                break;

            }
        }

        if (isAnyCellAffected) {
            affectedChilds.add(child);
            startPlaceChildFromZeroThPos(stay, child);
        } else {
            markCells(allCellsInfo);
        }


    }

    private void startPlaceChildFromZeroThPos(boolean stay, View child) {
        //Both are app or one app and folder
        try {
            CellInfo cellInfo = ((CellInfo) child.getTag());
            if (cellInfo.getIsAppOrFolderOrWidget() == 1 || cellInfo.getIsAppOrFolderOrWidget() == 2) {
                if (dragInfo.getIsAppOrWidget() && dragInfo.getFolderInfo() == null) {

                    if (stay) {
                        cellToBePlaced = new CellInfo();
                        copyCellInfo(cellInfo, cellToBePlaced);

                        if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
                            cellInfo.setAddToExitingFolder(true);
                            //Add To Existing Folder for Temp
                        } else {
                            //Create Folder For Temp
                            cellToBePlaced.setFolderInfo(new FolderInfo(dragInfo.getAppInfo(), cellInfo.getAppInfo()));
                            cellToBePlaced.setAppInfo(null);
                        }
                    } else {
                        cellToBePlaced = null;
                        verifyCells(child);
                    }
                } else {
                    cellToBePlaced = null;
                    verifyCells(child);
                }
            } else {
                cellToBePlaced = null;
                verifyCells(child);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void transition(View child, boolean val) {
        ImageView layout = (ImageView) child.findViewById(R.id.grid_image);
        layout.setBackground(trans);

        CellInfo cellInfo = (CellInfo) child.getTag();
        cellInfo.setTransition(val);
        if (true) {
            trans.reverseTransition(1000);
            transChild = child;
        } else {
            transChild = null;
        }


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

    void verifyCells(View child) {
        boolean isPlaceMentSuccessFul = false;

        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                if (checkFromCell(x, y, child)) {
                    isPlaceMentSuccessFul = true;
                    break;
                }
            }
        }

        greenSignal.add(isPlaceMentSuccessFul);
    }

    private void shiftAndAddToNewPos() {
        try {

            for (int i = 0; i < affectedChilds.size(); i++) {
                View child = (View) affectedChilds.get(i);

                CellInfo cellInfo = (CellInfo) child.getTag();
                int width = cellWidth * cellInfo.getSpanX();
                int height = cellHeight * cellInfo.getSpanY();


                ArrayList<ArrayList<Integer>> allCellsInfo = cellInfo.getDragMatrices();
                ArrayList<Integer> firstPos = allCellsInfo.get(0);
                layoutParams = new FrameLayout.LayoutParams(width, height);
                int leftMargin = firstPos.get(0) * cellWidth + (launcherApplication.getMaxGapLR() * (firstPos.get(0) + 1));
                int topMargin = firstPos.get(1) * cellHeight + (launcherApplication.getMaxGapTB() * (firstPos.get(1) + 1));

                layoutParams.setMargins(leftMargin, topMargin, 0, 0);
                cellInfo.setDragLayoutParams(layoutParams);
                pageLayout.removeView(child);

                pageLayout.addView(child, layoutParams);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void markCells(ArrayList<ArrayList<Integer>> allCellsInfo) {
        for (int i = 0; i < allCellsInfo.size(); i++) {
            ArrayList<Integer> cells = allCellsInfo.get(i);
            tempCellsMatrix[cells.get(0)][cells.get(1)] = true;
        }
    }


    private boolean checkFromCell(int x, int y, View child) {
        ArrayList<ArrayList<Integer>> allCellsInfo = new ArrayList<>();
        boolean isPlaceMentSuccessFul = true;
        CellInfo cellInfo = ((CellInfo) child.getTag());

        int actualSpanX = cellInfo.getSpanX() - 1;
        int actualSpanY = cellInfo.getSpanY() - 1;

        int scaleXEndPos = x + actualSpanX;
        int scaleYEndPos = y + actualSpanY;

        if (scaleXEndPos > launcherApplication.getCellCountX() - 1) {
            return false;
        }

        if (scaleYEndPos > launcherApplication.getCellCountY() - 1) {
            return false;
        }


        for (int i = x; i <= scaleXEndPos; i++) {
            for (int j = y; j <= scaleYEndPos; j++) {
                ArrayList<Integer> arrayList = new ArrayList<>();
                arrayList.add(i);
                arrayList.add(j);
                allCellsInfo.add(arrayList);

                if (tempCellsMatrix[i][j]) {
                    isPlaceMentSuccessFul = false;
                    return isPlaceMentSuccessFul;
                }
            }
        }
        if (isPlaceMentSuccessFul) {
            markCells(allCellsInfo);
            cellInfo.setDragMatrices(allCellsInfo);
        }


        return isPlaceMentSuccessFul;

    }

    private ArrayList<Integer> findNearestCells() {
        ArrayList arrayList = new ArrayList<Integer>();

        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                arrayList.clear();
                arrayList.add(x);
                arrayList.add(y);

                if (launcherApplication.mapMatrixPosToRec.get(arrayList).contains(X, Y)) {
                    return arrayList;
                }

            }
        }
        return arrayList;
    }


    private boolean stayOrMoveForAppDrag(int left, int right, int top, int bottom) {
        int x, y;

        int centerX =  nearestCell.get(0) * cellWidth +
                (1 * cellWidth) / 2;
        int centerY = nearestCell.get(1) * cellHeight +
                (1 * cellHeight) / 2;

        x = X-centerX ;
        y = Y-centerY;



        int distance = (int) Math.sqrt(x * x
                + y * y);
        Log.v(TAG, "centerX  ::  centerY  :: x ::  y " + centerX  + "   " + centerY + "   " + X + "   " +Y + "  " + distance) ;

        if (distance < 150) {
            return true;
        } else {
            return false;
        }

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
        boolean success = false;
        success = launcherApplication.getLauncher().mAppWidgetManager.bindAppWidgetIdIfAllowed(this.appWidgetId, dragInfo.getWidgetInfo().getComponentName());
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
       // hostView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
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
        cellInfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), tempAllCellInfo));
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
        int leftMargin = nearestCell.get(0) * cellWidth + (launcherApplication.getMaxGapLR() * (nearestCell.get(0) + 1));
        int topMargin = nearestCell.get(1) * cellHeight + (launcherApplication.getMaxGapTB() * (nearestCell.get(1) + 1));
        layoutParams.setMargins(leftMargin, topMargin, 0, 0);

        if (dragInfo.getIsAppOrWidget()) {
            if (!dragInfo.getDropExternal() && dragInfo.getFolderInfo() != null) {
                CellInfo cellInfo = (CellInfo) drag_view.getTag();
                cellInfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), tempAllCellInfo));
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
        dragInfo.setIsAppOrWidget(true);
        CellInfo cellInfo = (CellInfo) v.getTag();
        dragInfo.setAppInfo(cellInfo.getAppInfo());
        dragInfo.setFolderInfo(cellInfo.getFolderInfo());
        dragInfo.setDropExternal(false);
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
        dragInfo.setIsAppOrWidget(false);
        CellInfo cellInfo = (CellInfo) launcherAppWidgetHostView.getTag();
        dragInfo.setWidgetInfo(cellInfo.getWidgetInfo());
        dragInfo.setDropExternal(false);
        dragInfo.setMatrixCells(cellInfo.getMatrixCells());
        launcherApplication.dragInfo = dragInfo;
        launcherApplication.dragAnimation(launcherAppWidgetHostView, View.INVISIBLE);
    }
}
