package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Created by sunita on 10/13/16.
 */

public class DrawerFragment extends Fragment {

    private static AppsListingFragment appsListingFragment;
    private static WidgetsListingFragment widgetsListingFragment;
    private Context context;
    private LauncherApplication application;
    private View fragmentView;

    public static DrawerFragment newInstance() {
        DrawerFragment f = new DrawerFragment();
        appsListingFragment = AppsListingFragment.newInstance();
        widgetsListingFragment = WidgetsListingFragment.newInstance();
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.drawer_fragment, container, false);
        application = (LauncherApplication) ((Activity) context).getApplication();
        Button btnApp = (Button) fragmentView.findViewById(R.id.btnApp);
        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceView(appsListingFragment, "Apps");
            }
        });
        Button btnWidget = (Button) fragmentView.findViewById(R.id.btnWidget);
        btnWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceView(widgetsListingFragment, "Apps");
            }
        });

        replaceView(appsListingFragment, "Apps");

        return fragmentView;
    }

    void replaceView(Fragment fragment, String tag) {
        try {
            FragmentTransaction ft = getChildFragmentManager()
                    .beginTransaction();
            ft.replace(R.id.fragment_container, fragment, tag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(tag);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


}
