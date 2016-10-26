package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

                application.viewList = new ArrayList<>();
                fragmentList = new ArrayList<>();


                db = DatabaseHandler.getInstance(context);
                mModel = application.setDeskTopFragment(this);

                fragmentView = inflater.inflate(R.layout.desktop_fragment, container, false);

                initViews();
                //  startTimer();

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


//        blur_relative = (RelativeLayout) fragmentView.findViewById(R.id.blur_relative);
//        blur_relative.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                //scrollView.setVisibility(View.VISIBLE);
//                return true;
//            }
//        });

//
        scrollView = (ObservableScrollView) fragmentView.findViewById(R.id.scrollView);


        addDragListener();

//        if (DatabaseHandler.itemInfosList != null && !DatabaseHandler.itemInfosList.isEmpty()) {
//            populateDesktop();
//        }
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
        wallpaperLayout = (FrameLayout) fragmentView.findViewById(R.id.wallpaper_layout);
        wallpaperLayout.setTag(0);
//        parentLayout = (FrameLayout) fragmentView.findViewById(R.id.relative_view);
//        parentLayout.setTag(1);
//        FrameLayout parentLayoutSecond = (FrameLayout) fragmentView.findViewById(R.id.relative_view_second);
//        parentLayoutSecond.setTag(2);
//        FrameLayout parentLayoutThird = (FrameLayout) fragmentView.findViewById(R.id.relative_view_third);
//        parentLayoutThird.setTag(3);
//        FrameLayout parentLayoutFourth = (FrameLayout) fragmentView.findViewById(R.id.relative_view_fourth);
//        parentLayoutFourth.setTag(4);

        PageDragListener pageDragListenerZero = new PageDragListener(context, fragmentView, wallpaperLayout);

//        PageDragListener pageDragListener = new PageDragListener(context, fragmentView, parentLayout);
//        PageDragListener pageDragListenerSecond = new PageDragListener(context, fragmentView, parentLayoutSecond);
//        PageDragListener pageDragListenerThird = new PageDragListener(context, fragmentView, parentLayoutThird);
//        PageDragListener pageDragListenerFourth = new PageDragListener(context, fragmentView, parentLayoutFourth);

        wallpaperLayout.setOnDragListener(pageDragListenerZero);
//        parentLayout.setOnDragListener(pageDragListener);
//        parentLayoutSecond.setOnDragListener(pageDragListenerSecond);
//        parentLayoutThird.setOnDragListener(pageDragListenerThird);
//        parentLayoutFourth.setOnDragListener(pageDragListenerFourth);

        //change
        // application.setPageDragListener(pageDragListenerSecond);

    }

    private void addNotifyWidget() {
//        RecyclerView notificationRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.noti_widget);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
//        notificationRecyclerView.setLayoutManager(linearLayoutManager);
//        notificationWidgetAdapter = new NotificationWidgetAdapter(getActivity());
//        notificationRecyclerView.setAdapter(notificationWidgetAdapter);
//        notifyDataInNotificationWidget();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        static Context context;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        View rootView;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber, Context _context, int color) {
            PlaceholderFragment fragment = new PlaceholderFragment();

            context = _context;
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt("Color", color);
            fragment.setArguments(args);


            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            try {
                LauncherApplication launcherApplication = (LauncherApplication) ((Activity) context).getApplication();

                if (launcherApplication.viewList.size() < getArguments().getInt(ARG_SECTION_NUMBER)) {
                    rootView = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_layout, container, false);
                    Log.v("PlaceHolder", " position :: " + Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

                    PageDragListener pageDragListener = new PageDragListener(context, rootView, (FrameLayout) rootView);
                    ((FrameLayout) rootView).setOnDragListener(pageDragListener);

                    launcherApplication.viewList.add(rootView);

                    TextView textView = (TextView) rootView.findViewById(R.id.textview);
                    textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
                    rootView.setBackgroundColor(getArguments().getInt("Color"));
                } else {
                    rootView = launcherApplication.viewList.get(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rootView;


        }
    }

    public class DummyAdapter extends FragmentPagerAdapter {

        public DummyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "PAGE 1";
                case 1:
                    return "PAGE 2";
                case 2:
                    return "PAGE 3";
            }
            return null;
        }

    }


}





