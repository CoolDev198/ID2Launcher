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

import java.util.ArrayList;

/**
 * Created by sunita on 8/2/16.
 */
public class AppsListingFragment extends Fragment {
    private ListView navList;
    private AllAppsListAdapter adapter;
    private static DrawerLayout drawer;
    private View fragmentView;


    Context context;
    private LauncherApplication launcherApplication;


    public static AppsListingFragment newInstance(DrawerLayout drawerLayout) {
        drawer = drawerLayout;

        AppsListingFragment f = new AppsListingFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        launcherApplication = (LauncherApplication) getActivity().getApplication();
        fragmentView = inflater.inflate(R.layout.apps_listing_fragment, container, false);
        drawerAppsListing();
        return fragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void drawerAppsListing() {
        try {
            setListAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void seperateCharNumApps() {
        try {
            ArrayList<ApplicationInfo> appInfos = launcherApplication.mModel.mBgAllAppsList.data;

            ArrayList<ApplicationInfo> listDigitAppInfo = new ArrayList<>();
            ArrayList<ApplicationInfo> listAppInfo = new ArrayList<>();
            for (int i = 0; i < appInfos.size(); i++) {
                char ch = appInfos.get(i).getAppname().toString().charAt(0);
                if (ch >= 'A' && ch <= 'Z') {
                    listAppInfo.add(appInfos.get(i));
                } else {
                    if (ch >= 'a' && ch <= 'z') {
                        listAppInfo.add(appInfos.get(i));
                    } else {
                        listDigitAppInfo.add(appInfos.get(i));
                    }

                }
            }

            appInfos.clear();

            for (int i = 0; i < listDigitAppInfo.size(); i++) {
                appInfos.add(listDigitAppInfo.get(i));
            }

            for (int i = 0; i < listAppInfo.size(); i++) {
                appInfos.add(listAppInfo.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setListAdapter() {
        try {
            seperateCharNumApps();
            navList = (ListView) fragmentView.findViewById(R.id.list);
            navList.setClickable(false);
            adapter = new AllAppsListAdapter(getActivity(), drawer);
            navList.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
