package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import id2.id2me.com.id2launcher.models.AppInfoModel;

/**
 * Created by bliss76 on 21/06/16.
 */
public class AllAppsListAdapter extends BaseAdapter implements SectionIndexer {
    private static LayoutInflater inflater = null;
    private LauncherApplication launcherApplication;
    ArrayList<AppInfoModel> list;
    ArrayList<ArrayList<AppInfoModel>> groupList;
    ArrayList<View> items = null;
    DrawerLayout drawerLayout;
    int NO_OF_APPS_IN_ROW = 3;
    private Activity activity;
    HashMap<String, Integer> mapIndex;
    String[] sections;

    public AllAppsListAdapter(Activity activity, DrawerLayout drawerLayout) {
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
        mapIndex = new LinkedHashMap<String, Integer>();
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

                    if (!mapIndex.containsKey(modifyChar)) {
                        mapIndex.put(modifyChar, x);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Log.v("sectionList", sectionList.toString());
        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);
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

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.adapter_viewitem, null);
                items.add(vi);
                holder = new ViewHolder();
                holder.gridView = (AppGridView) vi.findViewById(R.id.grid);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            setNoOfColumnsOfGrid(holder.gridView);
            AllAppsGridAdapter adapter = new AllAppsGridAdapter(activity, groupList.get(position), drawerLayout);
            holder.gridView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }

    public int getPositionForSection(int section) {
        Log.d("section", "" + section);
        return mapIndex.get(sections[section]);
    }

    public int getSectionForPosition(int position) {
        Log.d("position", "" + position);
        return 0;
    }

    public Object[] getSections() {
        return sections;
    }

    public static class ViewHolder {
        public AppGridView gridView;
    }


}
