package id2.id2me.com.id2launcher.database;
/**
 * Created by bliss105 on 14/07/16.
 */
public class FolderInfo {

    private int folderId;
    private String folderName = "";
    private int pageId;

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

}
