package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by CrazyInnoTech on 11-11-2016.
 */

public class FolderItemView implements  View.OnTouchListener, View.OnClickListener{

    final Handler handler = new Handler();
    static View item;
    Context context;
    MotionEvent event;
    LauncherApplication launcherApplication;
    FolderItemView.GestureListener gestureListener;
    GestureDetector gestureDetector;
    public static ArrayList<ImageView> folderImgs;
    private DatabaseHandler db;
    private long folderId;
    View blur_relative;
    private ObservableScrollView container;

    public FolderItemView(Context context, Bitmap icon, ItemInfoModel itemInfo, FrameLayout.LayoutParams layoutParams) {
        item = ((Activity) context).getLayoutInflater().inflate(R.layout.folder_view, null, true);
        item.setOnTouchListener(this);
    }

    public FolderItemView(Context context, DatabaseHandler db, long folderId) {

        this.context = context;
        this.db = db;
        this.folderId = folderId;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        item = ((Activity) context).getLayoutInflater().inflate(R.layout.folder_view, null, true);

        ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);

        setFolderView(context, item, itemInfoModels);

        //ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);
        item.setOnTouchListener(this);
        item.setTag(folderId);

        blur_relative = launcherApplication.desktopFragment.findViewById(R.id.blur_relative);
        blur_relative.setLayoutParams(new RelativeLayout.LayoutParams(launcherApplication.getScreenWidth(), launcherApplication.getScreenHeight()));
        container = (ObservableScrollView) launcherApplication.desktopFragment.findViewById(R.id.scrollView);
        blur_relative.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                System.out.println("touch blurr");
                blur_relative.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                return false;
               /* gestureListener.setView(blur_relative);
                return gestureDetector.onTouchEvent(motionEvent);*/
            }

        });

        /*blur_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blur_relative.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
            }

        });*/
        gestureListener = new FolderItemView.GestureListener();
        gestureDetector = new GestureDetector(context, gestureListener);

    }


    public View getView() {
        return item;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        Log.v("FolderItemView", "  x:: y ;; " + ev.getX() + "  " + ev.getY());
       /* if(blur_relative != null){
            if(blur_relative.getVisibility() == View.VISIBLE){
                blur_relative.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);

            }
        }*/

        gestureListener.setView(v);
        return gestureDetector.onTouchEvent(ev);
    }

    void performClick(View view) {
        Log.v("FolderItemView ", " Click");
        ItemInfoModel itemInfoModel = (ItemInfoModel) view.getTag();
//        if (itemInfoModel.getItemType() == DatabaseHandler.ITEM_TYPE_FOLDER) {
//            if (launcherApplication.folderView != null) {
//                cellLayout.removeView(launcherApplication.folderView);
//            }
//            ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(itemInfoModel.getId());
//           // getPopUp(itemInfoModels);
//        } else {
        //launchApplication(itemInfoModel);
        //  }

        ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);
        container.setVisibility(View.GONE);
        blur_relative.setVisibility(View.VISIBLE);

        AppGridView appGridView = (AppGridView) blur_relative.findViewById(R.id.folder_gridView);
        appGridView.setNumColumns(3);
        FolderGridAdapter adapter = new FolderGridAdapter(itemInfoModels, context, R.layout.pop_up_grid, appGridView);
        appGridView.setAdapter(adapter);


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

    private void setFolderImagesList(LinearLayout folder_view) {
        folderImgs = new ArrayList<>();
        for (int i = 0; i < folder_view.getChildCount(); i++) {
            LinearLayout horzontalLayout = (LinearLayout) folder_view.getChildAt(i);
            for (int j = 0; j < horzontalLayout.getChildCount(); j++) {
                folderImgs.add((ImageView) horzontalLayout.getChildAt(j));
            }
        }
    }

    private  void setFolderView(Context context, View view, ArrayList<ItemInfoModel> itemInfoModels) {
        setFolderImagesList((LinearLayout) view);
        for (int i = 0; i < folderImgs.size(); i++) {
            if (i < itemInfoModels.size()) {
                folderImgs.get(i).setBackground(null);
                folderImgs.get(i).setImageBitmap(ItemInfoModel.getIconFromCursor(itemInfoModels.get(i).getIcon(), context));
                folderImgs.get(i).setVisibility(View.VISIBLE);
            } else {
                //folderImgs.get(i).setVisibility(View.INVISIBLE);
                folderImgs.get(i).setBackground(ContextCompat.getDrawable(context,R.drawable.folder_empty_icon));
                //iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.img));

            }
        }
    }

    public static Bitmap getBitmapFolderView(){
        try {
            item.buildDrawingCache();
            Bitmap folderBitmap = item.getDrawingCache();
            return folderBitmap;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onClick(View view) {

    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private View view;

        @Override
        public void onLongPress(MotionEvent e) {
            Log.v("FolderItemView ", " on long press : ");
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
            Log.v("FolderItemView ", " onSingleTapConfirmed: ");
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.v("FolderItemView ", " onShowPress: ");

            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.v("FolderItemView ", " onSingleTapUp: ");
           // if(view.getId()!= R.id.blur_relative){
                performClick(view);
           // }

            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            Log.v("FolderItemView ", " onDown: ");
            return true;
        }
    }


}
