package id2.id2me.com.id2launcher;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.List;

/**
 * Created by bliss76 on 26/05/16.
 */

public class HorizontalPagerAdapter extends FragmentStatePagerAdapter {
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

    public void updateFragments(long folderId) {
        try {

            for (int i = 2; i < fragments.size(); i++) {
                if (fragments.get(i) instanceof FolderFragment) {
                    FolderFragment folderFragment = (FolderFragment) fragments.get(i);
                    if (folderFragment.folderId == folderId) {
                        folderFragment.updateView();
                        break;
                    }
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

