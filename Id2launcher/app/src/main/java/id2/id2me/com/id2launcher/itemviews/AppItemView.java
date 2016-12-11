package id2.id2me.com.id2launcher.itemviews;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import id2.id2me.com.id2launcher.DragSource;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.models.AppInfoModel;

/**
 * Created by sunita on 11/2/16.
 */

public class AppItemView extends LinearLayout  {

    private AppInfoModel appInfoModel;
    GestureListener gestureListener;
    GestureDetector gestureDetector;
    private DragSource dragSource;

     AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(context, gestureListener);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void setTextVisibility(int value){
        TextView textView =(TextView)findViewById(R.id.drawer_grid_text) ;
        if(textView!=null)
            textView.setVisibility(value);
    }

    private void launchApplication() {
        try {
            Intent intent = null;
            String pckName = appInfoModel.getPname();

            if (pckName != null) {
                intent = getContext().getPackageManager()
                        .getLaunchIntentForPackage(pckName);

                getContext().startActivity(intent);

            } else {
                Toast.makeText(getContext(),
                        getContext().getResources().getText(R.string.appNotFound),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void setAppInfoModel(AppInfoModel appInfoModel) {
        this.appInfoModel = appInfoModel;
        ImageView imageView = (ImageView) findViewById(R.id.drawer_grid_image);
        TextView textView =(TextView)findViewById(R.id.drawer_grid_text) ;
        if(textView!=null)
        textView.setText(appInfoModel.getAppname());
        if(imageView!=null)
        imageView.setImageBitmap(appInfoModel.getBitmapIcon());
        this.setTag(appInfoModel);
    }

    public void setDragSource(DragSource dragSource) {
        this.dragSource = dragSource;
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public void onLongPress(MotionEvent e) {
            LauncherApplication launcherApplication = LauncherApplication.getApp();
            launcherApplication.dragInfo = appInfoModel;
            launcherApplication.dragInfo.setDropExternal(false);
            launcherApplication.getLauncher().resetPage();
            launcherApplication.getLauncher().getWokSpace().beginDragShared(AppItemView.this,dragSource);
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



}
