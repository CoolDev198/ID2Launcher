package id2.id2me.com.id2launcher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.listingviews.ListingContainerView;
import id2.id2me.com.id2launcher.models.AppInfo;

/**
 * Created by Pinto on 24/09/16.
 */
public class AllAppAdapter extends RecyclerView.Adapter<AllAppAdapter.MyViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter, View.OnLongClickListener {

    private ArrayList<AppInfo> groupList;
    private HashMap<Integer, String> mapIndex;
    private LauncherApplication launcherApplication;
    private ListingContainerView listeners;

    public AllAppAdapter(ListingContainerView listeners) {
        try {
            this.listeners = listeners;
            launcherApplication = LauncherApplication.getApp();
            this.groupList = launcherApplication.mModel.mBgAllAppsList.data;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getTextToShowInBubble(int pos) {
        return  groupList.get(pos).toString().toUpperCase().substring(0,0);

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item_view, parent, false);
        itemView.setOnLongClickListener(listeners);
        itemView.setOnClickListener(listeners);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AppInfo appInfoModel = groupList.get(position);
        ((AppItemView) holder.itemView).setAppInfoModel(appInfoModel);
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }

    }
}
