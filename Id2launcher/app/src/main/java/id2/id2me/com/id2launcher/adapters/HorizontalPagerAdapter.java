package id2.id2me.com.id2launcher.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.FirstFragment;
import id2.id2me.com.id2launcher.MyFragment;
import id2.id2me.com.id2launcher.folder.FolderFragmentInterface;
import id2.id2me.com.id2launcher.drawer.DrawerHandler;
import id2.id2me.com.id2launcher.database.AppInfo;

/**
 * Created by bliss76 on 26/05/16.
 */
public class HorizontalPagerAdapter extends FragmentPagerAdapter implements FolderFragmentInterface
{
    private List<String>fragmentNames;
    private List<Fragment> fragments;
    Context context;
    DrawerLayout drawerLayout;
    DrawerHandler drawerHandler;


    public HorizontalPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> fragmentNames) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
        this.fragmentNames = fragmentNames;
        FirstFragment.setListner(this);
    }


    @Override
    public Fragment getItem(int position) {

        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public void addOrUpdateFolder(String name, ArrayList<AppInfo> appInfos) {

        if(!fragmentNames.contains(name)){
            fragmentNames.add(name);
            fragments.add(MyFragment.newInstance(appInfos));
            this.notifyDataSetChanged();

        }
    }







}

