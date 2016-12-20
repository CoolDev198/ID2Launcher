package id2.id2me.com.id2launcher.models;

/**
 * Created by apple on 23/02/16.
 */
public class NotificationWidgetInfo {

    private String pName;
    private String appName;
    private String appImageName; //Resource Identifier

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int count;

    public String getPname() {
        return pName;
    }

    public void setPname(String pName) {
        this.pName = pName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppImageName() {
        return appImageName;
    }

    public void setAppImageName(String appImageName) {
        this.appImageName = appImageName;
    }
}

