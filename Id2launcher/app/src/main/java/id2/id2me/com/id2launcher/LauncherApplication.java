package id2.id2me.com.id2launcher;

import android.app.Application;
import android.appwidget.AppWidgetHost;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.database.FolderInfo;

/**
 * Created by sunita on 7/27/16.
 */
public class LauncherApplication extends Application {
    public View folderView;
    public boolean isDrawerOpen = false;
    public ArrayList<FolderInfo> folderFragmentsInfo;
    public DragInfo dragInfo;
    private PageDragListener pageDragListener;
    public LauncherAppWidgetHost mAppWidgetHost;
    private int cellCountX, cellCountY, maxGapLR, maxGapTB;
    public boolean cellsMatrix[][];
    private Launcher launcher;

    public HashMap<ArrayList<Integer>, Rect> mapMatrixPosToRec;
    public View desktopFragment;

    @Override
    public void onCreate() {
        super.onCreate();
        setCellCountX();
        setCellCountY();
        mapMatrixPosToRec = new HashMap<>();
        folderFragmentsInfo = new ArrayList<>();
        cellsMatrix = new boolean[cellCountX][cellCountY];

    }

    public Typeface getTypeFace() {
        Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), "fonts/Roboto-Regular.ttf");
        return typeface;
    }

    public void dragAnimation(View view) {
        try {
            ClipData.Item item = new ClipData.Item(
                    (CharSequence) (""));

            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData data = new ClipData("",
                    mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);
            view.startDrag(data, shadowBuilder, view, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    public void setPageDragListener(PageDragListener pageDragListener) {
        this.pageDragListener = pageDragListener;
    }

    public PageDragListener getPageDragListener() {
        return pageDragListener;
    }

    public int getMaxGapLR() {
        return maxGapLR;
    }

    public int getMaxGapTB() {
        return maxGapTB;
    }

    public int getCellCountX() {
        return cellCountX;
    }

    public void setCellsMatrix(int[] matrix, boolean val) {
        cellsMatrix[matrix[0]][matrix[1]] = val;
    }

    public boolean getCellMatrixVal(int[] matrix) {
        return cellsMatrix[matrix[0]][matrix[1]];
    }

    public int getCellCountY() {
        return cellCountY;
    }

    public boolean[][] getCellMatrix() {
        return cellsMatrix;
    }

    public int getScreenHeight() {
        int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        return height;

    }

    public int getScreenWidth() {
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    public int getCellHeight() {
        int cellHeight = (int) getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.cell_height);
        return cellHeight;
    }

    public int getCellWidth() {
        int cellWidth = (int) getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.cell_width);
        return cellWidth;
    }

    public int getScreenDensity() {
        int density = (int) getApplicationContext().getResources().getDisplayMetrics().density;
        return density;
    }


    public void setCellCountX() {
        int countX = getScreenWidth() / getCellWidth();
        cellCountX = countX;
        setMaxGapForLR();
    }

    public void setCellCountY() {
        int countY = getScreenHeight() / getCellHeight();
        cellCountY = countY;
        setMaxGapForTB();
    }

    public int calculateExtraSpaceWidthWise() {
        int extraWidthSpace = getScreenWidth() - cellCountX * getCellWidth();
        return extraWidthSpace;
    }

    public int calculateExtraSpaceHeightWise() {
        int extraHeightSpace = getScreenHeight() - cellCountY * getCellHeight();
        return extraHeightSpace;
    }

    public void setMaxGapForLR() {
        int paddingLR = calculateExtraSpaceWidthWise() / (cellCountX + 1);
        maxGapLR = paddingLR;

    }

    public void setMaxGapForTB() {
        int paddingTB = calculateExtraSpaceHeightWise() / (cellCountY + 1);
        maxGapTB = paddingTB;

    }

    public int convertFromPixelToDp(int dimension) {
        int dimensionInDp = dimension / getScreenDensity();
        return dimensionInDp;
    }

    public int convertFromDpToPixel(int resource) {
        int dimensionInPixel = getApplicationContext().getResources().getDimensionPixelOffset(resource);
        return dimensionInPixel;
    }


}
