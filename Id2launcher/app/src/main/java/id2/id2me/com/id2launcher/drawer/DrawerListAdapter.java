package id2.id2me.com.id2launcher.drawer;

import android.annotation.SuppressLint;
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

import id2.id2me.com.id2launcher.general.AppGridView;
import id2.id2me.com.id2launcher.HomeActivity;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.general.Utility;
import id2.id2me.com.id2launcher.database.AppInfo;

/**
 * Created by bliss76 on 21/06/16.
 */
public class DrawerListAdapter extends BaseAdapter implements SectionIndexer
{
    private Activity activity;
    private static LayoutInflater inflater = null;
    int i = 0;
    public static HashMap<String, ArrayList<AppInfo>> sortedMap, backUpMap;
    ArrayList<AppInfo> listDigitAppInfo;
    public static List<Object> symbols;
    public static List<Object> backupSymbols;
    ArrayList<View> items = null;
    final int NO_OF_APPS_IN_ROW = 3;
    DrawerLayout drawerLayout;
    AppGridView appGridView;
    ArrayList arrayList_collection;
    // To differentiate between arrange app and home activity click events.
    boolean appClickEvent = true;

    public DrawerListAdapter(Activity activity,
                       HashMap<String, ArrayList<AppInfo>> sortedMap,
                       ArrayList<AppInfo> listDigitAppInfo, AppGridView appGridView,DrawerLayout drawerLayout, ArrayList arrayList_collection) {
        try {
            this.activity = activity;
            this.listDigitAppInfo = listDigitAppInfo;
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.sortedMap = sortedMap;
            this.appGridView=appGridView;
            this.arrayList_collection=arrayList_collection;

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

    public void refresh(){
        try {
            this.sortedMap = Utility.sortedMap;

            backUpMap = new HashMap<String, ArrayList<AppInfo>>();
            backUpMap = (HashMap<String, ArrayList<AppInfo>>) Utility.sortedMap.clone();

            this.symbols = Arrays.asList(Utility.sortedMap.keySet().toArray());

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

    public static class ViewHolder {
        public TextView symbol;
        public AppGridView gridView;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;
        String symbol = "";
        int noOfApps = 0;
        ArrayList<AppInfo> appInfos;

        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.adapter_viewitem, null);
                //vi.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
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
            setNoOfColumnsOfGrid(holder.gridView);
            setGridViewTotalHeight(noOfApps, holder.gridView);
            DrawerGridAdapter adapter = new DrawerGridAdapter(activity, appInfos, drawerLayout, appClickEvent,appGridView);
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
                    R.dimen.gridview_height);
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = totalHeight;
            gridView.setLayoutParams(params);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        Log.v("Width",""+gridView.getWidth());
       gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }

    String mSectionName;

    String mFirstSpell;
    private SideBar indexBar;

    public int getPositionForSection(int section) {
        try {
            for (int i = 0; i < HomeActivity.mNames.size(); i++) {
                mSectionName = HomeActivity.mNames.get(i);
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

    class ConverterToFirstSpellThread implements Runnable {

        public void run() {
            mFirstSpell = SpellUtil.converterToFirstSpell(mSectionName);
        }

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


}
