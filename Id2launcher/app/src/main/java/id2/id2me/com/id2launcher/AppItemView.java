package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by sunita on 11/2/16.
 */

public class AppItemView extends RelativeLayout implements View.OnTouchListener {

    Context context;
    LauncherApplication launcherApplication;
    GestureListener gestureListener;
    GestureDetector gestureDetector;


    public AppItemView(Context context, ItemInfoModel itemInfo) {
        super(context);
        this.context = context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        inflate(getContext(), R.layout.grid_item, this);

        ImageView imageView = (ImageView) findViewById(R.id.grid_image);
        imageView.setImageBitmap(ItemInfoModel.getIconFromCursor(itemInfo.getIcon(), context));
        this.setOnTouchListener(this);

        gestureListener = new GestureListener(this);
        gestureDetector = new GestureDetector(context, gestureListener);


    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        //  Log.v("AppItemView", "  x:: y ;; " + ev.getX() + "  " + ev.getY());
        return gestureDetector.onTouchEvent(ev);
    }

    void performClick(View view) {
        // Log.v("AppItemView ", " Click");
        ItemInfoModel itemInfoModel = (ItemInfoModel) view.getTag();
        launchApplication(itemInfoModel);
    }

    private void launchApplication(ItemInfoModel itemInfoModel) {
        try {
            Intent intent = null;
            String pckName = itemInfoModel.getPname();

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

        private AppItemView view;

        public GestureListener(AppItemView appItemView) {
            view = appItemView;
        }

        public Bitmap getBitmapView() {
            try {
                view.buildDrawingCache();
                Bitmap folderBitmap = view.getDrawingCache();
                return folderBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        public void onLongPress(MotionEvent e) {
            //   Log.v("AppItemView ", " on long press : ");
            launcherApplication.dragInfo = (ItemInfoModel) view.getTag();
            launcherApplication.dragInfo.setDropExternal(false);
            launcherApplication.prepareDrag(getBitmapView(), new Point((int) e.getX(), (int) e.getY()),view.getWidth(),view.getHeight());
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

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //   Log.v("AppItemView ", " onSingleTapUp: ");
            performClick(view);
            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            //  Log.v("AppItemView ", " onDown: ");
            return true;
        }
    }


}
