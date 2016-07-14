package id2.id2me.com.id2launcher;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.folder.FolderGridAdapter;
import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.general.AppGridView;

/**
 * Created by bliss76 on 26/05/16.
 */
public class MyFragment extends Fragment {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    View fragmentView;
    ArrayList<AppInfo> appinfos;
    final int NO_OF_APPS_IN_ROW = 3;


    public static final MyFragment newInstance(ArrayList<AppInfo> appinfos) {
        MyFragment f = new MyFragment();
        Bundle bdl = new Bundle();
        bdl.putSerializable("list", appinfos);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            appinfos = (ArrayList<AppInfo>) getArguments().getSerializable("list");//.size();
            int size = appinfos.size();
            //Toast.makeText(getActivity(),size+" myfrg",Toast.LENGTH_SHORT).show();
            fragmentView = inflater.inflate(R.layout.first_fragment, container, false);
            AppGridView appGridView = (AppGridView) fragmentView.findViewById(R.id.mygridview);
            FolderGridAdapter adapter = new FolderGridAdapter(appinfos,getActivity());
            int noOfApps = appinfos.size();
            setColumnWidth(appGridView);
            setNoOfColumnsOfGrid(appGridView);
            //setGridViewTotalHeight(noOfApps, appGridView);
            appGridView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragmentView;
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
                    * (int) getActivity().getResources().getDimension(
                    R.dimen.gridview_height);
            Log.v("Total Height", "" + totalHeight);
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = totalHeight;
            gridView.setLayoutParams(params);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }

}
