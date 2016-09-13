package id2.id2me.com.id2launcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.notificationWidget.NotificationWidgetModel;

/**
 * Created by sunita on 9/13/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "id2launcher";

    // Notification table name
    private final static String TABLE_NOTI_WIDGET = "notification_table";


    //Notification table column name
    private final static String COLUMN_NOTI_PNAME = "p_name";
    private final static String COLUMN_NOTI_APP_NAME = "app_name";
    private final static String COLUMN_NOTI_COUNT = "noti_count";
    private final static String COLUMN_NOTI_APP_IMG = "noti_app_img";
    private static DatabaseHandler sInstance;

    Context context ;

    public static synchronized DatabaseHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTIFICATION_WIDGET_TABLE = "CREATE TABLE "
                + TABLE_NOTI_WIDGET + "("
                + COLUMN_NOTI_PNAME + " TEXT PRIMARY KEY,"
                + COLUMN_NOTI_APP_NAME + " TEXT,"
                + COLUMN_NOTI_COUNT + " INTEGER,"
                +COLUMN_NOTI_APP_IMG + " TEXT "
                + ")";
        db.execSQL(CREATE_NOTIFICATION_WIDGET_TABLE);


        insertNotificationData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTI_WIDGET);

        // Create tables again
        onCreate(db);
    }

    public void resetNotificationCount(String packageName) {

        SQLiteDatabase  db = this.getWritableDatabase();
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

        SQLiteDatabase  db = this.getWritableDatabase();
        Cursor cursor =null;
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

         ArrayList <String> notificationPackages =new ArrayList<>();

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

    public void insertNotificationData(SQLiteDatabase db) {

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

    public void getNotificationData() {

        List<NotificationWidgetModel> notificationWidgetModels = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            notificationWidgetModels = new ArrayList<NotificationWidgetModel>();

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
}
