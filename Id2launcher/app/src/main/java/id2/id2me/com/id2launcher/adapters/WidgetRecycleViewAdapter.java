package id2.id2me.com.id2launcher.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.WidgetsListManager;
import id2.id2me.com.id2launcher.listingviews.ListingContainerView;
import id2.id2me.com.id2launcher.models.PendingAddItemInfo;

/**
 * Created by sunita on 8/9/16.
 */
public class WidgetRecycleViewAdapter extends RecyclerView.Adapter<WidgetRecycleViewAdapter.MyViewHolder>  {


    Context context;
    LauncherApplication launcherApplication;
    private List<PendingAddItemInfo> widgetInfoList;
    private WidgetsListManager widgetsListManager;
    private PendingAddItemInfo widgetInfo;
    private ListingContainerView listeners;

    public WidgetRecycleViewAdapter(ListingContainerView listeners) {
        this.listeners=listeners;
        launcherApplication = LauncherApplication.getApp();
        loadWidgets();
    }

    private void loadWidgets() {
        widgetsListManager = new WidgetsListManager(context);
        widgetInfoList = widgetsListManager.getInstalledWidgets();
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
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        widgetInfo = widgetInfoList.get(position);
        holder.widget_name.setText("");
        holder.widget_dim.setText("");
        holder.widgetInfo = widgetInfo;

    }

    @Override
    public int getItemCount() {
        return widgetInfoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView widget_name, widget_dim;
        ImageView widget_preview_img;
        PendingAddItemInfo widgetInfo;

        private MyViewHolder(View view) {
            super(view);
            widget_dim = (TextView) view.findViewById(R.id.widget_dims);
            widget_name = (TextView) view.findViewById(R.id.widget_name);
            widget_preview_img = (ImageView) view.findViewById(R.id.widget_preview);
        }
    }

}
