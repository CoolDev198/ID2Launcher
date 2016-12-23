package id2.id2me.com.id2launcher;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import id2.id2me.com.id2launcher.DragSource;
import id2.id2me.com.id2launcher.DropTarget;
import id2.id2me.com.id2launcher.Launcher;
import id2.id2me.com.id2launcher.LauncherAnimUtils;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.PendingAddItemInfo;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.itemviews.WidgetImageView;
import id2.id2me.com.id2launcher.models.AppInfo;
import timber.log.Timber;

/**
 * Created by sunita on 12/16/16.
 */

public class ListingContainerView extends FrameLayout implements View.OnLongClickListener, View.OnClickListener, DragSource {
    // Caching
    private Canvas mCanvas;
    private IconCache mIconCache;
    PendingAddWidgetInfo mCreateWidgetInfo = null;
    private boolean mDraggingWidget = false;
    public ListingContainerView(Context context) {
        super(context);

    }

    public ListingContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListingContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onClick(View child) {
        Timber.v("On CLick");
        if (child instanceof AppItemView) {
            final AppInfo appInfo = (AppInfo) child.getTag();
            Launcher mLauncher = LauncherApplication.getApp().getLauncher();
            mLauncher.startActivitySafely(child,appInfo.intent,appInfo);
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
        }  else if (child instanceof WidgetItemView) {
            //Object rawInfo = child.getTag();
            PendingAddItemInfo itemInfo = (PendingAddItemInfo) child.getTag();
            if(itemInfo instanceof PendingAddWidgetInfo){
                PendingAddWidgetInfo info = (PendingAddWidgetInfo) itemInfo;
                mCreateWidgetInfo = new PendingAddWidgetInfo(info);
            }

            //mCreateWidgetInfo = new PendingAddWidgetInfo((PendingAddWidgetInfo) child.getTag());
            beginDraggingWidget(child);
        }
        return true;
    }

    private void beginDraggingWidget(View child) {
        mDraggingWidget = true;
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        launcherApplication.getLauncher().resetPage();
        mIconCache = launcherApplication.mIconCache;
        mCanvas = new Canvas();
        //get the wudget priview image for drag representation
        ImageView image = (ImageView) child.findViewById(R.id.widget_preview);
        PendingAddItemInfo itemInfo = (PendingAddItemInfo) child.getTag();

        // If the ImageView doesn't have a drawable yet, the widget preview hasn't been loaded and
        // we abort the drag.
        /*if (image.getDrawable() == null) {
            mDraggingWidget = false;
            return false;
        }*/

        //Compose drag image
        Bitmap preview;
        Bitmap outline;
        float scale = 1f;

        if (itemInfo instanceof PendingAddWidgetInfo) {
            // This can happen in some weird cases involving multi-touch. We can't start dragging
            // the widget if this is null, so we break out.
            if (mCreateWidgetInfo == null) {
                //return false;
            }

            PendingAddWidgetInfo createWidgetInfo = mCreateWidgetInfo;
            itemInfo = createWidgetInfo;

            BitmapDrawable previewDrawable = (BitmapDrawable) image.getDrawable();
            float minScale = 1.25f;

            String id = createWidgetInfo.componentName.getPackageName()+""+createWidgetInfo.previewImage;
            preview = launcherApplication.mHashMapBitmap.get(id);
            /*preview = getWidgetPreview(createWidgetInfo.componentName, createWidgetInfo.previewImage,
                    createWidgetInfo.icon, spanX, spanY, maxWidth, maxHeight);*/

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
            Drawable icon = mIconCache.getFullResIcon(createShortcutInfo.shortcutActivityInfo);
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
        launcherApplication.getLauncher().getWokSpace().onDragStartedWithItem(itemInfo, outline, clipAlpha);
        launcherApplication.getLauncher().getWokSpace().beginDragWidget(image, preview, this, itemInfo, scale);
        //outline.recycle();
        //preview.recycle();
        //return true;

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
