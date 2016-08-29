package id2.id2me.com.id2launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.drawer.AllAppsListAdapter;

/**
 * Created by sunita on 8/2/16.
 */
public class AppsListingFragment extends Fragment {
    private static ArrayList<AppInfo> appInfos;
    private ListView navList;
    private AllAppsListAdapter adapter;
    private static DrawerLayout drawer;
    private ArrayList<String> mNames;
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
            setListAdapter(appInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getSortedAllAppModeHaspMap(ArrayList<AppInfo> list) {
        try {
            Collections.sort(list, new Comparator<AppInfo>() {
                public int compare(AppInfo o1, AppInfo o2) {
                    return o1.getAppname().toString().trim().toUpperCase().compareTo(o2.getAppname().toString().trim().toUpperCase());
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setListAdapter(ArrayList<AppInfo> list) {
        try {
            navList = (ListView) fragmentView.findViewById(R.id.list);
            navList.setClickable(false);
            adapter = new AllAppsListAdapter(getActivity(), list,drawer);
            navList.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





}
