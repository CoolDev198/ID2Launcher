package id2.id2me.com.id2launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.WidgetInfoModel;


/**
 * Created by sunita on 8/2/16.
 */
public class WidgetsListingFragment extends Fragment {

    View fragmentView;
    Context context;
    final String TAG = "WidgetsListingFragment";
    ArrayList<WidgetInfoModel> widgetInfos;
    private RecyclerView recyclerView;

    public static WidgetsListingFragment newInstance() {
        WidgetsListingFragment widgetsListingFragment = new WidgetsListingFragment();
        return widgetsListingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        widgetInfos = new ArrayList<>();
        fragmentView = inflater.inflate(R.layout.widgets_listing_fragment, container, false);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            recyclerView = (RecyclerView) fragmentView.findViewById(R.id.widget_recycle_view);
            GridLayoutManager manager = new GridLayoutManager(context, 2);
            recyclerView.setLayoutManager(manager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(new WidgetRecycleViewAdapter(context,recyclerView));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "On Destroy View called");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        recyclerView.stopScroll();
        Log.v(TAG, "On Detach called");

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(TAG, "On Resume called");
    }


}
