package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import id2.id2me.com.id2launcher.models.WidgetInfoModel;

/**
 * Created by sunita on 8/9/16.
 */
public class WidgetRecycleViewAdapter extends RecyclerView.Adapter<WidgetRecycleViewAdapter.MyViewHolder> implements View.OnLongClickListener {


    private List<WidgetInfoModel> widgetInfoList;
    Context context;
    RecyclerView recycleView;
    private WidgetsListManager widgetsListManager;
    private WidgetInfoModel widgetInfo;
    LauncherApplication launcherApplication;
    @Override
    public boolean onLongClick(View v) {
        try {
            ((LauncherApplication) ((Activity) context).getApplication()).dragInfo = ((MyViewHolder)v.getTag()).widgetInfo;
             launcherApplication.dragAnimation(v.findViewById(R.id.widget_preview));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView widget_name, widget_dim;
        ImageView widget_preview_img;
        WidgetInfoModel widgetInfo;

        private MyViewHolder(View view) {
            super(view);
            widget_dim = (TextView) view.findViewById(R.id.widget_dims);
            widget_name = (TextView) view.findViewById(R.id.widget_name);
            widget_preview_img = (ImageView) view.findViewById(R.id.widget_preview);
        }
    }


    private void loadWidgets() {
        widgetsListManager = new WidgetsListManager(context);
        widgetInfoList = widgetsListManager.getInstalledWidgets();
    }

    public WidgetRecycleViewAdapter(Context context, RecyclerView recycleView) {
        this.recycleView = recycleView;

        this.context = context;
        launcherApplication= (LauncherApplication)((Activity)context).getApplication();
        loadWidgets();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_listing_item, parent, false);
        MyViewHolder myViewHolder= new MyViewHolder(itemView);
        itemView.setTag(myViewHolder);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        widgetInfo = widgetInfoList.get(position);
        holder.itemView.setOnLongClickListener(this);
        holder.widget_name.setText(widgetInfo.getWidgetName());
        holder.widget_dim.setText("");
        holder.widgetInfo = widgetInfo;
        holder.widget_preview_img.setImageDrawable(new BitmapDrawable(context.getResources(), widgetsListManager.getWidgetPreview(widgetInfo.getComponentName(), widgetInfo.getPreview(), 350,380))); //To Do

    }

    @Override
    public int getItemCount() {
        return widgetInfoList.size();
    }
}
