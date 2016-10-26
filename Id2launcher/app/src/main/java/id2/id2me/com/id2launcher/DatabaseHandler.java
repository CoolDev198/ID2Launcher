package id2.id2me.com.id2launcher;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.models.ItemInfoModel;
import id2.id2me.com.id2launcher.models.NotificationWidgetModel;

public class DatabaseHandler extends SQLiteOpenHelper {


    public static final int ITEM_TYPE_APP = 1;
    public static final int ITEM_TYPE_FOLDER = 2;
    public static final int ITEM_TYPE_APPWIDGET = 3;
    public static final String COLUMN_ICON = "icon";
    static final long CONTAINER_DESKTOP = -100;
    static final long CONTAINER_FOLDER = -101;
    static final int ITEM_TYPE_WIDGET_CLOCK = 1000;
    static final int ITEM_TYPE_WIDGET_SEARCH = 1001;
    static final int ITEM_TYPE_WIDGET_PHOTO_FRAME = 1002;
    private static final String DATABASE_NAME = "id2launcher";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NOTI_WIDGET = "notification_table";
    private static final String TABLE_ITEMS = "items_table";
    private static final String COLUMN_NOTI_PNAME = "p_name";
    private static final String COLUMN_NOTI_APP_NAME = "app_name";
    private static final String COLUMN_NOTI_COUNT = "noti_count";
    private static final String COLUMN_NOTI_APP_IMG = "noti_app_img";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SCREEN = "screenID";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CELLX = "cellx";
    private static final String COLUMN_CELLY = "celly";
    private static final String COLUMN_SPANX = "spanx";
    private static final String COLUMN_SPANY = "spany";
    private static final String COLUMN_APPWIDGET_ID = "appWidgetID";
    private static final String COLUMN_ITEM_TYPE = "itemType";
    private static final String COLUMN_PNAME = "pname";
    private static final String COLUMN_ICON_TYPE = "iconType";
    private static final String COLUMN_INTENT = "intent";
    private static final String COLUMN_CONTAINER = "container";
    public static ArrayList<ItemInfoModel> itemInfosList;
    static DatabaseHandler sInstance;
    int maxID = -1;
    Context context;

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (sInstance == null) {
            itemInfosList = new ArrayList<>();
            sInstance = new DatabaseHandler(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTIFICATION_WIDGET_TABLE = "CREATE TABLE " + TABLE_NOTI_WIDGET + " (" +
                COLUMN_NOTI_PNAME + " TEXT PRIMARY KEY," + COLUMN_NOTI_APP_NAME + " TEXT," + COLUMN_NOTI_COUNT +
                " INTEGER," + COLUMN_NOTI_APP_IMG + " TEXT " + ")";

        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_SCREEN + " INTEGER,"
                + COLUMN_TITLE + " TEXT," + COLUMN_INTENT + " TEXT," + COLUMN_CONTAINER + " INTEGER," +
                COLUMN_CELLX + " INTEGER," + COLUMN_CELLY + " INTEGER," + COLUMN_SPANX + " INTEGER," + COLUMN_SPANY + " INTEGER,"
                + COLUMN_ITEM_TYPE + " INTEGER," + COLUMN_APPWIDGET_ID + " INTEGER NOT NULL DEFAULT -1," + COLUMN_ICON_TYPE + " INTEGER,"
                + COLUMN_PNAME + " TEXT," + COLUMN_ICON + " BLOB " + ")";

        db.execSQL(CREATE_NOTIFICATION_WIDGET_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
        insertNotificationData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void resetNotificationCount(String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            String[] whereArgs = {packageName};
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOTI_COUNT, 0);
            db.update(TABLE_NOTI_WIDGET, values, COLUMN_NOTI_PNAME + " =?", whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateNotificationData(String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String[] column = {COLUMN_NOTI_COUNT};
            cursor = db.query(TABLE_NOTI_WIDGET, null, COLUMN_NOTI_PNAME + " = ? ", new String[]{packageName}, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                int count = cursor.getInt((cursor.getColumnIndex(COLUMN_NOTI_COUNT)));
                count++;
                ContentValues values = new ContentValues();
                values.put(COLUMN_NOTI_COUNT, count);
                db.update(TABLE_NOTI_WIDGET, values, COLUMN_NOTI_PNAME + " = ? ", new String[]{packageName});
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            cursor.close();
        }
    }

    public List<String> getNotificationPackages() {
        ArrayList<String> notificationPackages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("select " + COLUMN_NOTI_PNAME + " from " + TABLE_NOTI_WIDGET, null);
            res.moveToFirst();
            String pckName;
            while (res.isAfterLast() == false) {
                pckName = res.getString((res.getColumnIndex(COLUMN_NOTI_PNAME)));
                notificationPackages.add(pckName);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            res.close();
        }
        return notificationPackages;
    }

    private void insertNotificationData(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            String[] packageArray = context.getResources().getStringArray(R.array.noti_app_id);
            String[] appName = context.getResources().getStringArray(R.array.noti_app_name);
            String[] appImage = context.getResources().getStringArray(R.array.noti_app_image);
            ContentValues values = new ContentValues();
            for (int i = 0; i < packageArray.length; i++) {
                values.put(COLUMN_NOTI_PNAME, packageArray[i]);
                values.put(COLUMN_NOTI_APP_NAME, appName[i]);
                values.put(COLUMN_NOTI_APP_IMG, appImage[i]);
                values.put(COLUMN_NOTI_COUNT, 0);
                db.insert(TABLE_NOTI_WIDGET, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private void initializeMaxId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_ITEMS, null);
            cursor.moveToFirst();
            maxID = cursor.getInt(0);

        } catch (Exception e) {
            maxID = -1;
        } finally {
            cursor.close();
        }
    }

    public void addOrMoveItemInfo(ItemInfoModel itemInfo) {
        try {
            if (itemInfo.getId() == ItemInfoModel.NO_ID) {
                addItemInfoToDataBase(itemInfo);
            } else {
                modifyItemInfo(itemInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moveItemInfo(ItemInfoModel itemInfo) {

    }

    public void modifyItemInfo(ItemInfoModel itemInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTAINER, itemInfo.getContainer());
            values.put(COLUMN_CELLX, itemInfo.getCellX());
            values.put(COLUMN_CELLY, itemInfo.getCellY());
            db.update(TABLE_ITEMS, values, COLUMN_ID + " = ? ", new String[]{itemInfo.getId() + ""});
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            //   cursor.close();
        }


    }

    public ArrayList<ItemInfoModel> getItemsInfo() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {

            res = db.rawQuery("select * from " + TABLE_ITEMS, null);
            res.moveToFirst();

            while (res.isAfterLast() == false) {

                ItemInfoModel itemInfo = new ItemInfoModel();
                itemInfo.setPname(res.getString((res.getColumnIndex(COLUMN_PNAME))));
                itemInfo.setIcon(res.getBlob(res.getColumnIndex(COLUMN_ICON)));
                itemInfo.setCellY(res.getInt(res.getColumnIndex(COLUMN_CELLY)));
                itemInfo.setCellX(res.getInt(res.getColumnIndex(COLUMN_CELLX)));
                itemInfo.setScreen(res.getInt(res.getColumnIndex(COLUMN_SCREEN)));
                itemInfo.setContainer(res.getInt(res.getColumnIndex(COLUMN_CONTAINER)));
                itemInfosList.add(itemInfo);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            res.close();
        }

        return itemInfosList;
    }

    public void addItemInfoToDataBase(ItemInfoModel itemInfo) {
        initializeMaxId();
        try {
            maxID++;
            itemInfo.setId(maxID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, itemInfo.getId());
            values.put(COLUMN_CONTAINER, itemInfo.getContainer());
            values.put(COLUMN_CELLX, itemInfo.getCellX());
            values.put(COLUMN_CELLY, itemInfo.getCellY());
            values.put(COLUMN_ICON, itemInfo.getIcon());
            values.put(COLUMN_INTENT, itemInfo.getIntent());
            values.put(COLUMN_SPANX, itemInfo.getSpanX());
            values.put(COLUMN_SPANY, itemInfo.getSpanY());
            values.put(COLUMN_APPWIDGET_ID, itemInfo.getAppWidgetId());
            values.put(COLUMN_TITLE, itemInfo.getTitle());
            values.put(COLUMN_ITEM_TYPE, itemInfo.getItemType());
            values.put(COLUMN_ICON_TYPE, itemInfo.getIconType());
            values.put(COLUMN_PNAME, itemInfo.getPname());
            values.put(COLUMN_SCREEN,itemInfo.getScreen());
            db.insert(TABLE_ITEMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteItemInfo() {
    }

    public void updateItemInfo() {
    }


    public void getNotificationData() {
        ArrayList<NotificationWidgetModel> notificationWidgetModels = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            notificationWidgetModels = new ArrayList<>();
            cursor = db.query(TABLE_NOTI_WIDGET, null, COLUMN_NOTI_COUNT + ">?", new String[]{"0"}, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                NotificationWidgetModel notificationWidgetModel = new NotificationWidgetModel();
                notificationWidgetModel.setPname(cursor.getString(0));
                notificationWidgetModel.setAppName(cursor.getString(1));
                notificationWidgetModel.setCount(cursor.getInt(2));
                notificationWidgetModel.setAppImageName(cursor.getString(3));
                notificationWidgetModels.add(notificationWidgetModel);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        LauncherApplication.notificationWidgetModels = notificationWidgetModels;
    }

    public ArrayList<ItemInfoModel> getAppsListOfFolder(long id) {
        ArrayList<ItemInfoModel> itemInfoModels = null;
        Cursor res = null;
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            itemInfoModels = new ArrayList<>();
            res = db.query(TABLE_ITEMS, null, COLUMN_CONTAINER + "=?", new String[]{id + ""}, null, null, null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                ItemInfoModel itemInfo = new ItemInfoModel();
                itemInfo.setPname(res.getString((res.getColumnIndex(COLUMN_PNAME))));
                itemInfo.setIcon(res.getBlob(res.getColumnIndex(COLUMN_ICON)));
                itemInfo.setCellY(res.getInt(res.getColumnIndex(COLUMN_CELLY)));
                itemInfo.setCellX(res.getInt(res.getColumnIndex(COLUMN_CELLX)));
                itemInfo.setContainer(res.getInt(res.getColumnIndex(COLUMN_CONTAINER)));
                itemInfoModels.add(itemInfo);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            res.close();
        }
        return itemInfoModels;
    }

    public long addFolderToDatabase(ItemInfoModel itemInfo) {
        initializeMaxId();
        try {
            maxID++;
            itemInfo.setId(maxID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, itemInfo.getId());
            values.put(COLUMN_CONTAINER, itemInfo.getContainer());
            values.put(COLUMN_CELLX, itemInfo.getCellX());
            values.put(COLUMN_CELLY, itemInfo.getCellY());
            values.put(COLUMN_ICON, itemInfo.getIcon());
            values.put(COLUMN_INTENT, itemInfo.getIntent());
            values.put(COLUMN_SPANX, itemInfo.getSpanX());
            values.put(COLUMN_SPANY, itemInfo.getSpanY());
            values.put(COLUMN_APPWIDGET_ID, itemInfo.getAppWidgetId());
            values.put(COLUMN_TITLE, itemInfo.getTitle());
            values.put(COLUMN_ITEM_TYPE, itemInfo.getItemType());
            values.put(COLUMN_ICON_TYPE, itemInfo.getIconType());
            values.put(COLUMN_PNAME, itemInfo.getPname());
            values.put(COLUMN_SCREEN,itemInfo.getScreen());
            db.insert(TABLE_ITEMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return itemInfo.getId();
    }
}