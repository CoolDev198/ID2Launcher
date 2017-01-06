package id2.id2me.com.id2launcher.listingviews;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import id2.id2me.com.id2launcher.AppWidgetResizeFrame;
import id2.id2me.com.id2launcher.DragLayer;
import id2.id2me.com.id2launcher.DragSource;
import id2.id2me.com.id2launcher.DropTarget;
import id2.id2me.com.id2launcher.Launcher;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.models.PendingAddItemInfo;
import id2.id2me.com.id2launcher.models.PendingAddShortcutInfo;
import id2.id2me.com.id2launcher.models.PendingAddWidgetInfo;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.itemviews.WidgetItemView;
import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.models.AppInfo;
import timber.log.Timber;

/**
 * Created by sunita on 12/16/16.
 */

public class ListingContainerView extends FrameLayout implements View.OnLongClickListener, View.OnClickListener, DragSource {
    static final int WIDGET_NO_CLEANUP_REQUIRED = -1;
    static final int WIDGET_PRELOAD_PENDING = 0;
    static final int WIDGET_BOUND = 1;
    static final int WIDGET_INFLATED = 2;
    int mWidgetCleanupState = WIDGET_NO_CLEANUP_REQUIRED;
    int mWidgetLoadingId = -1;
    Launcher mLauncher;
    // Caching
    private Canvas mCanvas;
    private Rect mTmpRect = new Rect();
    private Runnable mInflateWidgetRunnable = null;
    private Runnable mBindWidgetRunnable = null;

    public ListingContainerView(Context context) {
        this(context, null);
    }

    public ListingContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListingContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mLauncher = LauncherApplication.getApp().getLauncher();
    }

    @Override
    public void onClick(View child) {
        Timber.v("On CLick");
        if (child instanceof AppItemView) {
            final AppInfo appInfo = (AppInfo) child.getTag();
            Launcher mLauncher = LauncherApplication.getApp().getLauncher();
            mLauncher.startActivitySafely(child, appInfo.intent, appInfo);
        } else {
            Toast.makeText(getContext(), R.string.long_press_widget_to_add,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onLongClick(View child) {
        Timber.v("On Long Click");
        if (child instanceof AppItemView) {
            beginDraggingApplication(child);
        } else if (child instanceof WidgetItemView) {
            beginDraggingWidget(child);
        }
        return true;
    }

    private void beginDraggingWidget(View child) {
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        launcherApplication.getLauncher().resetPage();

        mCanvas = new Canvas();
        //get the wudget priview image for drag representation
        ImageView image = (ImageView) child.findViewById(R.id.widget_preview);
        PendingAddItemInfo itemInfo = (PendingAddItemInfo) child.getTag();

        if (itemInfo.spanX <= 0 || itemInfo.spanY <= 0) {
            itemInfo.spanX = itemInfo.minSpanX;
            itemInfo.spanY = itemInfo.minSpanY;
        }
        //Compose drag image
        Bitmap preview;
        Bitmap outline;
        float scale = 1f;

        if (itemInfo instanceof PendingAddWidgetInfo) {
            // This can happen in some weird cases involving multi-touch. We can't start dragging
            // the widget if this is null, so we break out.

            PendingAddWidgetInfo createWidgetInfo = new PendingAddWidgetInfo((PendingAddWidgetInfo) itemInfo);

            preloadWidget(createWidgetInfo);

            BitmapDrawable previewDrawable = (BitmapDrawable) image.getDrawable();

            String id = createWidgetInfo.componentName.getPackageName() + "" + createWidgetInfo.previewImage;
            preview = launcherApplication.mHashMapBitmap.get(id);

            // Determine the image view drawable scale relative to the preview
            float[] mv = new float[9];
            Matrix m = new Matrix();
            m.setRectToRect(
                    new RectF(0f, 0f, (float) preview.getWidth(), (float) preview.getHeight()),
                    new RectF(0f, 0f, (float) previewDrawable.getIntrinsicWidth(),
                            (float) previewDrawable.getIntrinsicHeight()),
                    Matrix.ScaleToFit.START);
            m.getValues(mv);
            scale = (float) mv[0];
        } else {
            PendingAddShortcutInfo createShortcutInfo = (PendingAddShortcutInfo) child.getTag();
            Drawable icon = launcherApplication.mIconCache.getFullResIcon(createShortcutInfo.shortcutActivityInfo);
            preview = Bitmap.createBitmap(icon.getIntrinsicWidth(),
                    icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            mCanvas.setBitmap(preview);
            mCanvas.save();
            renderDrawableToBitmap(icon, preview, 0, 0,
                    icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            mCanvas.restore();
            mCanvas.setBitmap(null);
            itemInfo.spanX = itemInfo.spanY = 1;
        }
        // Don't clip alpha values for the drag outline if we're using the default widget preview
        boolean clipAlpha = !(itemInfo instanceof PendingAddWidgetInfo &&
                (((PendingAddWidgetInfo) itemInfo).previewImage == 0));

        // Save the preview for the outline generation, then dim the preview
        outline = Bitmap.createScaledBitmap(preview, preview.getWidth(), preview.getHeight(),
                false);

        launcherApplication.getLauncher().getWokSpace().beginDragWidget(itemInfo, outline, clipAlpha, image, preview, this, itemInfo, scale);

    }

    private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h) {
        renderDrawableToBitmap(d, bitmap, x, y, w, h, 1f);
    }

    private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h,
                                        float scale) {
        if (bitmap != null) {
            Canvas c = new Canvas(bitmap);
            c.scale(scale, scale);
            Rect oldBounds = d.copyBounds();
            d.setBounds(x, y, x + w, y + h);
            d.draw(c);
            d.setBounds(oldBounds); // Restore the bounds
            c.setBitmap(null);
        }
    }

    Bundle getDefaultOptionsForWidget(Launcher launcher, PendingAddWidgetInfo info) {
        Bundle options = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) { //widget resize available since 4.2
            AppWidgetResizeFrame.getWidgetSizeRanges(mLauncher, info.spanX, info.spanY, mTmpRect);
            Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(mLauncher,
                    info.componentName, null);

            float density = getResources().getDisplayMetrics().density;
            int xPaddingDips = (int) ((padding.left + padding.right) / density);
            int yPaddingDips = (int) ((padding.top + padding.bottom) / density);

            options = new Bundle();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                    mTmpRect.left - xPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                    mTmpRect.top - yPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                    mTmpRect.right - xPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                    mTmpRect.bottom - yPaddingDips);
        }
        return options;
    }

    private void preloadWidget(final PendingAddWidgetInfo info) {
        final AppWidgetProviderInfo pInfo = info.info;
        final Bundle options = getDefaultOptionsForWidget(mLauncher, info);

        if (pInfo.configure != null) {
            info.bindOptions = options;
            return;
        }

        mWidgetCleanupState = WIDGET_PRELOAD_PENDING;
        mBindWidgetRunnable = new Runnable() {
            @Override
            public void run() {
                mWidgetLoadingId = mLauncher.getAppWidgetHost().allocateAppWidgetId();
                // Options will be null for platforms with JB or lower, so this serves as an
                // SDK level check.
                if (options == null) {
                    if (AppWidgetManager.getInstance(mLauncher).bindAppWidgetIdIfAllowed(
                            mWidgetLoadingId, info.componentName)) {
                        mWidgetCleanupState = WIDGET_BOUND;
                    }
                } else {
                    //TODO add options bundle for 4.2+
                    if (AppWidgetManager.getInstance(mLauncher).bindAppWidgetIdIfAllowed(
                            mWidgetLoadingId, info.componentName)) {
                        mWidgetCleanupState = WIDGET_BOUND;
                    }

                }
            }
        };
        post(mBindWidgetRunnable);

        mInflateWidgetRunnable = new Runnable() {
            @Override
            public void run() {
                if (mWidgetCleanupState != WIDGET_BOUND) {
                    return;
                }
                AppWidgetHostView hostView = mLauncher.
                        getAppWidgetHost().createView(getContext(), mWidgetLoadingId, pInfo);
                info.boundWidget = hostView;
                mWidgetCleanupState = WIDGET_INFLATED;
                hostView.setVisibility(INVISIBLE);
                int[] unScaledSize = mLauncher.getWokSpace().estimateItemSize(info.spanX,
                        info.spanY, info, false);

                // We want the first widget layout to be the correct size. This will be important
                // for width size reporting to the AppWidgetManager.
                DragLayer.LayoutParams lp = new DragLayer.LayoutParams(unScaledSize[0],
                        unScaledSize[1]);
                lp.x = lp.y = 0;
                lp.customPosition = true;
                hostView.setLayoutParams(lp);
                mLauncher.getDragLayer().addView(hostView);
            }
        };
        post(mInflateWidgetRunnable);
    }

    private void beginDraggingApplication(View child) {
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        launcherApplication.getLauncher().resetPage();
        launcherApplication.getLauncher().getWokSpace().beginDragShared(child, this);
    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success) {

    }
}
