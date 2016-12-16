package id2.id2me.com.id2launcher.models;

import android.content.Intent;
import android.graphics.Bitmap;

import id2.id2me.com.id2launcher.DatabaseHandler;

/**
 * Created by bliss76 on 27/05/16.
 */
public class AppInfo extends ItemInfo implements  Cloneable {
    private String appname = "";
    private String pname = "";
    private Bitmap icon;
    /**
     * The intent used to start the application.
     */
    public Intent intent;

    public AppInfo(){
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
        catch(CloneNotSupportedException e )
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

}
