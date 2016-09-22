package id2.id2me.com.id2launcher.notificationWidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import id2.id2me.com.id2launcher.DatabaseHandler;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.models.NotificationWidgetModel;

/**
 * Created by apple on 23/02/16.
 */
public class NotificationWidgetAdapter extends RecyclerView.Adapter<NotificationWidgetAdapter.ViewHolder> {

    

    Context context;
    DatabaseHandler db;
    public NotificationWidgetAdapter(Context context) {

        db =  DatabaseHandler.getInstance(context);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.view_notification_widget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final NotificationWidgetModel model = LauncherApplication.notificationWidgetModels.get(position);
        holder.appImage.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(model.getAppImageName(), "drawable", context.getPackageName())));
        holder.notificationCount.setText(model.getCount() + "");

        holder.appContainer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                db.resetNotificationCount(model.getPname());
                try {

                        holder.notificationCount.setText(model.getCount() + "");
                        Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(model.getPname());
                        context.startActivity( LaunchIntent );
                        db.getNotificationData();
                        NotificationWidgetAdapter.this.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return LauncherApplication.notificationWidgetModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout appContainer;
        public ImageView appImage;
        public TextView notificationCount;

        public ViewHolder(View itemView) {
            super(itemView);
            appContainer = (RelativeLayout) itemView.findViewById(R.id.noti_widget_container);
            appImage = (ImageView) itemView.findViewById(R.id.noti_widget_icon);
            notificationCount = (TextView) itemView.findViewById(R.id.notification_count);
        }
    }
}
