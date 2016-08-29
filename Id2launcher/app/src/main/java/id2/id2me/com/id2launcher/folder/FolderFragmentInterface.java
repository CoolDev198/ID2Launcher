package id2.id2me.com.id2launcher.folder;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.database.AppInfo;

/**
 * Created by bliss76 on 15/06/16.
 */
public interface FolderFragmentInterface {

    void addFolder(int target, AppInfo draggedAppInfo, AppInfo targetAppInfo);

    void updateFolder(int target, AppInfo
             draggedAppInfo);

    void updateFolderGridAdapter(int pos);

    void ChangeFolderToApp(int pos,AppInfo appInfo);

    void removeFolder(int target);
}
