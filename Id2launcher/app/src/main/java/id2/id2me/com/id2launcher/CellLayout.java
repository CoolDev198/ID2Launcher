package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by sunita on 10/17/16.
 */

public class CellLayout extends FrameLayout {

    LauncherApplication launcherApplication;
    private boolean cellsMatrix[][];
    private int cellCountY;
    private int cellCountX;
    private int height;
    PageDragListener pageDragListener;
    private String TAG;
    private int mCellPaddingLeft;
    private int mCellPaddingTop;

    public CellLayout(Context context, int heightResource) {
        super(context);
        height = getResources().getDimensionPixelSize(heightResource);
        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
        init(context);
    }

    public void setDragListener(PageDragListener dragListener) {
        this.pageDragListener = dragListener;
    }
    public PageDragListener getDragListener(){
        return  this.pageDragListener;
    }

    private void setCellsMatrix(int[] matrix, boolean val) {
        cellsMatrix[matrix[0]][matrix[1]] = val;
    }

    public void markCells(int cellx, int celly, int spanx, int spany) {

        int xStart = cellx;
        int yStart = celly;
        int xEnd = (cellx + spanx - 1);
        int yEnd = (celly + spany - 1);


        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                //      Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                setCellsMatrix(new int[]{x, y}, true);
            }
        }
    }

    public void unMarkCells(int cellx, int celly, int spanx, int spany) {

        int xStart = cellx;
        int yStart = celly;
        int xEnd = (cellx + spanx - 1);
        int yEnd = (celly + spany - 1);

        if (cellx != -1 && celly != -1) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    //Log.v(TAG, "cells unmarked :: " + x + "  " + y);
                    setCellsMatrix(new int[]{x, y}, false);
                }
            }
        }

    }

    public boolean getCellMatrixVal(int[] matrix) {
        return cellsMatrix[matrix[0]][matrix[1]];
    }


    private void setCellCountX() {
        int countX = launcherApplication.getScreenWidth() / launcherApplication.getCellWidth();
        cellCountX = countX;
    }

    private void setCellCountY() {
        int countY = height/ launcherApplication.getCellHeight();
        cellCountY = countY;
    }


    public int getCellCountX() {
        return cellCountX;
    }

    public int getCellCountY() {
        return cellCountY;
    }

    void init(Context context) {
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        setCellCountX();
        setCellCountY();
        cellsMatrix = new boolean[cellCountX][cellCountY];
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);

        int cellWidth = (int) context.getResources().getDimension(R.dimen.cell_width);
        int cellHeight = (int) context.getResources().getDimension(R.dimen.cell_height);

        int appIconSize = (int) context.getResources().getDimension(R.dimen.app_icon_size);

        mCellPaddingLeft = (cellWidth - appIconSize) / 2;
        mCellPaddingTop = (cellHeight - appIconSize) / 2;
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

}

