package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;

/**
 * Created by CrazyInnoTech on 11-11-2016.
 */

public class FolderItemView extends LinearLayout implements DragSource, View.OnTouchListener {

    Context context;
    LauncherApplication launcherApplication;
    FolderItemView.GestureListener gestureListener;
    GestureDetector gestureDetector;
    View blur_relative;
    private DatabaseHandler db;
    private long folderId;
    private ObservableScrollView container;
    private DragController dragController;
    private FolderIcon folderIcon;


    public FolderItemView(Context context, DatabaseHandler db, long folderId) {
        super(context);
        this.context = context;

        this.setOrientation(VERTICAL);
        this.setLayoutParams(new FrameLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.app_icon_size), getResources().getDimensionPixelSize(R.dimen.app_icon_size)));
        this.db = db;
        this.folderId = folderId;

        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();

        this.setOnTouchListener(this);
        this.setTag(folderId);

    }

    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     *
     * @return A new UserFolder.
     */
    static FolderItemView fromXml(Context context) {
        return (FolderItemView) LayoutInflater.from(context).inflate(R.layout.folder_item_view, null);
    }


    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        Log.v("FolderItemView", "  x:: y ;; " + ev.getX() + "  " + ev.getY());
        return gestureDetector.onTouchEvent(ev);
    }
//
//    void performClick(View view) {
//        // Log.v("FolderItemView ", " Click");
//        ArrayList<ItemInfo> itemInfoModels = db.getAppsListOfFolder(folderId);
//        container.setVisibility(View.GONE);
//        blur_relative.setVisibility(View.VISIBLE);
//
//        Blurry.with(context)
//                .radius(25)
//                .sampling(1)
//                .color(Color.argb(66, 255, 255, 255))
//                .async()
//                .capture(launcherApplication.desktopFragment.findViewById(R.id.scrollView))
//                .into(blur_relative);
//
//        AppGridView appGridView = (AppGridView) blur_relative.findViewById(R.id.folder_gridView);
//        appGridView.setNumColumns(3);
//        FolderGridAdapter adapter = new FolderGridAdapter(itemInfoModels, context, R.layout.app_item_view, appGridView);
//        appGridView.setAdapter(adapter);
//    }


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

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success) {

    }

    public ArrayList<View> getItemsInReadingOrder() {
        ArrayList<View> appList = new ArrayList<>();
        return appList;
    }

    public void setDragController(DragController dragController) {
        this.dragController = dragController;
    }

    public void setFolderIcon(FolderIcon folderIcon) {
        this.folderIcon = folderIcon;
    }

    public void bind(FolderInfo folderInfo) {

    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        FolderItemView folderItemView;
        GestureListener(FolderItemView folderItemView){
            this.folderItemView=folderItemView;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //  Log.v("FolderItemView ", " on long press : ");
            launcherApplication.dragInfo = (ItemInfo) folderItemView.getTag();
           // launcherApplication.dragAnimation(folderItemView, new Point((int) e.getX(), (int) e.getY()));
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
            //performClick(folderItemView);
            return super.onSingleTapUp(e);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            Log.v("FolderItemView ", " onDown: ");
            return true;
        }
    }
}
