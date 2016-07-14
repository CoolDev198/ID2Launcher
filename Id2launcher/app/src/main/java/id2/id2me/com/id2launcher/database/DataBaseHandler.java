package id2.id2me.com.id2launcher.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bliss105 on 14/07/16.
 */

public class DataBaseHandler extends SQLiteOpenHelper {

    //All Static variables

    public  static  DataBaseHandler dataBaseHandler;
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "launcherSettings";

    // Folder table name
    private static final String TABLE_FOLDER_SETTINGS = "folderSettings";

    // Page table name
    private static final String TABLE_PAGE_SETTINGS = "pageSettings";

    // App table name
    private static final String TABLE_APP_SETTINGS = "appSettings";

    private static final String COLUMN_FOLDER_ID = "folder_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PAGE_ID = "page_id";
    private static final String COLUMN_APP_PACKAGE_NAME = "pname";

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_PAGE_TABLE = "CREATE TABLE " + TABLE_PAGE_SETTINGS + "("
                + COLUMN_PAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT )";


        String CREATE_FOLDER_TABLE = "CREATE TABLE " + TABLE_FOLDER_SETTINGS + "("
                + COLUMN_FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT,"
                + COLUMN_PAGE_ID + " INTEGER )";

        String CREATE_APP_TABLE = "CREATE TABLE " + TABLE_APP_SETTINGS + "("
                + COLUMN_APP_PACKAGE_NAME + " TEXT ," + COLUMN_NAME + " TEXT ,"+ COLUMN_FOLDER_ID + " INTEGER ,"
                + COLUMN_PAGE_ID + " INTEGER " + ")";

        db.execSQL(CREATE_PAGE_TABLE);
        db.execSQL(CREATE_FOLDER_TABLE);
        db.execSQL(CREATE_APP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Adding new App Info
    void addAppSettings(AppInfo appInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_PACKAGE_NAME, appInfo.getPname()); // App Package Name
        values.put(COLUMN_NAME, appInfo.getAppname()); // App Name
        values.put(COLUMN_FOLDER_ID, appInfo.getFolderId());
        values.put(COLUMN_PAGE_ID, appInfo.getPageId());
        db.insert(TABLE_APP_SETTINGS, null, values);  // Inserting Row
        db.close(); // Closing database connection
    }

    // Adding new Folder Info
    void addFolderSettings(FolderInfo folderInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, folderInfo.getFolderName()); // Folder Name
        values.put(COLUMN_PAGE_ID, folderInfo.getPageId()); // Page Id
        values.put(COLUMN_FOLDER_ID,folderInfo.getFolderId()); //Folder Id
        db.insert(TABLE_FOLDER_SETTINGS, null, values);  // Inserting Row
        db.close(); // Closing database connection
    }
    // Adding new Page Info
    void addPageSettings(PageInfo pageInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, pageInfo.getPageName()); // Page Name
        db.insert(TABLE_PAGE_SETTINGS, null, values);  // Inserting Row
        db.close(); // Closing database connection
    }
    // Getting Page Count
    public int getPageCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PAGE_SETTINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        // return count
        return cursor.getCount();
    }

    // Getting All AppSettings
    public List<AppInfo> getAllAppSettings() {
        List<AppInfo> contactList = new ArrayList<AppInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_APP_SETTINGS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AppInfo appInfo = new AppInfo();
                appInfo.setPname(cursor.getString(0));
                appInfo.setAppname(cursor.getString(1));
                appInfo.setFolderId(Integer.parseInt(cursor.getString(2)));
                appInfo.setPageId(Integer.parseInt(cursor.getString(3)));

                // Adding app info to list
                contactList.add(appInfo);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }}
