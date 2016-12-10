package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import id2.id2me.com.id2launcher.customscroll.RecyclerViewFastScroller;
import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by Pinto on 24/09/16.
 */
public class AllAppAdapter extends RecyclerView.Adapter<AllAppAdapter.MyViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter{

    ArrayList<AppInfoModel> groupList;
    HashMap<Integer, String> mapIndex;
    private LauncherApplication launcherApplication;
    private Context mContext;

    public AllAppAdapter(Context context) {
        try {
            this.mContext = context;
            launcherApplication = (LauncherApplication)((Activity)context).getApplication();
            this.groupList = launcherApplication.mModel.mBgAllAppsList.data;
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

                mapIndex.put(x, modifyChar);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (mapIndex.containsKey(pos)) {
            return mapIndex.get(pos);
        } else {
            return "";
        }

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(new AppItemView(mContext));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AppInfoModel appInfoModel = groupList.get(position);
        ((AppItemView) holder.itemView).init(appInfoModel);
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }

    }
}
