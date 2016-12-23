package id2.id2me.com.id2launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
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

import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.notificationWidget.NotificationWidgetAdapter;
import id2.id2me.com.id2launcher.wallpaperEditor.MainActivity;


/**
 * Created by bliss76 on 26/05/16.
 */
public class DesktopFragment extends Fragment  {


    static DragController dragController;

    Launcher launcher;
    ObservableScrollView scrollView;
    int[] mTargetCell;
    private View fragmentView = null;
    private Context context;
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

            WorkSpace workSpace =(WorkSpace)fragmentView.findViewById(R.id.container);
            WallpaperContainer wallpaperContainer =(WallpaperContainer) fragmentView.findViewById(R.id.wallpaper_layout);
            launcher = (Launcher) getActivity();
            dragController.addDropTarget(workSpace);
            dragController.addDropTarget(wallpaperContainer);
            launcher.setWokSpace(workSpace);
            launcher.setScrollView((ObservableScrollView) fragmentView.findViewById(R.id.scrollView));
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


    }

    private void addDefaultScreens() {
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        LinearLayout containerL = (LinearLayout) fragmentView.findViewById(R.id.container);
        CellLayout child;
        int defaultScreens = launcherApplication.DEFAULT_SCREENS;
        for (int i = 0; i < defaultScreens; i++) {
            child = (CellLayout)
                    launcher.getLayoutInflater().inflate(R.layout.workspace_dragging_screen, null);
            child.setTag(i);

            if (i == 0) {
                //child.setBackgroundColor(Color.BLACK);
            } else if(i==1){
                //child.setBackgroundColor(Color.YELLOW);
            } else if(i==2){
                //child.setBackgroundColor(Color.RED);
            } else if(i==3){
                //child.setBackgroundColor(Color.GREEN);
            }

            containerL.addView(child);
        }
    }

    private void addWallpaperCropper() {
        wallpaperLayout = (RelativeLayout) fragmentView.findViewById(R.id.wallpaper_layout);
       // LauncherApplication.wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);
        wallpaperLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                return false;
            }
        });

    }

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


}


