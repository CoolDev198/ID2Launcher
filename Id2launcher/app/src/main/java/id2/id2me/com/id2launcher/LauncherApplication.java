package id2.id2me.com.id2launcher;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id2.id2me.com.id2launcher.models.ItemInfoModel;
import id2.id2me.com.id2launcher.models.NotificationWidgetModel;
import timber.log.Timber;

/**
 * Created by sunita on 7/27/16.
 */
public class LauncherApplication extends Application {

    public static final int DEFAULT_SCREENS = 2;
    public static ImageView wallpaperImg;
    public static List<NotificationWidgetModel> notificationWidgetModels;
    static LauncherApplication launcherApplication;
    private static float density;
    public final int CELL_COUNT_X = 4;
    public final int CELL_COUNT_Y = 5;
    public boolean isDrawerOpen = false;
    public ArrayList<ItemInfoModel> folderFragmentsInfo;
    public ItemInfoModel dragInfo;
    public LauncherAppWidgetHost mAppWidgetHost;
    public LauncherModel mModel;
    public HashMap<ArrayList<Integer>, Rect> mapMatrixPosToRec;
    public View desktopFragment;
    private Launcher launcher;

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

        mIconCache = new IconCache(this);
        launcherApplication = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.v("Inited Timber Debug Tree");
        }

        mapMatrixPosToRec = new HashMap<>();
        folderFragmentsInfo = new ArrayList<>();


        mModel = new LauncherModel(this);
        density = getResources().getDisplayMetrics().density;

        addBroadCastReceiver();

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

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
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
