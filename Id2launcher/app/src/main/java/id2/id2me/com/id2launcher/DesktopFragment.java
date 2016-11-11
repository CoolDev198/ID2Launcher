package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.FolderInfoModel;
import id2.id2me.com.id2launcher.models.ItemInfoModel;
import id2.id2me.com.id2launcher.notificationWidget.NotificationWidgetAdapter;


/**
 * Created by bliss76 on 26/05/16.
 */
public class DesktopFragment extends Fragment implements LauncherModel.Callbacks {
    private static final float MIN_SCALE = 0.75f;
    private static final int Default_Screens = 4;
    final Handler handler = new Handler();

    String TAG = "DesktopFragment";
    //context menu ids
    Dialog customDialog;
    Timer timer;
    TimerTask timerTask;
    List<Fragment> fragmentList;
    private ArrayList<AppInfoModel> appInfos;
    private View fragmentView = null;
    private Context context;
    private LauncherApplication application;
    private FrameLayout parentLayout;
    private ImageView wallpaperImg;
    private FrameLayout wallpaperLayout;
    private AppsListingFragment appsListingFragment;
    private DatabaseHandler db;
    private NotificationWidgetAdapter notificationWidgetAdapter;
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            notifyDataInNotificationWidget();
        }
    };
    private View blur_relative;
    private LauncherModel mModel;

    public static DesktopFragment newInstance() {
        DesktopFragment f = new DesktopFragment();

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

            application = (LauncherApplication) ((Activity) context).getApplication();
            if (application.desktopFragment == null) {

                  /* communicator between notification service and activity*/

                application.viewList = new ArrayList<>();
                fragmentList = new ArrayList<>();


                db = DatabaseHandler.getInstance(context);
                mModel = application.setDeskTopFragment(this);

                fragmentView = inflater.inflate(R.layout.desktop_fragment, container, false);

                initViews();

                application.desktopFragment = fragmentView;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return application.desktopFragment;

    }

    // Refresh notification widget
    public void notifyDataInNotificationWidget() {
        db.getNotificationData();
        notificationWidgetAdapter.notifyDataSetChanged();

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, 500); //
    }

    @Override
    public void bindAppsAdded() {
        appsListingFragment.setListAdapter();

    }

    @Override
    public void bindAppsUpdated() {
        appsListingFragment.setListAdapter();
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

        //wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);


        addNotifyWidget();

        addWallpaperCropper();


        ObservableScrollView scrollView = (ObservableScrollView) fragmentView.findViewById(R.id.scrollView);
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
        addDragListener();


    }

    private void addDefaultScreens() {
        LinearLayout containerL = (LinearLayout) fragmentView.findViewById(R.id.container);
        CellLayout child;

        for (int i = 0; i < Default_Screens; i++) {
            if (i == 0) {
                child = new CellLayout(context, R.dimen.wallpaper_cell_layout);
            } else {
                child = new CellLayout(context, R.dimen.cell_layout_height);

            }
            containerL.addView(child);
        }
    }

    private void addWallpaperCropper() {
        //  wallpaperLayout = (RelativeLayout) fragmentView.findViewById(R.id.wallpaper_layout);
//        LauncherApplication.wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);
//        //wallpaperLayout.setOnDragListener(new WallpaperDragListener(getActivity(), application.getPageDragListener(), fragmentView.findViewById(R.id.layout_remove), fragmentView.findViewById(R.id.layout_uninstall)));
//        wallpaperLayout.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Intent intent = new Intent(getActivity(), MainActivity.class);
//                startActivity(intent);
//                return false;
//            }
//        });

    }

    private void addDragListener() {

        LinearLayout containerL = (LinearLayout) fragmentView.findViewById(R.id.container);
        for (int i = 1; i < containerL.getChildCount(); i++) {
            CellLayout child = (CellLayout) containerL.getChildAt(i);
            child.setTag(i);
            child.setOnDragListener(new PageDragListener(context, fragmentView, child));
        }
    }

    private void addNotifyWidget() {
        RecyclerView notificationRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.noti_widget);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        notificationRecyclerView.setLayoutManager(linearLayoutManager);
        notificationWidgetAdapter = new NotificationWidgetAdapter(getActivity());
        notificationRecyclerView.setAdapter(notificationWidgetAdapter);
        notifyDataInNotificationWidget();
    }

    private void populateDesktop() {
        HashMap<Long, FolderInfoModel> folderInfoHashMap = new HashMap<>();

        for (int i = 0; i < DatabaseHandler.itemInfosList.size(); i++) {
            ItemInfoModel itemInfo = DatabaseHandler.itemInfosList.get(i);
            int type = itemInfo.getItemType();

            int width = application.getCellWidth() * itemInfo.getSpanX();
            int height = application.getCellHeight() * itemInfo.getSpanY();


            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
            int leftMargin = itemInfo.getCellX() * application.getCellWidth() + application.getMaxGapLR() * (itemInfo.getCellX());
            int topMargin = itemInfo.getCellY() * application.getCellWidth() + application.getMaxGapTB() * (itemInfo.getCellY());
            layoutParams.setMargins(leftMargin, topMargin, 0, 0);


            switch (type) {

                case DatabaseHandler.ITEM_TYPE_APP:

                    if (itemInfo.getContainer() == DatabaseHandler.CONTAINER_DESKTOP) {
                        application.getPageDragListener().addAppToPage(ItemInfoModel.createIconBitmap(BitmapFactory.decodeByteArray(itemInfo.getIcon(), 0, itemInfo.getIcon().length), context), itemInfo, layoutParams);
                    }
                    break;
                case DatabaseHandler.ITEM_TYPE_FOLDER:

                    application.getPageDragListener().addFolderToPage(ItemInfoModel.createIconBitmap(BitmapFactory.decodeByteArray(itemInfo.getIcon(), 0, itemInfo.getIcon().length), context), itemInfo, layoutParams);

                    break;
                case DatabaseHandler.ITEM_TYPE_APPWIDGET:
                    application.getPageDragListener().addWidgetToPage(itemInfo.getAppWidgetId(), itemInfo, layoutParams);

                    break;
            }
        }
    }

    public void initializeTimerTask() {

//        timerTask = new TimerTask() {
//            public void run() {
//
//                //use a handler to run a toast that shows the current timestamp
//                handler.post(new Runnable() {
//                    public void run() {
//                        //get the current timeStamp
//                        Blurry.with(context)
//                                .radius(25)
//                                .sampling(1)
//                                .color(Color.argb(66, 255, 255, 255))
//                                .async()
//                                .capture(scrollView)
//                                .into(blur_relative);
//                    }
//                });
//            }
//        };
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        super.onDestroyView();
    }


}





