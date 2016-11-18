package id2.id2me.com.id2launcher;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by bliss76 on 26/05/16.
 */

public class HorizontalPagerAdapter extends FragmentPagerAdapter {
    ViewPager nonSwipeViewPager;
    private List<Fragment> fragments;

    public HorizontalPagerAdapter(FragmentManager fm, List<Fragment> fragments, ViewPager pager) {
        super(fm);
        this.nonSwipeViewPager = pager;
        this.fragments = fragments;
    }

    public void addNewFolderFragment(long folderId) {
        fragments.add(FolderFragment.newInstance(folderId));
        this.notifyDataSetChanged();

    }

    public void updateFragments(int position, ArrayList<ItemInfoModel> appInfos) {
        try {
            if (position > 1  && position < fragments.size()) {
                FolderFragment fragment = (FolderFragment) fragments.get(position);
                FolderGridAdapter folderGridAdapter = ((FolderGridAdapter) fragment.getAdapter());
                if (folderGridAdapter != null) {
                    folderGridAdapter.setAppInfos(appInfos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Fragment getItem(int position) {

        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }


}

