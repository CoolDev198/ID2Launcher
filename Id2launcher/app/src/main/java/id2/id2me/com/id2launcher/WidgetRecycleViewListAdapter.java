package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Process;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import id2.id2me.com.id2launcher.itemviews.WidgetImageView;
import id2.id2me.com.id2launcher.itemviews.WidgetItemView;
import id2.id2me.com.id2launcher.listingviews.ListingContainerView;
import id2.id2me.com.id2launcher.models.PendingAddItemInfo;
import id2.id2me.com.id2launcher.models.PendingAddShortcutInfo;
import id2.id2me.com.id2launcher.models.PendingAddWidgetInfo;
import timber.log.Timber;

/**
 * Created by CrazyInnoTech on 21-12-2016.
 */


public class WidgetRecycleViewListAdapter extends RecyclerView.Adapter<WidgetRecycleViewListAdapter.MyViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, View.OnKeyListener {

    LauncherApplication launcherApplication;
    private ListingContainerView listeners;
    private ArrayList<Object> items;
    private ArrayList<Object> widgetList = new ArrayList<>();
    private ArrayList<Object> shortcutList = new ArrayList<>();
    PackageManager mPackageManager;
    private int mAppIconSize;

    private final float sWidgetPreviewIconPaddingPercentage = 0.25f;
    // Caching
    private IconCache mIconCache;

    // Used for drawing shortcut previews
    BitmapCache mCachedShortcutPreviewBitmap = new BitmapCache();
    PaintCache mCachedShortcutPreviewPaint = new PaintCache();
    CanvasCache mCachedShortcutPreviewCanvas = new CanvasCache();

    // Used for drawing widget previews
    CanvasCache mCachedAppWidgetPreviewCanvas = new CanvasCache();
    RectCache mCachedAppWidgetPreviewSrcRect = new RectCache();
    RectCache mCachedAppWidgetPreviewDestRect = new RectCache();
    PaintCache mCachedAppWidgetPreviewPaint = new PaintCache();
    private int mCellWidth;
    private int mCellHeight;

    private final Rect mOriginalImagePadding = new Rect();

    public WidgetRecycleViewListAdapter(ListingContainerView listeners) {
        this.listeners=listeners;
        launcherApplication = LauncherApplication.getApp();
        mPackageManager = launcherApplication.getPackageManager();
        items = launcherApplication.mModel.mWidgetList;
        mAppIconSize = (int)launcherApplication.getResources().getDimension(R.dimen.app_icon_size);
        mIconCache = launcherApplication.mIconCache;
        mCellWidth = launcherApplication.getCellWidth();
        mCellHeight = launcherApplication.getCellHeight();
        launcherApplication.mHashMapBitmap = new HashMap<>();
        Timber.v("Widget List Size : " + items.size());
    }


    @Override
    public void onViewAttachedToWindow(MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        LayoutInflater vi =
                (LayoutInflater) launcherApplication.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.customized_widget_item, null);
        final ImageView image = (ImageView) v.findViewById(R.id.widget_preview);
        mOriginalImagePadding.left = image.getPaddingLeft();
        mOriginalImagePadding.top = image.getPaddingTop();
        mOriginalImagePadding.right = image.getPaddingRight();
        mOriginalImagePadding.bottom = image.getPaddingBottom();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WidgetItemView itemView = (WidgetItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customized_widget_item, parent, false);
        itemView.setOnLongClickListener(listeners);
        itemView.setOnClickListener(listeners);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Object rawInfo = items.get(position);

        bindItem(holder ,rawInfo);
    }

    private void bindItem(MyViewHolder holder, Object rawInfo ) {
        WidgetItemView widgetItemView = (WidgetItemView) holder.itemView;
        PendingAddItemInfo createItemInfo = null;
        if(rawInfo instanceof AppWidgetProviderInfo){
            //widgetList.add(rawInfo);
            AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
            createItemInfo = new PendingAddWidgetInfo(info, null, null);
            // Determine the widget spans and min resize spans.
            int[] spanXY = Launcher.getSpanForWidget(launcherApplication, info);
            createItemInfo.spanX = spanXY[0];
            createItemInfo.spanY = spanXY[1];
            int[] minSpanXY = Launcher.getMinSpanForWidget(launcherApplication, info);
            createItemInfo.minSpanX = minSpanXY[0];
            createItemInfo.minSpanY = minSpanXY[1];

            widgetItemView.applyFromAppWidgetProviderInfo(info, -1, spanXY);
            widgetItemView.setTag(createItemInfo);

        } else if(rawInfo instanceof ResolveInfo) {
            ResolveInfo info = (ResolveInfo) rawInfo;
            createItemInfo = new PendingAddShortcutInfo(info.activityInfo);
            createItemInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
            createItemInfo.componentName = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);

            widgetItemView.applyFromResolveInfo(mPackageManager, info);
            widgetItemView.setTag(createItemInfo);

        }


        String key_id = null;
        if(rawInfo instanceof  AppWidgetProviderInfo){
            AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
            key_id = info.provider.getPackageName()+""+info.previewImage;
        } else if(rawInfo instanceof ResolveInfo) {
            ResolveInfo resolveInfo = (ResolveInfo) rawInfo;
            key_id = resolveInfo.activityInfo.packageName;
        }

        Bitmap b = launcherApplication.mHashMapBitmap.get(key_id);
        if(b != null){
            holder.widget_preview_img.setImageBitmap(b);
        } else {
            int maxPreviewWidth = launcherApplication.getCellWidth();
            int maxPreviewHeight = launcherApplication.getCellHeight();
            if(rawInfo instanceof AppWidgetProviderInfo){
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
                int[] cellSpans = Launcher.getSpanForWidget(launcherApplication.getApplicationContext(), info);
                int maxWidth = Math.min(maxPreviewWidth,
                        estimateCellWidth(cellSpans[0]));
                int maxHeight = Math.min(maxPreviewHeight,
                        estimateCellHeight(cellSpans[1]));
                b = getWidgetPreview(info.provider, info.previewImage, info.icon,
                        cellSpans[0], cellSpans[1], maxWidth, maxHeight);
                holder.widget_preview_img.setImageBitmap(b);
                String id = info.provider.getPackageName()+""+info.previewImage;
                launcherApplication.mHashMapBitmap.put(id,b);
            } else if (rawInfo instanceof ResolveInfo){
                ResolveInfo info = (ResolveInfo) rawInfo;
                maxPreviewWidth = launcherApplication.getScreenWidth()/3;
                b = getShortcutPreview(info, maxPreviewWidth, maxPreviewHeight);
                holder.widget_preview_img.setImageBitmap(b);
                launcherApplication.mHashMapBitmap.put(info.activityInfo.packageName,b);
            }
        }

        Timber.v("Widget Size : " + widgetList.size());
        Timber.v("Shortcut Widget Size : " + shortcutList.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return false;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView widget_name, widget_dim;
        ImageView widget_preview_img;
        PendingAddItemInfo widgetInfo;

        private MyViewHolder(WidgetItemView view) {
            super(view);
            widget_dim = (TextView) view.findViewById(R.id.widget_dims);
            widget_name = (TextView) view.findViewById(R.id.widget_name);
            widget_preview_img = (ImageView) view.findViewById(R.id.widget_preview);
        }
    }



    private Bitmap getWidgetPreview(ComponentName provider, int previewImage,
                                    int iconId, int cellHSpan, int cellVSpan, int maxWidth,
                                    int maxHeight) {
        // Load the preview image if possible
        String packageName = provider.getPackageName();
        if (maxWidth < 0) maxWidth = Integer.MAX_VALUE;
        if (maxHeight < 0) maxHeight = Integer.MAX_VALUE;

        Drawable drawable = null;
        if (previewImage != 0) {
            drawable = mPackageManager.getDrawable(packageName, previewImage, null);
            if (drawable == null) {
                Timber.w("Can't load widget preview drawable 0x" +
                        Integer.toHexString(previewImage) + " for provider: " + provider);
            }
        }

        int bitmapWidth;
        int bitmapHeight;
        Bitmap defaultPreview = null;
        boolean widgetPreviewExists = (drawable != null);
        if (widgetPreviewExists) {
            bitmapWidth = drawable.getIntrinsicWidth();
            bitmapHeight = drawable.getIntrinsicHeight();
        } else {
            // Generate a preview image if we couldn't load one
            if (cellHSpan < 1) cellHSpan = 1;
            if (cellVSpan < 1) cellVSpan = 1;

            BitmapDrawable previewDrawable = (BitmapDrawable) launcherApplication.getResources()
                    .getDrawable(R.mipmap.widget_preview_tile);
            final int previewDrawableWidth = previewDrawable
                    .getIntrinsicWidth();
            final int previewDrawableHeight = previewDrawable
                    .getIntrinsicHeight();
            bitmapWidth = previewDrawableWidth * cellHSpan; // subtract 2 dips
            bitmapHeight = previewDrawableHeight * cellVSpan;

            defaultPreview = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas c = mCachedAppWidgetPreviewCanvas.get();
            c.setBitmap(defaultPreview);
            previewDrawable.setBounds(0, 0, bitmapWidth, bitmapHeight);
            previewDrawable.setTileModeXY(Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT);
            previewDrawable.draw(c);
            c.setBitmap(null);

            // Draw the icon in the top left corner
            int minOffset = (int) (mAppIconSize * sWidgetPreviewIconPaddingPercentage);
            int smallestSide = Math.min(bitmapWidth, bitmapHeight);
            float iconScale = Math.min((float) smallestSide
                    / (mAppIconSize + 2 * minOffset), 1f);

            try {
                Drawable icon = null;
                int hoffset =
                        (int) ((previewDrawableWidth - mAppIconSize * iconScale) / 2);
                int yoffset =
                        (int) ((previewDrawableHeight - mAppIconSize * iconScale) / 2);
                if (iconId > 0)
                    icon = mIconCache.getFullResIcon(packageName, iconId);
                if (icon != null) {
                    renderDrawableToBitmap(icon, defaultPreview, hoffset,
                            yoffset, (int) (mAppIconSize * iconScale),
                            (int) (mAppIconSize * iconScale));
                }
            } catch (Resources.NotFoundException e) {
            }
        }

        // Scale to fit width only - let the widget preview be clipped in the
        // vertical dimension
        float scale = 1f;
        if (bitmapWidth > maxWidth) {
            scale = maxWidth / (float) bitmapWidth;
        }
        if (scale != 1f) {
            bitmapWidth = (int) (scale * bitmapWidth);
            bitmapHeight = (int) (scale * bitmapHeight);
        }

        Bitmap preview = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                Bitmap.Config.ARGB_8888);

        // Draw the scaled preview into the final bitmap
        if (widgetPreviewExists) {
            renderDrawableToBitmap(drawable, preview, 0, 0, bitmapWidth,
                    bitmapHeight);
        } else {
            final Canvas c = mCachedAppWidgetPreviewCanvas.get();
            final Rect src = mCachedAppWidgetPreviewSrcRect.get();
            final Rect dest = mCachedAppWidgetPreviewDestRect.get();
            c.setBitmap(preview);
            src.set(0, 0, defaultPreview.getWidth(), defaultPreview.getHeight());
            dest.set(0, 0, preview.getWidth(), preview.getHeight());

            Paint p = mCachedAppWidgetPreviewPaint.get();
            if (p == null) {
                p = new Paint();
                p.setFilterBitmap(true);
                mCachedAppWidgetPreviewPaint.set(p);
            }
            c.drawBitmap(defaultPreview, src, dest, p);
            c.setBitmap(null);
        }
        return preview;
    }

    /**
     * Estimates the width that the number of hSpan cells will take up.
     */
    public int estimateCellWidth(int hSpan) {
        // TODO: we need to take widthGap into effect
        return hSpan * mCellWidth;
    }

    /**
     * Estimates the height that the number of vSpan cells will take up.
     */
    public int estimateCellHeight(int vSpan) {
        // TODO: we need to take heightGap into effect
        return vSpan * mCellHeight;
    }

    private Bitmap getShortcutPreview(ResolveInfo info, int maxWidth, int maxHeight) {
        Bitmap tempBitmap = mCachedShortcutPreviewBitmap.get();
        final Canvas c = mCachedShortcutPreviewCanvas.get();
        if (tempBitmap == null ||
                tempBitmap.getWidth() != maxWidth ||
                tempBitmap.getHeight() != maxHeight) {
            tempBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
            mCachedShortcutPreviewBitmap.set(tempBitmap);
        } else {
            c.setBitmap(tempBitmap);
            c.drawColor(0, PorterDuff.Mode.CLEAR);
            c.setBitmap(null);
        }
        // Render the icon
        Drawable icon = mIconCache.getFullResIcon(info);

        int paddingTop =
                launcherApplication.getResources().getDimensionPixelOffset(R.dimen.shortcut_preview_padding_top);
        int paddingLeft =
                launcherApplication.getResources().getDimensionPixelOffset(R.dimen.shortcut_preview_padding_left);
        int paddingRight =
                launcherApplication.getResources().getDimensionPixelOffset(R.dimen.shortcut_preview_padding_right);

        int scaledIconWidth = (maxWidth - paddingLeft - paddingRight);

        renderDrawableToBitmap(
                icon, tempBitmap, paddingLeft, paddingTop, scaledIconWidth, scaledIconWidth);

        Bitmap preview = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
        c.setBitmap(preview);
        Paint p = mCachedShortcutPreviewPaint.get();
        if (p == null) {
            p = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            p.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            p.setAlpha((int) (255 * 0.06f));
            mCachedShortcutPreviewPaint.set(p);
        }
        c.drawBitmap(tempBitmap, 0, 0, p);
        c.setBitmap(null);

        renderDrawableToBitmap(icon, preview, 0, 0, mAppIconSize, mAppIconSize);

        return preview;
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

    abstract class WeakReferenceThreadLocal<T> {
        private ThreadLocal<WeakReference<T>> mThreadLocal;
        public WeakReferenceThreadLocal() {
            mThreadLocal = new ThreadLocal<WeakReference<T>>();
        }

        abstract T initialValue();

        public void set(T t) {
            mThreadLocal.set(new WeakReference<T>(t));
        }

        public T get() {
            WeakReference<T> reference = mThreadLocal.get();
            T obj;
            if (reference == null) {
                obj = initialValue();
                mThreadLocal.set(new WeakReference<T>(obj));
                return obj;
            } else {
                obj = reference.get();
                if (obj == null) {
                    obj = initialValue();
                    mThreadLocal.set(new WeakReference<T>(obj));
                }
                return obj;
            }
        }
    }

    class CanvasCache extends WeakReferenceThreadLocal<Canvas> {
        @Override
        protected Canvas initialValue() {
            return new Canvas();
        }
    }

    class PaintCache extends WeakReferenceThreadLocal<Paint> {
        @Override
        protected Paint initialValue() {
            return null;
        }
    }

    class BitmapCache extends WeakReferenceThreadLocal<Bitmap> {
        @Override
        protected Bitmap initialValue() {
            return null;
        }
    }

    class RectCache extends WeakReferenceThreadLocal<Rect> {
        @Override
        protected Rect initialValue() {
            return new Rect();
        }
    }


}

