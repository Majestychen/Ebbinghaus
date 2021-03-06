package cn.watcherman.ebbinghaus.data;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.watcherman.ebbinghaus.R;
import cn.watcherman.ebbinghaus.data.EbbinghausContract.contentEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by minecraft on 17-3-13.
 */
public class InterfaceDatabase {
    private SQLiteDatabase db;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    Calendar calendar = Calendar.getInstance();
    Context context;

    public InterfaceDatabase(Context these, String RW) {
        context = these;
        EbbinghausDBOpenHelper mDBHelper = new EbbinghausDBOpenHelper(context);

        if (RW.equals("r")) {
            db = mDBHelper.getReadableDatabase();
        } else if (RW.equals("w")) {
            db = mDBHelper.getWritableDatabase();
        }
    }

    public List<String[]> getList(String all) {
        List<String[]> list = new ArrayList<>();
        String[] listAll;
        Cursor cursor;

        if(all.equals("all")){
            cursor = db.query(EbbinghausContract.contentEntry.TABLE_NAME,
                    null, null, null, null, null, null);
        }
        else {
            String selection = contentEntry.COLUMN_NEXT_TIMES_DATE + " = ?";
            String next_time_date = simpleDateFormat.format(calendar.getTime());
            String[] selectionArgs = {next_time_date};
            cursor = db.query(EbbinghausContract.contentEntry.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null);
        }


        int contentId = cursor.getColumnIndex(contentEntry.COLUMN_CONTENT),
                _IdId = cursor.getColumnIndex(contentEntry._ID),
                exec_timesId = cursor.getColumnIndex(contentEntry.COLUMN_EXEC_TIMES),
                set_dateId = cursor.getColumnIndex(contentEntry.COLUMN_SET_DATE),
                next_timeId = cursor.getColumnIndex(contentEntry.COLUMN_NEXT_TIMES_DATE);

        for (String _id, content, exec_times,set_date,next_time; cursor.moveToNext(); list.add(listAll)) {
            listAll = new String[5];
            _id = cursor.getString(_IdId);
            listAll[0] = _id;
            content = cursor.getString(contentId);
            listAll[1] = content;
            exec_times = cursor.getString(exec_timesId);
            listAll[2] = exec_times;
            set_date = cursor.getString(set_dateId);
            listAll[3] = set_date;
            next_time = cursor.getString(next_timeId);
            listAll[4] = next_time;
        }
        return list;

    }

    public boolean putNewData(String content) {
        ContentValues values = new ContentValues();
        values.put(contentEntry.COLUMN_CONTENT, content);
        values.put(contentEntry.COLUMN_SET_DATE, EbbinghausContract.usualConstant.DATE_NOW);
        calendar.add(Calendar.DATE, 1);
        String next_time_date = simpleDateFormat.format(calendar.getTime());
        values.put(contentEntry.COLUMN_NEXT_TIMES_DATE, next_time_date);
        long result = db.insert(contentEntry.TABLE_NAME, null, values);
        return result != -1;
    }

    public boolean markHaveDone(String _id) throws ParseException {
        SharedPreferences ebbinghausMemoryDay = context.getSharedPreferences(context.getString(R.string.ebbinghausMemoryDay_kel_file), Context.MODE_PRIVATE);
        //获得数据
        String[] projection = {contentEntry.COLUMN_NEXT_TIMES_DATE, contentEntry.COLUMN_EXEC_TIMES};
        String selection = contentEntry._ID + " = ?";
        String[] selectionArgs = {_id};
        Cursor cursor = db.query(contentEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        cursor.moveToFirst();
        int exec_timesId = cursor.getColumnIndex(contentEntry.COLUMN_EXEC_TIMES),
                next_time_dateId = cursor.getColumnIndex(contentEntry.COLUMN_NEXT_TIMES_DATE);
        //处理数据
        String next_time_date = cursor.getString(next_time_dateId);
        int exec_times = cursor.getInt(exec_timesId);
        Date date = simpleDateFormat.parse(next_time_date);
        calendar.setTime(date);
        calendar.add(Calendar.DATE, ebbinghausMemoryDay.getInt(String.valueOf(exec_times), 1));
        next_time_date = simpleDateFormat.format(calendar.getTime());
        //更新数据
        ContentValues values = new ContentValues();
        values.put(contentEntry.COLUMN_NEXT_TIMES_DATE, next_time_date);
        values.put(contentEntry.COLUMN_EXEC_TIMES, exec_times + 1);
        selection = contentEntry._ID + " = ?";
//      selectionArgs = { _id };
        int count = db.update(contentEntry.TABLE_NAME, values, selection, selectionArgs);

        return count >= 0;
    }
}
