package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
    final Handler handler = new Handler();

    String TAG = "DesktopFragment";
    //context menu ids
    TimerTask timerTask;
    List<Fragment> fragmentList;
    private ArrayList<AppInfoModel> appInfos;
    private static View fragmentView = null;
    private Context context;
    private LauncherApplication application;
    private ImageView wallpaperImg;
    private RelativeLayout wallpaperLayout;
    private AppsListingFragment appsListingFragment;
    private DatabaseHandler db;
    private NotificationWidgetAdapter notificationWidgetAdapter;
    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            notifyDataInNotificationWidget();
        }
    };
    private LauncherModel mModel;
    private static int mCellLayoutHeight;

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
                mCellLayoutHeight = (int) getResources().getDimension(R.dimen.cell_layout_height);

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

        addNotifyWidget();

        //  addWallpaperCropper();


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

        for (int i = 0; i < application.Default_Screens; i++) {
            if (i == 0) {
                child = new CellLayout(context, R.dimen.wallpaper_cell_layout);
                child.setBackgroundColor(Color.BLUE);
            } else {
                child = new CellLayout(context, R.dimen.cell_layout_height);

            }
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

    private void addDragListener() {

        fragmentView.findViewById(R.id.main_layout).setOnDragListener(new DesktopDragListener(context, fragmentView));
        LinearLayout containerL = (LinearLayout) fragmentView.findViewById(R.id.container);
        for (int i = 1; i < containerL.getChildCount(); i++) {
            CellLayout child = (CellLayout) containerL.getChildAt(i);
            child.setTag(i);
            PageDragListener pageDragListener = new PageDragListener(context, fragmentView, child);
            child.setOnDragListener(pageDragListener);
            child.setDragListener(pageDragListener);
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


    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        super.onDestroyView();
    }

}


