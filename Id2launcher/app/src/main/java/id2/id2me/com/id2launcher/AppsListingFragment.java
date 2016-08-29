package id2.id2me.com.id2launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.drawer.AllAppsListAdapter;
import id2.id2me.com.id2launcher.drawer.OverlayView;
import id2.id2me.com.id2launcher.drawer.SideBar;

/**
 * Created by sunita on 8/2/16.
 */
public class AppsListingFragment extends Fragment {
    private static ArrayList<AppInfo> appInfos;
    private ListView navList;
    private SideBar indexBar;
    private AllAppsListAdapter adapter;
    private static DrawerLayout drawer;
    private ArrayList<String> mNames;
    private TextView overlayTextView;
    private View fragmentView;


    Context context;


    public static AppsListingFragment newInstance(DrawerLayout drawerLayout, ArrayList<AppInfo> appInfo) {
        drawer=drawerLayout;
        appInfos=appInfo;
        AppsListingFragment f = new AppsListingFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.apps_listing_fragment, container, false);
        drawerAppsListing();
        return fragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    public void drawerAppsListing() {
        try {
            mNames = new ArrayList<>();
            ArrayList<AppInfo> listAppInfo = new ArrayList<AppInfo>();
            ArrayList<AppInfo> listDigitAppInfo = new ArrayList<AppInfo>();
            for (int i = 0; i < appInfos.size(); i++) {
                seperateCharNumApps(appInfos.get(i), listAppInfo,
                        listDigitAppInfo);
            }
            getSortedAllAppModeHaspMap(listAppInfo, listDigitAppInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                }
            }

//            Utility.sortedMap = sortedMap;
//            Utility.listDigitalAppInfo = listDigitalAppInfo;

            setListAdapter(sortedMap, listDigitalAppInfo);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setListAdapter(HashMap<String, ArrayList<AppInfo>> sortedMap,
                                ArrayList<AppInfo> listDigitAppInfo) {
        try {
            navList = (ListView) fragmentView.findViewById(R.id.list);
            navList.setClickable(false);
            adapter = new AllAppsListAdapter(getActivity(), sortedMap, listDigitAppInfo, drawer);
            navList.setAdapter(adapter);
            setAdapterToSideBarIndex(navList);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setAdapterToSideBarIndex(ListView navList) {
        try {
            indexBar = (SideBar) fragmentView.findViewById(R.id.side_index);
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



    private void seperateCharNumApps(AppInfo appInfo,
                                     ArrayList<AppInfo> listAppInfo, ArrayList<AppInfo> listDigitAppInfo) {
        try {
            if (Character.isDigit(appInfo.getAppname().charAt(0))) {
                listDigitAppInfo.add(appInfo);
                mNames.add(appInfo.getAppname());
            } else {
                listAppInfo.add(appInfo);
                mNames.add(appInfo.getAppname());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
