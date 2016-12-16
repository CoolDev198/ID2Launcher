package id2.id2me.com.id2launcher.models;

import java.util.ArrayList;

/**
 * Created by bliss105 on 14/07/16.
 */
public class FolderInfo extends ItemInfo {

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
