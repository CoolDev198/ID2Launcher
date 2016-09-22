package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import id2.id2me.com.id2launcher.notificationWidget.NotificationWidgetAdapter;
import id2.id2me.com.id2launcher.wallpaperEditor.MainActivity;
import jp.wasabeef.blurry.Blurry;


/**
 * Created by bliss76 on 26/05/16.
 */
public class DesktopFragment extends Fragment implements DrawerHandler, LauncherModel.Callbacks {
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    final int CONTEXT_MENU_CAMERA = 1;
    final int CONTEXT_MENU_GALLERY = 2;
    final Handler handler = new Handler();
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    String TAG = "DesktopFragment";
    //context menu ids
    Dialog customDialog;
    Timer timer;
    TimerTask timerTask;
    private ArrayList<ApplicationInfo> appInfos;
    private DrawerLayout drawer;
    private View fragmentView = null;
    private Context context;
    private LauncherApplication application;
    private FrameLayout parentLayout;
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
    private ObservableScrollView scrollView;
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
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, new IntentFilter("Msg"));

            application = (LauncherApplication) ((Activity) context).getApplication();
            if (application.desktopFragment == null) {

                  /* communicator between notification service and activity*/

                db = DatabaseHandler.getInstance(context);
                mModel = application.setDeskTopFragment(this);

                fragmentView = inflater.inflate(R.layout.desktop_fragment, container, false);

                initViews();
                setDrawerWidth();

                startTimer();
                ViewPager viewPager = (ViewPager) fragmentView.findViewById(R.id.viewpager);
                setupViewPagerAsInnerFragment(viewPager);
                TabLayout tabLayout = (TabLayout) fragmentView.findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);
                changeTabsFont(tabLayout);


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

    private void changeTabsFont(TabLayout tabLayout) {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(application.getTypeFace());
                }
            }
        }
    }

    private void setupViewPagerAsInnerFragment(ViewPager viewPager) {
        if (viewPager.getChildCount() == 0) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager()); // Nested Fragment
            appsListingFragment = AppsListingFragment.newInstance(drawer);
            adapter.addFragment(appsListingFragment, "Apps");
            adapter.addFragment(WidgetsListingFragment.newInstance(drawer), "Widgets");
            viewPager.setAdapter(adapter);
        }
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
        appsListingFragment.setListAdapter();
        for (int k = 0; k < packageNames.size(); k++) {
            for (int i = 0; i < parentLayout.getChildCount(); i++) {

                View child = parentLayout.getChildAt(i);
                ItemInfo cellInfo = (ItemInfo) child.getTag();
                if (cellInfo.getIsAppOrFolderOrWidget() == 1) {

                    if (cellInfo.getAppInfo().getPname().equalsIgnoreCase(packageNames.get(k))) {
                        parentLayout.removeView(child);
                        application.getPageDragListener().unMarkCells(cellInfo.getMatrixCells());
                        break;
                    }

                } else if (cellInfo.getIsAppOrFolderOrWidget() == 2) {
                    FolderInfo folderInfo = cellInfo.getFolderInfo();
//                    ArrayList<ApplicationInfo> applicationInfos = folderInfo.getAppInfos();
//                    for (int j = 0; j < applicationInfos.size(); i++) {
//                        if (applicationInfos.get(j).getPname().equalsIgnoreCase(packageNames.get(k))) {
//                            folderInfo.deleteAppInfo(applicationInfos.get(j));
//                            break;
//                        }
//
//                    }
                }
            }
        }
    }


    private void initViews() {

        wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);
        drawer = (DrawerLayout) fragmentView.findViewById(R.id.drawer_layout);

        if (drawer != null) {
            drawer.setDrawerListener(new MyDrawerListener(this, context, drawer));
        }

        addDragListener();

        addNotifyWidget();

        addWallpaperCropper();


        blur_relative = (RelativeLayout) fragmentView.findViewById(R.id.blur_relative);
        blur_relative.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.setVisibility(View.VISIBLE);
                return true;
            }
        });

        scrollView = (ObservableScrollView) fragmentView.findViewById(R.id.scrollView);
        scrollView.setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

                View view = scrollView.findViewById(R.id.wallpaper_img);
                if (view != null) {
                    view.setTranslationY(scrollView.getScrollY() / 2);
                }

            }
        });


        if (DatabaseHandler.itemInfosList != null && !DatabaseHandler.itemInfosList.isEmpty()) {
            populateDesktop();
        }
    }

    private void addWallpaperCropper() {
        wallpaperLayout = (RelativeLayout) fragmentView.findViewById(R.id.wallpaper_layout);
        LauncherApplication.wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);
        wallpaperLayout.setOnDragListener(new WallpaperDragListener(getActivity(), application.getPageDragListener(), fragmentView.findViewById(R.id.layout_remove), fragmentView.findViewById(R.id.layout_uninstall)));
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
        PageDragListener pageDragListener = new PageDragListener(context, fragmentView);
        parentLayout = (FrameLayout) fragmentView.findViewById(R.id.relative_view);
        parentLayout.setLayoutParams(new LinearLayout.LayoutParams(application.getScreenWidth(), application.getScreenHeight()));
        parentLayout.setOnDragListener(pageDragListener);
        application.setPageDragListener(pageDragListener);

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

        for (int i = 0; i < DatabaseHandler.itemInfosList.size(); i++) {
            ItemInfo itemInfo = DatabaseHandler.itemInfosList.get(i);
            int type = itemInfo.getItemType();

            int width = application.getCellWidth() * itemInfo.getSpanX();
            int height = application.getCellHeight() * itemInfo.getSpanY();


            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
            int leftMargin = itemInfo.getCellX() * application.getCellWidth() + application.getMaxGapLR() * (itemInfo.getCellX() + 1);
            int topMargin = itemInfo.getCellX() * application.getCellWidth() + application.getMaxGapLR() * (itemInfo.getCellX() + 1);
            layoutParams.setMargins(leftMargin, topMargin, 0, 0);

            HashMap<Integer, FolderInfo> folderInfoHashMap = new HashMap<>();

            switch (type) {

                case DatabaseHandler.ITEM_TYPE_APP:
                    switch (itemInfo.getContainer()) {
                        case DatabaseHandler.CONTAINER_DESKTOP:
                            application.getPageDragListener().addAppToPage(ItemInfo.createIconBitmap(BitmapFactory.decodeByteArray(itemInfo.getIcon(), 0, itemInfo.getIcon().length), context), itemInfo, layoutParams);
                            break;
                        default:
                            if (!folderInfoHashMap.containsKey(itemInfo.getContainer())) {
                                FolderInfo folderInfo = new FolderInfo();
                                folderInfo.setFolderId(itemInfo.getContainer());
                                folderInfoHashMap.put(itemInfo.getContainer(), folderInfo);
                                folderInfo.addNewItemInfo(itemInfo);
                            } else {
                                FolderInfo folderInfo = folderInfoHashMap.get(itemInfo.getContainer());
                                folderInfo.addNewItemInfo(itemInfo);
                            }
                            break;
                    }
                    break;
                case DatabaseHandler.ITEM_TYPE_FOLDER:

                     application.getPageDragListener().addFolderToPage(ItemInfo.createIconBitmap(BitmapFactory.decodeByteArray(itemInfo.getIcon(), 0, itemInfo.getIcon().length), context), itemInfo, layoutParams);

                    if (!folderInfoHashMap.containsKey(itemInfo.getContainer())) {
                        FolderInfo folderInfo = new FolderInfo();
                        folderInfo.setFolderId((int) itemInfo.getId());
                        folderInfoHashMap.put(itemInfo.getContainer(), folderInfo);
                    }

                    break;
                case DatabaseHandler.ITEM_TYPE_APPWIDGET:
                    break;
            }
        }
    }


    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        Blurry.with(context)
                                .radius(25)
                                .sampling(1)
                                .color(Color.argb(66, 255, 255, 255))
                                .async()
                                .capture(scrollView)
                                .into(blur_relative);
                    }
                });
            }
        };
    }


    private void setDrawerWidth() {
        try {
            RelativeLayout leftDrawer = (RelativeLayout) fragmentView.findViewById(R.id.left_drawer_layout);
            ViewGroup.LayoutParams params = leftDrawer.getLayoutParams();
            params.width = ((LauncherApplication) getActivity().getApplication()).getScreenWidth();
            leftDrawer.setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawerOpen() {
        application.isDrawerOpen = true;
        drawer.openDrawer(Gravity.LEFT);
    }

    @Override
    public void drawerClose() {
        application.isDrawerOpen = false;
        drawer.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        super.onDestroyView();
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {


        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}





