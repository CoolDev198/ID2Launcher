package id2.id2me.com.id2launcher;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

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
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return super.onInterceptTouchEvent(event);
    }


    public void setPagerAdapter(HorizontalPagerAdapter pagerAdapter) {
        this.pagerAdapter = pagerAdapter;
    }

    public void setApplication(LauncherApplication application) {
        this.application = application;
    }
}
