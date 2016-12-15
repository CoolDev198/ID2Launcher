package id2.id2me.com.id2launcher.models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import id2.id2me.com.id2launcher.DatabaseHandler;

/**
 * Created by bliss76 on 27/05/16.
 */
public class AppInfoModel extends ItemInfoModel implements  Cloneable {
    private String appname = "";
    private String pname = "";
    private Bitmap icon;
    private String className = "";

    public AppInfoModel(){
        setSpanX(1);
        setSpanY(1);
        setItemType(DatabaseHandler.ITEM_TYPE_APP);
    }

    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch( CloneNotSupportedException e )
        {
            return null;
        }
    }

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

    public Bitmap getBitmapIcon() {
        return icon;
    }

    public void setBitmapIcon(Bitmap icon) {
        this.icon = icon;
        setIcon(writeBitmap(icon));
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
