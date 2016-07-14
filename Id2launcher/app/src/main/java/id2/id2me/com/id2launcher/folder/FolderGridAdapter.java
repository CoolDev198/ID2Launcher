package id2.id2me.com.id2launcher.folder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.R;

/**
 * Created by bliss76 on 15/06/16.
 */
public class FolderGridAdapter extends BaseAdapter{
    ArrayList<AppInfo> appInfos;
    LayoutInflater inflater;
    int position;
    View grid;
    public  FolderGridAdapter(ArrayList<AppInfo> appInfos, Context context)
    {
        this.appInfos=appInfos;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }
    @Override
    public int getCount() {
        return appInfos.size();
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder = null;
        grid = convertView;
        this.position = position;

        try {
            if (convertView == null) {
                holder = new ViewHolder();
                grid = inflater.inflate(R.layout.grid_item, null);
                grid.setTag(holder);
                holder.itemText = (TextView) grid.findViewById(R.id.grid_text);
                holder.itemImage = (ImageView) grid.findViewById(R.id.grid_image);


            } else {
                holder = (ViewHolder) grid.getTag();
            }

            holder.itemImage.setTag(position);
            holder.pName=appInfos.get(position).getPname();
            holder.itemText.setText(appInfos.get(position).getAppname());
            holder.itemImage.setImageDrawable(appInfos.get(position).getIcon());


        } catch (Exception e) {


            e.printStackTrace();
        }

        return grid;

    }
    private static class ViewHolder {
        public TextView itemText;
        public ImageView itemImage;
        public String pName;
    }
}
