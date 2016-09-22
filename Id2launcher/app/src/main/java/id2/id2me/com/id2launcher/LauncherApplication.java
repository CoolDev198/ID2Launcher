package id2.id2me.com.id2launcher;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.models.DragInfoModel;
import id2.id2me.com.id2launcher.models.FolderInfoModel;
import id2.id2me.com.id2launcher.models.NotificationWidgetModel;

/**
 * Created by sunita on 7/27/16.
 */
public class LauncherApplication extends Application {
    public static ImageView wallpaperImg;
    public static List<NotificationWidgetModel> notificationWidgetModels;
    public View folderView;
    public boolean isDrawerOpen = false;
    public ArrayList<FolderInfoModel> folderFragmentsInfo;
    public DragInfoModel dragInfo;
    private PageDragListener pageDragListener;
    public LauncherAppWidgetHost mAppWidgetHost;
    private int cellCountX, cellCountY, maxGapLR, maxGapTB;
    public boolean cellsMatrix[][];
    private Launcher launcher;
    public LauncherModel mModel;
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

        mModel = new LauncherModel(this);


       addBroadCastReceiver();

    }

    private void addBroadCastReceiver() {
        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mModel, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mModel);
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
        this.launcher= launcher;
    }

    public LauncherModel setDeskTopFragment(DesktopFragment fragment) {
        mModel.initialize(fragment);
        return null;
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
