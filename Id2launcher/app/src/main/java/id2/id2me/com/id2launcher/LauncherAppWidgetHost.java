package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.View;

/**
 * Created by sunita on 8/21/16.
 */
public class LauncherAppWidgetHost  extends AppWidgetHost {

    Launcher mLauncher;

    public LauncherAppWidgetHost(Launcher launcher, int hostId) {
        super(launcher, hostId);
        mLauncher = launcher;
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
                                             AppWidgetProviderInfo appWidget) {
        return new LauncherAppWidgetHostView(context);
    }

    @Override
    public void stopListening() {
        super.stopListening();
        clearViews();
    }

    protected void onProvidersChanged() {
        // Once we get the message that widget packages are updated, we need to rebind items
        // in AppsCustomize accordingly.
        mLauncher.bindPackagesUpdated();
    }
}