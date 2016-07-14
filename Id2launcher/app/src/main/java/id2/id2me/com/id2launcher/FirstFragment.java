package id2.id2me.com.id2launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import id2.id2me.com.id2launcher.adapters.HorizontalPagerAdapter;
import id2.id2me.com.id2launcher.folder.FolderFragmentInterface;
import id2.id2me.com.id2launcher.drawer.DrawerHandler;
import id2.id2me.com.id2launcher.drawer.DrawerListAdapter;
import id2.id2me.com.id2launcher.drawer.MyDrawerListener;
import id2.id2me.com.id2launcher.drawer.OverlayView;
import id2.id2me.com.id2launcher.drawer.SideBar;
import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.general.AppGridView;
import id2.id2me.com.id2launcher.general.AppsLoader;
import id2.id2me.com.id2launcher.general.Utility;

/**
 * Created by bliss76 on 26/05/16.
 */
public class FirstFragment extends Fragment implements DrawerHandler {
    public static Activity getActivity;
    final int NO_OF_APPS_IN_ROW = 4;
    public static ArrayList<AppInfo> appInfos;
    ArrayList arrayList_collection, arrayList_id, arrayList_folderNames;
    int noOfApps = 0;
    RelativeLayout relativeLayout;

    DekstopGridAdapater gridAdapater;
    public static DrawerListAdapter adapter;
    private ListView navList;
    DrawerLayout drawer;
    private SideBar indexBar;
    private TextView overlayTextView;
    private HashMap<String, ArrayList<AppInfo>> sortedMap;
    public static ArrayList<String> mNames;

    static FolderFragmentInterface folderFragmentInterface;
    View fragmentView = null;
    AppGridView appGridView;

    public static FirstFragment newInstance() {
        FirstFragment f = new FirstFragment();
        return f;
    }

    public static void setListner(HorizontalPagerAdapter listner) {
        FirstFragment.folderFragmentInterface = listner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {

            loadApps();


            if (arrayList_collection == null) {
                arrayList_collection = new ArrayList();
                arrayList_id = new ArrayList();
                arrayList_folderNames = new ArrayList();
                getOriginalHashmap();
            }

            fragmentView = inflater.inflate(R.layout.first_fragment, container, false);
            relativeLayout = (RelativeLayout) fragmentView.findViewById(R.id.relative_view);
            appGridView = (AppGridView) fragmentView.findViewById(R.id.mygridview);
            initViews(getActivity());
            noOfApps = appInfos.size();
            setColumnWidth(appGridView);
            setNoOfColumnsOfGrid(appGridView);
            setGridViewTotalHeight(noOfApps, appGridView);
            gridAdapater = new DekstopGridAdapater(getActivity(), arrayList_collection, arrayList_id, relativeLayout, appGridView, appInfos, arrayList_folderNames, folderFragmentInterface);
            appGridView.setAdapter(gridAdapater);
            drawerAppsListing();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return fragmentView;
    }

    private void initViews(Activity activity) {
        drawer = (DrawerLayout) fragmentView.findViewById(R.id.drawer_layout);
        if(drawer !=null) {
            drawer.setDrawerListener(new MyDrawerListener(this, activity, drawer));
        }
    }

    private void loadApps() {
        try {

            this.appInfos = new AppsLoader().getVisibleInstalledApps(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOriginalHashmap() {
        for (int i = 0; i < appInfos.size(); i++) {

            ArrayList values = new ArrayList();
            values.add(appInfos.get(i));
            arrayList_collection.add(values);
            arrayList_id.add("app");
            arrayList_folderNames.add("id2/id2me/com/id2launcher/folder");

        }
    }


    void setGridViewTotalHeight(int noOfApps, GridView gridView) {
        try {
            int noOfRows = 0;

            if (noOfApps % 3 == 0) {
                noOfRows = noOfApps / 3;
            } else {
                noOfRows = (noOfApps / 3) + 1;
            }
            Log.v("rows", noOfRows + " " + noOfApps);
            int totalHeight = (noOfRows)
                    * (int) getActivity().getResources().getDimension(
                    R.dimen.gridview_height);
            Log.v("Total Height", "" + totalHeight);
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = totalHeight;
            gridView.setLayoutParams(params);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }

    public void drawerAppsListing() {
        mNames = new ArrayList<>();
        ArrayList<AppInfo> listAppInfo = new ArrayList<AppInfo>();
        ArrayList<AppInfo> listDigitAppInfo = new ArrayList<AppInfo>();
        for (int i = 0; i < appInfos.size(); i++) {
            seperateCharNumApps(appInfos.get(i), listAppInfo,
                    listDigitAppInfo);
        }
        getSortedAllAppModeHaspMap(listAppInfo, listDigitAppInfo);

    }

    @SuppressLint("DefaultLocale")
    private void getSortedAllAppModeHaspMap(ArrayList<AppInfo> list,
                                            ArrayList<AppInfo> listDigitalAppInfo) {
        try {
            sortedMap = new HashMap<String, ArrayList<AppInfo>>();
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
                } /*else {
                    listDigitalAppInfo.add(list.get(i));
                    mNames.add(list.get(i).getAppname());
                }*/

            }




           Log.v("SortedMap", "" + sortedMap);
            Utility.sortedMap = sortedMap;
            Utility.listDigitalAppInfo = listDigitalAppInfo;

            setListAdapter(sortedMap, listDigitalAppInfo);


} catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setListAdapter(HashMap<String, ArrayList<AppInfo>> sortedMap,
                                ArrayList<AppInfo> listDigitAppInfo) {
        try {
            setDrawerWidth();
            navList = (ListView)fragmentView.findViewById(R.id.drawer);
            navList.setClickable(false);
            adapter = new DrawerListAdapter(getActivity(), sortedMap, listDigitAppInfo,appGridView, drawer,arrayList_collection);
            navList.setAdapter(adapter);
            setAdapterToSideBarIndex(navList);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public  static void refreshAdapter() {
        try {
          //  adapter.refresh();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdapterToSideBarIndex(ListView navList) {
        try {
            indexBar = (SideBar)fragmentView. findViewById(R.id.side_index);
            adapter.setSideBar(indexBar);
            indexBar.setListView(navList);
            indexBar.setDrawer(drawer);
            if (overlayTextView == null) {
                WindowManager windowManager = (WindowManager) getActivity()
                        .getSystemService(Context.WINDOW_SERVICE);
                overlayTextView = (TextView) OverlayView.initOverlay(getActivity().getLayoutInflater(), windowManager);
                overlayTextView.setVisibility(View.INVISIBLE);
                indexBar.setWindowManager(windowManager);
                indexBar.setTextView(overlayTextView);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
    }

    private void setDrawerWidth() {
        try {
            RelativeLayout leftDrawer = (RelativeLayout) fragmentView.findViewById(R.id.left_drawer_layout);
            ViewGroup.LayoutParams params = leftDrawer.getLayoutParams();
            params.width = getScreenWidth(getActivity()) - 80;
            leftDrawer.setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int getScreenWidth(Activity a) {
        Display display = a.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;

    }

    private void seperateCharNumApps(AppInfo appInfo,
                                     ArrayList<AppInfo> listAppInfo, ArrayList<AppInfo> listDigitAppInfo) {
        try {
            if (Character.isDigit(appInfo.getAppname().charAt(0))) {
                listDigitAppInfo.add(appInfo);
                Log.v("ListDigit",""+listDigitAppInfo.add(appInfo));

                mNames.add(appInfo.getAppname());

            } else {
                listAppInfo.add(appInfo);
                mNames.add(appInfo.getAppname());


            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    @Override
    public void drawerOpen()
    {
        drawer.openDrawer(Gravity.LEFT);

        if (adapter != null) {
       //     adapter.notifyDataSetInvalidated();
        }

    }

    @Override
    public void drawerClose() {

        drawer.closeDrawer(Gravity.LEFT);
    }


}





