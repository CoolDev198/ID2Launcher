package id2.id2me.com.id2launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id2.id2me.com.id2launcher.models.AppInfoModel;

/*
 * Created by Sunita on 27/05/16.
 */

public class AllAppsList {

    private static final Canvas sCanvas = new Canvas();
    static PackageManager pm;
    static List<ResolveInfo> list;

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    /**
     * The list off all apps.
     */
    public ArrayList<AppInfoModel> data =
            new ArrayList<AppInfoModel>();

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
//            if (sIconWidth == -1) {
//                initStatics(context);
//            }

            int width = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
            int height = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }

            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
            ;
            int textureHeight = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
            ;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth - width) / 2;
            final int top = (textureHeight - height) / 2;

            icon.setBounds(left, top, left + width, top + height);

            icon.draw(canvas);
            canvas.setBitmap(null);
            return bitmap;
        }
    }

    public void clear() {
        data.clear();
        // TODO: do we clear these too?
    }

    public void getVisibleInstalledApps(Context context) {

        data.clear();

        pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        list = pm.queryIntentActivities(intent, 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));

        try {

            for (ResolveInfo rInfo : list) {

                AppInfoModel appInfo = new AppInfoModel();
                appInfo.setAppname(rInfo.activityInfo.loadLabel(pm)
                        .toString().trim());
                appInfo.setBitmapIcon(createIconBitmap(rInfo.activityInfo.loadIcon(pm), context));
                appInfo.setPname(rInfo.activityInfo.packageName);
                appInfo.setClassName(rInfo.activityInfo.name);
                data.add(appInfo);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Drawable getIconForWidget(String packageName){
        Drawable icon = null;
        try {
            for (ResolveInfo rInfo : list) {
                if(rInfo.activityInfo.packageName.equalsIgnoreCase(packageName)){
                    icon = rInfo.loadIcon(pm);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return icon;
    }



//    /**
//     * Add the icons for the supplied apk called packageName.
//     */
//    public void addPackage(Context context, String packageName) {
//        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
//
//        if (matches.size() > 0) {
//            for (ResolveInfo info : matches) {
//                add(new AppInfoModel(context.getPackageManager(), info, mIconCache, null));
//            }
//        }
//    }
//
//    /**
//     * Add the supplied AppInfoModel objects to the list, and enqueue it into the
//     * list to broadcast when notify() is called.
//     *
//     * If the app is already in the list, doesn't add it.
//     */
//    public void add(AppInfoModel info) {
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
//        final List<android.content.pm.AppInfoModel> data = this.data;
//        for (int i = data.size() - 1; i >= 0; i--) {
//            android.content.pm.AppInfoModel info = data.get(i);
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
//                final android.content.pm.AppInfoModel applicationInfo = data.get(i);
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
//                android.content.pm.AppInfoModel applicationInfo = findApplicationInfoLocked(
//                        info.activityInfo.applicationInfo.packageName,
//                        info.activityInfo.name);
//                if (applicationInfo == null) {
//                    add(new android.content.pm.AppInfoModel(context.getPackageManager(), info, mIconCache, null));
//                } else {
////                    mIconCache.remove(applicationInfo.componentName);
////                    mIconCache.getTitleAndIcon(applicationInfo, info, null);
//                    modified.add(applicationInfo);
//                }
//            }
//        } else {
//            // Remove all data for this package.
//            for (int i = data.size() - 1; i >= 0; i--) {
//                final android.content.pm.AppInfoModel applicationInfo = data.get(i);
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
//    private static boolean findActivity(ArrayList<AppInfoModel> apps, ComponentName component) {
//        final int N = apps.size();
//        for (int i=0; i<N; i++) {
//            final AppInfoModel info = apps.get(i);
//            if (info.getPname().equals(component)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Find an AppInfoModel object for the given packageName and className.
//     */
//    private AppInfoModel findApplicationInfoLocked(String packageName, String className) {
//        for (AppInfoModel info: data) {
//            final ComponentName component = info.intent.getComponent();
//            if (packageName.equals(component.getPackageName())
//                    && className.equals(component.getClassName())) {
//                return info;
//            }
//        }
//        return null;
//    }


}
