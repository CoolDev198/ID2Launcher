package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.models.AppInfoModel;

/**
 * Created by Pinto on 24/09/16.
 */
public class AllAppAdapter extends RecyclerView.Adapter<AllAppAdapter.MyViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

    private static LayoutInflater inflater = null;
    DrawerLayout drawerLayout;
    ArrayList<View> items = null;
    ArrayList<AppInfoModel> list;
    HashMap<Integer, String> mapIndex;
    ArrayList<ArrayList<AppInfoModel>> groupList;
    private Activity activity;
    private LauncherApplication launcherApplication;
    int NO_OF_APPS_IN_ROW = 3;

    public AllAppAdapter(Activity activity, DrawerLayout drawerLayout) {
        try {
            this.activity = activity;
            launcherApplication = (LauncherApplication) activity.getApplication();
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.drawerLayout = drawerLayout;
            items = new ArrayList<View>();


            this.list = launcherApplication.mModel.mBgAllAppsList.data;
            makeGroups();
            makeSections();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeSections() {
        mapIndex = new LinkedHashMap<Integer, String>();
        for (int x = 0; x < groupList.size(); x++) {
            try {
                String modifyChar;
                for (int i = 0; i < groupList.get(x).size(); i++) {
                    char ch = ((ArrayList<AppInfoModel>) groupList.get(x)).get(i).getAppname().charAt(0);
                    if (ch >= 'A' && ch <= 'Z') {
                        modifyChar = Character.toString(ch).toUpperCase();

                    } else {
                        if (ch >= 'a' && ch <= 'z') {
                            modifyChar = Character.toString(ch).toUpperCase();
                        } else {
                            modifyChar = "#";
                        }
                    }

                    if (!mapIndex.containsKey(x)) {
                        mapIndex.put(x, modifyChar);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void makeGroups() {
        ArrayList<AppInfoModel> arrayList = new ArrayList<>();
        groupList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(list.get(i));
            if (arrayList.size() % 3 == 0) {
                groupList.add(arrayList);
                arrayList = new ArrayList<>();
            }
        }
        if (arrayList.size() > 0) {
            groupList.add(arrayList);
        }
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if(mapIndex.containsKey(pos)) {
            return mapIndex.get(pos);
        }else{
            return "";
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_viewitem, parent, false);
        items.add(itemView);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        setNoOfColumnsOfGrid(holder.gridView);
        AllAppsGridAdapter adapter = new AllAppsGridAdapter(activity, groupList.get(position));
        holder.gridView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        //public TextView title, year, genre;
        public AppGridView gridView;

        public MyViewHolder(View view) {
            super(view);
            gridView = (AppGridView) view.findViewById(R.id.grid);
        }

    }
}
