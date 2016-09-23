package id2.id2me.com.id2launcher;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.HorizontalPagerAdapter;
import id2.id2me.com.id2launcher.DrawerHandler;

/**
 * Created by bliss105 on 15/07/16.
 */
public class NonSwipeViewPager extends ViewPager {

    DrawerHandler handler;
    private HorizontalPagerAdapter pagerAdapter;
    private LauncherApplication application;

    public NonSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                try {
                    if (position != 0) {
                     //   pagerAdapter.updateFragments(position, application.folderFragmentsInfo.get(position - 1).getAppInfos());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!application.isDrawerOpen) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (!application.isDrawerOpen) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }


    public void setPagerAdapter(HorizontalPagerAdapter pagerAdapter) {
        this.pagerAdapter = pagerAdapter;
    }

    public void setApplication(LauncherApplication application) {
        this.application = application;
    }
}
