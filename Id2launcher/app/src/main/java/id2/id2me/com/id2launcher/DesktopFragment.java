package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.notificationWidget.NotificationWidgetAdapter;
import id2.id2me.com.id2launcher.wallpaperEditor.MainActivity;


/**
 * Created by bliss76 on 26/05/16.
 */
public class DesktopFragment extends Fragment implements LauncherModel.Callbacks {
    private static final float MIN_SCALE = 0.75f;
    static DragController dragController;
    final Handler handler = new Handler();
    String TAG = "DesktopFragment";
    //context menu ids
    TimerTask timerTask;
    List<Fragment> fragmentList;
    ObservableScrollView scrollView;
    int[] mTargetCell;
    private ArrayList<AppInfoModel> appInfos;
    private View fragmentView = null;
    private Context context;
    private LauncherApplication application;
    private ImageView wallpaperImg;
    private RelativeLayout wallpaperLayout;
    private DatabaseHandler db;
    private NotificationWidgetAdapter notificationWidgetAdapter;
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            notifyDataInNotificationWidget();
        }
    };
    private LauncherModel mModel;
    private LinearLayout container;

    public static DesktopFragment newInstance(DragController _dragController) {
        DesktopFragment f = new DesktopFragment();
        dragController = _dragController;
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            // LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, new IntentFilter("Msg"));

            fragmentView = inflater.inflate(R.layout.desktop_fragment, container, false);
            ((Launcher)context).setWokSpace((WorkSpace) fragmentView);

            application = (LauncherApplication) ((Activity) context).getApplication();

            db = DatabaseHandler.getInstance(context);


            initViews();


        } catch (Exception e) {
            e.printStackTrace();
        }


        return fragmentView;

    }

    // Refresh notification widget
    public void notifyDataInNotificationWidget() {
        db.getNotificationData();
        notificationWidgetAdapter.notifyDataSetChanged();

    }

    @Override
    public void bindAppsAdded() {
       // appsListingFragment.setListAdapter();

    }

    @Override
    public void bindAppsUpdated() {
     //   appsListingFragment.setListAdapter();
    }

    @Override
    public void bindAppsRemoved(ArrayList<String> packageNames) {
//        appsListingFragment.setListAdapter();
//        for (int k = 0; k < packageNames.size(); k++) {
//            for (int i = 0; i < parentLayout.getChildCount(); i++) {
//
//                View child = parentLayout.getChildAt(i);
//                ItemInfoModel cellInfo = (ItemInfoModel) child.getTag();
//                if (cellInfo.getIsAppOrFolderOrWidget() == 1) {
//
//                    if (cellInfo.getAppInfo().getPname().equalsIgnoreCase(packageNames.get(k))) {
//                        parentLayout.removeView(child);
//                        application.getPageDragListener().unMarkCells(cellInfo.getMatrixCells());
//                        break;
//                    }
//
//                } else if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
//                    FolderInfoModel folderInfo = cellInfo.getFolderInfo();
////                    ArrayList<AppInfoModel> applicationInfos = folderInfo.getAppInfos();
////                    for (int j = 0; j < applicationInfos.size(); i++) {
////                        if (applicationInfos.get(j).getPname().equalsIgnoreCase(packageNames.get(k))) {
////                            folderInfo.deleteAppInfo(applicationInfos.get(j));
////                            break;
////                        }
////
////                    }
//                }
//            }
//        }
    }

    private void initViews() {

        addNotifyWidget();

        //  addWallpaperCropper();


        scrollView = (ObservableScrollView) fragmentView.findViewById(R.id.scrollView);
        container = (LinearLayout) fragmentView.findViewById(R.id.container);
        scrollView.setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

                try {
                    View view = fragmentView.findViewById(R.id.wallpaper_img);
                    if (view != null) {
                        view.setTranslationY(scrollView.getScrollY() / 2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        addDefaultScreens();
        // addDragListener();


    }

    private void addDefaultScreens() {
        LinearLayout containerL = (LinearLayout) fragmentView.findViewById(R.id.container);
        CellLayout child;
        int defaultScreens = application.DEFAULT_SCREENS;
        for (int i = 0; i < defaultScreens; i++) {
            child = new CellLayout(context);
            child.setTag(i);
//
//if(i==0){
//    child.setBackgroundColor(Color.BLACK);
//}else{
//    child.setBackgroundColor(Color.YELLOW);
//}

            containerL.addView(child);
        }
    }

    private void addWallpaperCropper() {
        wallpaperLayout = (RelativeLayout) fragmentView.findViewById(R.id.wallpaper_layout);
        LauncherApplication.wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);
        wallpaperLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                return false;
            }
        });

    }

//    private void populateDesktop() {
//        HashMap<Long, FolderInfoModel> folderInfoHashMap = new HashMap<>();
//
//        for (int i = 0; i < DatabaseHandler.itemInfosList.size(); i++) {
//            ItemInfoModel itemInfo = DatabaseHandler.itemInfosList.get(i);
//            int type = itemInfo.getItemType();
//
//            int width = application.getCellWidth() * itemInfo.getSpanX();
//            int height = application.getCellHeight() * itemInfo.getSpanY();
//
//
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
//            int leftMargin = itemInfo.getCellX() * application.getCellWidth();
//            int topMargin = itemInfo.getCellY() * application.getCellWidth();
//            layoutParams.setMargins(leftMargin, topMargin, 0, 0);
//
//
//            switch (type) {
//
//                case DatabaseHandler.ITEM_TYPE_APP:
//
//                    if (itemInfo.getContainer() == DatabaseHandler.CONTAINER_DESKTOP) {
//                        application.getPageDragListener().addAppToPage(ItemInfoModel.createIconBitmap(BitmapFactory.decodeByteArray(itemInfo.getIcon(), 0, itemInfo.getIcon().length), context), itemInfo, layoutParams);
//                    }
//                    break;
//                case DatabaseHandler.ITEM_TYPE_FOLDER:
//
//                    application.getPageDragListener().addFolderToPage(ItemInfoModel.createIconBitmap(BitmapFactory.decodeByteArray(itemInfo.getIcon(), 0, itemInfo.getIcon().length), context), itemInfo, layoutParams);
//
//                    break;
//                case DatabaseHandler.ITEM_TYPE_APPWIDGET:
//                    application.getPageDragListener().addWidgetToPage(itemInfo.getAppWidgetId(), itemInfo, layoutParams);
//
//                    break;
//            }
//        }
//    }

    private void addNotifyWidget() {
        RecyclerView notificationRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.noti_widget);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        notificationRecyclerView.setLayoutManager(linearLayoutManager);
        notificationWidgetAdapter = new NotificationWidgetAdapter(getActivity());
        notificationRecyclerView.setAdapter(notificationWidgetAdapter);
        notifyDataInNotificationWidget();
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        super.onDestroyView();
    }

  //  @Override
    public void enterScrollArea(int y, int x, MotionEvent event) {
        LinearLayout containerL = (LinearLayout) fragmentView.findViewById(R.id.container);

        for (int i = 0; i < containerL.getChildCount(); i++) {
            Rect rect = new Rect();
            View cellLayout = container.getChildAt(i);
            cellLayout.getHitRect(rect);

            int xN = x + scrollView.getScrollX();
            int yN = y + scrollView.getScrollY();

            Log.v("top :: ", cellLayout.getTop() + "  " + i);
            if (rect.contains(xN, yN)) {

                //  Log.v("actual ::: ", " id :: " + i + "  x ::: y :: " + xN + "  " + yN);
                // Log.v("actual values ::: ", " x :: y "  + (i + 1) +  " " + x + "  " + "   " + (y-(cellLayout.getTop()-scrollView.getScrollY())));
                if (cellLayout instanceof CellLayout) {
                    int ycalc = y - (cellLayout.getTop() - scrollView.getScrollY());
                    mTargetCell = ((CellLayout) cellLayout).findNearestArea(x, ycalc, 1, 1, mTargetCell);
                    Log.v("cell location  :::  id ", (i + 1) + " y ::" + ycalc + " cellx : " + mTargetCell[0] + "celly : " + mTargetCell[1]);

                }
            }

        }

        if (y > scrollView.getHeight() - 120) {
            scrollView.smoothScrollBy(0, 25);
        } else if (y < 100) {
            scrollView.smoothScrollBy(0, -25);
        }

    }
}


