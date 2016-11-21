package id2.id2me.com.id2launcher.models;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.graphics.drawable.Drawable;

import id2.id2me.com.id2launcher.DatabaseHandler;

/**
 * Created by sunita on 8/3/16.
 */
public class WidgetInfoModel extends ItemInfoModel{
    private String widgetName = "";
    private String pname = "";
    private int preview;
    private String className = "";
    private int pageId;
    private int minSpanX;
    private int minSpanY;

    public WidgetInfoModel(){
        setDropExternal(true);
        setIsItemCanPlaced(false);
        setItemType(DatabaseHandler.ITEM_TYPE_APPWIDGET);
    }
    public String getWidgetName() {
        return widgetName;
    }

    public void setWidgetName(String widgetName) {
        this.widgetName = widgetName;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getPreview() {
        return preview;
    }

    public void setPreview(int preview) {
        this.preview = preview;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMinSpanX(int minSpanX) {
        this.minSpanX = minSpanX;
    }

    public void setMinSpanY(int minSpanY) {
        this.minSpanY = minSpanY;
    }

    public int getMinSpanX() {
        return minSpanX;
    }

    public int getMinSpanY() {
        return minSpanY;
    }


}
