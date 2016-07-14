package id2.id2me.com.id2launcher.receivers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.FirstFragment;
import id2.id2me.com.id2launcher.general.Utility;
import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.general.AppsLoader;

/**
 * Created by bliss76 on 23/06/16.
 */
public class LauncherPackageChangeReceiver extends BroadcastReceiver
{
    private List<AppInfo> apps;
    private ArrayList<AppInfo> listAppInfo = new ArrayList<AppInfo>();
    private ArrayList<AppInfo> listDigitAppInfo = new ArrayList<AppInfo>();
    Activity activity;


    @Override
    public void onReceive(Context context, Intent intent) {
        String actionType = intent.getAction().toString();
        try {


            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentName = taskInfo.get(0).topActivity;

            String data = intent.getData().toString();
            String pName = data.substring(8);


            if (actionType.equalsIgnoreCase("android.intent.action.PACKAGE_ADDED")) {

                if(!checkAppUpdate(pName)) {
                    /*if (componentName.getClassName().contains("ArrangeApps")) {
                        apps = new AppsLoader(ArrangeApps.activity).getVisibleInstalledApps();
                        setApp(apps);
                        ((HomeActivity) (HomeActivity.activity)).refreshAdapter();*/
                        //((ArrangeApps) (ArrangeApps.activity)).refreshAdapter();
                   //  if (componentName.getClassName().contains("FirstFragment"))
                   {
                        apps = new AppsLoader().getVisibleInstalledApps((Activity) context);
                        setApp(apps);
                        FirstFragment.refreshAdapter();
                    }
                }
            }

            if (actionType.equalsIgnoreCase("android.intent.action.PACKAGE_REMOVED")) {


                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                Log.v("updated package :: ", pName);

                if (!isReplacing) {


                    /*if (componentName.getClassName().contains("FirsttFragment")) {
                        apps = new AppsLoader().getVisibleInstalledApps((Activity)context);
                        setApp(apps);
                        FirstFragment.refreshAdapter();


                    }else{*/
                         {
                            apps = new AppsLoader().getVisibleInstalledApps((Activity) context);
                            setApp(apps);
                            FirstFragment.refreshAdapter();

                        }


                   // }

                }

            }
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    boolean checkAppUpdate(String pName){

        for(int i=0;i<FirstFragment.appInfos.size();i++) {
            if (FirstFragment.appInfos.get(i).getPname().equalsIgnoreCase(pName)) {
                //Log.v("updated package :: return true ::" , pName);
                return true;
            }
        }

        return false;
    }


    /*
    If uninstall happens through settings
     */
    /*private int checkAppExistsInAnyCategory(String pname) {

        for (int i = 0; i < HomeActivity.categoryArry.size(); i++) {
            ArrayList<AppInfo> appInfos = HomeActivity.hashMapPlayStoreCategory
                    .get(HomeActivity.categoryArry.get(i));
            for (int j = 0; j < appInfos.size(); j++) {
                if (appInfos.get(j) != null) {
                    if (pname.equalsIgnoreCase(
                            appInfos.get(j).getPname())) {
                        if (!apps.contains(appInfos.get(j))) {

                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }*/

    private void setApp(List<AppInfo> apps) {

        FirstFragment.appInfos = (ArrayList)apps;
        FirstFragment.mNames.clear();

        listAppInfo = new ArrayList<AppInfo>();
        listDigitAppInfo = new ArrayList<AppInfo>();

        for (int i = 0; i < apps.size(); i++) {
            seperateCharNumApps(apps.get(i), listAppInfo,
                    listDigitAppInfo);
        }

        getSortedAllAppModeHaspMap(listAppInfo, listDigitAppInfo);

    }

    private void getSortedAllAppModeHaspMap(ArrayList<AppInfo> list,
                                            ArrayList<AppInfo> listDigitalAppInfo) {
        try {

            HashMap<String, ArrayList<AppInfo>> sortedMap = new HashMap<String, ArrayList<AppInfo>>();
            Collections.sort(list, new Comparator<AppInfo>() {
                public int compare(AppInfo o1, AppInfo o2) {
                    return o1.getAppname().compareTo(o2.getAppname());
                }
            });

            for (int i = 0; i < list.size(); i++) {
                String startIndex = list.get(i).getAppname().substring(0, 1)
                        .toUpperCase();
                if (startIndex.charAt(0) >= 'A' && startIndex.charAt(0) <= 'Z') {
                    if (sortedMap.containsKey(startIndex)) {
                        sortedMap.get(startIndex).add(list.get(i));
                    } else {
                        ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
                        sortedMap.put(startIndex, appInfoList);
                        appInfoList.add(list.get(i));

                    }
                }else {
                    listDigitalAppInfo.add(list.get(i));
                    FirstFragment.mNames.add(list.get(i).getAppname());
                }
            }

            Utility.sortedMap.clear();
            Utility.sortedMap.putAll(sortedMap);

            Utility.listDigitalAppInfo.clear();
            Utility.listDigitalAppInfo.addAll(listDigitalAppInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void seperateCharNumApps(AppInfo appInfo,
                                     ArrayList<AppInfo> listAppInfo, ArrayList<AppInfo> listDigitAppInfo) {
        try {
            if (Character.isDigit(appInfo.getAppname().charAt(0))) {

                listDigitAppInfo.add(appInfo);
                FirstFragment.mNames.add(appInfo.getAppname());


            } else {

                listAppInfo.add(appInfo);
                FirstFragment.mNames.add(appInfo.getAppname());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
