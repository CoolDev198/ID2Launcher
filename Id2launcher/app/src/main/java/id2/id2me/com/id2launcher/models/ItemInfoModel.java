package id2.id2me.com.id2launcher.models;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import id2.id2me.com.id2launcher.AllAppsList;
import id2.id2me.com.id2launcher.DatabaseHandler;
import id2.id2me.com.id2launcher.R;

public class ItemInfoModel {

    public static final int NO_ID = -1; /*The id in the settings database for this item */
    long id = NO_ID;
    private long container = NO_ID;

    private int cellX = NO_ID;
    private int cellY = NO_ID;

    private byte[] icon;
    private String intent = "";
    private int spanX = 1;
    private int spanY = 1;
    private int appWidgetId = NO_ID;
    private String title = "";
    private int itemType = DatabaseHandler.ITEM_TYPE_APP;
    private int iconType = 0;
    private String pname = "";
    private boolean dropExternal;
    private boolean isItemCanPlaced;
    private int tempCellX=NO_ID;
    private int tempCellY=NO_ID;
    private boolean isExisitingFolder;
    private ComponentName componentName;
    private AppWidgetProviderInfo appWidgetProviderInfo;
    private int screen;

    public int getTmpCellY() {
        return tempCellY;
    }

    public int getTmpCellX() {
        return tempCellX;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getContainer() {
        return container;
    }

    public void setContainer(long container) {
        this.container = container;
    }

    public int getCellX() {
        return cellX;
    }

    public void setCellX(int cellX) {
        this.cellX = cellX;
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

    public void setSpanX(int spanX) {
        this.spanX = spanX;
    }

    public int getSpanY() {
        return spanY;
    }

    public void setSpanY(int spanY) {
        this.spanY = spanY;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getIconType() {
        return iconType;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public void setIsExisitingFolder(boolean isExisitingFolder){
        this.isExisitingFolder=isExisitingFolder;
    }

    public boolean getIsExisitingFolder() {
        return isExisitingFolder;
    }

    public void setDropExternal(boolean dropExternal) {
        this.dropExternal = dropExternal;
    }

    public boolean getDropExternal() {
        return dropExternal;
    }


    public boolean getIsItemCanPlaced() {
        return isItemCanPlaced;
    }

    public void setIsItemCanPlaced(boolean isItemCanPlaced) {
        this.isItemCanPlaced = isItemCanPlaced;
    }


    public static Bitmap createIconBitmap(Bitmap icon, Context context) {
        int textureWidth = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
        int textureHeight = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
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

  public   static byte[] writeBitmap(Bitmap bitmap) {
        byte[] data = null;
        if (bitmap != null) {
            data = flattenBitmap(bitmap);
        }
        return data;
    }

    public void setTempCellX(int tempCellX) {
        this.tempCellX = tempCellX;
    }


    public void setTempCellY(int tempCellY) {
        this.tempCellY = tempCellY;
    }

    public static Bitmap getIconFromCursor(byte[] data,Context context) {
        try {
            return ItemInfoModel.createIconBitmap(
                    BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    public void setAppWidgetId(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public void setComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public AppWidgetProviderInfo getAppWidgetProviderInfo() {
        return appWidgetProviderInfo;
    }

    public void setAppWidgetProviderInfo(AppWidgetProviderInfo appWidgetProviderInfo) {
        this.appWidgetProviderInfo = appWidgetProviderInfo;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public int getScreen() {
        return screen;
    }
}