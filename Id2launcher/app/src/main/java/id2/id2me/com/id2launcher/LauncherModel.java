package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.listingviews.ListingContainerView;
import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.LauncherAppWidgetInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;
import timber.log.Timber;

/**
 * Created by bliss76 on 23/06/16.
 */
public class LauncherModel extends BroadcastReceiver {

    private final LauncherApplication mApp;
    private LoaderTask mLoaderTask;
    static final boolean DEBUG_LOADERS = false;
    static final String TAG = "LauncherModel";
    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    private static final Handler sWorker = new Handler();
    private DeferredHandler mHandler = new DeferredHandler();

    private WeakReference<Callbacks> mCallbacks;
    static {
        sWorkerThread.start();
    }
    // When we are loading pages synchronously, we can't just post the binding of items on the side
    // pages as this delays the rotation process.  Instead, we wait for a callback from the first
    // draw (in Workspace) to initiate the binding of the remaining side pages.  Any time we start
    // a normal load, we also clear this set of Runnables.
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();
    private final Object mLock = new Object();
    public AllAppsList mBgAllAppsList;
    public ArrayList<Object> mWidgetList;
    LauncherApplication launcherApplication;
    private boolean mStopped;
    private int mBatchSize; // 0 is all apps at once
    private int mAllAppsLoadDelay; // milliseconds between batches

    private IconCache mIconCache;
    private Bitmap mDefaultIcon;

    private RefreshAdapter refreshAdapter;

    public LauncherModel(IconCache iconCache) {

        mBgAllAppsList = new AllAppsList(iconCache);
        mWidgetList = new ArrayList<>();
        mIconCache = iconCache;
        mApp = LauncherApplication.getApp();
        mDefaultIcon = Utilities.createIconBitmap(
                mIconCache.getFullResDefaultActivityIcon(), mApp);
        Resources res = mApp.getResources();
        mAllAppsLoadDelay = res.getInteger(R.integer.config_allAppsBatchLoadDelay);
        mBatchSize = res.getInteger(R.integer.config_allAppsBatchSize);
    }

    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     */
    static int getCellLayoutChildId(
            long container, int screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | (screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }

    public void startLoader(boolean isLaunching, int synchronousBindPage) {
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching);
            }

            // Clear any deferred bind-runnables from the synchronized load process
            // We must do this before any loading/binding is scheduled below.
            mDeferredBindRunnables.clear();

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mApp, isLaunching);
                mLoaderTask.run();
            }
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }
    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
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

    public void setRefreshAdapter(RefreshAdapter refreshAdapter) {
        this.refreshAdapter = refreshAdapter;
    }


    public interface Callbacks {
        public boolean setLoadOnResume();
        public int getCurrentWorkspaceScreen();
        public void startBinding();
        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems();
        public void bindAppWidget(LauncherAppWidgetInfo info);
        public void bindAllApplications(ArrayList<AppInfo> apps);
        public void bindAppsAdded(ArrayList<AppInfo> apps);
        public void bindAppsUpdated(ArrayList<AppInfo> apps);
        public void bindAppsRemoved(ArrayList<String> packageNames, boolean permanent);
        public void bindPackagesUpdated();
        public boolean isAllAppsVisible();
        public boolean isAllAppsButtonRank(int rank);
        public void bindSearchablesChanged();
        public void onPageBoundSynchronously(int page);
    }

    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;

        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }

        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo(a);
            ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }


    }

    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
     class LoaderTask implements Runnable {
        private Context mContext;
        private boolean mIsLaunching;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;

        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
        }


        boolean isLaunching() {
            return mIsLaunching;
        }


        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }
        /**
         * Gets the callbacks object.  If we've been stopped, or if the launcher object
         * has somehow been garbage collected, return null instead.  Pass in the Callbacks
         * object that was around when the deferred message was scheduled, and if there's
         * a new Callbacks object around then also return null.  This will save us from
         * calling onto it with data that will be ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        private void loadAllAppsByBatch() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Timber.v("LoaderTask running with no launcher (loadAllAppsByBatch)");
                return;
            }

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> apps = null;

            int N = Integer.MAX_VALUE;

            int startIndex;
            int i = 0;
            int batchSize = -1;
            while (i < N && !mStopped) {
                if (i == 0) {
                    mBgAllAppsList.clear();
                    final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    apps = packageManager.queryIntentActivities(mainIntent, 0);
                    if (DEBUG_LOADERS) {
                        Timber.d(TAG, "queryIntentActivities took "
                                + (SystemClock.uptimeMillis() - qiaTime) + "ms");
                    }
                    if (apps == null) {
                        return;
                    }
                    N = apps.size();
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "queryIntentActivities got " + N + " apps");
                    }
                    if (N == 0) {
                        // There are no apps?!?
                        return;
                    }
                    if (mBatchSize == 0) {
                        batchSize = N;
                    } else {
                        batchSize = mBatchSize;
                    }

                    final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    Collections.sort(apps,
                            new LauncherModel.ShortcutNameComparator(packageManager, mLabelCache));
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "sort took "
                                + (SystemClock.uptimeMillis() - sortTime) + "ms");
                    }
                }

                final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                startIndex = i;
                for (int j = 0; i < N && j < batchSize; j++) {
                    // This builds the icon bitma®ps.
                    mBgAllAppsList.add(new AppInfo(packageManager, apps.get(i),
                            mIconCache, mLabelCache));
                    i++;
                }

                final boolean first = i <= batchSize;
                final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                final ArrayList<AppInfo> added = mBgAllAppsList.added;
                mBgAllAppsList.added = new ArrayList<AppInfo>();

                mHandler.post(new Runnable() {
                    public void run() {
                        final long t = SystemClock.uptimeMillis();
                        if (callbacks != null) {
                            if (first) {
                                callbacks.bindAllApplications(added);
                            } else {
                                callbacks.bindAppsAdded(added);
                            }
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "bound " + added.size() + " apps in "
                                        + (SystemClock.uptimeMillis() - t) + "ms");
                            }
                        } else {
                            Log.i(TAG, "not binding apps: no Launcher activity");
                        }
                    }
                });

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "batch of " + (i - startIndex) + " icons processed in "
                            + (SystemClock.uptimeMillis() - t2) + "ms");
                }

                if (mAllAppsLoadDelay > 0 && i < N) {
                    try {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                        }
                        Thread.sleep(mAllAppsLoadDelay);
                    } catch (InterruptedException exc) {
                    }
                }
            }

            if (DEBUG_LOADERS) {
                Log.d(TAG, "cached all " + N + " apps in "
                        + (SystemClock.uptimeMillis() - t) + "ms"
                        + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
            }
        }

        private void loadAllWidgetList(){

            PackageManager mPackageManager = mContext.getPackageManager();
            List<AppWidgetProviderInfo> widgets =
                    AppWidgetManager.getInstance(mContext).getInstalledProviders();
            Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            List<ResolveInfo> shortcuts = mPackageManager.queryIntentActivities(shortcutsIntent, 0);
            for (AppWidgetProviderInfo widget : widgets) {
                if (widget.minWidth > 0 && widget.minHeight > 0) {
                    // Ensure that all widgets we show can be added on a workspace of this size
                    int[] spanXY = Launcher.getSpanForWidget(mContext, widget);
                    int[] minSpanXY = Launcher.getMinSpanForWidget(mContext, widget);
                    int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
                    int minSpanY = Math.min(spanXY[1], minSpanXY[1]);
                    /*if (minSpanX <= LauncherModel.getCellCountX() &&
                            minSpanY <= LauncherModel.getCellCountY()) {
                        mWidgetList.add(widget);
                    } else {
                        Log.e(TAG, "Widget " + widget.provider + " can not fit on this device (" +
                                widget.minWidth + ", " + widget.minHeight + ")");
                    }*/
                    mWidgetList.add(widget);
                } else {
                    Log.e(TAG, "Widget " + widget.provider + " has invalid dimensions (" +
                            widget.minWidth + ", " + widget.minHeight + ")");
                }
            }

            mWidgetList.addAll(shortcuts);
            Collections.sort(mWidgetList,
                    new WidgetAndShortcutNameComparator(mPackageManager));
        }

        @Override
        public void run() {
            loadAllAppsByBatch();
            loadAllWidgetList();
        }
    }

        private class PackageUpdatedTask implements Runnable {
        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted
        int mOp;
        String[] mPackages;


        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {


            final String[] packages = mPackages;
            final int N = packages.length;

            ArrayList<AppInfo> added = null;
            ArrayList<AppInfo> modified = null;
            final ArrayList<String> removedPackageNames = new ArrayList<String>();

            switch (mOp) {
                case OP_ADD:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.addPackage " + packages[i]);
                        mBgAllAppsList.addPackage(mApp, packages[i]);
                    }

                    break;
                case OP_UPDATE:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.updatePackage " + packages[i]);
                        mBgAllAppsList.updatePackage(mApp, packages[i]);
                    }
                    if(refreshAdapter!=null)
                    refreshAdapter.appAdded();
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i = 0; i < N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.removePackage(packages[i]);
                    }
                    if(refreshAdapter!=null)
                    refreshAdapter.appRemoved();
                    break;
            }
            if (added != null) {
                sWorker.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //mCallbacks.();

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

                          // callbacks.bindAppsUpdated();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }

    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, String> mLabelCache;
        WidgetAndShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, String>();
            mCollator = Collator.getInstance();
        }
        public final int compare(Object a, Object b) {
            String labelA, labelB;
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
                labelA = (a instanceof AppWidgetProviderInfo) ?
                        ((AppWidgetProviderInfo) a).label :
                        ((ResolveInfo) a).loadLabel(mPackageManager).toString();
                mLabelCache.put(a, labelA);
            }
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                labelB = (b instanceof AppWidgetProviderInfo) ?
                        ((AppWidgetProviderInfo) b).label :
                        ((ResolveInfo) b).loadLabel(mPackageManager).toString();
                mLabelCache.put(b, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };


    ShortcutInfo infoFromShortcutIntent(Context context, Intent data, Bitmap fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Bitmap icon = null;
        boolean customIcon = false;
        Intent.ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof Intent.ShortcutIconResource) {
                try {
                    iconResource = (Intent.ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = Utilities.createIconBitmap(
                            mIconCache.getFullResIcon(resources, id), context);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
        }
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;

        return info;
    }

    private Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

}
