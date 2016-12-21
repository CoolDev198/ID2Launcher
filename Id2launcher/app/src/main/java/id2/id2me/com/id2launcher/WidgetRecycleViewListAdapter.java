package id2.id2me.com.id2launcher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.listingviews.ListingContainerView;
import id2.id2me.com.id2launcher.models.PendingAddItemInfo;
import timber.log.Timber;

/**
 * Created by CrazyInnoTech on 21-12-2016.
 */

public class WidgetRecycleViewListAdapter extends RecyclerView.Adapter<WidgetRecycleViewListAdapter.MyViewHolder>  {


    LauncherApplication launcherApplication;
    private PendingAddItemInfo widgetInfo;
    private ListingContainerView listeners;
    private ArrayList<Object> items;


    public WidgetRecycleViewListAdapter(ListingContainerView listeners) {
        this.listeners=listeners;
        launcherApplication = LauncherApplication.getApp();
        items = launcherApplication.mModel.mWidgetList;
        Timber.v("Widget List Size : " + items.size());
        //loadWidgets();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_listing_item, parent, false);
        itemView.setOnLongClickListener(listeners);
        itemView.setOnClickListener(listeners);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        itemView.setTag(myViewHolder);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView widget_name, widget_dim;
        ImageView widget_preview_img;
        PendingAddItemInfo widgetInfo;

        private MyViewHolder(View view) {
            super(view);
            /*widget_dim = (TextView) view.findViewById(R.id.widget_dims);
            widget_name = (TextView) view.findViewById(R.id.widget_name);
            widget_preview_img = (ImageView) view.findViewById(R.id.widget_preview);*/
        }
    }

}

