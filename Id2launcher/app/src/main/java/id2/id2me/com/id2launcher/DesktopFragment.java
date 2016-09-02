package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.drawer.DrawerHandler;
import id2.id2me.com.id2launcher.drawer.MyDrawerListener;
import id2.id2me.com.id2launcher.folder.FolderFragmentInterface;
import id2.id2me.com.id2launcher.general.AllAppsListManager;

/**
 * Created by bliss76 on 26/05/16.
 */
public class DesktopFragment extends Fragment implements DrawerHandler, View.OnLongClickListener {
    private ArrayList<AppInfo> appInfos;
    private DrawerLayout drawer;
    private View fragmentView = null;
    private Context context;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    private LauncherApplication application;
    private FrameLayout parentLayout;
    private ImageView wallpaperImg;

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
            application = (LauncherApplication) ((Activity) context).getApplication();
            if (application.desktopFragmentView == null) {

                fragmentView = inflater.inflate(R.layout.desktop_fragment, container, false);


                loadApps();
                application.desktopFragmentView = fragmentView;
                initViews();
                updateObjectsFromDatabase();
                setDrawerWidth();


                ViewPager viewPager = (ViewPager) fragmentView.findViewById(R.id.viewpager);
                setupViewPagerAsInnerFragment(viewPager);
                TabLayout tabLayout = (TabLayout) fragmentView.findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);
                changeTabsFont(tabLayout);

                ObservableScrollView scrollView = (ObservableScrollView) fragmentView.findViewById(R.id.scrollView);

                scrollView.setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
                    @Override
                    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

                        View view = scrollView.findViewById(R.id.wallpaper_img);

                        if (view != null) {
                            view.setTranslationY(scrollView.getScrollY() / 2);
                        }

                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return application.desktopFragmentView;
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
            adapter.addFragment(AppsListingFragment.newInstance(drawer, appInfos), "Apps");
            adapter.addFragment(WidgetsListingFragment.newInstance(drawer), "Widgets");
            viewPager.setAdapter(adapter);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId()==R.id.wallpaper_img){
            startWallpaperChooserActivity();

        }
        return true;
    }

    public void setWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wallpaperImg.setImageDrawable(wallpaperDrawable);
        }
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

    private void updateObjectsFromDatabase() {
    }


    private void initViews() {
        wallpaperImg = (ImageView) fragmentView.findViewById(R.id.wallpaper_img);
        wallpaperImg.setOnLongClickListener(this);
        drawer = (DrawerLayout) fragmentView.findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.setDrawerListener(new MyDrawerListener(this, context, drawer));
        }
        parentLayout = (FrameLayout) fragmentView.findViewById(R.id.relative_view);


        PageDragListener pageDragListener = new PageDragListener(context, parentLayout);
        parentLayout.setLayoutParams(new LinearLayout.LayoutParams(application.getScreenWidth(), application.getScreenHeight()));
        application.setPageDragListener(pageDragListener);

       /* parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (application.folderView != null) {
                    parentLayout.removeView(application.folderView);
                    application.folderView = null;
                }
            }
        });*/
        parentLayout.setOnDragListener(application.getPageDragListener());

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

    private void loadApps() {
        try {
            this.appInfos = new AllAppsListManager().getVisibleInstalledApps(context);
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

    private void startWallpaperChooserActivity() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SET_WALLPAPER);
        intent.setClassName("com.android.wallpaperchooser", "com.android.wallpaperchooser.WallpaperPickerActivity");
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}





