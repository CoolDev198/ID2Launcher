package id2.id2me.com.id2launcher.drawer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.general.AppGridView;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.database.AppInfo;

/**
 * Created by bliss76 on 21/06/16.
 */
public class DrawerGridAdapter extends BaseAdapter implements View.OnClickListener,View.OnLongClickListener
{
  //  public static DragNDropAdapterInterface dragNDropAdapterInterface;
  public static DragNDropAdapterInterface dragNDropAdapterInterface;
    private static View draggedItem;
    LayoutInflater inflater;
    ArrayList<AppInfo> gridList;
    DrawerLayout drawerLayout;
    boolean appClickEvent = false;
    private Context mContext;
    private String[] categoryArry;
    AppGridView appGridView;
   // ArrayList arrayList_collection;

    public DrawerGridAdapter(Context c, ArrayList<AppInfo> gridList, DrawerLayout drawerLayout,
                       boolean appClickEvent,AppGridView appGridView) {
        this.appClickEvent = appClickEvent;
        this.drawerLayout = drawerLayout;
        mContext = c;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.gridList=gridList;
        this.appGridView=appGridView;
        drawerLayout.setOnDragListener(
                new MyDragListener());

    }

    public static void setVisibility() {
        if (draggedItem != null) {
            draggedItem.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getCount() {
        return gridList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            actionOnLongClick();
            UninstallAnimation(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View grid=convertView;
        try {
            if (convertView == null) {
                grid = inflater.inflate(R.layout.drawer_grid_item, null);
                holder = new ViewHolder();
                grid.setTag(holder);
                holder.itemText = (TextView) grid.findViewById(R.id.drawer_grid_text);
                holder.itemImage = (ImageView) grid.findViewById(R.id.drawer_grid_image);

                grid.setOnClickListener(this);
                grid.setOnLongClickListener(this);
            } else {
                holder = (ViewHolder) grid.getTag();
            }
            gridList.get(position);
            holder.itemImage.setTag(position);
            holder.pName = gridList.get(position).getPname();
            holder.itemText.setText(gridList.get(position).getAppname());
            holder.itemImage.setImageDrawable(gridList.get(position).getIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return grid;
    }

    @SuppressLint("RtlHardcoded")
    public void lauchApp(AppInfo appInfo) {
        try {
            Intent intent = null;
            String pckName = appInfo.getPname();

            if (pckName != null) {
                    intent = mContext.getPackageManager()
                            .getLaunchIntentForPackage(pckName);
                mContext.startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                Toast.makeText(mContext,
                        mContext.getResources().getText(R.string.appNotFound),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {


            e.printStackTrace();
        }
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onClick(View v) {
        try {
            Toast.makeText(mContext,"Hi I am here",Toast.LENGTH_LONG).show();
            if (appClickEvent) {
                lauchApp(gridList.get(Integer.parseInt(v
                        .findViewById(R.id.drawer_grid_image).getTag().toString())));
            } else {
                drawerLayout.closeDrawer(Gravity.LEFT);
                dragNDropAdapterInterface.addAppToListView(gridList.get(Integer
                        .parseInt(v.findViewById(R.id.grid_image).getTag()
                                .toString())));
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void UninstallAnimation(View view) {


        ClipData.Item item = new ClipData.Item(
                (CharSequence) ((ViewHolder) view.getTag()).itemText.getText().toString());

        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData(((ViewHolder) view.getTag()).itemText.getText().toString(),
                mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                view);
        shadowBuilder.onProvideShadowMetrics(
                new Point(view.getWidth(), view.getHeight()),
                new Point((int) view.getX() / 2, (int) view
                        .getY() / 2));
        view.startDrag(data, // data to be dragged
                shadowBuilder, // drag shadow
                view, // local data about the drag and drop
                // operation
                0 // no needed flags
        );

        view.setVisibility(View.INVISIBLE);

    }

    void actionOnLongClick() {
        try {
            drawerLayout.findViewById(R.id.all_apps_header).setVisibility(View.GONE);
            drawerLayout.findViewById(R.id.remove_header_layout).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ViewHolder {
        public TextView itemText;
        public ImageView itemImage;
        public String pName;
    }

    class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {

            // Handles each of the expected events
            switch (event.getAction()) {

                // signal for the start of a drag and drop operation.
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    drawerLayout.findViewById(R.id.top_header_drawer).setVisibility(View.VISIBLE);

                    break;

                // the drag point has entered the bounding box of the View
                case DragEvent.ACTION_DRAG_ENTERED:
                    // change the shape of the view


                    break;

                // the user has moved the drag shadow outside the bounding box of
                // the View
                case DragEvent.ACTION_DRAG_EXITED:
                    // change the shape of the view
                    // back to normal
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int drop_y = (int) event.getY();
                    int remove_bottomy = (drawerLayout
                            .findViewById(R.id.remove_header_layout))
                            .getBottom();
                    int remove_topy = (drawerLayout
                            .findViewById(R.id.remove_header_layout))
                            .getHeight() + 70;

                    if ((remove_bottomy > drop_y)
                            || (drop_y <= (remove_bottomy + remove_topy))) {

                        drawerLayout.findViewById(R.id.remove_header_layout)
                                .setBackgroundColor(Color.RED);
                    } else {
                        drawerLayout.findViewById(R.id.remove_header_layout)
                                .setBackgroundColor(Color.TRANSPARENT);
                    }

                    // change the shape of the view
                    // back to normal
                    break;
                // drag shadow has been released,the drag point is within the
                // bounding box of the View
                case DragEvent.ACTION_DROP:
                    // if the view is the bottomlinear, we accept the drag item

                    try {
                        draggedItem = (View) event.getLocalState();

                        drop_y = (int) event.getY();
                        remove_bottomy = (drawerLayout
                                .findViewById(R.id.remove_header_layout))
                                .getBottom();
                        remove_topy = (drawerLayout
                                .findViewById(R.id.remove_header_layout))
                                .getHeight() + 70;

                        if ((remove_bottomy > drop_y)
                                || (drop_y <= (remove_bottomy + remove_topy))) {

                            Uri packageUri = Uri.parse("package:" + ((ViewHolder) ((View) event.getLocalState()).getTag()).pName);
                            Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                            mContext.startActivity(uninstallIntent);


                        } else {
                            View view = (View) event.getLocalState();
                            view.setVisibility(View.VISIBLE);
                        }

                        drawerLayout.findViewById(R.id.all_apps_header).setVisibility(View.GONE);
                        drawerLayout.findViewById(R.id.remove_header_layout).setVisibility(View.GONE);
                        drawerLayout.findViewById(R.id.remove_header_layout)
                                .setBackgroundColor(Color.TRANSPARENT);
                        drawerLayout.findViewById(R.id.top_header_drawer).setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                // the drag and drop operation has concluded.
                case DragEvent.ACTION_DRAG_ENDED:
                    // go back to normal shape


                default:
                    break;
            }
            return true;
        }

    }
}


