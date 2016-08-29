package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import id2.id2me.com.id2launcher.folder.FolderFragmentInterface;
import id2.id2me.com.id2launcher.general.AppGridView;

/**
 * Created by sunita on 7/27/16.
 */
public class FolderPopUp extends RelativeLayout {

    final int NO_OF_APPS_IN_ROW = 2;

    public FolderPopUp(int pos, Context context, FolderFragmentInterface folderFragmentInterface, View parent) {
        super(context);

        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View v = inflater.inflate(R
                .layout.popup_view, null);

        AppGridView appGridView = (AppGridView) v.findViewById(R.id.mygridview);
        setColumnWidth(appGridView);
        setNoOfColumnsOfGrid(appGridView);
        //FolderGridAdapter adapter = new FolderGridAdapter(pos, context, R.layout.pop_up_grid, appGridView, folderFragmentInterface, parent);
       // appGridView.setAdapter(adapter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    void setColumnWidth(GridView gridView) {
        int width = (int) (gridView.getWidth() / NO_OF_APPS_IN_ROW) - 30;
        gridView.setColumnWidth(width);
    }

    void setNoOfColumnsOfGrid(GridView gridView) {
        gridView.setNumColumns(NO_OF_APPS_IN_ROW);
    }

}
