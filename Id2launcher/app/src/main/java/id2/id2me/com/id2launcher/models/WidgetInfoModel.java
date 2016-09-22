package id2.id2me.com.id2launcher.models;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.graphics.drawable.Drawable;

/**
 * Created by sunita on 8/3/16.
 */
public class WidgetInfoModel {
    private String widgetName = "";
    private String pname = "";
    private int preview;
    private ComponentName componentName;
    private String className = "";
    private int pageId;
    private int spanX;
    private int spanY;
    private int minSpanX;
    private int minSpanY;
    private AppWidgetProviderInfo appWidgetProviderInfo;

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

    public void setSpanX(int x) {
        this.spanX = x;
    }

    public int getSpanX() {
        return spanX;
    }

    public void setSpanY(int y) {
        this.spanY = y;
    }

    public int getSpanY() {
        return spanY;
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
}
