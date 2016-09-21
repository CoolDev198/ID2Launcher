package id2.id2me.com.id2launcher.database;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.ItemInfo;

/**
 * Created by bliss105 on 14/07/16.
 */
public class FolderInfo {

    private int folderId;
    private String folderName = "";
    private int pageId;
    private ArrayList<ItemInfo> appInfos;

    public FolderInfo(ItemInfo dragPosApp, ItemInfo targetPosApp) {
        if (appInfos == null) {
            appInfos = new ArrayList<>();
        }
        appInfos.add(dragPosApp);
        appInfos.add(targetPosApp);
    }
    public FolderInfo(){

    }
    public void addNewItemInfo(ItemInfo dragPosApp){
        if (appInfos == null) {
            appInfos = new ArrayList<>();
        }
        appInfos.add(dragPosApp);
    }

    public ArrayList<ItemInfo> getAppInfos(){
        return appInfos;
    }
    public void setAppInfos(ArrayList<ItemInfo> appInfos){
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

    public void  deleteAppInfo(ApplicationInfo appInfo) {
        appInfos.remove(appInfo);
    }


    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }
}
