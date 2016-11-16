package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.models.ItemInfoModel;
import id2.id2me.com.id2launcher.models.NotificationWidgetModel;

/**
 * Created by sunita on 7/27/16.
 */
public class LauncherApplication extends Application {
    public static ImageView wallpaperImg;
    public static List<NotificationWidgetModel> notificationWidgetModels;
    private static float density;
    public View folderView;
    public boolean isDrawerOpen = false;
    public ArrayList<ItemInfoModel> folderFragmentsInfo;
    public ItemInfoModel dragInfo;
    public LauncherAppWidgetHost mAppWidgetHost;
    public LauncherModel mModel;
    public HashMap<ArrayList<Integer>, Rect> mapMatrixPosToRec;
    public View desktopFragment;
    public int currentScreen = 1;
    public boolean isTimerTaskCompleted = true;
    public List<View> viewList;
    private PageDragListener pageDragListener;
    private Launcher launcher;
    private HolographicOutlineHelper mOutlineHelper;
    private final int MIN_NO_OF_APP = 0;
    public boolean isDragStarted = false;


    public static float getScreenDensity() {
        return density;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mapMatrixPosToRec = new HashMap<>();
        folderFragmentsInfo = new ArrayList<>();


        mModel = new LauncherModel(this);
        density = getResources().getDisplayMetrics().density;

        addBroadCastReceiver();
        mOutlineHelper = new HolographicOutlineHelper();

    }

    private void addBroadCastReceiver() {
        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mModel, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mModel);
    }

    public Typeface getTypeFace() {
        Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), "fonts/Roboto-Regular.ttf");
        return typeface;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    public LauncherModel setDeskTopFragment(DesktopFragment fragment) {
        mModel.initialize(fragment);
        return null;
    }

    public PageDragListener getPageDragListener() {
        return pageDragListener;
    }

    public void setPageDragListener(PageDragListener pageDragListener) {
        this.pageDragListener = pageDragListener;
    }


    public int getScreenHeight() {
        int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        return height;

    }

    public int getScreenWidth() {
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    public int getCellHeight() {
        int cellHeight = (int) getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.cell_height);
        return cellHeight;
    }

    public int getCellWidth() {
        int cellWidth = (int) getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.cell_width);
        return cellWidth;
    }



    public float convertFromPixelToDp(int dimension) {
        float dimensionInDp = (dimension / getScreenDensity());
        return dimensionInDp;
    }

    public float convertFromDpToPixel(int resource) {
        float dimensionInPixel = getApplicationContext().getResources().getDimensionPixelOffset(resource);
        return dimensionInPixel;
    }

    public Bitmap getOutLinerBitmap(Bitmap bitmap) { // i 0 for App and 1 for Widget
        Bitmap outlinerBitmap = null;
        try {
            final Canvas canvas = new Canvas();
            /*if(i == 0){
                outlinerBitmap = createDragOutline(bitmap, canvas, 2, bitmap.getWidth(), bitmap.getHeight(), false);
            } else if(i == 1){
                outlinerBitmap = createDragOutline();
            }*/
            outlinerBitmap = createDragOutline(bitmap, canvas, 2, bitmap.getWidth(), bitmap.getHeight(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outlinerBitmap;
    }

    private Bitmap createDragOutline(Bitmap orig, Canvas canvas, int padding, int w, int h,
                                     boolean clipAlpha) {
        final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);

        Rect src = new Rect(0, 0, orig.getWidth(), orig.getHeight());
        float scaleFactor = Math.min((w - padding) / (float) orig.getWidth(),
                (h - padding) / (float) orig.getHeight());
        int scaledWidth = (int) (scaleFactor * orig.getWidth());
        int scaledHeight = (int) (scaleFactor * orig.getHeight());
        Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);

        // center the image
        dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);

        canvas.drawBitmap(orig, src, dst, null);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor,
                clipAlpha);
        canvas.setBitmap(null);

        return b;
    }


    public void dragAnimation(View view) {
        try {


            isDragStarted=true;
            ClipData.Item item = new ClipData.Item(
                    (CharSequence) (""));

            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData data = new ClipData("",
                    mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);

            addScreen();

            addMargin();

            if (dragInfo.getDropExternal()) {
                currentScreen = 1;
                ((ObservableScrollView) desktopFragment.findViewById(R.id.scrollView)).scrollTo(0, ((LinearLayout) desktopFragment.findViewById(R.id.container)).getChildAt(0).getTop());
            }

            view.startDrag(data, shadowBuilder, view, 0);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dragAnimation(View view, Point point) {

        isDragStarted=true;
        ClipData.Item item = new ClipData.Item(
                (CharSequence) (""));

        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData("",
                mimeTypes, item);
        DragShadowBuilder shadowBuilder = new DragShadowBuilder(
                view, point);
        view.setVisibility(View.INVISIBLE);

        currentScreen = dragInfo.getScreen();


        addScreen();

        if (!dragInfo.getDropExternal()) {
            int screen;
            if (dragInfo.getScreen() == 1) {
                screen = 0;
            } else {
                screen = dragInfo.getScreen();
            }

            ((ObservableScrollView) desktopFragment.findViewById(R.id.scrollView)).scrollTo(0, ((LinearLayout) desktopFragment.findViewById(R.id.container)).getChildAt(screen).getTop() - getResources().getDimensionPixelSize(R.dimen.extra_move));
        }



        addMargin();

        //view.findViewById(R.id.grid_image).setScaleX(1.2f);
        /*LinearLayout containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);
        int childcount = containerL.getChildCount();
        for (int i=0; i < childcount; i++){
            View v = containerL.getChildAt(i);

            if(i == (childcount -1)){
                // Do your Task here
                System.out.println("last child of desktop reached");
            }
        }*/
        view.startDrag(data, shadowBuilder, view, 0);

    }

    public void addMargin() {
        try {

            desktopFragment.findViewById(R.id.drop_target_layout).setVisibility(View.VISIBLE);
            desktopFragment.findViewById(R.id.drag_layer).setScaleX(0.85f);
            desktopFragment.findViewById(R.id.drag_outline_img).setPivotY(0.5f);
            desktopFragment.findViewById(R.id.drag_outline_img).setPivotX(0.5f);
            desktopFragment.findViewById(R.id.drag_outline_img).setScaleX(0.98f);
            desktopFragment.findViewById(R.id.drag_outline_img).setScaleY(0.85f);

            LinearLayout containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);

            for (int i = 0; i < containerL.getChildCount(); i++) {
                View view = containerL.getChildAt(i);

                if (i==0) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                   params.height=getResources().getDimensionPixelOffset(R.dimen.wallpaper_height_after_anim);
//                  params.setMargins(margin, 0, margin, 0);
                  containerL.updateViewLayout(view, params);

                } else  {
                 //   view.setScaleY(0.98f);
                    view.setBackgroundColor(getResources().getColor(R.color.frame_color));
                }
                if(view instanceof CellLayout) {
                    CellLayout viewF = (CellLayout)view;
                    System.out.println("CellLayout Launcher Application");
                    for (int j = 0; j < viewF.getChildCount();j++){
                        View child = viewF.getChildAt(j);
                        child.setPivotY(0.5f);
                        child.setPivotX(0.5f);
                        child.setScaleX(0.98f);
                        child.setScaleY(0.85f);
                    }

                }



            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeMargin() {
        try {
            desktopFragment.findViewById(R.id.drop_target_layout).setVisibility(View.GONE);
            desktopFragment.findViewById(R.id.drag_layer).setScaleX(1f);
            desktopFragment.findViewById(R.id.drag_outline_img).setScaleX(1f);
            desktopFragment.findViewById(R.id.drag_outline_img).setScaleY(1f);
            LinearLayout containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);


            for (int i = 0; i < containerL.getChildCount(); i++) {
                View view = containerL.getChildAt(i);
                view.setScaleY(1f);
                view.setScaleX(1f);
                if (i == 0) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                  //  params.setMargins(0, 0, 0, 0);
                    params.height = getResources().getDimensionPixelOffset(R.dimen.wallpaper_height);
                    containerL.updateViewLayout(view, params);
                } else {

                    view.setBackgroundColor(Color.TRANSPARENT);
                }


                if(view instanceof  CellLayout) {
                    CellLayout viewF = (CellLayout)view;
                    for (int j = 0; j < viewF.getChildCount();j++){
                        View child = viewF.getChildAt(j);
                        child.setScaleX(1f);
                        child.setScaleY(1f);
                        //CellLayout cellLayout = (CellLayout) child;
                    }

                    /*if(viewF.getChildCount() > 1){
                        removeScreen(viewF);
                    }*/

                }



            }
            int lastChild = 2;
            if(containerL.getChildCount() > 2){
                lastChild = containerL.getChildCount();
                //removeScreen(lastChild);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addScreen(){
        try {
            LinearLayout containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);
            System.out.println("Container Child count before add : " + containerL.getChildCount());
            //CellLayout child = new CellLayout(DesktopFragment.context, mCellLayoutHeight);
            CellLayout cellLayout = (CellLayout) containerL.getChildAt(containerL.getChildCount() - 1);
            if(cellLayout.getChildCount() > MIN_NO_OF_APP){
                CellLayout child = new CellLayout(launcher, R.dimen.cell_layout_height);
                child.setTag(containerL.getChildCount());
                PageDragListener pageDragListener=new PageDragListener(launcher, desktopFragment, child);
                child.setOnDragListener(pageDragListener);
                child.setDragListener(pageDragListener);
                containerL.addView(child);
            }

            System.out.println("Container Child count after add : " + containerL.getChildCount());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void removeScreen(int position){
        try {
            LinearLayout containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);
            System.out.println("Container Child count before removing : " + containerL.getChildCount());
            CellLayout cellLayout = (CellLayout) containerL.getChildAt(position);
            if(cellLayout.getChildCount() <= MIN_NO_OF_APP){
                containerL.removeView(cellLayout);
            }

            //CellLayout child = new CellLayout(DesktopFragment.context, mCellLayoutHeight);
            System.out.println("Container Child count after removing : " + containerL.getChildCount());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
