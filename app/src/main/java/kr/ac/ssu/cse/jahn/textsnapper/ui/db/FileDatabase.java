package kr.ac.ssu.cse.jahn.textsnapper.ui.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class FileDatabase {

    static final String DB_NAME = "TextSnapper.db";
    static final String TABLE_FILE = "File";
    static final int    DB_VERSION = 1;

    Context mContext = null;

    private static FileDatabase mFileDatabase = null;
    private SQLiteDatabase mDatabase = null;

    /**
     * singleton 패턴으로 구현
     */
    public static FileDatabase getInstance(Context context) {
        if(mFileDatabase == null) {
            mFileDatabase = new FileDatabase(context);
            Log.d("DEBUG9", "DB Manager created");
        }
        return mFileDatabase;
    }

    private FileDatabase(Context context) {
        mContext = context;

        mDatabase = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        Log.d("DEBUG9", "DB opened!" + mDatabase.isOpen() + mDatabase.toString());

        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS  " + TABLE_FILE
                + "("+"filename  TEXT, "
                    + "file   FILE);");
    }

    public long insert(ContentValues addRowValue) {
        return mDatabase.insert(TABLE_FILE, null, addRowValue);
    }

    public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mDatabase.query(TABLE_FILE, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public int update(ContentValues updateRowValue, String whereClause, String[] whereArgs) {
        return mDatabase.update(TABLE_FILE, updateRowValue, whereClause, whereArgs);
    }

    public int delete(String whereClause, String[] whereArgs) {
        return mDatabase.delete(TABLE_FILE, whereClause, whereArgs);
    }
}
