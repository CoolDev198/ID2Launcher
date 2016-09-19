package id2.id2me.com.id2launcher;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import id2.id2me.com.id2launcher.database.ApplicationInfo;
import id2.id2me.com.id2launcher.database.FolderInfo;
import id2.id2me.com.id2launcher.database.WidgetInfo;
import id2.id2me.com.id2launcher.general.AllAppsList;

public class ItemInfo {

    int isAppOrFolderOrWidget;
    FolderInfo folderInfo;
    ApplicationInfo appInfo;
    WidgetInfo widgetInfo;
    View view;
    ArrayList<ArrayList<Integer>>  matrixCells;
    private FrameLayout.LayoutParams layoutParams;
    private ArrayList<ArrayList<Integer>> dragMatrices;
    private FrameLayout.LayoutParams dragLayoutParams;
    private boolean addToExitingFolder;

    public FolderInfo getFolderInfo() {
        return folderInfo;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setFolderInfo(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
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

    static final int NO_ID = -1; /*The id in the settings database for this item */
    long id = NO_ID;
    private int container = NO_ID;
    private int cellX = NO_ID;
    private int cellY = NO_ID;
    private byte[] icon;
    private String intent="";
    private int spanX = 1;
    private int spanY = 1;
    private int appWidgetId=NO_ID;
    private String title="";
    private int itemType = DatabaseHandler.ITEM_TYPE_APP;
    private int iconType=0;
    private String pname="";



    public  static Bitmap createIconBitmap(Bitmap icon, Context context) {
        int textureWidth = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);;
        int textureHeight = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);;
        int sourceWidth = icon.getWidth();
        int sourceHeight = icon.getHeight();
        if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
            // Icon is bigger than it should be; clip it (solves the GB->ICS migration case)
            return Bitmap.createBitmap(icon,
                    (sourceWidth - textureWidth) / 2,
                    (sourceHeight - textureHeight) / 2,
                    textureWidth, textureHeight);
        } else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
            // Icon is the right size, no need to change it
            return icon;
        } else {
            // Icon is too small, render to a larger bitmap
            final Resources resources = context.getResources();
            return AllAppsList.createIconBitmap(new BitmapDrawable(resources, icon), context);
        }
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static byte[]  writeBitmap(Bitmap bitmap) {
        byte[] data=null;
        if (bitmap != null) {
          data = flattenBitmap(bitmap);
        }
        return data;
    }


    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContainer() {
        return container;
    }

    public int getCellX() {
        return cellX;
    }

    public void setCellX(int cellX){
        this.cellX=cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public void setCellY(int cellY) {
        this.cellY = cellY;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public String getIntent() {
        return intent;
    }

    public int getSpanX() {
        return spanX;
    }

    public int getSpanY() {
        return spanY;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public String getTitle() {
        return title;
    }

    public int getItemType() {
        return itemType;
    }

    public int getIconType() {
        return iconType;
    }

    public String getPname() {
        return pname;
    }

    public void setAddToExitingFolder(boolean addToExitingFolder) {
        this.addToExitingFolder = addToExitingFolder;
    }

    public void setContainer(int container) {
        this.container = container;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }
}