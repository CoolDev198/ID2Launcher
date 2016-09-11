package id2.id2me.com.id2launcher.folder;

import id2.id2me.com.id2launcher.database.ApplicationInfo;

/**
 * Created by bliss76 on 15/06/16.
 */
public interface FolderFragmentInterface {

    void addFolder(int target, ApplicationInfo draggedAppInfo, ApplicationInfo targetAppInfo);

    void updateFolder(int target, ApplicationInfo
             draggedAppInfo);

    void updateFolderGridAdapter(int pos);

    void ChangeFolderToApp(int pos,ApplicationInfo appInfo);

    void removeFolder(int target);
}
