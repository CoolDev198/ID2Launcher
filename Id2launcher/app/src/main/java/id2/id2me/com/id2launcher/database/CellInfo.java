package id2.id2me.com.id2launcher.database;

import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by bliss105 on 20/07/16.
 */
public class CellInfo {
    int isAppOrFolderOrWidget;//1 ,2, 3
    FolderInfo folderInfo;
    AppInfo appInfo;
    WidgetInfo widgetInfo;
    View view;
    int spanX,spanY;
    ArrayList<ArrayList<Integer>>  matrixCells;
    private FrameLayout.LayoutParams layoutParams;
    private ArrayList<ArrayList<Integer>> dragMatrices;
    private FrameLayout.LayoutParams dragLayoutParams;
    private boolean addToExitingFolder;
    private boolean transition;

    public FolderInfo getFolderInfo() {
        return folderInfo;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setFolderInfo(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public int getIsAppOrFolderOrWidget() {
        return isAppOrFolderOrWidget;
    }

    public void setIsAppOrFolderOrWidget(int isAppOrFolderOrWidget) {
        this.isAppOrFolderOrWidget = isAppOrFolderOrWidget;
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void setMatrixCells(ArrayList<ArrayList<Integer>> matrixCells) {
        this.matrixCells = matrixCells;
    }

    public  ArrayList<ArrayList<Integer>> getMatrixCells() {
        return matrixCells;
    }

    public int getSpanX() {
        return spanX;
    }

    public int getSpanY() {
        return spanY;
    }

    public FrameLayout.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    public void setLayoutParams(FrameLayout.LayoutParams layoutParams) {
        this.layoutParams = layoutParams;
    }

    public WidgetInfo getWidgetInfo() {
        return widgetInfo;
    }

    public void setWidgetInfo(WidgetInfo widgetInfo) {
        this.widgetInfo = widgetInfo;
    }

    public void setSpanY(int spanY) {
        this.spanY = spanY;
    }

    public void setSpanX(int spanX) {
        this.spanX = spanX;
    }

    public void setDragMatrices(ArrayList<ArrayList<Integer>> dragMatrices) {
        this.dragMatrices = dragMatrices;
    }

    public ArrayList<ArrayList<Integer>> getDragMatrices() {
        return dragMatrices;
    }

    public FrameLayout.LayoutParams getDragLayoutParams() {
        return dragLayoutParams;
    }

    public void setDragLayoutParams(FrameLayout.LayoutParams dragLayoutParams) {
        this.dragLayoutParams = dragLayoutParams;
    }

    public void setAddToExitingFolder(boolean addToExitingFolder) {
        this.addToExitingFolder = addToExitingFolder;
    }

    public void setTransition(boolean transition) {
        this.transition = transition;
    }

    public boolean getTransition() {
        return transition;
    }
}
