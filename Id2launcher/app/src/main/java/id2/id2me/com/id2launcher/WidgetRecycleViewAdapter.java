package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import id2.id2me.com.id2launcher.models.WidgetInfoModel;

/**
 * Created by sunita on 8/9/16.
 */
public class WidgetRecycleViewAdapter extends RecyclerView.Adapter<WidgetRecycleViewAdapter.MyViewHolder> implements View.OnTouchListener {


    private final GestureListener gestureListener;
    private final GestureDetector gestureDetector;
    Context context;
    RecyclerView recycleView;
    LauncherApplication launcherApplication;
    private List<WidgetInfoModel> widgetInfoList;
    private WidgetsListManager widgetsListManager;
    private WidgetInfoModel widgetInfo;

    public WidgetRecycleViewAdapter(Context context, RecyclerView recycleView) {
        this.recycleView = recycleView;

        this.context = context;
        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(context, gestureListener);
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        loadWidgets();


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureListener.setView(v);
        return gestureDetector.onTouchEvent(event);
    }

    private void loadWidgets() {
        widgetsListManager = new WidgetsListManager(context);
        widgetInfoList = widgetsListManager.getInstalledWidgets();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_listing_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        itemView.setTag(myViewHolder);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        widgetInfo = widgetInfoList.get(position);
        holder.itemView.setOnTouchListener(this);
        holder.widget_name.setText(widgetInfo.getWidgetName());
        holder.widget_dim.setText("");
        holder.widgetInfo = widgetInfo;
        holder.widget_preview_img.setImageDrawable(new BitmapDrawable(context.getResources(), widgetsListManager.getWidgetPreview(widgetInfo.getComponentName(), widgetInfo.getPreview(), 350, 380))); //To Do

    }

    @Override
    public int getItemCount() {
        return widgetInfoList.size();
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

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private View view;
      private View preview;
        public Bitmap getBitmapView() {
            try {
                preview.buildDrawingCache();
                Bitmap folderBitmap = preview.getDrawingCache();
                return folderBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {

            MyViewHolder holder=((MyViewHolder) view.getTag());
            ((LauncherApplication) ((Activity) context).getApplication()).dragInfo = holder.widgetInfo;
            launcherApplication.getLauncher().resetPage();
            launcherApplication.prepareDrag(getBitmapView(), new Point((int) e.getX(), (int) e.getY()), preview.getWidth(), preview.getHeight());
            super.onLongPress(e);
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //  Log.v("AppItemView ", " onSingleTapConfirmed: ");
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            //    Log.v("AppItemView ", " onShowPress: ");
            super.onShowPress(e);
        }

        void setView(View view) {
            this.view = view;
            this.preview=view.findViewById(R.id.widget_preview);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            //  Log.v("AppItemView ", " onDown: ");
            return true;
        }
    }

}
