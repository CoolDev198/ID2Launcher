package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by sunita on 11/2/16.
 */

public class AppItemView implements View.OnTouchListener {

    final Handler handler = new Handler();
    View item;
    Context context;
    MotionEvent event;
    LauncherApplication launcherApplication;
    GestureListener gestureListener;
    GestureDetector gestureDetector;

    public AppItemView(Context context, Bitmap icon, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {

        item = ((Activity) context).getLayoutInflater().inflate(R.layout.grid_item, null, true);

        ImageView imageView = (ImageView) item.findViewById(R.id.grid_image);
        imageView.setImageBitmap(icon);
        item.setOnTouchListener(this);

    }

    public AppItemView(Context context, ItemInfoModel itemInfo) {

        this.context = context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        item = ((Activity) context).getLayoutInflater().inflate(R.layout.grid_item, null, true);

        ImageView imageView = (ImageView) item.findViewById(R.id.grid_image);
        imageView.setImageBitmap(ItemInfoModel.getIconFromCursor(itemInfo.getIcon(), context));
        item.setOnTouchListener(this);

        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(context, gestureListener);


    }


    public View getView() {
        return item;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        Log.v("AppItemView", "  x:: y ;; " + ev.getX() + "  " + ev.getY());
        gestureListener.setView(v);
        return gestureDetector.onTouchEvent(ev);
    }

    void performClick(View view) {
        Log.v("AppItemView ", " Click");
        ItemInfoModel itemInfoModel = (ItemInfoModel) view.getTag();
//        if (itemInfoModel.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
//            if (launcherApplication.folderView != null) {
//                cellLayout.removeView(launcherApplication.folderView);
//            }
//            ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(itemInfoModel.getId());
//           // getPopUp(itemInfoModels);
//        } else {
        launchApplication(itemInfoModel);
        //  }
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

        private View view;

        @Override
        public void onLongPress(MotionEvent e) {
            Log.v("AppItemView ", " on long press : ");
            launcherApplication.dragInfo = (ItemInfoModel) view.getTag();
            launcherApplication.dragInfo.setDropExternal(false);
            launcherApplication.dragAnimation(view, new Point((int) e.getX(), (int) e.getY()));
            super.onLongPress(e);
        }

        public void setView(View view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.v("AppItemView ", " onSingleTapConfirmed: ");

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.v("AppItemView ", " onShowPress: ");

            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.v("AppItemView ", " onSingleTapUp: ");
            performClick(view);
            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            Log.v("AppItemView ", " onDown: ");
            return true;
        }
    }


}
