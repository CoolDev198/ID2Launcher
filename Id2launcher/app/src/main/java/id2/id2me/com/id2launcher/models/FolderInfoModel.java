package id2.id2me.com.id2launcher.models;

import java.util.ArrayList;

/**
 * Created by bliss105 on 14/07/16.
 */
public class FolderInfoModel extends ItemInfoModel{

    private int folderId;
    private String folderName = "";
    private int pageId;
    private ArrayList<ItemInfoModel> appInfos;

    public FolderInfoModel(ItemInfoModel dragPosApp, ItemInfoModel targetPosApp) {
        if (appInfos == null) {
            appInfos = new ArrayList<>();
        }
        appInfos.add(dragPosApp);
        appInfos.add(targetPosApp);
    }
    public FolderInfoModel(){

    }
    public void addNewItemInfo(ItemInfoModel dragPosApp){
        if (appInfos == null) {
            appInfos = new ArrayList<>();
        }
        appInfos.add(dragPosApp);
    }

    public ArrayList<ItemInfoModel> getAppInfos(){
        return appInfos;
    }
    public void setAppInfos(ArrayList<ItemInfoModel> appInfos){
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

    public void  deleteAppInfo(AppInfoModel appInfo) {
        appInfos.remove(appInfo);
    }


    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }
}
