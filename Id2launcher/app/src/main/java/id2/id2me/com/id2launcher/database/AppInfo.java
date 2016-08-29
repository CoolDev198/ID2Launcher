package id2.id2me.com.id2launcher.database;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by bliss76 on 27/05/16.
 */
public class AppInfo {
    private String appname = "";
    private String pname = "";
    private Drawable icon;
    private String className = "";
    private int pageId;
    private int folderId;

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
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


    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
