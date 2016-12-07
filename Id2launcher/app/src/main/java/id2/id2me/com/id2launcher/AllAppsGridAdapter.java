package id2.id2me.com.id2launcher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by bliss76 on 21/06/16.
 */
public class AllAppsGridAdapter extends BaseAdapter implements View.OnTouchListener {
    private final LauncherApplication launcherApplication;
    private final GestureListener gestureListener;
    private final GestureDetector gestureDetector;
    LayoutInflater inflater;
    ArrayList<AppInfoModel> gridList;
    private Context mContext;
    String TAG="AllAppsGridAdapter";

    public AllAppsGridAdapter(Context c, ArrayList<AppInfoModel> gridList) {
        mContext = c;
        launcherApplication = (LauncherApplication) ((Activity) mContext).getApplication();
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.gridList = gridList;
        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(mContext, gestureListener);
    }


    @Override
    public int getCount() {
        return gridList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View grid = convertView;
        try {
            if (convertView == null) {
                grid = inflater.inflate(R.layout.drawer_grid_item, null);
                holder = new ViewHolder();
                grid.setTag(holder);

                grid.setOnTouchListener(this);
                holder.itemText = (TextView) grid.findViewById(R.id.drawer_grid_text);
                try {
                    holder.itemText.setTypeface(launcherApplication.getTypeFace());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.itemImage = (ImageView) grid.findViewById(R.id.drawer_grid_image);


            } else {
                holder = (ViewHolder) grid.getTag();
            }

            gridList.get(position);
            holder.itemImage.setTag(position);
            holder.appInfo = gridList.get(position);
            holder.pName = gridList.get(position).getPname();
            holder.itemText.setText(gridList.get(position).getAppname());
            holder.itemImage.setImageBitmap(gridList.get(position).getBitmapIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grid;
    }

    public void launchApp(AppInfoModel appInfo) {
        try {
            Intent intent = null;
            String pckName = appInfo.getPname();

            if (pckName != null) {
                intent = mContext.getPackageManager()
                        .getLaunchIntentForPackage(pckName);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext,
                        mContext.getResources().getText(R.string.appNotFound),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
       // Log.v(TAG," on touch");
        gestureListener.setView(v);
        return gestureDetector.onTouchEvent(event);
    }


    private static class ViewHolder {
        public TextView itemText;
        public ImageView itemImage;
        public String pName;
        public AppInfoModel appInfo;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private View view;
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
            ((LauncherApplication) ((Activity) mContext).getApplication()).dragInfo = (ItemInfoModel) gridList.get(Integer.parseInt(view
                    .findViewById(R.id.drawer_grid_image).getTag().toString())).clone();
            launcherApplication.getLauncher().resetPage();
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

        void setView(View view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //   Log.v("AppItemView ", " onSingleTapUp: ");
            performClick(view);
            return super.onSingleTapUp(e);
        }

        private void performClick(View view) {
        //    launchApp((AppInfoModel) view.getTag());
        }


        @Override
        public boolean onDown(MotionEvent e) {
            //  Log.v("AppItemView ", " onDown: ");
            return true;
        }
    }

}


