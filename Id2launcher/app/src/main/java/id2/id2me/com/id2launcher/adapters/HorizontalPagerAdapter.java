package id2.id2me.com.id2launcher.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.FolderFragment;
import id2.id2me.com.id2launcher.FolderGridAdapter;
import id2.id2me.com.id2launcher.database.ApplicationInfo;
import id2.id2me.com.id2launcher.general.NonSwipeViewPager;

/**
 * Created by bliss76 on 26/05/16.
 */

public class HorizontalPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    NonSwipeViewPager nonSwipeViewPager;

    public HorizontalPagerAdapter(FragmentManager fm, List<Fragment> fragments, NonSwipeViewPager pager) {
        super(fm);
        this.nonSwipeViewPager = pager;
        this.fragments = fragments;
    }

    public void addNewFolderFragment(){
        int count = fragments.size();
       fragments.add(FolderFragment.newInstance());
        this.notifyDataSetChanged();

    }
    public void updateFragments(int position , ArrayList<ApplicationInfo> appInfos){
        if(position!=0) {
            FolderFragment fragment = (FolderFragment) fragments.get(position);
            FolderGridAdapter folderGridAdapter=   ((FolderGridAdapter) fragment.getAdapter());
            folderGridAdapter.setAppInfos(appInfos);

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

