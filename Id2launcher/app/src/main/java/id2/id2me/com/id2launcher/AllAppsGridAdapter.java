package id2.id2me.com.id2launcher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by bliss76 on 21/06/16.
 */
public class AllAppsGridAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
    private final LauncherApplication launcherApplication;
    LayoutInflater inflater;
    ArrayList<AppInfoModel> gridList;
    DrawerLayout drawerLayout;
    AppGridView appGridView;
    private Context mContext;

    public AllAppsGridAdapter(Context c, ArrayList<AppInfoModel> gridList, DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
        mContext = c;
        launcherApplication= (LauncherApplication)((Activity)mContext).getApplication();
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.gridList = gridList;
    }


    @Override
    public int getCount() {
        return gridList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            dragAnimation(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

            gridList.get(position);
            holder.itemImage.setTag(position);
            holder.appInfo = gridList.get(position);
            holder.pName = gridList.get(position).getPname();
            holder.itemText.setText(gridList.get(position).getAppname());
            holder.itemImage.setImageBitmap(gridList.get(position).getBitmapIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grid;
    }

    public void launchApp(AppInfoModel appInfo) {
        try {
            Intent intent = null;
            String pckName = appInfo.getPname();

            if (pckName != null) {
                intent = mContext.getPackageManager()
                        .getLaunchIntentForPackage(pckName);
                mContext.startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
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
    public void onClick(View v) {
        try {
            launchApp(gridList.get(Integer.parseInt(v
                    .findViewById(R.id.drawer_grid_image).getTag().toString())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dragAnimation(View view) {
        try {

            ((LauncherApplication) ((Activity) mContext).getApplication()).dragInfo = (ItemInfoModel) gridList.get(Integer.parseInt(view
                    .findViewById(R.id.drawer_grid_image).getTag().toString())).clone();

            launcherApplication.dragAnimation(view.findViewById(R.id.drawer_grid_image));
            drawerLayout.closeDrawer(Gravity.LEFT);

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


