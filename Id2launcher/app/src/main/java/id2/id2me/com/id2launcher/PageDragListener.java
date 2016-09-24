package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import id2.id2me.com.id2launcher.models.AppInfoModel;
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
    View dropTargetLayout, scrollView, blur_relative;
    private FrameLayout.LayoutParams layoutParams;
    private int[] nearestCell;
    private ItemInfoModel cellToBePlaced;
    private boolean isDragStarted = false;
    private ItemInfoModel dragInfo;

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


    void onDrag(DragEvent event) {
        X = (int) event.getX();
        Y = (int) event.getY();
        goAhead();

    }

    private void calculateReqCells() {
        try {
            if (dragInfo.getDropExternal()) {
                findAvalCells();
            } else {
                pageLayout.removeView(drag_view);
                dragInfo.setTempCellX(dragInfo.getCellX());
                dragInfo.setTempCellY(dragInfo.getCellY());
                unMarkCells(dragInfo.getTmpCellX(), dragInfo.getCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
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
                } else {
                    dragInfo.setIsItemCanPlaced(false);
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

                if (nearestCell[0] == -1 && nearestCell[1] == -1) {
                    nearestCell = nearCellsObj;
                }
                ticks = 0;
            }

            if (ticks > 6) {
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
            markCells(dragInfo.getCellX(), dragInfo.getCellY(), dragInfo.getSpanX(), dragInfo.getSpanY());
            dragInfo.setIsItemCanPlaced(true);
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

            if (dragInfo.getIsItemCanPlaced()) {

                dragInfo.setCellX(dragInfo.getTmpCellX());
                dragInfo.setCellY(dragInfo.getTmpCellY());

                Toast.makeText(context, "cellx :: "+ nearestCell[0] +" celly:: " +nearestCell[1], Toast.LENGTH_LONG).show();

//                if (cellToBePlaced != null) {
//                    if (cellToBePlaced.getIsAppOrFolderOrWidget() == 2) {
//                        addToExistingFolder();
//                    } else {
//                        createNewFolder();
//                    }
//                } else {
                calculateLayoutParams();
                //}

                // copyDragMatricesToActualMatrices();


            } else {
                if (dragInfo.getDropExternal())
                    Toast.makeText(context, "No room available", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createOrUpdateItemInfo(View view) {

        try {

            if (dragInfo.getDropExternal()) {

                if (cellToBePlaced != null) {
                    dragInfo.setContainer(DatabaseHandler.CONTAINER_FOLDER);
                } else {
                    dragInfo.setContainer(DatabaseHandler.CONTAINER_DESKTOP);
                }

                view.setTag(dragInfo);

                db.addOrMoveItemInfo(dragInfo);
            } else {
                ItemInfoModel cellInfo = (ItemInfoModel) view.getTag();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyDragMatricesToActualMatrices() {
        try {
            for (int i = 0; i < pageLayout.getChildCount(); i++) {
                View child = (View) pageLayout.getChildAt(i);
                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
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


        for (int x = xStart; x < xEnd; x++) {
            for (int y = yStart; y < yEnd; y++) {
                //Log.v(TAG, "cells unmarked :: " + x + "  " + y);
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
            for (int x = xStart; x < xEnd; x++) {
                for (int y = yStart; y < yEnd; y++) {
                    //Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                    launcherApplication.setCellsMatrix(new int[]{x, y}, false);
                }
            }
        }

    }


    private void shiftAndAddToNewPos() {

        for (int i = 0; i < pageLayout.getChildCount(); i++) {
            try {
                View child = (View) pageLayout.getChildAt(i);

                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();

                int width = cellWidth * cellInfo.getSpanX();
                int height = cellHeight * cellInfo.getSpanY();
                layoutParams = new FrameLayout.LayoutParams(width, height);

                int[] bestCell = null;

                int[] cells = new int[]{cellInfo.getCellX(), cellInfo.getCellY()};

                if (Arrays.equals(cells, nearestCell) && findDistanceFromEachCell(nearestCell[0], nearestCell[1]) < 75) {

                    if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP) {
                        cellToBePlaced = new ItemInfoModel();
                        Log.v(TAG ,"new folder created");

                    } else if (dragInfo.getItemType() == DatabaseHandler.ITEM_TYPE_APP && cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                        Log.v(TAG ,"added to existing folder");
                    } else  {
                           cellToBePlaced = null;
                          Log.v(TAG,"move affected cell");
                        try {
                            bestCell = findBestCell(cellInfo.getSpanX(), cellInfo.getSpanY());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                if (bestCell != null) {

                    int leftMargin = bestCell[0] * cellWidth + (launcherApplication.getMaxGapLR() * (bestCell[0] + 1));
                    int topMargin = bestCell[1] * cellHeight + (launcherApplication.getMaxGapTB() * (bestCell[1] + 1));

                    layoutParams.setMargins(leftMargin, topMargin, 0, 0);
                    pageLayout.removeView(child);
                    pageLayout.addView(child, layoutParams);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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

        HashMap<Float, int[]> arrayListHashMap = new HashMap<>();
        ArrayList<Float> distances = new ArrayList<>();

        for (int x = 0; x < launcherApplication.getCellCountX(); x++) {
            for (int y = 0; y < launcherApplication.getCellCountY(); y++) {
                int[] matrix = new int[]{x, y};

                float distance = findDistanceFromEachCell(x, y);
                arrayListHashMap.put(distance, matrix);

                distances.add(distance);
            }
        }


        if (distances.size() > 0) {
            Collections.sort(distances);
        }

        return arrayListHashMap.get(distances.get(0));
    }


    private float findDistanceFromEachCell(int x, int y) {


        int centerX = (x * cellWidth) +
                (spanX * cellWidth) / 2;
        int centerY = (y * cellHeight) +
                (spanY * cellHeight) / 2;

        float distance = (float) Math.sqrt(Math.pow(X - centerX, 2) +
                Math.pow(Y - centerY, 2));

        // Log.v(TAG, "  " + x + "   " + y + "  " + distance);

        return distance;
    }

    private void addWidgetToPage() {

        try {
            if (dragInfo.getDropExternal()) {
                this.appWidgetId = ((LauncherApplication) ((Activity) context).getApplication()).mAppWidgetHost.allocateAppWidgetId();
                //   this.appWidgetProviderInfo = dragInfo.getWidgetInfo().getAppWidgetProviderInfo();

                checkIsWidgetBinded();
            } else {
                addWidgetOnInternalDragAndDrop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkIsWidgetBinded() {
//        boolean success = launcherApplication.getLauncher().mAppWidgetManager.bindAppWidgetIdIfAllowed(this.appWidgetId, dragInfo.getWidgetInfo().getComponentName());
//        if (success) {
//            askToConfigure(null);
//        } else {
//          //  launcherApplication.getLauncher().startActivityForBindingWidget(appWidgetId, dragInfo.getWidgetInfo().getComponentName());
//        }
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
           // imageView.setImageBitmap();
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            pageLayout.addView(view, layoutParams);
            createOrUpdateItemInfo(view);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            //  View child = cellToBePlaced.getView();
            //   cellToBePlaced.setIsAppOrFolderOrWidget(2);
//            child.setTag(cellToBePlaced);
//            ImageView imageView = (ImageView) child.findViewById(R.id.grid_image);
//            imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.folder_icon));
//            child.setOnClickListener(this);
//            child.setOnLongClickListener(this);

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
                ItemInfoModel cellInfo = (ItemInfoModel) ((View) pageLayout.getChildAt(i)).getTag();
                if (cellInfo.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
                    //     launcherApplication.folderFragmentsInfo.add(cellInfo.getFolderInfo());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addToExistingFolder() {
        try {
            //     cellToBePlaced.getFolderInfo().addNewItemInfo(dragInfo.getAppInfo());
            // View child = cellToBePlaced.getView();
            //child.setTag(cellToBePlaced);
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
            int leftMargin = dragInfo.getCellX() * cellWidth + (launcherApplication.getMaxGapLR() * (dragInfo.getCellX() + 1));
            int topMargin = dragInfo.getCellY() * cellHeight + (launcherApplication.getMaxGapTB() * (dragInfo.getCellX() + 1));
            layoutParams.setMargins(leftMargin, topMargin, 0, 0);

            addAppToPage();

//            if (dragInfo.getIsAppOrFolderOrWidget() != DatabaseHandler.ITEM_TYPE_APPWIDGET) {
//                if (dragInfo.getIsAppOrFolderOrWidget() == DatabaseHandler.ITEM_TYPE_FOLDER) {
//                    ItemInfoModel cellInfo = (ItemInfoModel) drag_view.getTag();
//                    cellInfo.setMatrixCells(copyArray(new ArrayList<ArrayList<Integer>>(), dragInfo.getDragMatrices()));
//                    pageLayout.addView(drag_view, layoutParams);
//                } else {
//                    addAppToPage();
//                }
//            } else {
//                addWidgetToPage();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        ItemInfoModel cellInfo = (ItemInfoModel) v.getTag();
//        if (cellInfo.getIsAppOrFolderOrWidget() == DatabaseHandler.ITEM_TYPE_FOLDER) {
//            if (launcherApplication.folderView != null) {
//                pageLayout.removeView(launcherApplication.folderView);
//            }
//            getPopUp(cellInfo.getFolderInfo().getAppInfos());
//        }

    }

    @Override
    public boolean onLongClick(View v) {
        try {
            launcherApplication.dragInfo = (ItemInfoModel) v.getTag();
            ;
            launcherApplication.dragAnimation(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void getPopUp(ArrayList<ItemInfoModel> appInfos) {
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
//            DragInfoModel dragInfo = new DragInfoModel();
//            //dragInfo.setIsAppOrFolderOrWidget(3);
//            dragInfo.setIsItemCanPlaced(true);
//            ItemInfoModel cellInfo = null;
//            try {
//                cellInfo = (ItemInfoModel) launcherAppWidgetHostView.getTag();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        //    dragInfo.setWidgetInfo(cellInfo.getWidgetInfo());
//            dragInfo.setDropExternal(false);
            //    dragInfo.setMatrixCells(cellInfo.getMatrixCells());
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
                    //   if (dragInfo.getDragMatrices().size() > 0) {
                    onDrop();
                    //} else {
                    Toast.makeText(context, "Invalid Drop Location", Toast.LENGTH_LONG).show();
                    //}
                    //    unMarkCells(dragInfo.getDragMatrices());

                } else {

                    //   if (dragInfo.getDragMatrices() != null) {
                    //  unMarkCells(dragInfo.getDragMatrices());
                    ArrayList<ArrayList<Integer>> tempCellInfo = new ArrayList<>();
                    //  copyArray(tempCellInfo, dragInfo.getMatrixCells());
                    // dragInfo.setDragMatrices(tempCellInfo);
                    shiftAndAddToNewPos();
                    //}
                    onDrop();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeViewFromDesktop() {
//        if (dragInfo.getDragMatrices() != null) {
        //          unMarkCells(dragInfo.getDragMatrices());
        shiftAndAddToNewPos();
        isAvailableCellsGreater = false;
        isRequiredCellsCalculated = false;
        isItemCanPlaced = false;
        cellToBePlaced = null;
        dropTargetLayout.setVisibility(View.INVISIBLE);
    }
    // }
}
