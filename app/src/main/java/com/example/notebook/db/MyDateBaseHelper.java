package com.example.notebook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDateBaseHelper extends SQLiteOpenHelper {
    public MyDateBaseHelper(Context context) {
        super(context, "NOTE_DB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table Note (" +
                "Id Integer primary key autoincrement," +
                "Title varchar(30)," +
                "Content text," +
                "Time datetime not null," +
                "Writer varchar(10))";
//        String sql_createTable_Photo = "create table Photo(" +
//                "Id Integer primary key autoincrement," +
//                "NoteId Integer," +
//                "Url String not null," +
//                "foreign key (NoteId) references Note(Id))";
        db.execSQL(sql);
//        db.execSQL(sql_createTable_Photo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
