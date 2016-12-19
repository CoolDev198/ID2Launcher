package id2.id2me.com.id2launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import id2.id2me.com.id2launcher.listingviews.AppsListingView;
import id2.id2me.com.id2launcher.listingviews.WidgetsListingView;
import timber.log.Timber;

/**
 * Created by sunita on 10/13/16.
 */

public class DrawerFragment extends Fragment   {

    Button btnApp, btnWidget;
    AppsListingView appsListingView;
    WidgetsListingView widgetsListingView;
    private View fragmentView;

    public static DrawerFragment newInstance() {
        DrawerFragment f = new DrawerFragment();
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.drawer_fragment, container, false);
        btnApp = (Button) fragmentView.findViewById(R.id.btnApps);
        btnWidget = (Button) fragmentView.findViewById(R.id.btnWidget);
        appsListingView = (AppsListingView) fragmentView.findViewById(R.id.app_listing_view);
        widgetsListingView = (WidgetsListingView) fragmentView.findViewById(R.id.widget_listing_view);

        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnWidget.setBackground(null);
                btnApp.setBackgroundResource(R.drawable.round_transperant_border);
                widgetsListingView.setVisibility(View.GONE);
                appsListingView.setVisibility(View.VISIBLE);
            }
        });

        btnWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnApp.setBackground(null);
                btnWidget.setBackgroundResource(R.drawable.round_transperant_border);
                appsListingView.setVisibility(View.GONE);
                widgetsListingView.setVisibility(View.VISIBLE);

            }
        });

        return fragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
