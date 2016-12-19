package id2.id2me.com.id2launcher;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.ItemInfo;

/**
 * Created by bliss76 on 15/06/16.
 */
public class FolderGridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    ArrayList<ItemInfo> appInfos;
    LayoutInflater inflater;
    View grid;
    int layout;
    Context context;

    public FolderGridAdapter(ArrayList<ItemInfo> appInfos, Context context, int layout) {
        this.layout = layout;
        this.context = context;
        if(appInfos==null){
            this.appInfos=new ArrayList<>();
        }else {
            this.appInfos = appInfos;
        }
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //appGridView.setOnItemClickListener(this);

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        grid = convertView;

        try {
            if (convertView == null) {
                holder = new ViewHolder();
                grid = inflater.inflate(layout, null);
                grid.setTag(holder);
                holder.itemImage = (ImageView) grid.findViewById(R.id.grid_image);

            } else {
                holder = (ViewHolder) grid.getTag();
            }

            holder.itemInfoModel=appInfos.get(position);
            holder.itemImage.setTag(position);
          //  holder.pName = appInfos.get(position).getPname();
           // holder.itemImage.setImageBitmap(ItemInfo.getIconFromCursor(appInfos.get(position).getIcon(),context));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return grid;

    }

    public void launchApp(String pckName) {
        try {
            Intent intent = null;
            intent = context.getPackageManager()
                    .getLaunchIntentForPackage(pckName);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        launchApp(((ViewHolder) view.getTag()).pName);
    }

    public void setAppInfos(ArrayList<ItemInfo> appInfos) {
        this.appInfos = appInfos;
        this.notifyDataSetChanged();
    }


    private static class ViewHolder {
        public ImageView itemImage;
        public String pName;
        ItemInfo itemInfoModel;
    }
}
