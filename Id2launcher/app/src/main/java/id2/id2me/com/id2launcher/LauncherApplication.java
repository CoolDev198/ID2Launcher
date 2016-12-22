package id2.id2me.com.id2launcher;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.NotificationWidgetInfo;
import timber.log.Timber;

/**
 * Created by sunita on 7/27/16.
 */
public class LauncherApplication extends Application {

    public static final int DEFAULT_SCREENS = 1;
    public static ImageView wallpaperImg;
    public static List<NotificationWidgetInfo> notificationWidgetModels;
    static LauncherApplication launcherApplication;
    private static float density;
    public final int CELL_COUNT_X = 4;
    public final int CELL_COUNT_Y = 5;
    public boolean isDrawerOpen = false;
    public ItemInfo dragInfo;
    public LauncherAppWidgetHost mAppWidgetHost;
    public LauncherModel mModel;
    public View desktopFragment;
    private Launcher launcher;
    public static HashMap<String, Bitmap> mHashMapBitmap;

    public static LauncherApplication getApp() {
        return launcherApplication;
    }

    public static float getScreenDensity() {
        return density;
    }

    public IconCache mIconCache;

    @Override
    public void onCreate() {
        super.onCreate();
      //  Fabric.with(this, new Crashlytics());

        ButterKnife.setDebug(true);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.v("Inited Timber Debug Tree");
        }

        launcherApplication = this;

        mIconCache = new IconCache(this);
        mModel = new LauncherModel(mIconCache);
        addBroadCastReceiver();
        density = getResources().getDisplayMetrics().density;
    }

    private void addBroadCastReceiver() {
        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mModel, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mModel);
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public LauncherModel setLauncher(Launcher launcher) {
        this.launcher = launcher;
        mModel.initialize(launcher);
        return mModel;
    }

    public int getCellHeight() {

        int cellHeight = (int) getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.cell_height);
        return cellHeight;
    }

    public int getCellWidth() {
        int cellWidth = (int) getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.cell_width);
        return cellWidth;
    }

}
