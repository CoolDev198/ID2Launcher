package id2.id2me.com.id2launcher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.AppInfoModel;

/**
 * Created by bliss76 on 23/06/16.
 */
public class LauncherModel extends BroadcastReceiver {

    static final boolean DEBUG_LOADERS = false;
    static final String TAG = "LauncherModel";
    LauncherApplication launcherApplication;
    public AllAppsList mBgAllAppsList;
    private final Object mLock = new Object();
    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    private Callbacks callbacks;

    //private WeakReference<Callbacks> mCallbacks;
    static {
        sWorkerThread.start();
    }

    public void initialize(Callbacks callbacks) {
        this.callbacks = callbacks;

//        synchronized (mLock) {
//            mCallbacks = new WeakReference<Callbacks>(callbacks);
//        }
    }

    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        return  null;
    }

    public static int getCellLayoutChildId(long container, int screen, int x, int y, int spanX, int spanY) {
        return 0;
    }

    public interface Callbacks {
        //        public boolean setLoadOnResume();
//        public int getCurrentWorkspaceScreen();
//        public void startBinding();
//        public void bindItems(ArrayList<ItemInfoModel> shortcuts, int start, int end);
//        public void bindFolders(HashMap<Long,FolderInfoModel> folders);
//        public void finishBindingItems();
//        public void bindAppWidget(LauncherAppWidgetInfo info);
//        public void bindAllApplications(ArrayList<AppInfoModel> apps);
        public void bindAppsAdded();

        public void bindAppsUpdated();

        public void bindAppsRemoved(ArrayList<String> packageNames);
//        public void bindPackagesUpdated();
//        public boolean isAllAppsVisible();
//        public boolean isAllAppsButtonRank(int rank);
//        public void bindSearchablesChanged();
//        public void onPageBoundSynchronously(int page);
    }

    private static final Handler sWorker = new Handler();

    public LauncherModel(LauncherApplication launcherApplication) {
        this.launcherApplication = launcherApplication;
        mBgAllAppsList = new AllAppsList();
        mBgAllAppsList.getVisibleInstalledApps(launcherApplication);

    }


    void enqueuePackageUpdated(PackageUpdatedTask task) {
        sWorker.post(task);
    }


    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_LOADERS) Log.d(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }

            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[]{packageName}));
            }
        }
    }


    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted


        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = launcherApplication;

            final String[] packages = mPackages;
            final int N = packages.length;

            ArrayList<AppInfoModel> added = null;
            ArrayList<AppInfoModel> modified = null;
            final ArrayList<String> removedPackageNames = new ArrayList<String>();

            switch (mOp) {
                case OP_ADD:
                    added = new ArrayList<AppInfoModel>();
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS) Log.v(TAG, "mAllAppsList.addPackage " + packages[i]);
                        mBgAllAppsList.getVisibleInstalledApps(context);
                    }

                    break;
                case OP_UPDATE:
                    modified = new ArrayList<AppInfoModel>();
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS) Log.v(TAG, "mAllAppsList.updatePackage " + packages[i]);
                        mBgAllAppsList.getVisibleInstalledApps(context);
                    }

                    break;
                case OP_REMOVE:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS) Log.v(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.getVisibleInstalledApps(context);
                    }


                    break;
                case OP_UNAVAILABLE:

                    break;
            }

            if (added != null) {
                sWorker.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            callbacks.bindAppsAdded();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            if (modified != null) {
                sWorker.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            callbacks.bindAppsUpdated();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }


}
