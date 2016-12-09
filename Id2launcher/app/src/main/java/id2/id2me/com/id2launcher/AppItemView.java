package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.ItemInfoModel;
import timber.log.Timber;

/**
 * Created by sunita on 11/2/16.
 */

public class AppItemView extends LinearLayout implements DragSource {

    Context context;
    LauncherApplication launcherApplication;
    private AppInfoModel appInfoModel;
    Launcher launcher;
    GestureListener gestureListener;
    GestureDetector gestureDetector;


    public AppItemView(Context context, AppInfoModel appInfoModel) {
        super(context);
        this.context = context;
        launcher=(Launcher)context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        inflate(getContext(), R.layout.grid_item, this);
        init(appInfoModel);
    }

    public AppItemView(Context mContext) {
        super(mContext);
        this.context = mContext;
        launcher=(Launcher)context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        inflate(getContext(), R.layout.drawer_grid_item, this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void init(AppInfoModel appInfoModel){
        this.appInfoModel=appInfoModel;
        this.setTag(appInfoModel);
        ImageView imageView = (ImageView) findViewById(R.id.drawer_grid_image);
        TextView textView =(TextView)findViewById(R.id.drawer_grid_text) ;
        textView.setText(appInfoModel.getAppname());
        imageView.setImageBitmap(appInfoModel.getBitmapIcon());

        gestureListener = new GestureListener(this);
        gestureDetector = new GestureDetector(context, gestureListener);
    }


    private void launchApplication() {
        try {
            Intent intent = null;
            String pckName = appInfoModel.getPname();

            if (pckName != null) {
                intent = context.getPackageManager()
                        .getLaunchIntentForPackage(pckName);

                context.startActivity(intent);

            } else {
                Toast.makeText(context,
                        context.getResources().getText(R.string.appNotFound),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        AppItemView v;
        GestureListener(AppItemView appItemView){
            this.v=appItemView;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            launcherApplication.dragInfo = appInfoModel;
            launcherApplication.dragInfo.setDropExternal(false);
            launcher.resetPage();
            launcher.getWokSpace().onDragStartedWithItem(v);
            launcher.getWokSpace().beginDragShared(v,AppItemView.this);
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {

            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            launchApplication();
            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }


    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success) {

    }


}
