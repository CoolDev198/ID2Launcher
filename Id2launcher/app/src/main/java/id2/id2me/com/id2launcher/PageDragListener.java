package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import id2.id2me.com.id2launcher.models.ItemInfoModel;


/**
 * Created by sunita on 8/9/16.
 */

class PageDragListener implements View.OnDragListener {

    final String TAG = "PageDragListener";
    final Handler handler = new Handler();
    private final LinearLayout containerL;
    LauncherApplication launcherApplication;
    Context context;
    int cellWidth, cellHeight;
    CellLayout cellLayout;
    AppWidgetProviderInfo appWidgetProviderInfo;
    int appWidgetId = 0;
    View drag_view;
    boolean isItemCanPlaced, isLoaderStarted, isRequiredCellsCalculated, isAvailableCellsGreater;
    int spanX = 1, spanY = 1, X, Y;
    int ticks = 0;
    DatabaseHandler db;
    View dropTargetLayout;
    ObservableScrollView container;
    ArrayList<View> reorderView;
    ArrayList<View> folderTempApps;
    Timer timer;
    View desktopFragment;
    private FrameLayout.LayoutParams layoutParams;
    private int[] nearestCell;
    private ItemInfoModel cellToBePlaced;
    private ItemInfoModel dragInfo;
    private LauncherAppWidgetHostView hostView;
    private TimerTask timerTask;
    private ImageView mOutlineView;
    private int mCellPaddingLeft;
    private int mCellPaddingTop;

   public PageDragListener(Context mContext, View desktopFragment, CellLayout pageLayout) {
        this.cellLayout = pageLayout;
        launcherApplication = (LauncherApplication) ((Activity) mContext).getApplication();
        cellWidth = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellWidth();
        cellHeight = ((LauncherApplication) ((Activity) mContext).getApplication()).getCellHeight();
        this.context = mContext;

        int cellWidth = (int) context.getResources().getDimension(R.dimen.cell_width);
        int cellHeight = (int) context.getResources().getDimension(R.dimen.cell_height);

        int appIconSize = (int) context.getResources().getDimension(R.dimen.app_icon_size);

        mCellPaddingLeft = (cellWidth - appIconSize) / 2;
        mCellPaddingTop = (cellHeight - appIconSize) / 2;

        System.out.println("Cell Padding l : " + mCellPaddingLeft + " t : " + mCellPaddingTop);

        db = DatabaseHandler.getInstance(context);
        this.desktopFragment = desktopFragment;
        dropTargetLayout = desktopFragment.findViewById(R.id.drop_target_layout);
        container = (ObservableScrollView) desktopFragment.findViewById(R.id.scrollView);
        containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);
        mOutlineView = (ImageView) desktopFragment.findViewById(R.id.drag_outline_img);

        init();
    }


    void init() {
        isRequiredCellsCalculated = false;
        isAvailableCellsGreater = false;
        isLoaderStarted = false;
        isItemCanPlaced = false;
        reorderView = new ArrayList<>();
        folderTempApps = new ArrayList<>();
        nearestCell = new int[2];

    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                try {
                    //  Log.v(TAG, "Drag Started");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                Log.v(TAG, "Drag Entered");
                launcherApplication.isTimerTaskCompleted = true;
                if (timer != null) {
                    timer.cancel();
                }
                dragInfo = launcherApplication.dragInfo;
                int currentScreen = Integer.parseInt(cellLayout.getTag().toString());
                launcherApplication.currentScreen = currentScreen;


               // copyActualMatricesToDragMatrices();
                drag_view = (View) event.getLocalState();
             //   calculateReqCells();

                break;

            case DragEvent.ACTION_DRAG_EXITED:
                Log.v(TAG, "Drag EXITED");

            //    switchFrame();
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                try {

                    //   Log.v(TAG, "Drag locating");
                 //   onDrag(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DROP:
                //  Log.v(TAG, "DROP Action");
                try {

                 //   onDrop();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:

                Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }



}
