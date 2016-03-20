package com.example.dell.login;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBHandler extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION= 1;
    private static final String DATABASE_NAME="user.db";
    private static final String TABLE_NAME="users";
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_PASS="password";
    private static final String COLUMN_NAME="name";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w("MyApp", "onCreate");
        String query="CREATE TABLE "+TABLE_NAME+"("+
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PASS + " TEXT " +
                ");";
        Log.w("MyApp", "onCreate");
        db.execSQL(query);
        Log.w("MyApp", "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query="DROP TABLE IF EXISTS "+"TABLE_NAME";
        db.execSQL(query);
        onCreate(db);
    }
    public void addUser(UserDetails user)
    {
        Log.w("MyApp", "addUser");
        ContentValues values=new ContentValues();
        values.put(COLUMN_ID, user.get_id());
        values.put(COLUMN_NAME, user.get_name());
        values.put(COLUMN_PASS, user.get_password());
        SQLiteDatabase db=getWritableDatabase();

        db.insertOrThrow(TABLE_NAME, null, values);
    }
    public void deleteUser(UserDetails user)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=\"" + user.get_id() + "\";");
    }
    public boolean userExists(String username)
    {
        Log.w("MyApp", "userExists");
        SQLiteDatabase db=this.getReadableDatabase();
        Log.w("MyApp", "userExists");
        String query="SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_ID +"=\"" + username + "\";";
        Cursor c=db.rawQuery(query, null);
        Log.w("MyApp", "userExists");
        return c.moveToFirst();
    }
    public String userN(String username)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_ID +"=\"" + username + "\";";
        Cursor c=db.rawQuery(query, null);
        if(c.moveToFirst())
            return c.getString(c.getColumnIndex(COLUMN_NAME));
        db.close();
        return null;
    }
    public boolean validate(String username, String password)
    {
        Log.w("MyApp", "validate");
        SQLiteDatabase db=this.getReadableDatabase();
        Log.w("MyApp", "validate");
        String query="SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_ID +"=\"" + username + "\" AND "+
                COLUMN_PASS+"=\""+password+"\";";
        Cursor c=db.rawQuery(query, null);
        return c.moveToFirst();
    }
    public void changePassword(String username, String newPassword)
    {
        Log.w("MyApp", "changePassword");
        SQLiteDatabase db=this.getWritableDatabase();
        Log.w("MyApp", "changePassword");
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASS, newPassword);
        Log.w("MyApp", "changePassword");
        // Which row to update, based on the ID
        String selection = COLUMN_ID +  " = ? ";
        String[] selectionArgs = new String[]{username};

        db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.w("MyApp", "changePassword");
    }
}
