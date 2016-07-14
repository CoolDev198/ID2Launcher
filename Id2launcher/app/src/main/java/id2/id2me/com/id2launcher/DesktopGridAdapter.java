package id2.id2me.com.id2launcher;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.folder.FolderFragmentInterface;
import id2.id2me.com.id2launcher.general.AppGridView;

/**
 * Created by bliss76 on 27/05/16.
 */


class DekstopGridAdapater extends BaseAdapter implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    public static Context mContext;
    public static AppGridView appGridView;
    public static ImageView img;
    LayoutInflater inflater;
    ArrayList<AppInfo> appInfos;
    RelativeLayout mrelativeLayout;
    View grid;
    int position;
    ArrayList arrayList_collection, arrayList_id, arrayList_folderNames;
    int drag_position;
    MyDragListener myDragListener;
    FolderFragmentInterface folderFragmentInterface;
    boolean click = true;

    public DekstopGridAdapater(Context context, ArrayList arrayList_collection, ArrayList arrayList_id, RelativeLayout relativeLayout, AppGridView appGridView, ArrayList<AppInfo> appInfos, ArrayList arrayList_folderNames, FolderFragmentInterface folderFragmentInterface) {
        mContext = context;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mrelativeLayout = relativeLayout;
        this.appInfos = appInfos;

        this.folderFragmentInterface = folderFragmentInterface;
        this.arrayList_collection = arrayList_collection;
        this.arrayList_id = arrayList_id;
        this.arrayList_folderNames = arrayList_folderNames;

        this.appGridView = appGridView;
        appGridView.setOnItemLongClickListener(this);
        appGridView.setOnItemClickListener(this);
        myDragListener = new MyDragListener(arrayList_collection, arrayList_id, arrayList_folderNames, drag_position, appInfos, this, folderFragmentInterface);
        relativeLayout.setOnDragListener(myDragListener);

    }

    @Override
    public int getCount() {


        return arrayList_collection.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        grid = convertView;
        this.position = position;

        try {
            if (convertView == null) {
                holder = new ViewHolder();
                grid = inflater.inflate(R.layout.grid_item, null);
                grid.setTag(holder);
                holder.itemText = (TextView) grid.findViewById(R.id.grid_text);
                img = holder.itemImage = (ImageView) grid.findViewById(R.id.grid_image);


            } else {
                holder = (ViewHolder) grid.getTag();
            }

            holder.itemImage.setTag(position);
            holder.pName = ((AppInfo) ((ArrayList) arrayList_collection.get(position)).get(0)).getPname();
            if (arrayList_id.get(position).toString().equalsIgnoreCase("app")) {
                holder.itemText.setText(((AppInfo) ((ArrayList) arrayList_collection.get(position)).get(0)).getAppname());
                holder.itemImage.setImageDrawable(((AppInfo) ((ArrayList) arrayList_collection.get(position)).get(0)).getIcon());
            } else {
                holder.itemImage.setImageResource(R.mipmap.folder_icon);
                holder.itemText.setText("folder");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return grid;

    }


    public void launchApp(AppInfo appInfo) {
        try {
            Intent intent = null;
            String pckName = appInfo.getPname();

            intent = mContext.getPackageManager()
                    .getLaunchIntentForPackage(pckName);
            mContext.startActivity(intent);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        UninstallAnimation(view);
        drag_position = position;
        Log.v("Position", "" + drag_position);
        myDragListener.setDragPos(drag_position);
        //  Toast.makeText(mContext, "" + drag_position, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (arrayList_id.get(position).toString().equalsIgnoreCase("app")) {
                AppInfo appinfo = (AppInfo) (((ArrayList) arrayList_collection.get(position)).get(0));
                launchApp(appinfo);
            } else {
                //showPopup();
                // myDragListener.resetAfterDrop(view);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPopup() {
        PopupWindow popup = new PopupWindow(mContext);
        if (click) {
            int popupWidth = 800;
            int popupHeight = 800;

            RelativeLayout relativeLayout = (RelativeLayout) grid.findViewById(R.id.relative_view);
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = layoutInflater.inflate(R.layout.first_fragment, relativeLayout);


            popup.setContentView(layout);
            popup.setWidth(popupWidth);
            popup.setHeight(popupHeight);
            popup.setFocusable(true);
            popup.showAtLocation(layout, Gravity.CENTER, 50, 50);
            //  myDragListener.resetAfterDrop(view);
            AppInfo appInfo = (AppInfo) ((ArrayList) arrayList_collection.get(drag_position)).get(0);
            ((ArrayList) arrayList_collection.get(myDragListener.target)).add(appInfo);
            Log.v("Mika", "" + ((ArrayList) arrayList_collection.get(myDragListener.target)).add(appInfo));

            click = false;
        } else {
            popup.dismiss();
            click = true;
        }

    }


    private static class ViewHolder {
        public TextView itemText;
        public ImageView itemImage;
        public String pName;
    }

}


class MyDragListener implements View.OnDragListener {


    ArrayList arrayList_collection, arrayList_id, arrayList_folderNames;
    int drag_position;
    int target;
    ArrayList<AppInfo> appInfos;
    List<Fragment> fList;
    FolderFragmentInterface folderFragmentInterface;
    private View draggedItem;
    private RelativeLayout mrelativeLayout;
    private DekstopGridAdapater adapter;


    MyDragListener(ArrayList arrayList_collection, ArrayList arrayList_id, ArrayList arrayList_folderNames, int drag_position, ArrayList<AppInfo> appInfos, DekstopGridAdapater adapter, FolderFragmentInterface folderFragmentInterface) {
        this.arrayList_collection = arrayList_collection;
        this.arrayList_id = arrayList_id;
        this.arrayList_folderNames = arrayList_folderNames;
        this.drag_position = drag_position;
        this.appInfos = appInfos;
        this.adapter = adapter;
        this.folderFragmentInterface = folderFragmentInterface;
    }


    @Override
    public boolean onDrag(View v, DragEvent event) {

        // Handles each of the expected events
        switch (event.getAction()) {

            // signal for the start of a drag and drop operation.
            case DragEvent.ACTION_DRAG_STARTED:
                // do nothing
                mrelativeLayout = (RelativeLayout) v.findViewById(R.id.relative_view);
                mrelativeLayout.setVisibility(View.VISIBLE);
                return true;

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


                // change the shape of the view
                // back to normal
                break;
            // drag shadow has been released,the drag point is within the
            // bounding box of the View
            case DragEvent.ACTION_DROP:
                // if the view is the bottomlinear, we accept the drag item

                try {
                    int X = (int) event.getX();
                    int Y = (int) event.getY();

                    View view = (View) event.getLocalState();
                    // view.setX(event.getX());
                    // view.setX(event.getY());
                    target = DekstopGridAdapater.appGridView.pointToPosition(X, Y);
                    //   Toast.makeText(DekstopGridAdapater.mContext, "" + target, Toast.LENGTH_SHORT).show();
                    resetAfterDrop(v);

                    Log.v("Cordinates", "" + target);

                    view.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            // the drag and drop operation has concluded.
            case DragEvent.ACTION_DRAG_ENDED:
                return true;

            default:
                break;
        }
        return true;
    }

    public void resetAfterDrop(View view) {
        try {
            if (drag_position != -1) {

                String name = "";
                if (drag_position > target) {

                    if (arrayList_id.get(target).toString().equalsIgnoreCase("app")) {
                        AppInfo appInfo = (AppInfo) ((ArrayList) arrayList_collection.get(drag_position)).get(0);
                        ((ArrayList) arrayList_collection.get(target)).add(appInfo);
                        arrayList_collection.remove((ArrayList) arrayList_collection.get(drag_position));
                        arrayList_id.remove(drag_position);


                        if (arrayList_id.get(target).toString().equalsIgnoreCase("app")) {
                            arrayList_id.set(target, "folder");
                            name = "folder" + HomeActivity.folderincr;
                            HomeActivity.folderincr++;
                            arrayList_folderNames.set(target, name);
                        } else {
                            name = arrayList_folderNames.get(target).toString();
                        }

                        folderFragmentInterface.addOrUpdateFolder(name, (ArrayList) arrayList_collection.get(target));
                    }

                } else {

                    if (arrayList_id.get(target).toString().equalsIgnoreCase("app")) {
                        AppInfo appInfo = (AppInfo) ((ArrayList) arrayList_collection.get(target)).get(0);

                        ((ArrayList) arrayList_collection.get(drag_position)).add(appInfo);
                        arrayList_collection.remove((ArrayList) arrayList_collection.get(target));
                        arrayList_id.remove(target);


                        if (arrayList_id.get(drag_position).toString().equalsIgnoreCase("app")) {
                            arrayList_id.set(drag_position, "folder");

                            name = "folder" + HomeActivity.folderincr;
                            HomeActivity.folderincr++;
                            arrayList_folderNames.set(drag_position, name);
                        } else {
                            name = arrayList_folderNames.get(drag_position).toString();
                        }

                        folderFragmentInterface.addOrUpdateFolder(name, (ArrayList) arrayList_collection.get(drag_position));

                    }
                }
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setDragPos(int dragPos) {
        this.drag_position = dragPos;
    }

    public void setTargetPos(int target) {
        this.target = target;
    }
}

