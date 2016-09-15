package id2.id2me.com.id2launcher;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemInfo {
    static final int NO_ID = -1; /*The id in the settings database for this item */
    long id = NO_ID;
    private int container = NO_ID;
    private int cellX = NO_ID;
    private int cellY = NO_ID;
    private byte[] icon;
    private String intent;
    private int spanX = 1;
    private int spanY = 1;
    private int appWidgetId;
    private String title;
    private int itemType = DatabaseHandler.ITEM_TYPE_APP;
    private int iconType;
    private String pname;

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

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(DatabaseHandler.COLUMN_ICON, data);
        }
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

    public int getCellY() {
        return cellY;
    }

    public byte[] getIcon() {
        return icon;
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
}