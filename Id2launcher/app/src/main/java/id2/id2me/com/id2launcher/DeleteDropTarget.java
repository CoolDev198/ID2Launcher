package id2.id2me.com.id2launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import id2.id2me.com.id2launcher.listingviews.ListingContainerView;
import id2.id2me.com.id2launcher.models.AppInfo;
import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.LauncherAppWidgetInfo;
import id2.id2me.com.id2launcher.models.PendingAddItemInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;

/**
 * Created by CrazyInnoTech on 05-01-2017.
 */

public class DeleteDropTarget extends ButtonDropTarget {
    private static int DELETE_ANIMATION_DURATION = 285;
    private ColorStateList mOriginalTextColor;
    private TransitionDrawable mUninstallDrawable;
    private TransitionDrawable mRemoveDrawable;
    private TransitionDrawable mCurrentDrawable;

    public DeleteDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the drawable
        mOriginalTextColor = getTextColors();


        mHoverColor = ContextCompat.getColor(getContext(),R.color.delete_target_hover_tint);
        mUninstallDrawable = (TransitionDrawable)
                ContextCompat.getDrawable(getContext(),R.drawable.uninstall_target_selector);
        mRemoveDrawable = (TransitionDrawable) ContextCompat.getDrawable(getContext(),R.drawable.remove_target_selector);

        mRemoveDrawable.setCrossFadeEnabled(true);
        mUninstallDrawable.setCrossFadeEnabled(true);

        // The current drawable is set to either the remove drawable or the uninstall drawable
        // and is initially set to the remove drawable, as set in the layout xml.
        mCurrentDrawable = (TransitionDrawable) getCurrentDrawable();


    }

    private boolean isAllAppsApplication(DragSource source, Object info) {
        return (source instanceof ListingContainerView) && (info instanceof ApplicationInfo);
    }
    private boolean isAllAppsWidget(DragSource source, Object info) {
        if (source instanceof ListingContainerView) {
            if (info instanceof PendingAddItemInfo) {
                PendingAddItemInfo addInfo = (PendingAddItemInfo) info;
                switch (addInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        return true;
                }
            }
        }
        return false;
    }
    private boolean isDragSourceWorkspaceOrFolder(DragObject d) {
        //return (d.dragSource instanceof WorkSpace) || (d.dragSource instanceof Folder);
        return (d.dragSource instanceof WorkSpace);
    }
    private boolean isWorkspaceOrFolderApplication(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof ShortcutInfo);
    }
    private boolean isWorkspaceOrFolderWidget(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof LauncherAppWidgetInfo);
    }
    private boolean isWorkspaceFolder(DragObject d) {
        return (d.dragSource instanceof WorkSpace) && (d.dragInfo instanceof FolderInfo);
    }

    private void setHoverColor() {
        mCurrentDrawable.startTransition(mTransitionDuration);
        setTextColor(mHoverColor);
    }
    private void resetHoverColor() {
        mCurrentDrawable.resetTransition();
        setTextColor(mOriginalTextColor);
    }

    @Override
    public boolean acceptDrop(DragObject d) {
        // We can remove everything including App shortcuts, folders, widgets, etc.
        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        boolean isVisible = true;
        boolean isUninstall = false;

        // If we are dragging a widget from AppsCustomize, hide the delete target
        if (isAllAppsWidget(source, info)) {
            isVisible = false;
        }

        // If we are dragging an application from AppsCustomize, only show the control if we can
        // delete the app (it was downloaded), and rename the string to "uninstall" in such a case
        if (isAllAppsApplication(source, info)) {
            AppInfo appInfo = (AppInfo) info;
            if ((appInfo.flags & AppInfo.DOWNLOADED_FLAG) != 0) {
                isUninstall = true;
            } else {
                isVisible = false;
            }
            isVisible = false;
        }

        if (isUninstall) {
            setCompoundDrawablesWithIntrinsicBounds(mUninstallDrawable, null, null, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(mRemoveDrawable, null, null, null);
        }
        mCurrentDrawable = (TransitionDrawable) getCurrentDrawable();

        mActive = isVisible;
        resetHoverColor();
        ((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (getText().length() > 0) {
            setText(isUninstall ? R.string.uninstall_zone_label
                    : R.string.remove_zone_label);
        }
    }

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        super.onDragEnter(dragObject);
        setHoverColor();
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        super.onDragExit(dragObject);

        if (!dragObject.dragComplete) {
            resetHoverColor();
        } else {
            // Restore the hover color if we are deleting
            dragObject.dragView.setColor(mHoverColor);
        }

    }

    @Override
    public void onDrop(DragObject dragObject) {
        animateToTrashAndCompleteDrop(dragObject);
    }

    private void animateToTrashAndCompleteDrop(final DragObject d) {
        DragLayer dragLayer = mLauncher.getDragLayer();
        Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
        Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                mCurrentDrawable.getIntrinsicWidth(), mCurrentDrawable.getIntrinsicHeight());
        float scale = (float) to.width() / from.width();

        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {

                completeDrop(d);
            }
        };
        dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 0.1f, 0.1f,
                DELETE_ANIMATION_DURATION, new DecelerateInterpolator(2),
                new LinearInterpolator(), onAnimationEndRunnable,
                DragLayer.ANIMATION_END_DISAPPEAR, null);
    }

    private void completeDrop(DragObject d) {
        ItemInfo item = (ItemInfo) d.dragInfo;
       // Toast.makeText(getContext(),"Remove Drop Target", Toast.LENGTH_SHORT).show();
        if (isAllAppsApplication(d.dragSource, item)) {
            // Uninstall the application if it is being dragged from AppsCustomize
            //mLauncher.startApplicationUninstallActivity((ApplicationInfo) item);
        } else if (isWorkspaceOrFolderApplication(d)) {
         //   LauncherModel.deleteItemFromDatabase(mLauncher, item);
        } else if (isWorkspaceFolder(d)) {
            // Remove the folder from the workspace and delete the contents from launcher model
            FolderInfo folderInfo = (FolderInfo) item;
          //  mLauncher.removeFolder(folderInfo);
          //  LauncherModel.deleteFolderContentsFromDatabase(mLauncher, folderInfo);
        } else if (isWorkspaceOrFolderWidget(d)) {
            // Remove the widget from the workspace
            //mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
          //  LauncherModel.deleteItemFromDatabase(mLauncher, item);

            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    }
                }.start();
            }
        }
    }


}
