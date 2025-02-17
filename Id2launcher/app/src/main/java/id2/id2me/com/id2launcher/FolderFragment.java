package id2.id2me.com.id2launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.ItemInfo;

/**
 * Created by bliss76 on 26/05/16.
 */
public class FolderFragment extends Fragment {
    static final String FolderId = "folderId";
    final int NO_OF_APPS_IN_ROW = 3;
    View fragmentView;
    ArrayList<ItemInfo> itemInfoModels;
    private FolderGridAdapter adapter;
    private DatabaseHandler db;
    public long folderId;
    public static final FolderFragment newInstance(long folderId) {
        FolderFragment f = new FolderFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("folderId", folderId);
        f.setArguments(bundle);
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
            folderId = getArguments().getLong(FolderId);
            RecyclerView appGridView = (RecyclerView) fragmentView.findViewById(R.id.folder_gridView);
         //   setColumnWidth(appGridView);
           // setNoOfColumnsOfGrid(appGridView);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //if (fragmentView != null) {
         // updateView();
      //  }
    }

//    void updateView(){
//        AppGridView appGridView = (AppGridView) fragmentView.findViewById(R.id.folder_gridView);
//        itemInfoModels = db.getAppsListOfFolder(folderId);
//        if (itemInfoModels != null) {
//            adapter = new FolderGridAdapter(itemInfoModels, getActivity(), R.layout.folder_grid, appGridView);
//        }
//
//        appGridView.setAdapter(adapter);
//    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null)
            db = DatabaseHandler.getInstance(context);
    }

    @Override
    public boolean getUserVisibleHint() {
        return super.getUserVisibleHint();

    }

    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }
}
