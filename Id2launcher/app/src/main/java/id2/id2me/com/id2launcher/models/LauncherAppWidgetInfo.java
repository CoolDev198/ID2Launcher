package id2.id2me.com.id2launcher.models;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;

import id2.id2me.com.id2launcher.AppWidgetResizeFrame;
import id2.id2me.com.id2launcher.Launcher;
import id2.id2me.com.id2launcher.LauncherSettings;
import id2.id2me.com.id2launcher.models.ItemInfo;

/**
 * Created by sunita on 12/19/16.
 */
public class LauncherAppWidgetInfo extends ItemInfo {

    /**
     * Indicates that the widget hasn't been instantiated yet.
     */
    static final int NO_ID = -1;

    /**
     * Identifier for this widget when talking with
     * {@link android.appwidget.AppWidgetManager} for updates.
     */
    int appWidgetId = NO_ID;

    ComponentName providerName;

    // TODO: Are these necessary here?
    int minWidth = -1;
    int minHeight = -1;

    private boolean mHasNotifiedInitialWidgetSizeChanged;

    /**
     * View that holds this widget after it's been created.  This view isn't created
     * until Launcher knows it's needed.
     */
    public AppWidgetHostView hostView = null;

    public LauncherAppWidgetInfo(int appWidgetId, ComponentName providerName) {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
        this.appWidgetId = appWidgetId;
        this.providerName = providerName;

        // Since the widget isn't instantiated yet, we don't know these values. Set them to -1
        // to indicate that they should be calculated based on the layout and minWidth/minHeight
        spanX = -1;
        spanY = -1;
    }

    /**
     * When we bind the widget, we should notify the widget that the size has changed if we have not
     * done so already (only really for default workspace widgets).
     */
    public void onBindAppWidget(Launcher launcher) {
        if (!mHasNotifiedInitialWidgetSizeChanged) {
        }
    }

    /**
     * Trigger an update callback to the widget to notify it that its size has changed.
     */
    public void notifyWidgetSizeChanged(Launcher launcher) {
        AppWidgetResizeFrame.updateWidgetSizeRanges(hostView, launcher, spanX, spanY);
        mHasNotifiedInitialWidgetSizeChanged = true;
    }

    @Override
    public String toString() {
        return "AppWidget(id=" + Integer.toString(appWidgetId) + ")";
    }

    @Override
    void unbind() {
        super.unbind();
        hostView = null;
    }
}
