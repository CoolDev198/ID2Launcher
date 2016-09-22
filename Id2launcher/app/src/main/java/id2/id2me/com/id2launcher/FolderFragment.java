package id2.id2me.com.id2launcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by bliss76 on 26/05/16.
 */
public class FolderFragment extends Fragment {
    private   FolderGridAdapter adapter;

    final int NO_OF_APPS_IN_ROW = 3;
    View fragmentView;

    public static final FolderFragment newInstance() {
        FolderFragment f = new FolderFragment();
        return f;
    }

    public FolderGridAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            fragmentView = inflater.inflate(R.layout.folder_fragment, container, false);
            AppGridView appGridView = (AppGridView) fragmentView.findViewById(R.id.mygridview);
            adapter = new FolderGridAdapter(null, getActivity(), R.layout.folder_grid, appGridView);
            setColumnWidth(appGridView);
            setNoOfColumnsOfGrid(appGridView);
            appGridView.setAdapter(adapter);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragmentView;
    }


    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }
}
