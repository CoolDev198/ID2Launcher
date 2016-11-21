package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.ItemInfoModel;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by CrazyInnoTech on 11-11-2016.
 */

public class FolderItemView extends LinearLayout implements View.OnTouchListener {

    Context context;
    LauncherApplication launcherApplication;
    FolderItemView.GestureListener gestureListener;
    GestureDetector gestureDetector;
    View blur_relative;
    private DatabaseHandler db;
    private long folderId;
    private ObservableScrollView container;


    public FolderItemView(Context context, DatabaseHandler db, long folderId) {
        super(context);
        this.context = context;

        this.setOrientation(VERTICAL);
        this.setLayoutParams(new FrameLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.app_icon_size), getResources().getDimensionPixelSize(R.dimen.app_icon_size)));
        this.db = db;
        this.folderId = folderId;

        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();

        init();
        setFolderView();

        this.setOnTouchListener(this);
        this.setTag(folderId);



    }

    private void init() {

        this.addView(inflate(getContext(), R.layout.folder_view, null));
        this.addView(inflate(getContext(), R.layout.folder_view, null));
        this.addView(inflate(getContext(), R.layout.folder_view, null));

        blur_relative = launcherApplication.desktopFragment.findViewById(R.id.blur_relative);
        blur_relative.setLayoutParams(new RelativeLayout.LayoutParams(launcherApplication.getScreenWidth(), launcherApplication.getScreenHeight()));
        container = (ObservableScrollView) launcherApplication.desktopFragment.findViewById(R.id.scrollView);
        blur_relative.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                blur_relative.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                return false;

            }

        });

        gestureListener = new GestureListener(this);
        gestureDetector = new GestureDetector(context, gestureListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        Log.v("FolderItemView", "  x:: y ;; " + ev.getX() + "  " + ev.getY());
        return gestureDetector.onTouchEvent(ev);
    }

    void performClick(View view) {
        // Log.v("FolderItemView ", " Click");
        ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);
        container.setVisibility(View.GONE);
        blur_relative.setVisibility(View.VISIBLE);

        Blurry.with(context)
                .radius(25)
                .sampling(1)
                .color(Color.argb(66, 255, 255, 255))
                .async()
                .capture(launcherApplication.desktopFragment.findViewById(R.id.scrollView))
                .into(blur_relative);

        AppGridView appGridView = (AppGridView) blur_relative.findViewById(R.id.folder_gridView);
        appGridView.setNumColumns(3);
        FolderGridAdapter adapter = new FolderGridAdapter(itemInfoModels, context, R.layout.pop_up_grid, appGridView);
        appGridView.setAdapter(adapter);
    }


    private ArrayList setFolderImagesList() {
        ArrayList folderImgs = new ArrayList<>();
        int count = this.getChildCount();
        for (int i = 0; i < this.getChildCount(); i++) {
            LinearLayout horizontalLayout = (LinearLayout) this.getChildAt(i);
            for (int j = 0; j < horizontalLayout.getChildCount(); j++) {
                View child=horizontalLayout.getChildAt(j);
                folderImgs.add(child);
            }
        }
        return folderImgs;
    }

    void setFolderView() {

        try {
            ArrayList<ImageView> folderImgs = setFolderImagesList();
            ArrayList<ItemInfoModel> itemInfoModels = db.getAppsListOfFolder(folderId);

            for (int i = 0; i < folderImgs.size(); i++) {
                if (i < itemInfoModels.size()) {
                    folderImgs.get(i).setBackground(null);
                    folderImgs.get(i).setImageBitmap(ItemInfoModel.getIconFromCursor(itemInfoModels.get(i).getIcon(), context));
                    folderImgs.get(i).setVisibility(View.VISIBLE);
                } else {
                    folderImgs.get(i).setBackground(ContextCompat.getDrawable(context, R.drawable.folder_empty_icon));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFolderView() {
        try {
            this.buildDrawingCache();
            Bitmap folderBitmap = this.getDrawingCache();
            return folderBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addFragmentToHorizontalPagerAdapter() {
        launcherApplication.getLauncher().addNewFolderFragment(folderId);
    }

    private void updateFoldersFragment() {
        try {
            launcherApplication.getLauncher().updateFolderFragment(folderId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addedToExistingFolder(){
        this.setFolderView();
        this.updateFoldersFragment();
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        FolderItemView folderItemView;
        GestureListener(FolderItemView folderItemView){
            this.folderItemView=folderItemView;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //  Log.v("FolderItemView ", " on long press : ");
            launcherApplication.dragInfo = (ItemInfoModel) folderItemView.getTag();
            launcherApplication.dragInfo.setDropExternal(false);
            launcherApplication.dragAnimation(folderItemView, new Point((int) e.getX(), (int) e.getY()));
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //  Log.v("FolderItemView ", " onSingleTapConfirmed: ");
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // Log.v("FolderItemView ", " onShowPress: ");

            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Log.v("FolderItemView ", " onSingleTapUp: ");
            performClick(folderItemView);
            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            Log.v("FolderItemView ", " onDown: ");
            return true;
        }
    }
}
