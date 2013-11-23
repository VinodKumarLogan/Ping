package com.example.pingv2;
import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PingChat";
    private static final String TABLE_MESSAGE = "messages";
    private static final String KEY_ID = "id";
    private static final String KEY_MESSAGE = "message";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGE_TABLE = "CREATE TABLE " + TABLE_MESSAGE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE+")";
        db.execSQL(CREATE_MESSAGE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           }
 
     void addMessage(Messages msg) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, msg.getMessage());
        db.insert(TABLE_MESSAGE, null, values);
        db.close();
    }
     
    public List<Messages> getAllMessages() {
        List<Messages> messageList = new ArrayList<Messages>();
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGE; 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Messages msg = new Messages();
                msg.setID(Integer.parseInt(cursor.getString(0)));
                msg.setMessage(cursor.getString(1));
                messageList.add(msg);
            } while (cursor.moveToNext());
        }
        return messageList;
    }
    public int getMessageCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MESSAGE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

}