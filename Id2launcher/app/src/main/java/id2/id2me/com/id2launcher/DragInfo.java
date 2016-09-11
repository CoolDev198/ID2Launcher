package id2.id2me.com.id2launcher;

import android.view.View;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.database.ApplicationInfo;
import id2.id2me.com.id2launcher.database.FolderInfo;
import id2.id2me.com.id2launcher.database.WidgetInfo;

/**
 * Created by sunita on 8/9/16.
 */
public class DragInfo {

    //true - App and false - Widget
    private boolean isAppOrWidget;
    private ApplicationInfo appInfo;
    private WidgetInfo widgetInfo;
    private FolderInfo folderInfo;
    private View widgetView;
    private  boolean dropExternal;
    ArrayList<ArrayList<Integer>>  matrixCells;
    private int spanY,spanX;
    private ArrayList<ArrayList<Integer>> dragMatrices;
    private boolean isItemCanPlaced;
    private int isAppOrFolderOrWidget;
    private View originalView;
    private View dragView;


    public WidgetInfo getWidgetInfo() {
        return widgetInfo;
    }

    public void setWidgetInfo(WidgetInfo widgetInfo) {
        this.widgetInfo = widgetInfo;
    }

    public View getWidgetView() {
        return widgetView;
    }

    public void setWidgetView(View widgetView) {
        this.widgetView = widgetView;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }


    public void setDropExternal(boolean dropExternal) {
        this.dropExternal = dropExternal;
    }

    public boolean getDropExternal() {
        return dropExternal;
    }


    public void setFolderInfo(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    public FolderInfo getFolderInfo() {
        return folderInfo;
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

    public void setSpanX(int spanX) {
        this.spanX = spanX;
    }

    public void setSpanY(int spanY) {
        this.spanY = spanY;
    }

    public void setDragMatrices(ArrayList<ArrayList<Integer>> dragMatrices) {
        this.dragMatrices = dragMatrices;
    }

    public ArrayList<ArrayList<Integer>> getDragMatrices() {
        return dragMatrices;
    }


    public boolean getIsItemCanPlaced() {
        return isItemCanPlaced;
    }

    public void setIsItemCanPlaced(boolean isItemCanPlaced) {
        this.isItemCanPlaced = isItemCanPlaced;
    }

    public int getIsAppOrFolderOrWidget() {
        return isAppOrFolderOrWidget;
    }

    public void setIsAppOrFolderOrWidget(int isAppOrFolderOrWidget) {
        this.isAppOrFolderOrWidget = isAppOrFolderOrWidget;
    }

    public void setOriginalView(View originalView) {
        this.originalView = originalView;
    }

    public View getOriginalView() {
        return originalView;
    }

    public void setDragView(View dragView) {
        this.dragView = dragView;
    }

    public View getDragView() {
        return dragView;
    }
}
