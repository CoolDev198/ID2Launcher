package id2.id2me.com.id2launcher.listingviews;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.AllAppAdapter;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.ListingContainerView;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.RefreshAdapter;
import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.models.AppInfo;

/**
 * Created by sunita on 8/2/16.
 */
public class AppsListingView extends ListingContainerView implements RefreshAdapter{

    private AllAppAdapter adapter;


    public AppsListingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRefreshAdaperListener();

    }

    void setRefreshAdaperListener(){
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        launcherApplication.mModel.setRefreshAdapter(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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

    @Override
    public void appAdded() {
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void appRemoved() {
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

}
