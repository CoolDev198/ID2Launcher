package id2.id2me.com.id2launcher.database;

import java.util.ArrayList;

/**
 * Created by bliss105 on 14/07/16.
 */
public class FolderInfo {

    private int folderId;
    private String folderName = "";
    private int pageId;
    private ArrayList<AppInfo> appInfos;

    public FolderInfo(AppInfo dragPosApp, AppInfo targetPosApp) {
        if (appInfos == null) {
            appInfos = new ArrayList<>();
        }
        appInfos.add(dragPosApp);
        appInfos.add(targetPosApp);
    }
    public FolderInfo(){

    }
    public void addNewAppInfo(AppInfo dragPosApp){
        if (appInfos == null) {
            appInfos = new ArrayList<>();
        }
        appInfos.add(dragPosApp);
    }

    public ArrayList<AppInfo> getAppInfos(){
        return appInfos;
    }
    public void setAppInfos(ArrayList<AppInfo> appInfos){
        this.appInfos=appInfos;
    }
    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getFolderId() {
        return folderId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public void  deleteAppInfo(AppInfo appInfo) {
        appInfos.remove(appInfo);
    }
}
