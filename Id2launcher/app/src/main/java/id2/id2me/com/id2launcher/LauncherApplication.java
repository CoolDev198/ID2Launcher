package id2.id2me.com.id2launcher;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    public int pos = 1;
    public Bitmap outlineBitmap;
    private PageDragListener pageDragListener;
    private int cellCountX, cellCountY, maxGapLR, maxGapTB;
    private Launcher launcher;
    private HolographicOutlineHelper mOutlineHelper;
    private final Rect mTempRect = new Rect();

    public static float getScreenDensity() {
        return density;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setCellCountX();
        setCellCountY();


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

    public int getMaxGapLR() {
        return maxGapLR;
    }

    public int getMaxGapTB() {
        return maxGapTB;
    }

    public int getCellCountX() {
        return cellCountX;
    }


    public int getCellCountY() {
        return cellCountY;
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

    public void setCellCountX() {
        int countX = getScreenWidth() / getCellWidth();
        cellCountX = countX;
        setMaxGapForLR();
    }

    public void setCellCountY() {
        int countY = getScreenHeight() / getCellHeight();
        cellCountY = countY;
        setMaxGapForTB();
    }

    public int calculateExtraSpaceWidthWise() {
        int extraWidthSpace = getScreenWidth() - cellCountX * getCellWidth();
        return extraWidthSpace;
    }

    public int calculateExtraSpaceHeightWise() {
        int extraHeightSpace = getScreenHeight() - cellCountY * getCellHeight();
        return extraHeightSpace;
    }

    public void setMaxGapForLR() {
        int paddingLR = calculateExtraSpaceWidthWise() / (cellCountX + 1);
        maxGapLR = paddingLR;

    }

    public void setMaxGapForTB() {
        int paddingTB = calculateExtraSpaceHeightWise() / (cellCountY + 1);
        maxGapTB = paddingTB;

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

    public Bitmap createDragOutline(View v, Canvas canvas, int padding) {
        final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        final Bitmap b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        canvas.setBitmap(null);
        return b;
    }

    public void dragAnimation(View view) {
        try {
            ClipData.Item item = new ClipData.Item(
                    (CharSequence) (""));

            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData data = new ClipData("",
                    mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);
            if (dragInfo.getDropExternal()) {
                ((ObservableScrollView) desktopFragment.findViewById(R.id.scrollView)).scrollTo(0, ((LinearLayout) desktopFragment.findViewById(R.id.container)).getChildAt(0).getTop());
            }
            view.startDrag(data, shadowBuilder, view, 0);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dragAnimation(View view, Point point) {
        ClipData.Item item = new ClipData.Item(
                (CharSequence) (""));

        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData("",
                mimeTypes, item);
        DragShadowBuilder shadowBuilder = new DragShadowBuilder(
                view, point);
         view.setVisibility(View.INVISIBLE);
        if (!dragInfo.getDropExternal()) {
            int screen;
            if (dragInfo.getScreen() == 1) {
                screen = 0;
            } else {
                screen = dragInfo.getScreen();
            }
            ((ObservableScrollView) desktopFragment.findViewById(R.id.scrollView)).scrollTo(0, ((LinearLayout) desktopFragment.findViewById(R.id.container)).getChildAt(screen).getTop());
        }

        //view.findViewById(R.id.grid_image).setScaleX(1.2f);

        view.startDrag(data, shadowBuilder, view, 0);
    }

    private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
        final Rect clipRect = mTempRect;
        v.getDrawingRect(clipRect);

        boolean textVisible = false;

        destCanvas.save();
        if (v instanceof TextView && pruneToDrawable) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
            destCanvas.translate(padding / 2, padding / 2);
            d.draw(destCanvas);
        } else {
            /*if (v instanceof FolderIcon) {
                // For FolderIcons the text can bleed into the icon area, and so we need to
                // hide the text completely (which can't be achieved by clipping).
                if (((FolderIcon) v).getTextVisible()) {
                    ((FolderIcon) v).setTextVisible(false);
                    textVisible = true;
                }
            } else if (v instanceof BubbleTextView) {
                final BubbleTextView tv = (BubbleTextView) v;
                clipRect.bottom = tv.getExtendedPaddingTop() - (int) BubbleTextView.PADDING_V +
                        tv.getLayout().getLineTop(0);
            } else if (v instanceof TextView) {
                final TextView tv = (TextView) v;
                clipRect.bottom = tv.getExtendedPaddingTop() - tv.getCompoundDrawablePadding() +
                        tv.getLayout().getLineTop(0);
            }
            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Region.Op.REPLACE);
            v.draw(destCanvas);

            // Restore text visibility of FolderIcon if necessary
            if (textVisible) {
                ((FolderIcon) v).setTextVisible(true);
            }*/
        }
        destCanvas.restore();
    }
}
