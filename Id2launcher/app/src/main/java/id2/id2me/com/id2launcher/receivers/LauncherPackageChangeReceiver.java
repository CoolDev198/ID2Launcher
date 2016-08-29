package id2.id2me.com.id2launcher.receivers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.database.AppInfo;

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

//                if(!checkAppUpdate(pName)) {
//                    /*if (componentName.getClassName().contains("ArrangeApps")) {
//                        apps = new AllAppsListManager(ArrangeApps.activity).getVisibleInstalledApps();
//                        setApp(apps);
//                        ((Launcher) (Launcher.activity)).refreshAdapter();*/
//                        //((ArrangeApps) (ArrangeApps.activity)).refreshAdapter();
//                   //  if (componentName.getClassName().contains("DesktopFragment"))
//                   {
//                        apps = new AllAppsListManager().getVisibleInstalledApps((Activity) context);
//                        setApp(apps);
//                        //DesktopFragment.refreshAdapter();
//                    }
//                }
            }

            if (actionType.equalsIgnoreCase("android.intent.action.PACKAGE_REMOVED")) {


                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                Log.v("updated package :: ", pName);

                if (!isReplacing) {


                    /*if (componentName.getClassName().contains("FirsttFragment")) {
                        apps = new AllAppsListManager().getVisibleInstalledApps((Activity)context);
                        setApp(apps);
                        DesktopFragment.refreshAdapter();


                    }else{*/
                         {
//                            apps = new AllAppsListManager().getVisibleInstalledApps((Activity) context);
//                            setApp(apps);
                           // DesktopFragment.refreshAdapter();

                        }


                   // }

                }

            }
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

//    boolean checkAppUpdate(String pName){
//
//        for(int i=0;i< DesktopFragment.appInfos.size();i++) {
//            if (DesktopFragment.appInfos.get(i).getPname().equalsIgnoreCase(pName)) {
//                //Log.v("updated package :: return true ::" , pName);
//                return true;
//            }
//        }
//
//        return false;
//    }


    /*
    If uninstall happens through settings
     */
    /*private int checkAppExistsInAnyCategory(String pname) {

        for (int i = 0; i < Launcher.categoryArry.size(); i++) {
            ArrayList<AppInfo> appInfos = Launcher.hashMapPlayStoreCategory
                    .get(Launcher.categoryArry.get(i));
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

//    private void setApp(List<AppInfo> apps) {
//
//        DesktopFragment.appInfos = (ArrayList)apps;
//        DesktopFragment.mNames.clear();
//
//        listAppInfo = new ArrayList<AppInfo>();
//        listDigitAppInfo = new ArrayList<AppInfo>();
//
//        for (int i = 0; i < apps.size(); i++) {
//            seperateCharNumApps(apps.get(i), listAppInfo,
//                    listDigitAppInfo);
//        }
//
//        getSortedAllAppModeHaspMap(listAppInfo, listDigitAppInfo);
//
//    }
//
//    private void getSortedAllAppModeHaspMap(ArrayList<AppInfo> list,
//                                            ArrayList<AppInfo> listDigitalAppInfo) {
//        try {
//
//            HashMap<String, ArrayList<AppInfo>> sortedMap = new HashMap<String, ArrayList<AppInfo>>();
//            Collections.sort(list, new Comparator<AppInfo>() {
//                public int compare(AppInfo o1, AppInfo o2) {
//                    return o1.getWidgetName().compareTo(o2.getWidgetName());
//                }
//            });
//
//            for (int i = 0; i < list.size(); i++) {
//                String startIndex = list.get(i).getWidgetName().substring(0, 1)
//                        .toUpperCase();
//                if (startIndex.charAt(0) >= 'A' && startIndex.charAt(0) <= 'Z') {
//                    if (sortedMap.containsKey(startIndex)) {
//                        sortedMap.get(startIndex).add(list.get(i));
//                    } else {
//                        ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
//                        sortedMap.put(startIndex, appInfoList);
//                        appInfoList.add(list.get(i));
//
//                    }
//                }else {
//                    listDigitalAppInfo.add(list.get(i));
//                    DesktopFragment.mNames.add(list.get(i).getWidgetName());
//                }
//            }
//
//            Utility.sortedMap.clear();
//            Utility.sortedMap.putAll(sortedMap);
//
//            Utility.listDigitalAppInfo.clear();
//            Utility.listDigitalAppInfo.addAll(listDigitalAppInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

//    private void seperateCharNumApps(AppInfo appInfo,
//                                     ArrayList<AppInfo> listAppInfo, ArrayList<AppInfo> listDigitAppInfo) {
//        try {
//            if (Character.isDigit(appInfo.getWidgetName().charAt(0))) {
//
//                listDigitAppInfo.add(appInfo);
//                DesktopFragment.mNames.add(appInfo.getWidgetName());
//
//
//            } else {
//
//                listAppInfo.add(appInfo);
//                DesktopFragment.mNames.add(appInfo.getWidgetName());
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
