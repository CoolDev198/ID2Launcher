package id2.id2me.com.id2launcher.listingviews;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.adapters.AllAppAdapter;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.models.AppInfo;

/**
 * Created by sunita on 8/2/16.
 */
public class AppsListingView extends ListingContainerView {

    private ArrayList<AppInfo> appInfos;
    private RecyclerView recyclerView;
    private AllAppAdapter adapter;

    public AppsListingView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

       // drawerAppsListing();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new AllAppAdapter(this);
        recyclerView.setAdapter(adapter);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final RecyclerViewFastScroller fastScroller = (RecyclerViewFastScroller) findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller, R.id.fastscroller_bubble,
                R.id.fastscroller_handle);
    }

//    private void drawerAppsListing() {
//        try {
//            setListAdapter();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void seperateCharNumApps() {
//        try {
//            ArrayList<AppInfo> listDigitAppInfo = new ArrayList<>();
//            ArrayList<AppInfo> listAppInfo = new ArrayList<>();
//            for (int i = 0; i < appInfos.size(); i++) {
//                char ch = appInfos.get(i).title.toString().charAt(0);
//                if (ch >= 'A' && ch <= 'Z') {
//                    listAppInfo.add(appInfos.get(i));
//                } else {
//                    if (ch >= 'a' && ch <= 'z') {
//                        listAppInfo.add(appInfos.get(i));
//                    } else {
//                        listDigitAppInfo.add(appInfos.get(i));
//                    }
//
//                }
//            }
//
//            appInfos.clear();
//
//            for (int i = 0; i < listDigitAppInfo.size(); i++) {
//                appInfos.add(listDigitAppInfo.get(i));
//            }
//
//            for (int i = 0; i < listAppInfo.size(); i++) {
//                appInfos.add(listAppInfo.get(i));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void setListAdapter() {
//        try {
//            seperateCharNumApps();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }


}
