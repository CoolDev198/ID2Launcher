package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by Pinto on 24/09/16.
 */
public class AllAppAdapter extends BaseAdapter implements RecyclerViewFastScroller.BubbleTextGetter, View.OnClickListener, View.OnLongClickListener {

    private static LayoutInflater inflater = null;
    DrawerLayout drawerLayout;
    ArrayList<View> items = null;
    ArrayList<AppInfoModel> groupList;
    HashMap<Integer, String> mapIndex;
    //ArrayList<ArrayList<AppInfoModel>> groupList;
    private Activity activity;
    private LauncherApplication launcherApplication;
    int NO_OF_APPS_IN_ROW = 3;
    private Context mContext;

    public AllAppAdapter(Activity activity, DrawerLayout drawerLayout) {
        try {
            this.activity = activity;
            this.mContext = activity;
            launcherApplication = (LauncherApplication) activity.getApplication();
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.drawerLayout = drawerLayout;
            items = new ArrayList<View>();


            this.groupList = launcherApplication.mModel.mBgAllAppsList.data;
            //makeGroups();
            makeSections();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void makeSections() {
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
    }*/

   /* private void makeGroups() {
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
    }*/

    private void makeSections() {
        mapIndex = new LinkedHashMap<Integer, String>();
        for (int x = 0; x < groupList.size(); x++) {
            try {
                String modifyChar;

                    //char ch = ((ArrayList<AppInfoModel>) groupList.get(x)).get(i).getAppname().charAt(0);
                char ch = groupList.get(x).getAppname().charAt(0);
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


            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        View grid = convertView;
        try {
            if (convertView == null) {
                grid = inflater.inflate(R.layout.drawer_grid_item, null);
                holder = new ViewHolder();
                grid.setTag(holder);
                holder.itemText = (TextView) grid.findViewById(R.id.drawer_grid_text);
                try {
                    holder.itemText.setTypeface(launcherApplication.getTypeFace());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.itemImage = (ImageView) grid.findViewById(R.id.drawer_grid_image);

                grid.setOnClickListener(this);
                grid.setOnLongClickListener(this);
            } else {
                holder = (ViewHolder) grid.getTag();
            }

            groupList.get(position);
            holder.itemImage.setTag(position);
            holder.appInfo = groupList.get(position);
            holder.pName = groupList.get(position).getPname();
            holder.itemText.setText(groupList.get(position).getAppname());
            holder.itemImage.setImageBitmap(groupList.get(position).getBitmapIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grid;
    }

    @Override
    public void onClick(View v) {
        try {
            launchApp(groupList.get(Integer.parseInt(v
                    .findViewById(R.id.drawer_grid_image).getTag().toString())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void launchApp(AppInfoModel appInfo) {
        try {
            Intent intent = null;
            String pckName = appInfo.getPname();

            if (pckName != null) {
                intent = mContext.getPackageManager()
                        .getLaunchIntentForPackage(pckName);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext,
                        mContext.getResources().getText(R.string.appNotFound),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        try {
            dragAnimation(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void dragAnimation(View view) {
        try {

            ((LauncherApplication) ((Activity) mContext).getApplication()).dragInfo = (ItemInfoModel) groupList.get(Integer.parseInt(view
                    .findViewById(R.id.drawer_grid_image).getTag().toString())).clone();

            launcherApplication.getLauncher().resetPage();
            launcherApplication.dragAnimation(view.findViewById(R.id.drawer_grid_image));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static class ViewHolder {
        public TextView itemText;
        public ImageView itemImage;
        public String pName;
        public AppInfoModel appInfo;
    }
}
