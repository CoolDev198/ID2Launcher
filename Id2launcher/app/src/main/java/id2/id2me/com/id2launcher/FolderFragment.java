package id2.id2me.com.id2launcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by bliss76 on 26/05/16.
 */
public class FolderFragment extends Fragment {
    final int NO_OF_APPS_IN_ROW = 3;
    View fragmentView;
    int id;
    private FolderGridAdapter adapter;
    private DatabaseHandler db;

    public static final FolderFragment newInstance(int count) {
        FolderFragment f = new FolderFragment();
        f.id = count;
        return f;
    }

    public FolderGridAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            fragmentView = inflater.inflate(R.layout.popup_view, container, false);
            db = DatabaseHandler.getInstance(getActivity());

            AppGridView appGridView = (AppGridView) fragmentView.findViewById(R.id.folder_gridView);
            ArrayList<ItemInfoModel> itemInfoModels = getAppsListFromDataBase();

            if (itemInfoModels != null) {
                adapter = new FolderGridAdapter(itemInfoModels, getActivity(), R.layout.folder_grid, appGridView);
            }
            setColumnWidth(appGridView);
            setNoOfColumnsOfGrid(appGridView);
            appGridView.setAdapter(adapter);
            setRetainInstance(true);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragmentView;
    }

    private ArrayList<ItemInfoModel> getAppsListFromDataBase() {
        ItemInfoModel itemInfoModel = ((LauncherApplication) getActivity().getApplication()).folderFragmentsInfo.get(id - 1);

        return db.getAppsListOfFolder(itemInfoModel.getId());

    }

    @Override
    public void onResume() {
        Log.v("", "On resume called");
        if (adapter != null) {
            ArrayList<ItemInfoModel> itemInfoModels = getAppsListFromDataBase();
            adapter.setAppInfos(itemInfoModels);
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }
}
