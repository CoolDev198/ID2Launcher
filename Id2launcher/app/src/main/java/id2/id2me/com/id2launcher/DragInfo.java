package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.view.View;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.database.FolderInfo;
import id2.id2me.com.id2launcher.database.WidgetInfo;

/**
 * Created by sunita on 8/9/16.
 */
public class DragInfo {

    //true - App and false - Widget
    private boolean isAppOrWidget;
    private AppInfo appInfo;
    private WidgetInfo widgetInfo;
    private FolderInfo folderInfo;
    private View widgetView;
    private  boolean dropExternal;
    ArrayList<ArrayList<Integer>>  matrixCells;
    private int spanY,spanX;
    private ArrayList<ArrayList<Integer>> dragMatrices;

    public boolean getIsAppOrWidget() {
        return isAppOrWidget;
    }

    public void setIsAppOrWidget(boolean val) {
        this.isAppOrWidget = val;
    }

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

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
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

}
