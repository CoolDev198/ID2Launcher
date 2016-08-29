package id2.id2me.com.id2launcher.drawer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.AllAppsGridAdapter;
import id2.id2me.com.id2launcher.Launcher;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.general.AppGridView;

/**
 * Created by bliss76 on 21/06/16.
 */
public class AllAppsListAdapter extends BaseAdapter implements SectionIndexer {
    public static HashMap<String, ArrayList<AppInfo>> sortedMap, backUpMap;
    public static List<Object> symbols;
    public static List<Object> backupSymbols;
    private static LayoutInflater inflater = null;
    private  LauncherApplication launcherApplication;
    int i = 0;
    ArrayList<AppInfo> listDigitAppInfo;
    ArrayList<View> items = null;
    DrawerLayout drawerLayout;
    int NO_OF_APPS_IN_ROW = 3;
    String mSectionName;
    String mFirstSpell;
    private Activity activity;
    private SideBar indexBar;

    public AllAppsListAdapter(Activity activity,
                              HashMap<String, ArrayList<AppInfo>> sortedMap,
                              ArrayList<AppInfo> listDigitAppInfo, DrawerLayout drawerLayout) {
        try {
            this.activity = activity;
            launcherApplication= (LauncherApplication)activity.getApplication();
            this.listDigitAppInfo = listDigitAppInfo;
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.sortedMap = sortedMap;

            backUpMap = new HashMap<String, ArrayList<AppInfo>>();
            backUpMap = (HashMap<String, ArrayList<AppInfo>>) sortedMap.clone();

            this.symbols = Arrays.asList(sortedMap.keySet().toArray());

            this.drawerLayout = drawerLayout;

            sortKeys();

            backupSymbols = new ArrayList<Object>();

            if (listDigitAppInfo.size() != 0) {
                backupSymbols.add("#");
            }
            for (int i = 0; i < symbols.size(); i++) {
                backupSymbols.add(symbols.get(i));
            }

            items = new ArrayList<View>();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        try {
            this.sortedMap = launcherApplication.sortedMap;

            backUpMap = new HashMap<String, ArrayList<AppInfo>>();
            backUpMap = (HashMap<String, ArrayList<AppInfo>>) launcherApplication.sortedMap.clone();

            this.symbols = Arrays.asList(launcherApplication.sortedMap.keySet().toArray());

            sortKeys();

            backupSymbols = new ArrayList<Object>();

            if (listDigitAppInfo.size() != 0) {
                backupSymbols.add("#");
            }
            for (int i = 0; i < symbols.size(); i++) {
                backupSymbols.add(symbols.get(i));
            }

            items = new ArrayList<View>();

            //  SideBar.mLetter.clear();

            indexBar.setSideBarLetters(backupSymbols);

            indexBar.invalidate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void sortKeys() {
        try {
            Collections.sort(symbols, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
        String symbol = "";
        int noOfApps = 0;
        ArrayList<AppInfo> appInfos;

        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.adapter_viewitem, null);
                items.add(vi);
                holder = new ViewHolder();
                holder.symbol = (TextView) vi.findViewById(R.id.txt_symbol);
                holder.gridView = (AppGridView) vi.findViewById(R.id.grid);
                vi.setTag(holder);

                if (position == 0) {

                    indexBar.setSideBarLetters(backupSymbols);
                }

            } else {
                holder = (ViewHolder) vi.getTag();
            }

            symbol = backupSymbols.get(position).toString();

            if (position == 0) {

                if (listDigitAppInfo.size() != 0) {
                    noOfApps = listDigitAppInfo.size();
                    appInfos = listDigitAppInfo;
                } else {
                    noOfApps = sortedMap.get(symbol).size();
                    appInfos = sortedMap.get(symbol);
                }


                mSectionName = appInfos.get(0).getAppname();

            } else {
                noOfApps = sortedMap.get(symbol).size();
                appInfos = sortedMap.get(symbol);
                mSectionName = appInfos.get(0).getAppname();
            }

            holder.symbol.setText(symbol);
            setColumnWidth(holder.gridView);
            setGridViewTotalHeight(noOfApps, holder.gridView);
            setNoOfColumnsOfGrid(holder.gridView);
            AllAppsGridAdapter adapter = new AllAppsGridAdapter(activity, appInfos, drawerLayout);
            holder.gridView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    @Override
    public int getCount() {
        return backupSymbols.size();
    }

    void setGridViewTotalHeight(int noOfApps, GridView gridView) {
        try {
            int noOfRows = 0;
            if (noOfApps % 3 == 0) {
                noOfRows = noOfApps / 3;
            } else {
                noOfRows = (noOfApps / 3) + 1;
            }
            Log.v("rows", noOfRows + " " + noOfApps);
            int totalHeight = (noOfRows)
                    * (int) activity.getResources().getDimension(
                    R.dimen.cell_height);
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = totalHeight;
            gridView.setLayoutParams(params);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        Log.v("Width", "" + gridView.getWidth());
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }

    public int getPositionForSection(int section) {
        try {
            for (int i = 0; i < Launcher.mNames.size(); i++) {
                mSectionName = Launcher.mNames.get(i);
                new ConverterToFirstSpellThread().run();
                final char firstChar = mFirstSpell.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public int getSectionForPosition(int position) {
        return 0;
    }

    public Object[] getSections() {
        return null;
    }

    public void setSideBar(SideBar indexBar) {
        // TODO Auto-generated method stub
        this.indexBar = indexBar;
    }

    public static class ViewHolder {
        public TextView symbol;
        public AppGridView gridView;
    }

    class ConverterToFirstSpellThread implements Runnable {

        public void run() {
            mFirstSpell = SpellUtil.converterToFirstSpell(mSectionName);
        }

    }


}
