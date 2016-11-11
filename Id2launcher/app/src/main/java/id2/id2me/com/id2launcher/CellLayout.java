package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by sunita on 10/17/16.
 */

public class CellLayout extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {

    LauncherApplication launcherApplication;
    private boolean cellsMatrix[][];
    private int cellCountY;
    private int cellCountX;
    private int height;

    public CellLayout(Context context, int heightResource) {
        super(context);
        height = getResources().getDimensionPixelSize(heightResource);
        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
        init(context);
    }

    public void setCellsMatrix(int[] matrix, boolean val) {
        cellsMatrix[matrix[0]][matrix[1]] = val;
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
        setOnHierarchyChangeListener(this);
    }


    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }

}

