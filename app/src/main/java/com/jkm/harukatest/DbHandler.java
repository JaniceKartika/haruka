package com.jkm.harukatest;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {
    private static final String TAG = DbHandler.class.getSimpleName();

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "studentsDB";
    private static final String STUDENTS_TABLE = "students";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LNG = "longitude";

    Context context;

    public DbHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate DB");
        String CREATE_STUDENTS_TABLE = "CREATE TABLE " + STUDENTS_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, "
                + KEY_LAT + " TEXT, " + KEY_LNG + " TEXT" + ")";
        db.execSQL(CREATE_STUDENTS_TABLE);

        String studentsCSV = "students.csv";
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(studentsCSV);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        db.beginTransaction();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns[0].trim().equals("id")) {
                    Log.d(TAG, "Skip header.");
                    continue;
                }
                Student student = new Student();
                student.setID(Integer.valueOf(columns[0].trim()));
                student.setName(columns[1].trim());
                student.setLatitude(columns[2].trim());
                student.setLongitude(columns[3].trim());
                addStudent(db, student);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STUDENTS_TABLE);
        onCreate(db);
    }

    public void addStudent(SQLiteDatabase sqliteDatabase, Student student) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, student.getID());
        contentValues.put(KEY_NAME, student.getName());
        contentValues.put(KEY_LAT, student.getLatitude());
        contentValues.put(KEY_LNG, student.getLongitude());

        sqliteDatabase.insert(STUDENTS_TABLE, null, contentValues);
    }

    public List<Student> getStudentList() {
        List<Student> studentList = new ArrayList<>();
        String query = "SELECT * FROM " + STUDENTS_TABLE;

        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        Cursor cursor = sqliteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Student student = new Student();
                student.setID(Integer.parseInt(cursor.getString(0)));
                student.setName(cursor.getString(1));
                student.setLatitude(cursor.getString(2));
                student.setLongitude(cursor.getString(3));

                studentList.add(student);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return studentList;
    }

    public int getStudentsCount() {
        String query = "SELECT * FROM " + STUDENTS_TABLE;
        SQLiteDatabase sqliteDatabase = this.getReadableDatabase();
        Cursor cursor = sqliteDatabase.rawQuery(query, null);
        cursor.close();
        return cursor.getCount();
    }
}
