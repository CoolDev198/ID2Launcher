package id2.id2me.com.id2launcher.general;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.database.AppInfo;

/**
 * Created by Sunita on 27/05/16.
 */
public class AllAppsListManager
{

    public ArrayList<AppInfo> getVisibleInstalledApps(Context context) {

        WidgetsListManager widgetsListManager = new WidgetsListManager(context);
        PackageManager pm = ((Activity) context).getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        ArrayList<AppInfo> res = new ArrayList<AppInfo>();

        try {
            for (ResolveInfo rInfo : list) {

                AppInfo appInfo = new AppInfo();

                appInfo.setAppname(rInfo.activityInfo.loadLabel(pm)
                        .toString());
                appInfo.setIcon(rInfo.activityInfo.loadIcon(pm));
                appInfo.setPname(rInfo.activityInfo.packageName);
                appInfo.setClassName(rInfo.activityInfo.name);
                res.add(appInfo);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  res;
    }

        /* These functions should be to added here */

    // add package

    // remove package

    // update package

}
