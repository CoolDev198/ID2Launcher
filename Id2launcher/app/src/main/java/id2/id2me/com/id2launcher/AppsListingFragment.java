package id2.id2me.com.id2launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.models.AppInfoModel;

/**
 * Created by sunita on 8/2/16.
 */
public class AppsListingFragment extends Fragment {
    private static DrawerLayout drawer;
    Context context;
    private ListView navList;
    private AllAppAdapter adapter;
    private View fragmentView;
    private LauncherApplication launcherApplication;
    private RecyclerView recyclerView;


    public static AppsListingFragment newInstance() {
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
            ArrayList<AppInfoModel> appInfos = launcherApplication.mModel.mBgAllAppsList.data;

            ArrayList<AppInfoModel> listDigitAppInfo = new ArrayList<>();
            ArrayList<AppInfoModel> listAppInfo = new ArrayList<>();
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

            recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
            //adapter = new AllAppsListAdapter(getActivity(), drawer);
            adapter = new AllAppAdapter(getActivity(), drawer);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            final RecyclerViewFastScroller fastScroller = (RecyclerViewFastScroller) fragmentView.findViewById(R.id.fastscroller);
            fastScroller.setRecyclerView(recyclerView);
            fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fastscroller_bubble,
                    R.id.fastscroller_handle);
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
