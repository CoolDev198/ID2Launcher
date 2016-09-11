package id2.id2me.com.id2launcher.general;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id2.id2me.com.id2launcher.database.ApplicationInfo;

/*
 * Created by Sunita on 27/05/16.
 */

public class AllAppsList
{

    /** The list off all apps. */
    public ArrayList<ApplicationInfo> data =
            new ArrayList<ApplicationInfo>();

//    /** The list of apps that have been added since the last notify() call. */
//    public ArrayList<ApplicationInfo> added =
//            new ArrayList<ApplicationInfo>();
//    /** The list of apps that have been removed since the last notify() call. */
//    public ArrayList<ApplicationInfo> removed = new ArrayList<ApplicationInfo>();
//    /** The list of apps that have been modified since the last notify() call. */
//    public ArrayList<ApplicationInfo> modified = new ArrayList<ApplicationInfo>();

   // private IconCache mIconCache;


    public void clear() {
        data.clear();
        // TODO: do we clear these too?
////        added.clear();
////        removed.clear();
//        modified.clear();
    }


    public void getVisibleInstalledApps(Context context) {

        data.clear();

        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));

        try {

            for (ResolveInfo rInfo : list) {

                ApplicationInfo appInfo = new ApplicationInfo();
                appInfo.setAppname(rInfo.activityInfo.loadLabel(pm)
                        .toString().trim());
                appInfo.setIcon(rInfo.activityInfo.loadIcon(pm));
                appInfo.setPname(rInfo.activityInfo.packageName);
                appInfo.setClassName(rInfo.activityInfo.name);
                data.add(appInfo);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    /**
//     * Add the icons for the supplied apk called packageName.
//     */
//    public void addPackage(Context context, String packageName) {
//        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
//
//        if (matches.size() > 0) {
//            for (ResolveInfo info : matches) {
//                add(new ApplicationInfo(context.getPackageManager(), info, mIconCache, null));
//            }
//        }
//    }
//
//    /**
//     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
//     * list to broadcast when notify() is called.
//     *
//     * If the app is already in the list, doesn't add it.
//     */
//    public void add(ApplicationInfo info) {
//        if (findActivity(data, info.componentName)) {
//            return;
//        }
//        data.add(info);
//        added.add(info);
//    }
//
//
//    /**
//     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
//     */
//    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
//        final PackageManager packageManager = context.getPackageManager();
//
//        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        mainIntent.setPackage(packageName);
//
//        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
//        return apps != null ? apps : new ArrayList<ResolveInfo>();
//    }
//
//
//    /**
//     * Remove the apps for the given apk identified by packageName.
//     */
//    public void removePackage(String packageName) {
//        final List<android.content.pm.ApplicationInfo> data = this.data;
//        for (int i = data.size() - 1; i >= 0; i--) {
//            android.content.pm.ApplicationInfo info = data.get(i);
//            final ComponentName component = info.intent.getComponent();
//            if (packageName.equals(component.getPackageName())) {
//                removed.add(info);
//                data.remove(i);
//            }
//        }
//        // This is more aggressive than it needs to be.
//        mIconCache.flush();
//    }
//
//    /**
//     * Add and remove icons for this package which has been updated.
//     */
//    public void updatePackage(Context context, String packageName) {
//        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
//        if (matches.size() > 0) {
//            // Find disabled/removed activities and remove them from data and add them
//            // to the removed list.
//            for (int i = data.size() - 1; i >= 0; i--) {
//                final android.content.pm.ApplicationInfo applicationInfo = data.get(i);
//                final ComponentName component = applicationInfo.intent.getComponent();
//                if (packageName.equals(component.getPackageName())) {
//                    if (!findActivity(matches, component)) {
//                        removed.add(applicationInfo);
//                        mIconCache.remove(component);
//                        data.remove(i);
//                    }
//                }
//            }
//
//            // Find enabled activities and add them to the adapter
//            // Also updates existing activities with new labels/icons
//            int count = matches.size();
//            for (int i = 0; i < count; i++) {
//                final ResolveInfo info = matches.get(i);
//                android.content.pm.ApplicationInfo applicationInfo = findApplicationInfoLocked(
//                        info.activityInfo.applicationInfo.packageName,
//                        info.activityInfo.name);
//                if (applicationInfo == null) {
//                    add(new android.content.pm.ApplicationInfo(context.getPackageManager(), info, mIconCache, null));
//                } else {
////                    mIconCache.remove(applicationInfo.componentName);
////                    mIconCache.getTitleAndIcon(applicationInfo, info, null);
//                    modified.add(applicationInfo);
//                }
//            }
//        } else {
//            // Remove all data for this package.
//            for (int i = data.size() - 1; i >= 0; i--) {
//                final android.content.pm.ApplicationInfo applicationInfo = data.get(i);
//                final ComponentName component = applicationInfo.intent.getComponent();
//                if (packageName.equals(component.getPackageName())) {
//                    removed.add(applicationInfo);
//                  //  mIconCache.remove(component);
//                    data.remove(i);
//                }
//            }
//        }
//    }
//
//    /**
//     * Returns whether <em>apps</em> contains <em>component</em>.
//     */
//    private static boolean findActivity(ArrayList<ApplicationInfo> apps, ComponentName component) {
//        final int N = apps.size();
//        for (int i=0; i<N; i++) {
//            final ApplicationInfo info = apps.get(i);
//            if (info.getPname().equals(component)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Find an ApplicationInfo object for the given packageName and className.
//     */
//    private ApplicationInfo findApplicationInfoLocked(String packageName, String className) {
//        for (ApplicationInfo info: data) {
//            final ComponentName component = info.intent.getComponent();
//            if (packageName.equals(component.getPackageName())
//                    && className.equals(component.getClassName())) {
//                return info;
//            }
//        }
//        return null;
//    }
}
